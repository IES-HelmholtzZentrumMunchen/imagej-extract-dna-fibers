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
package coordinates;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

/**
 * Define the a point in image space.
 * 
 * The origin in at top-left of the image.
 * 
 * @author julien.pontabry
 */
public class ImagePoint extends Point2D {
	/** The X coordinate of image. */
	public int x;
	
	/** The Y coordinate of image. */
	public int y;
	
	/** Pre-computed constant (useful in particular for local model estimation). */
	protected static final double PIover2 = Math.PI/2.;
	
	/**
	 * Default constructor.
	 * 
	 * Gives the origin point.
	 */
	public ImagePoint(){
		// ---
	}
	
	/**
	 * Constructor.
	 * @param x Input X coordinate.
	 * @param y Input Y coordinate.
	 */
	public ImagePoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Copy constructor.
	 * @param p Input point to copy.
	 */
	public ImagePoint(ImagePoint p) {
		this.x = p.x;
		this.y = p.y;
	}
	
	/**
	 * @see java.awt.geom.Point2D#getX()
	 */
	@Override
	public double getX() {
		return this.x;
	}

	/**
	 * @see java.awt.geom.Point2D#getY()
	 */
	@Override
	public double getY() {
		return this.y;
	}

	/**
	 * @see java.awt.geom.Point2D#setLocation(double, double)
	 */
	@Override
	public void setLocation(double x, double y) {
		this.x = (int)x;
		this.y = (int)y;
	}

	/**
	 * Integer override of setLocation method.
	 * @param x The new integer X-coordinate of the point.
	 * @param y The new integer Y-coordinate of the point.
	 * @see java.awt.geom.Point2D#setLocation(double, double)
	 */
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Convert point to string format.
	 */
	public String toString() {
		return "ImagePoint["+this.x+","+this.y+"]";
	}
	
	/**
	 * Addition of two points (as in vector space).
	 * @param p Point to add to current point.
	 */
	public ImagePoint add(ImagePoint p) {
		this.x += p.x;
		this.y += p.y;
		return this;
	}
	
	/** Addition of the current point to the specified list of points.
	 * The spirit is opposed as the add to point method: here the coordinates
	 * of the object point is added to the coordinates of the points in the list.
	 * @param listOfPoints List of points to translate.
	 * @return The translated list of points.
	 */
	public List<ImagePoint> addTo(List<ImagePoint> listOfPoints) {
		List<ImagePoint> modifiedList = new Vector<>();
		
		for (ImagePoint p : listOfPoints) {
			modifiedList.add(p.add(this));
		}
		
		return modifiedList;
	}
	
	/**
	 * Subtraction of two points (as in vector space).
	 * @param p Point to subtract to current point.
	 */
	public ImagePoint subtract(ImagePoint p) {
		this.x -= p.x;
		this.y -= p.y;
		return this;
	}
	
	/**
	 * Subtraction of the current point to the specified list of points.
	 * The spirit is opposed as the subtract to point method: here the coordinates
	 * of the object point is subtracted to the coordinates of the points in the list.
	 * @param listOfPoints List of points to translate.
	 * @return The translated list of points.
	 */
	public List<ImagePoint> subtractTo(List<ImagePoint> listOfPoints) {
		List<ImagePoint> modifiedList = new Vector<>();
		
		for (ImagePoint p : listOfPoints) {
			modifiedList.add(p.add(this));
		}
		
		return modifiedList;
	}
	
	/**
	 * Compute euclidian distance between two points. 
	 * @param p1 First input point.
	 * @param p2 Second input point.
	 * @return Euclidian distance between input points.
	 */
	public static double distanceBetweenPoints(ImagePoint p1, ImagePoint p2) {
		return Math.sqrt(ImagePoint.squaredDistanceBetweenPoints(p1, p2));
	}
	
	/**
	 * Compute euclidian squared distance between two points. 
	 * @param p1 First input point.
	 * @param p2 Second input point.
	 * @return Euclidian squared distance between input points.
	 */
	public static double squaredDistanceBetweenPoints(ImagePoint p1, ImagePoint p2) {
		double diffX = p1.x - p2.x;
		double diffY = p1.y - p2.y;
		
		return diffX*diffX + diffY*diffY;
	}
	
	/**
	 * Compute euclidian distance of this point to the input.
	 * @param p Input point.
	 * @return Euclidian distance between this point and the input point.
	 */
	public double squaredDistanceToPoint(ImagePoint p) {
		return ImagePoint.squaredDistanceBetweenPoints(this, p);
	}
	
	/**
	 * Compute euclidian distance of this point to the input.
	 * @param p Input point.
	 * @return Euclidian distance between this point and the input point.
	 */
	public double distanceToPoint(ImagePoint p) {
		return ImagePoint.distanceBetweenPoints(this, p);
	}
	
