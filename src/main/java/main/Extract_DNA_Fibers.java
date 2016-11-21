/*
 * Manipulate and analyse DNA fibers data
 * This plugin extracts and unfold the DNA fibers selected by a curve ROI
 * Copyright (C) 2016  Julien Pontabry (Helmholtz IES)

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package main;

import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.plugin.ZProjector;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.Binary;

import coordinates.*;


/**
 * Plugin for DNA fibers extraction and unfolding.
 *
 * After DNA fibers detection (manually or automatically), the fibers are
 * located by their centerline (represented by a line ROI). They can be
 * unfolded (to make them straight) with this plugin.
 *
 * @author Julien Pontabry
 */
public class Extract_DNA_Fibers implements PlugInFilter {
	/** The input image. */
	protected ImagePlus image = null;
	
	/** The thickness of fibers to detect. */
	protected double thickness = 2;
	
	/** The first channel to take into account. */
	protected int firstChannel = 1;
	
	/** The second channel to take into account. */
	protected int secondChannel = 1;
	
	/** The number of couple of points to sample in image space (single points in Hough space). */
	protected int numberOfPoints = 1000;
	
	/** The sensitivity of candidate points selection. */
	protected double selectionSensitivity = 0.33;
	
	/** The angular sensitivity (in degrees, bandwidth on theta axis in Hough space). */
	protected double angularSensitivity = 2.5;
	
	/** The thickness sensitivity (in pixels, bandwidth on rho axis in Hough space). */
	protected double thicknessSensitivity = 5;
	
	/** The maximum gap (in pixels) allowed between two segments (merge if smaller). */
	protected double maxSegmentGap = 30;
	
	/** The minimum length (in pixels) of a segment to be considered. */
	protected double minSegmentLength = 50;
	
	/** Maximal distance (in pixels) to the Hough line of a pixel to be considered as a part of a segment. */
	protected double widthTolerance = 1.0;

	/** Half-size of the window used when estimating local models. */
	protected int localWindowHalfSize = 25;

	/**
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus imp) {
		// Get inputs
		this.image = imp;
		this.secondChannel = this.image.getNChannels();

		// Finish the setup
		return DOES_8G | DOES_16 | DOES_32 | NO_CHANGES;
	}

	/**
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		if (this.showAndCheckDialog()) {
			List<Line> segments = Extract_DNA_Fibers.detectFibers(this.image, this.thickness, this.firstChannel, this.secondChannel, 
					this.numberOfPoints, this.angularSensitivity, this.thicknessSensitivity, this.selectionSensitivity,
					this.maxSegmentGap, this.minSegmentLength, this.widthTolerance, this.localWindowHalfSize );
			
			RoiManager manager = new RoiManager();
			for (Line l : segments)
				manager.addRoi(l);
		}
	}
	
	/**
	 * Single method for fibers detection in input image.
	 * @param input Input image.
	 * @param thickness Thickness in pixels of the fibers.
	 * @param startSlice Project from this channel.
	 * @param endSlice Project until this channel.
	 * @param numberOfPoints Number of points to generate randomely in Hough space.
	 * @param angularSensitivity Soft threshold for angle (in degrees).
	 * @param thicknessSensitivity Soft threshold for line thickness (in pixels).
	 * @param selectionSensitivity Sensitivity to selection of candidates points (in [0,1]).
	 * @param maxSegmentGap Maximum gap allowed between two segments (merge if smaller).
	 * @param minSegmentLength Minimum length of a segment to be considered.
	 * @param widthTolerance Maximal distance to the Hough line of a pixel to be considered as a part of a segment.
	 * @param localWindowHalfSize Half size of the window used when estimating the local model.
	 * @return A list of segments as Line ROI.
	 */
	public static List<Line> detectFibers(ImagePlus input, double thickness, int startSlice, int endSlice, 
			int numberOfPoints, double angularSensitivity, double thicknessSensitivity, double selectionSensitivity,
			double maxSegmentGap, double minSegmentLength, double widthTolerance, int localWindowHalfSize) {
		IJ.showProgress(0, 4);
		ImagePlus skeletons = Extract_DNA_Fibers.extractSkeletons(input, startSlice, endSlice, thickness);
		skeletons.hide();
		skeletons.setTitle("Skeletons image");
		
		if (input.getRoi() == null)
			input.setRoi(0, 0, input.getWidth(), input.getHeight());

		IJ.showProgress(1, 4);
		List<HoughPoint> houghPoints = Extract_DNA_Fibers.buildHoughSpaceFromSkeletons(skeletons, input.getRoi(), numberOfPoints, localWindowHalfSize);

		IJ.showProgress(2, 4);
		List<HoughPoint> selectedPoints = Extract_DNA_Fibers.selectHoughPoints(houghPoints, selectionSensitivity, angularSensitivity, thicknessSensitivity);
		
		IJ.showProgress(3, 4);
		List<Line> segments = Extract_DNA_Fibers.buildSegments(skeletons, input.getRoi(), selectedPoints, maxSegmentGap, minSegmentLength, widthTolerance);
		
		skeletons.close();
		IJ.showProgress(4, 4);
		
		return segments;
	}
	
