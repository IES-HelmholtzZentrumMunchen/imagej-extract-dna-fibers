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

import coordinates.HoughPoint;
import coordinates.ImagePoint;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;

/**
 * @author julien.pontabry
 *
 */
public class HoughPointTest {
	/**
	 * Test method for {@link coordinates.HoughPoint#convertHoughPointToImageLine(ij.ImagePlus)}.
	 */
	@Test
	public void testConvertHoughPointToImageLine() {
		// Read some image to get the extent
		ImagePlus skeletons = IJ.openImage(Extract_DNA_FibersTest.testpath + "example_skeletons.zip");
		
		//
		// Origin top left
		//
		
		ImagePoint origin = new ImagePoint();//ImagePoint.getCenterPointOfImage(skeletons);
		
		// Vertical line
		HoughPoint p = new HoughPoint();
		Line l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(0, l.x1); assertEquals(0, l.x2);
		assertEquals(0, l.y1); assertEquals(skeletons.getHeight()-1, l.y2);
		
		// Horizontal line
		p = new HoughPoint(-Math.PI/2.0, 0);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(0, l.x1); assertEquals(skeletons.getWidth()-1, l.x2);
		assertEquals(0, l.y1); assertEquals(0, l.y2);
		
		// Diagonal lines
		p = new HoughPoint(-Math.PI/4.0, 0);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(0, l.x1); assertEquals(Math.min(skeletons.getWidth(), skeletons.getHeight())-1, l.x2);
		assertEquals(0, l.y1); assertEquals(Math.min(skeletons.getWidth(), skeletons.getHeight())-1, l.y2);
		
		p = new HoughPoint(Math.PI/4.0, Math.sqrt(Math.pow((skeletons.getWidth()-1)/2.0,2) + Math.pow((skeletons.getHeight()-1)/2.0, 2)));
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(636, l.x1); assertEquals(5, l.x2);
		assertEquals(0, l.y1); assertEquals(631, l.y2);
		
		// General cases
		p = new HoughPoint(-1.023, 0);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(0, l.x1); assertEquals(641, l.x2);
		assertEquals(0, l.y1); assertEquals(384, l.y2);
		
		p = new HoughPoint(-1.023, 102);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(183, l.x1); assertEquals(641, l.x2);
		assertEquals(0, l.y1); assertEquals(297, l.y2);
		
		p = new HoughPoint(1.023, 102);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(183, l.x1); assertEquals(0, l.x2);
		assertEquals(0, l.y1); assertEquals(119, l.y2);
		
		
		//
		// Origin image center
		//
		
		origin = ImagePoint.getCenterPointOfImage(skeletons);
		
		// Vertical line
		p = new HoughPoint();
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals((int)(skeletons.getWidth()/2.0), l.x1); assertEquals((int)(skeletons.getWidth()/2.0), l.x2);
		assertEquals(0, l.y1); assertEquals(skeletons.getHeight()-1, l.y2);
		
		// Horizontal line
		p = new HoughPoint(-Math.PI/2.0, 0);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(skeletons.getWidth()-1, l.x1); assertEquals(0, l.x2);
		assertEquals((int)(skeletons.getHeight()/2.0), l.y1); assertEquals((int)(skeletons.getHeight()/2.0), l.y2);
		
		// Diagonal lines
		p = new HoughPoint(-Math.PI/4.0, 0);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(636, l.x1); assertEquals(5, l.x2);
		assertEquals(Math.min(skeletons.getWidth(), skeletons.getHeight())-1, l.y1); assertEquals(0, l.y2);
		
		p = new HoughPoint(Math.PI/4.0, 0);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(637, l.x1); assertEquals(0, l.x2);
		assertEquals(0, l.y1); assertEquals(637, l.y2);
		
		// General cases
		p = new HoughPoint(-1.023, 0);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(0, l.x1); assertEquals(641, l.x2);
		assertEquals(123, l.y1); assertEquals(508, l.y2);
		
		p = new HoughPoint(-1.023, 102);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(18, l.x1); assertEquals(641, l.x2);
		assertEquals(0, l.y1); assertEquals(404, l.y2);
		
		p = new HoughPoint(1.023, 102);
		l = p.convertHoughPointToImageLine(skeletons, origin);
		assertEquals(20, l.x1); assertEquals(641, l.x2);
		assertEquals(631, l.y1); assertEquals(227, l.y2);
	}

}
