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


import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.plugin.ZProjector;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.Binary;


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
			// TODO Global needs: a. element-wise multiplication
			// TODO #1 Pre-process image (sum all images, auto threshold, morphological opening and skeletonization)
			// TODO #2 Populate Hough space (random selection of N couples of foreground pixels and creation of one point in Hough space; 
			// needs: a. image space to Hough space coordinates converter, b. coordinates system definition and c. foreground coordinates 
			// selector)
			// TODO #3 Select Hough points (search for local maxima in Hough space; needs: a. compute rescale factors --or take into account
			// anisotropic kernels--, b. point replication on borders of theta axis and c. mean shift of points with specified bandwidth)
			// TODO #4 Build segments (detect pixels along lines and break them into segments; needs: a. foreground coordinates selector)
		}
	}
	
	/**
	 * Prepare input image for DNA fibers extraction by extracting skeletons.
	 * @param input Input image.
	 * @param startSlice Project from this channel.
	 * @param endSlice Project until this channel.
	 * @param thickness Thickness in pixels of the fibers.
	 * @return A binary image of skeletons of input image.
	 */
	public static ImagePlus extractSkeletons(ImagePlus input, int startSlice, int endSlice, int thickness) {
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
	 * Show the dialog box for input parameters.
	 * @return True if the dialog box has been filled and accepted, false otherwise.
	 */
	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("DNA Fibers - extract and unfold");

		int number_of_columns = 3;
		gd.addNumericField("Thickness", this.thickness, 1, number_of_columns, "pixels");
		gd.addNumericField("Start at channel", this.firstChannel, 0, number_of_columns, "");
		gd.addNumericField("End at channel", this.secondChannel, 0, number_of_columns, "");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		this.thickness     = gd.getNextNumber();
		this.firstChannel  = (int)gd.getNextNumber();
		this.secondChannel = (int)gd.getNextNumber();

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
		ImagePlus image = IJ.openImage("src/main/resources/test/example_original.zip");
		image.show();

		// run the plugin
		Class<?> clazz = Extract_DNA_Fibers.class;
		IJ.runPlugIn(clazz.getName(), "");
	}
}