	/**
	 * Build segments from binary image and list of selected points in Hough space.
	 * @param binary Input binary image of segments to detect.
	 * @param roi Input roi.
	 * @param selectedPoints Output of Hough space creation and accumulation.
	 * @param maxGap Maximal allowed gap between two successive segments.
	 * @param minLength Minimal allowed length of a segment.
	 * @param tolerance Tolerance for pixel aggregation around line.
	 * @return
	 */
	public static List<Line> buildSegments(ImagePlus binary, Roi roi, List<HoughPoint> selectedPoints, double maxGap, double minLength, double tolerance) {
		// Precompute
		double    maxGap2 = maxGap * maxGap;
		double minLength2 = minLength * minLength;
		
		// Setup list of foreground pixels' coordinates in coordinate system with origin centered.
		ImagePoint origin = ImagePoint.getCenterPointOfImage(binary);
		List<ImagePoint> foregroundPoints = ImagePoint.getImageForegroundPoints(binary, roi, origin);
		
		// Setup generation functions to be executed in parallel
		List<Callable<List<Line>>> tasks = new Vector<>();

		IntStream.range(0, selectedPoints.size()).forEach(k -> {
			tasks.add(() -> {
				HoughPoint peak = selectedPoints.get(k);

				// Precompute
				double cosTheta = Math.cos(peak.theta);
				double sinTheta = Math.sin(peak.theta);

				// Keep only associated points with that particular peak and compute range
				List<ImagePoint> associatedPoints = new Vector<ImagePoint>();
				int minX = binary.getWidth(), maxX = 0;
				int minY = binary.getHeight(), maxY = 0;

				for (ImagePoint p : foregroundPoints) {
					double rho = p.x * cosTheta + p.y * sinTheta;

					if (peak.rho-tolerance <= rho && rho <= peak.rho+tolerance) {
						associatedPoints.add(p);

						if (p.x < minX)
							minX = p.x;
						else if (p.x > maxX)
							maxX = p.x;

						if (p.y < minY)
							minY = p.y;
						else if (p.y > maxY)
							maxY = p.y;
					}
				}

				// Sort coordinates by in direction of major coordinates change
				final int factor = (maxX-minX < maxY-minY) ? -1 : 1;

				associatedPoints.sort((o1,o2) -> {
					if (o1.x == o2.x)
						return factor * (o1.y - o2.y);
					else // o1.x != o2.x
						return factor * (o1.x - o2.x);
				});

				// Creates list of point indices describing gaps
				List<Integer> indices = new Vector<Integer>();
				indices.add(-1);

				for (int i = 0; i < associatedPoints.size()-1; i++) {
					double distance = associatedPoints.get(i).squaredDistanceToPoint(associatedPoints.get(i+1));

					if (distance > maxGap2)
						indices.add(i);
				}

				indices.add(associatedPoints.size()-1);

				// Accumulate segments
				List<Line> segments = new Vector<Line>();
				if (indices.size() > 2) {
					for (int i = 0; i < indices.size()-1; i++) {
						ImagePoint p1 = new ImagePoint(associatedPoints.get(indices.get(i)+1));
						ImagePoint p2 = new ImagePoint(associatedPoints.get(indices.get(i+1)));
	
						if (p1.squaredDistanceToPoint(p2) >= minLength2) {
							p1.add(origin); p2.add(origin);
							segments.add(new Line(p1.x, p1.y, p2.x, p2.y));
						}
					}
				}
				
				return segments;
			});
		});

		// Run threads in parallel and reduce results
		List<Line>   allSegments = new Vector<Line>();
		ExecutorService executor = Executors.newWorkStealingPool(1);

		try {
			executor.invokeAll(tasks)
			.stream()
			.map(future -> {
				try {
					return future.get();
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
				}
			})
			.forEach(segments -> {
				allSegments.addAll(segments);
			});
		}
		catch (Exception e) {
			IJ.error("Exception", "An exception occured!\n" + e.getMessage());
		}
		
		return allSegments;
	}
	
