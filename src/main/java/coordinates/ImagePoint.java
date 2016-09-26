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
}
