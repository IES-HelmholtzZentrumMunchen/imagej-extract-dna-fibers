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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ij.ImagePlus;
import ij.gui.Line;

/**
 * Define a point in Hough space.
 * 
 * The origin is centered and the space extent to [-pi/2,pi/2) x [-z,z].
 * 
 * @author julien.pontabry
 */
public class HoughPoint extends Point2D {
	/** The theta coordinate (angle). */
	public double theta;
	
	/** The rho coordinate (shift). */
	public double rho;
	
	/**
	 * Default constructor.
	 * 
	 * Gives the origin point.
	 */
	public HoughPoint() {
		this(0.0, 0.0);
	}
	
	/**
	 * Constructor.
	 * @param p Input point used to initialized new point.
	 */
	public HoughPoint(HoughPoint p) {
		this(p.theta, p.rho);
	}
	
	/**
	 * Constructor.
	 * @param theta Input angle coordinate.
	 * @param rho Input shift coordinate.
	 */
	public HoughPoint(double theta, double rho) {
		this.theta = theta;
		this.rho = rho;
	}

	/**
	 * @see java.awt.geom.Point2D#getX()
	 */
	@Override
	public double getX() {
		return this.theta;
	}

	/**
	 * @see java.awt.geom.Point2D#getY()
	 */
	@Override
	public double getY() {
		return this.rho;
	}

	/**
	 * @see java.awt.geom.Point2D#setLocation(double, double)
	 */
	@Override
	public void setLocation(double x, double y) {
		this.theta = x;
		this.rho = y;
	}
	
	/**
	 * Convert point to string format.
	 */
	public String toString() {
		return "HoughPoint["+this.theta+","+this.rho+"]";
	}
	
	/**
	 * Convert a point in Hough space into a line in image space.
	 * @param image Input image.
	 * @param origin Origin of the input image coordinates system.
	 * @return A line ROI.
	 */
	public Line convertHoughPointToImageLine(ImagePlus image, ImagePoint origin) {
		ImagePoint p1, p2;
		
		// Compute 2 points with Hough equation
		if (Math.abs(this.theta) < 1e-10){
			p1 = new ImagePoint((int)Math.round(this.rho), -10).add(origin);
			p2 = new ImagePoint((int)Math.round(this.rho), 10).add(origin);
			
			p1.y = 0;
			p2.y = image.getHeight()-1;
		}
		else { // this.theta > 0
			double tenCosTheta = 10*Math.cos(this.theta);
			double sinTheta = Math.sin(this.theta);
			
			p1 = new ImagePoint(10, (int)Math.round((this.rho - tenCosTheta) / sinTheta)).add(origin);
			p2 = new ImagePoint(-10, (int)Math.round((this.rho + tenCosTheta) / sinTheta)).add(origin);
			
			// Compute the affine equation
			double a = ((double)p2.y-(double)p1.y) / ((double)p2.x-(double)p1.x);
			double b = p1.y - a*p1.x;
			
			// Compute the border points
			Vector<ImagePoint> p = new Vector<>();
			
			double test = -b/a;
			if (0 <= test && test < image.getWidth())
				p.add(new ImagePoint((int)test, 0));
			
			test = (image.getHeight()-1-b)/a;
			if (0 <= test && test < image.getWidth())
				p.add(new ImagePoint((int)test, image.getHeight()-1));
			
			test = b;
			if (0 <= test && test < image.getWidth())
				p.add(new ImagePoint(0, (int)test));
			
			test = a*(image.getWidth()-1)+b;
			if (0 <= test && test < image.getWidth())
				p.add(new ImagePoint(image.getWidth()-1, (int)test));
			
			// It is possible that there are more than 2 points (for instance
			// when there is a diagonal line), but in that case, taking the
			// two first points (y=0 and y=h-1) is enough; we just remove doublons.
			Set<ImagePoint> tmp = new HashSet<>();
			tmp.addAll(p);
			p.clear();
			p.addAll(tmp);
			
			p1 = p.get(0);
			p2 = p.get(1);
		}
		
		return new Line(p1.x, p1.y, p2.x, p2.y);
	}
}