	/**
	 * Select points among candidates in Hough space based on neighborhood count.
	 * @param houghPoints Input points in Hough space.
	 * @param selectionSensitivity Sensitivity to selection of candidates points (in [0,1]).
	 * @param angularSensitivity Soft threshold for angle (in degrees).
	 * @param thicknessSensitivity Soft threshold for line thickness (in pixels).
	 * @return Selection of points in Hough space based on number of contributing points in neighborhood.
	 */
	public static List<HoughPoint> selectHoughPoints(List<HoughPoint> houghPoints, double selectionSensitivity, double angularSensitivity, double thicknessSensitivity) {
		// Compute range and bandwidths
		double       minValue = -Math.PI/2.0;
		double       maxValue = Math.PI/2.0;
		double thetaBandwidth = angularSensitivity * Math.PI/180.0;
		double   rhoBandwidth = thicknessSensitivity;
		
		// Manage borders
		List<HoughPoint> replicatedHoughPoints = Extract_DNA_Fibers.replicateHoughSpaceBorders(houghPoints, thetaBandwidth, maxValue, minValue, true);
		
		// Find modes
		MeanShift modesFinder = new MeanShift(new HoughPoint(thetaBandwidth, rhoBandwidth));
		modesFinder.runWith(replicatedHoughPoints);
		List<HoughPoint> modes = modesFinder.getModes();
		List<Integer>   labels = modesFinder.getLabels();
		
		// Get counts and maximal count
		int[] counts = new int[modes.size()];
		for (Integer i : labels)
			counts[i]++;
		
		int maximalCount = 0;
		for (Integer count : counts) {
			if (count > maximalCount)
				maximalCount = count;
		} 
		
		// Select candidate points (keep points within range)
		List<HoughPoint> selectedPoint = new Vector<HoughPoint>();
		for (int i = 0; i < modes.size(); i++) {
			if (counts[i] > selectionSensitivity*maximalCount &&
				Double.compare(modes.get(i).theta, minValue) > 0 && Double.compare(modes.get(i).theta, maxValue) < 0)
				selectedPoint.add(modes.get(i));
		}
		
		return selectedPoint;
	}
	
	/**
	 * Replicate borders of Hough space on angular axis (Theta).
	 * This operation is needed to avoid border effects when detecting points.
	 * @param houghPoints Input points.
	 * @param angularBandwidth Bandwidth for peak detection on angular axis.
	 * @param supBound Upper bound of angular axis.
	 * @param infBound Lower bound of angular axis.
	 * @param inverse True to inverse the other axis.
	 * @return Input points with replicated borders.
	 */
	public static List<HoughPoint> replicateHoughSpaceBorders(List<HoughPoint> houghPoints, double angularBandwidth, double supBound, double infBound, boolean inverse) {
		List<HoughPoint> replicatedHoughPoints = new Vector<HoughPoint>();
		replicatedHoughPoints.addAll(houghPoints);
		
		// range of angles
		double angularRange = supBound - infBound;
		
		// Inversion
		double factor = 1.0;
		if (inverse)
			factor = -1.0;
		
		// Add symmetric points
		for (HoughPoint p : houghPoints) {
			if (Double.compare(p.theta, supBound - 5.0*angularBandwidth) >= 0)
				replicatedHoughPoints.add(new HoughPoint(p.theta-angularRange, factor*p.rho));
			
			if (Double.compare(p.theta, infBound + 5.0*angularBandwidth) <= 0)
				replicatedHoughPoints.add(new HoughPoint(p.theta+angularRange, factor*p.rho));
		}
		
		return replicatedHoughPoints;
	}
	
