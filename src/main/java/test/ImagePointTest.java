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

import java.util.List;
import java.util.Random;

import org.junit.Test;

import coordinates.*;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;


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

	@Test
	public void testGetCenterPointOfImage() {
		ImagePoint p = new ImagePoint(), pexp = new ImagePoint();
		ImagePlus image;
		
		// Square image with odd width and height
		pexp = new ImagePoint(32,32);
		image = NewImage.createByteImage("", 65, 65, 1, NewImage.FILL_BLACK);
		p = ImagePoint.getCenterPointOfImage(image);
		assertEquals(pexp, p);
		
		// Rectangular image with odd width and height
		pexp = new ImagePoint(8,32);
		image = NewImage.createByteImage("", 17, 65, 1, NewImage.FILL_BLACK);
		p = ImagePoint.getCenterPointOfImage(image);
		assertEquals(pexp, p);
		
		// Square image with odd width and even height
		pexp = new ImagePoint(32,32);
		image = NewImage.createByteImage("", 65, 64, 1, NewImage.FILL_BLACK);
		p = ImagePoint.getCenterPointOfImage(image);
		assertEquals(pexp, p);
		
		// Rectangular image with odd width and even height
		pexp = new ImagePoint(8,32);
		image = NewImage.createByteImage("", 17, 64, 1, NewImage.FILL_BLACK);
		p = ImagePoint.getCenterPointOfImage(image);
		assertEquals(pexp, p);
		
		// Square image with even width and odd height
		pexp = new ImagePoint(32,32);
		image = NewImage.createByteImage("", 64, 65, 1, NewImage.FILL_BLACK);
		p = ImagePoint.getCenterPointOfImage(image);
		assertEquals(pexp, p);
		
		// Rectangular image with even width and odd height
		pexp = new ImagePoint(8,32);
		image = NewImage.createByteImage("", 16, 65, 1, NewImage.FILL_BLACK);
		p = ImagePoint.getCenterPointOfImage(image);
		assertEquals(pexp, p);
		
		// Square image with even width and height
		pexp = new ImagePoint(32,32);
		image = NewImage.createByteImage("", 64, 64, 1, NewImage.FILL_BLACK);
		p = ImagePoint.getCenterPointOfImage(image);
		assertEquals(pexp, p);
		
		// Rectangular image with even width and height
		pexp = new ImagePoint(8,32);
		image = NewImage.createByteImage("", 16, 64, 1, NewImage.FILL_BLACK);
		p = ImagePoint.getCenterPointOfImage(image);
		assertEquals(pexp, p);
	}
	
	@Test
	public void testGetImageForegroundPoints() {
		List<ImagePoint> list;
		ImagePlus image = NewImage.createByteImage("", 5, 5, 1, NewImage.FILL_BLACK);
		ImageProcessor processor = image.getProcessor();
		
		// Top-left origin
		ImagePoint origin = new ImagePoint();
		
		// No points
		processor.setValue(0); processor.fill();
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (!list.isEmpty()) fail("exptected empty list, got something");
		
		// One point only
		processor.setValue(0); processor.fill();
		processor.set(2, 2, 1);
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(processor.get(p.x, p.y), 1);
		}
		
		// Few points
		processor.setValue(0); processor.fill();
		processor.set(2, 2, 1);
		processor.set(1, 2, 1);
		processor.set(3, 2, 1);
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(processor.get(p.x, p.y), 1);
		}
		
		// All points
		processor.setValue(1); processor.fill();
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(processor.get(p.x, p.y), 1);
		}
		
		// Image center origin
		origin = ImagePoint.getCenterPointOfImage(image);
		
		// No points
		processor.setValue(0); processor.fill();
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (!list.isEmpty()) fail("exptected empty list, got something");
		
		// One point only
		processor.setValue(0); processor.fill();
		processor.set(2, 2, 1);
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(processor.get(p.x, p.y), 1);
		}
		
		// Few points
		processor.setValue(0); processor.fill();
		processor.set(2, 2, 1);
		processor.set(1, 2, 1);
		processor.set(3, 2, 1);
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(processor.get(p.x, p.y), 1);
		}
		
		// All points
		processor.setValue(1); processor.fill();
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(processor.get(p.x, p.y), 1);
		}
		
		// Random origin
		Random random = new Random();
		origin = new ImagePoint(random.nextInt(image.getWidth()), random.nextInt(image.getHeight()));
		
		// No points
		processor.setValue(0); processor.fill();
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (!list.isEmpty()) fail("exptected empty list, got something");
		
		// One point only
		processor.setValue(0); processor.fill();
		processor.set(2, 2, 1);
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(processor.get(p.x, p.y), 1);
		}
		
		// Few points
		processor.setValue(0); processor.fill();
		processor.set(2, 2, 1);
		processor.set(1, 2, 1);
		processor.set(3, 2, 1);
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(processor.get(p.x, p.y), 1);
		}
		
		// All points
		processor.setValue(1); processor.fill();
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(processor.get(p.x, p.y), 1);
		}
	}
}
