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
package test;

import static org.junit.Assert.*;
import org.junit.Test;

import coordinates.*;

public class ImagePointTest {
	private final double epsilon = 1e-10;

	@Test
	public void testConvertImagePointsToHoughPointImagePointImagePoint() {
		ImagePoint p1 = new ImagePoint(), p2 = new ImagePoint();
		HoughPoint p, pexp = new HoughPoint();
		
		// Vertical line
		p1.setLocation(10, 23); p2.setLocation(10, 50); pexp.setLocation(0, 10);
		p = ImagePoint.convertImagePointsToHoughPoint(p1, p2);
		assertEquals(pexp.theta, p.theta, this.epsilon);
		assertEquals(pexp.rho, p.rho, this.epsilon);
		
		// Horizontal line
		p1.setLocation(10, 21); p2.setLocation(31, 21); pexp.setLocation(-Math.PI/2, 21);
		p = ImagePoint.convertImagePointsToHoughPoint(p1, p2);
		assertEquals(pexp.theta, p.theta, this.epsilon);
		assertEquals(pexp.rho, p.rho, this.epsilon);
		
		// General lines
		p1.setLocation(10, 10); p2.setLocation(30, 30); pexp.setLocation(-Math.PI/4, 0);
		p = ImagePoint.convertImagePointsToHoughPoint(p1, p2);
		assertEquals(pexp.theta, p.theta, this.epsilon);
		assertEquals(pexp.rho, p.rho, this.epsilon);
		
		p1.setLocation(-10, -10); p2.setLocation(-30, -30); pexp.setLocation(-Math.PI/4, 0); // Lines are radially symmetric and theta is in [-pi/2,pi/2)
		p = ImagePoint.convertImagePointsToHoughPoint(p1, p2);
		assertEquals(pexp.theta, p.theta, this.epsilon);
		assertEquals(pexp.rho, p.rho, this.epsilon);
		
		p1.setLocation(10, -10); p2.setLocation(30, -30); pexp.setLocation(Math.PI/4, 0);
		p = ImagePoint.convertImagePointsToHoughPoint(p1, p2);
		assertEquals(pexp.theta, p.theta, this.epsilon);
		assertEquals(pexp.rho, p.rho, this.epsilon);
	}

}