	/**
	 * Prepare input image for DNA fibers extraction by extracting skeletons.
	 * @param input Input image.
	 * @param startSlice Project from this channel.
	 * @param endSlice Project until this channel.
	 * @param thickness Thickness in pixels of the fibers.
	 * @return A binary image of skeletons of input image.
	 */
	public static ImagePlus extractSkeletons(ImagePlus input, int startSlice, int endSlice, double thickness) {
		// Max-project the selected channels
		ZProjector projector = new ZProjector();
		projector.setImage(input);
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.setStartSlice(startSlice);
		projector.setStopSlice(endSlice);
		projector.doProjection();
		ImagePlus tmp = projector.getProjection();
		
		// Denoise the projected image
		GaussianBlur blur = new GaussianBlur();
		blur.blurGaussian(tmp.getProcessor(), 1.5);
		
		// Compute Laplacian
		IJ.run(tmp, "FeatureJ Laplacian", "compute smoothing=1.0");
		tmp = WindowManager.getImage(tmp.getTitle() +" Laplacian");

		// Threshold Laplacian
		tmp.getProcessor().setThreshold(tmp.getProcessor().getMin(), -1, ImageProcessor.BLACK_AND_WHITE_LUT);
		IJ.run(tmp, "Make Binary", "");

		// Clean the threshold result
		IJ.run(tmp, "Gray Morphology", "radius="+ IJ.d2s(thickness/2.0, 1) +" type=circle operator=open");
		
		// Skeletonize
		Binary skeletizator = new Binary();
		skeletizator.setup("skel", tmp);
		skeletizator.run(tmp.getProcessor());
		
		return tmp;
	}

	/**
	 * Set a list of points in Hough space from skeleton image.
	 * 
	 * The Hough points are constructed from at most <code>numberOfPoints</code> 
	 * randomly sampled couples of foreground point in image space.
	 * 
	 * The image space is defined to have origin at image center.
	 * 
	 * @param skeletons Input skeletons image (binary).
	 * @param roi Input roi.
	 * @param numberOfPoints Number of points to sample.
	 * @return A list of Hough points
	 */
	public static List<HoughPoint> buildHoughSpaceFromSkeletons(ImagePlus skeletons, Roi roi, int numberOfPoints, int windowSize) {
		// Setup list of foreground pixels' coordinates in coordinate system with origin centered.
		ImagePoint origin = ImagePoint.getCenterPointOfImage(skeletons);
		List<ImagePoint> foregroundPoints = ImagePoint.getImageForegroundPoints(skeletons, roi, origin);
		
		// Setup random generator
		Random generator = new Random();
		
		// Setup generation functions to be executed in parallel
		List<Callable<HoughPoint>> tasks = new Vector<>();
		
		IntStream.range(0, numberOfPoints).forEach(i -> {
			tasks.add(() -> {
				ImagePoint p0 = foregroundPoints.get(generator.nextInt(foregroundPoints.size()));
				
				// Select p0's neighborhood
				List<ImagePoint> neighborhood = new Vector<>();
				
				for (ImagePoint p : foregroundPoints) {
					if (!p.equals(p0) && p.distanceToPoint(p0) < windowSize)
						neighborhood.add(p);
				}
				
				if (!neighborhood.isEmpty())
					return p0.estimatedHoughPoint(neighborhood);
				else
					return null;
			});
		});
		
		// Run threads in parallel and reduce results
		Vector<HoughPoint> results = new Vector<>();
		ExecutorService executor = Executors.newWorkStealingPool();
		
	    try {
	        executor.invokeAll(tasks)
	        	.stream()
	        	.map(future -> {
	        		try {
	        			return future.get();
	        		}
	        		catch (Exception e) {
	        			throw new IllegalStateException(e);
	        		}
	        	})
	        	.forEach(result -> {
	        		if (result != null) 
	        			results.add(result);
	        	});
	    }
	    catch (Exception e) {
	    	IJ.error("Exception", "An exception occured!\n" + e.getMessage());
	    }
		
		return results;
	}

	/**
	 * Show the dialog box for input parameters.
	 * @return True if the dialog box has been filled and accepted, false otherwise.
	 */
	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("DNA Fibers - detection");

		int number_of_columns = 4;
		
		gd.addPanel(new gui.SeparatorPanel("Channels to use"));
		gd.addNumericField("Start at channel", this.firstChannel, 0, number_of_columns, "");
		gd.addNumericField("End at channel", this.secondChannel, 0, number_of_columns, "");
		
		gd.addPanel(new gui.SeparatorPanel("Local model"));
		gd.addNumericField("Thickness", this.thickness, 1, number_of_columns, "pixels");
		gd.addNumericField("Local window half-size", this.localWindowHalfSize, 0, number_of_columns, "pixels");
		
