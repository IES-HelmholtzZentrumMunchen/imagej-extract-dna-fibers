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
			p.setLocation(-Math.PI/2.0, p1.y);
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
	 * @param origin Origin of the coordinate system.
	 * @return A list of points coordinates.
	 */
	public static List<ImagePoint> getImageForegroundPoints(ImagePlus image, ImagePoint origin) {
		ImageProcessor processor = image.getProcessor();
		
		// Setup functions to be executed in parallel
		List<Callable<ImagePoint>> tasks = new Vector<>();
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				ImagePoint p = new ImagePoint(x, y);
				
				tasks.add(() -> {
					if (processor.get(p.x, p.y) > 0)
						return p;
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