	/**
	 * Estimate a straight line in Hesse normal form with a list of points.
	 * The line is estimated using regression. Instead of least squares, the
	 * Theil-Sen estimator is applied (robust estimator using the median).
	 * Note that p0 is not supposed to be in the list of points (no checking
	 * will be done for that).
	 * @param p0 A point that is known to be on the line to estimate.
	 * @param points A list of points used to estimate the line.
	 * @return The estimated Hough point (straight line in Hesse normal form).
	 */
	public static HoughPoint estimateHoughPoint(ImagePoint p0, List<ImagePoint> points) {
		// Since angles are circular quantities, it does not have
		// a proper ordering relationship and we need to center the
		// angles first to their center of mass.
		
		// Initialize
		List<java.lang.Double> thetas = new Vector<>();
		double alpha_sum_sin = 0., alpha_sum_cos = 0.;
		
		for (ImagePoint p : points) {
			// Estimate pairwise angle
			// We do not use the meth ImagePoint#convertImagePointsToHoughPoint
			// because we need to enforce the passing through p0 constraint.
			double theta;
			
			int a = p0.x - p.x;
			int b = p0.y - p.y;

			if (a == 0)
				theta = 0.;
			else if (b == 0)
				theta = -ImagePoint.PIover2;
			else
				theta = -Math.atan((double)a/(double)b);
			
			thetas.add(theta);
			thetas.add(theta + Math.PI);
			
			// Center on the center of mass
			double alpha = 2.*theta + Math.PI;
			alpha_sum_sin += Math.sin(alpha);
			alpha_sum_cos += Math.cos(alpha);
		}
		
		// Compute center of mass
		double alpha_mass = Math.atan2(-alpha_sum_sin, -alpha_sum_cos);
		double theta_mass = alpha_mass/2.;
		
		// Center pairwise angles on the center of mass and limit to range [-pi/2, pi/2[
		double inf_bound = -ImagePoint.PIover2+theta_mass, sup_bound = ImagePoint.PIover2+theta_mass;
		
		List<java.lang.Double> centered_thetas = new Vector<>();
		
		for (int i = 0; i < thetas.size(); i++) {
			double centered_theta = thetas.get(i);
			
			if (java.lang.Double.compare(centered_theta, inf_bound) >= 0 && java.lang.Double.compare(centered_theta, sup_bound) <= 0)
				centered_thetas.add(centered_theta);
		}
		
		// Estimate the fitting angle with Theil-Sen and map it back to original range
		centered_thetas.sort(null);
		double theta = centered_thetas.get(centered_thetas.size()/2);
		
		// Deduce rho (remember p0 is on the line)
		double rho = p0.x*Math.cos(theta) + p0.y*Math.sin(theta);
		
		
		return new HoughPoint(theta, rho);
	}
	
	/**
	 * Convenience method for straight line estimation (Hesse normal form).
	 * @param p0 A point that is known to be on the line to estimate.
	 * @param points A list of points used to estimate the line.
	 * @return The estimated Hough point (straight line in Hesse normal form).
	 */
	public HoughPoint estimatedHoughPoint(List<ImagePoint> points) {
		return ImagePoint.estimateHoughPoint(this, points);
	}
	
	/**
	 * Compute the Hough representation of the line going through input image points.
	 * 
	 * Two image points are enough to build a line and express this line in the Hough space.
	 * 
	 * @param p1 First input point.
	 * @param p2 Second input point.
	 * @return The Hough point corresponding to the line going through input points.
	 */
	public static HoughPoint convertImagePointsToHoughPoint(ImagePoint p1, ImagePoint p2) {
		HoughPoint p = new HoughPoint();
		
		int a = p1.x - p2.x;
		int b = p1.y - p2.y;
		
		if (a == 0)
			p.setLocation(0.0, p1.x);
		else if (b == 0)
			p.setLocation(-ImagePoint.PIover2, p1.y);
		else { // a != 0 && b != 0
			double theta = -Math.atan((double)a/(double)b);
			double cosTheta = Math.cos(theta);
			double sinTheta = Math.sin(theta);
			p.setLocation(theta, (p1.x*cosTheta + p1.y*sinTheta + p2.x*cosTheta + p2.y*sinTheta)/2.0);			
		}
		
		return p;
	}
	
	/**
	 * Convenience method for Hough point generation.
	 * @param p Second input point.
	 * @return The Hough point corresponding to the line going through current and input point.
	 * @see convertImagePointsToHoughPoint
	 */
	public HoughPoint convertImagePointsToHoughPoint(ImagePoint p) {
		return ImagePoint.convertImagePointsToHoughPoint(this, p);
	}
	
	/**
	 * Get the coordinates of the center point in image.
	 * @param image Input image.
	 * @return Center point.
	 */
	public static ImagePoint getCenterPointOfImage(ImagePlus image) {
		return new ImagePoint(image.getWidth()/2, image.getHeight()/2);
	}
	
	/**
	 * Get a list of point coordinates of foreground pixels in image.
	 * @param image Input image.
	 * @param roi Input roi.
	 * @param origin Origin of the coordinate system.
	 * @return A list of points coordinates.
	 */
	public static List<ImagePoint> getImageForegroundPoints(ImagePlus image, Roi roi, ImagePoint origin) {
		ImageProcessor processor = image.getProcessor();
		
		// Setup functions to be executed in parallel
		List<Callable<ImagePoint>> tasks = new Vector<>();
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				ImagePoint p = new ImagePoint(x, y);
				
				tasks.add(() -> {
					if (roi.contains(p.x, p.y) && processor.get(p.x, p.y) > 0)
						return p.subtract(origin);
					else
						return null;
				});
			}
		}
		
		// Run threads in parallel and reduce results
		Vector<ImagePoint> results = new Vector<>();
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
}
