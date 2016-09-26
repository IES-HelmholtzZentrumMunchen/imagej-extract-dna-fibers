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
		// ---
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
}