		gd.addPanel(new gui.SeparatorPanel("Selection of candidates"));
		gd.addNumericField("Number of samples", this.numberOfPoints, 0, number_of_columns, "");
		gd.addNumericField("Shift tolerance", this.thicknessSensitivity, 1, number_of_columns, "pixels");
		gd.addNumericField("Angular tolerance", this.angularSensitivity, 1, number_of_columns, "degrees");
		gd.addNumericField("Selection sensitivity", this.selectionSensitivity, 2, number_of_columns, "");
		
		gd.addPanel(new gui.SeparatorPanel("Segments building"));
		gd.addNumericField("Maximum segment gap", this.maxSegmentGap, 1, number_of_columns, "pixels");
		gd.addNumericField("Minimum segment length", this.minSegmentLength, 1, number_of_columns, "pixels");
		gd.addNumericField("Segment width tolerance", this.widthTolerance, 1, number_of_columns, "pixels");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		this.firstChannel         = (int)gd.getNextNumber();
		this.secondChannel        = (int)gd.getNextNumber();
		this.thickness            = gd.getNextNumber();
		this.localWindowHalfSize  = (int)gd.getNextNumber();
		this.numberOfPoints       = (int)gd.getNextNumber();
		this.thicknessSensitivity = gd.getNextNumber();
		this.angularSensitivity   = gd.getNextNumber();
		this.selectionSensitivity = gd.getNextNumber();
		this.maxSegmentGap        = gd.getNextNumber();
		this.minSegmentLength     = gd.getNextNumber();
		this.widthTolerance       = gd.getNextNumber();

		return true;
	}

	private boolean showAndCheckDialog() {
		// Call dialog
		boolean notCanceled = this.showDialog();
		boolean     checked = false;

		// Check parameters
		while(notCanceled && !checked) {
			if (Double.compare(this.thickness, 1) < 0)
				IJ.error("Input error", "Thickness must be greater or equal than 1 pixel!");
			else if (this.firstChannel < 1)
				IJ.error("Input error", "First channel number must be greater or equal than 1!");
			else if (this.firstChannel > this.image.getNChannels())
				IJ.error("Input error", "First channel number must be lesser or equal than "+ this.image.getNChannels() +"!");
			else if (this.secondChannel < 1)
				IJ.error("Input error", "Second channel number must be greater or equal than 1!");
			else if (this.secondChannel > this.image.getNChannels())
				IJ.error("Input error", "Second channel number must be lesser or equal than "+ this.image.getNChannels() +"!");
			else if (this.firstChannel > this.secondChannel)
				IJ.error("Input error", "First channel number must be lesser or equal than second channel number!");
			else if (Double.compare(this.angularSensitivity, 0.0) <= 0)
				IJ.error("Input error", "Angular sensitivity must be greater than zero!");
			else if (Double.compare(this.thicknessSensitivity, 0.0) <= 0)
				IJ.error("Input error", "Thickness sensitivity must be greater than zero!");
			else if (Double.compare(this.selectionSensitivity, 0.0) <= 0)
				IJ.error("Input error", "Selection sensitivity must be greater than zero!");
			else if (Double.compare(this.selectionSensitivity, 1.0) >= 0)
				IJ.error("Input error", "Selection sensitivity must be lesser than one!");
			else if (Double.compare(this.maxSegmentGap, 0.0) <= 0)
				IJ.error("Input error", "Maximal segment gap must be greater than zero!");
			else if (Double.compare(this.minSegmentLength, 0.0) <= 0)
				IJ.error("Input error", "Minimum segment length must be greater than zero!");
			else if (Double.compare(this.widthTolerance, 0.0) <= 0)
				IJ.error("Input error", "Segment width tolerance must be greater than zero!");
			else if (this.localWindowHalfSize < 2)
				IJ.error("Input error", "The loca window half-size must be at least 2 pixels!");
			else
				checked = true;
			
			if (!checked)
				notCanceled = this.showDialog();
		}

		return notCanceled;
	}

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads an
	 * image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// Setup the plugin directory path
		System.setProperty("plugins.dir", "/Applications/Fiji.app/plugins");

		// start ImageJ
		new ImageJ();

		// open the Clown sample
		ImagePlus image = IJ.openImage("src/test/resources/example_original.zip");
		image.show();

		// run the plugin
		Class<?> clazz = Extract_DNA_Fibers.class;
		IJ.runPlugIn(clazz.getName(), "");
	}
}
