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
import java.util.Vector;

import org.junit.Test;

import coordinates.*;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;


public class ImagePointTest {
	private final double epsilon = 1e-10;
	
	/**
	 * Test method for {@link ImagePoint#subtract(ImagePoint)}.
	 */
	@Test
	public void testSubtract() {
		ImagePoint p1 = new ImagePoint(), p2 = new ImagePoint();
		
		// Subtraction of null points
		assertEquals(p1, p1.subtract(p2));
		assertEquals(p2, p1.subtract(p2));
		
		// Subtract null point
		p1.setLocation(2, 11);
		assertEquals(p1, p1.subtract(p2));
		
		// General case
		p2.setLocation(5, -3);
		assertEquals(new ImagePoint(-3, 14), p1.subtract(p2));
	}
	
	/**
	 * Test method for {@link ImagePoint#subtractTo(List)
	 */
	@Test
	public void testSubtractTo() {
		List<ImagePoint> initialList = new Vector<>();
		initialList.add(new ImagePoint()); initialList.add(new ImagePoint(2,3));
		initialList.add(new ImagePoint(-1,-1)); initialList.add(new ImagePoint(-5,2));
		
		// Addition of null point
		ImagePoint p = new ImagePoint();
		List<ImagePoint> list = p.subtractTo(initialList);
		for (int i = 0; i < initialList.size(); i++) {
			assertEquals(list.get(i), initialList.get(i));
		}
		
		// General cases
		p = new ImagePoint(2,3);
		list = p.subtractTo(initialList);
		for (int i = 0; i < initialList.size(); i++) {
			assertEquals(list.get(i), initialList.get(i).subtract(p));
		}
		
		p = new ImagePoint(-2,-1);
		list = p.subtractTo(initialList);
		for (int i = 0; i < initialList.size(); i++) {
			assertEquals(list.get(i), initialList.get(i).subtract(p));
		}
		
		p = new ImagePoint(-10,3);
		list = p.subtractTo(initialList);
		for (int i = 0; i < initialList.size(); i++) {
			assertEquals(list.get(i), initialList.get(i).subtract(p));
		}
	}
	
	/**
	 * Test method for {@link ImagePoint#add(ImagePoint)}.
	 */
	@Test
	public void testAdd() {
		ImagePoint p1 = new ImagePoint(), p2 = new ImagePoint();
		
		// Addition of null points
		assertEquals(p1, p1.add(p2));
		assertEquals(p2, p1.add(p2));
		
		// Add null point
		p1.setLocation(2, 11);
		assertEquals(p1, p1.add(p2));
		
		// General case
		p2.setLocation(5, -3);
		assertEquals(new ImagePoint(7, 8), p1.add(p2));
	}
	
	/**
	 * Test method for {@link ImagePoint#addTo(List) 
	 */
	@Test
	public void testAddTo() {
		List<ImagePoint> initialList = new Vector<>();
		initialList.add(new ImagePoint()); initialList.add(new ImagePoint(2,3));
		initialList.add(new ImagePoint(-1,-1)); initialList.add(new ImagePoint(-5,2));
		
		// Addition of null point
		ImagePoint p = new ImagePoint();
		List<ImagePoint> list = p.addTo(initialList);
		for (int i = 0; i < initialList.size(); i++) {
			assertEquals(list.get(i), initialList.get(i));
		}
		
		// General cases
		p = new ImagePoint(2,3);
		list = p.addTo(initialList);
		for (int i = 0; i < initialList.size(); i++) {
			assertEquals(list.get(i), initialList.get(i).add(p));
		}
		
		p = new ImagePoint(-2,-1);
		list = p.addTo(initialList);
		for (int i = 0; i < initialList.size(); i++) {
			assertEquals(list.get(i), initialList.get(i).add(p));
		}
		
		p = new ImagePoint(-10,3);
		list = p.addTo(initialList);
		for (int i = 0; i < initialList.size(); i++) {
			assertEquals(list.get(i), initialList.get(i).add(p));
		}
	}
	
	/**
	 * Test method for {@link ImagePoint#distanceBetweenPoints(ImagePoint, ImagePoint)}
	 */
	@Test
	public void testDistanceBetweenPoints() {
		ImagePoint p1 = new ImagePoint(), p2 = new ImagePoint();
		double distance, expDistance;
		
		expDistance = 0.0;
		distance = ImagePoint.distanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
		
		p1 = new ImagePoint(1,1); p2 = new ImagePoint(-1,-1);
		expDistance = 2.0 * Math.sqrt(2.0);
		distance = ImagePoint.distanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
		
		p1 = new ImagePoint(1,3); p2 = new ImagePoint(1,2);
		expDistance = 1.0;
		distance = ImagePoint.distanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
		
		p1 = new ImagePoint(-1,-1); p2 = new ImagePoint();
		expDistance = Math.sqrt(2.0);
		distance = ImagePoint.distanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
		
		p1 = new ImagePoint(); p2 = new ImagePoint(0,100);
		expDistance = 100.0;
		distance = ImagePoint.distanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
	}
	
	/**
	 * Test method for {@link ImagePoint#squaredDistanceBetweenPoints(ImagePoint, ImagePoint)}
	 */
	@Test
	public void testSquaredDistanceBetweenPoints() {
		ImagePoint p1 = new ImagePoint(), p2 = new ImagePoint();
		double distance, expDistance;
		
		expDistance = 0.0;
		distance = ImagePoint.squaredDistanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
		
		p1 = new ImagePoint(1,1); p2 = new ImagePoint(-1,-1);
		expDistance = 8.0;
		distance = ImagePoint.squaredDistanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
		
		p1 = new ImagePoint(1,3); p2 = new ImagePoint(1,2);
		expDistance = 1.0;
		distance = ImagePoint.squaredDistanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
		
		p1 = new ImagePoint(-1,-1); p2 = new ImagePoint();
		expDistance = 2.0;
		distance = ImagePoint.squaredDistanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
		
		p1 = new ImagePoint(); p2 = new ImagePoint(0,100);
		expDistance = 10000.0;
		distance = ImagePoint.squaredDistanceBetweenPoints(p1, p2);
		assertEquals(expDistance, distance, this.epsilon);
	}

	/**
	 * Test method for {@link ImagePoint#convertImagePointsToHoughPointImagePointImagePoint(ImagePoint, ImagePoint)}.
	 */
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
	
	/**
	 * Test method for {@link ImagePoint#estimateHoughPoint(ImagePoint, List)}
	 */
	@Test
	public void testEstimateHoughPoint() {
		List<ImagePoint> points = new Vector<>();
		points.add(new ImagePoint(18,0)); points.add(new ImagePoint(18,1)); points.add(new ImagePoint(18,2));
		points.add(new ImagePoint(26,2)); points.add(new ImagePoint(18,3)); points.add(new ImagePoint(26,3));
		points.add(new ImagePoint(18,4)); points.add(new ImagePoint(19,5)); points.add(new ImagePoint(19,6));
		points.add(new ImagePoint(2,7)); points.add(new ImagePoint(19,7)); points.add(new ImagePoint(2,8));
		points.add(new ImagePoint(20,8)); points.add(new ImagePoint(20,9)); points.add(new ImagePoint(20,10));
		points.add(new ImagePoint(21,11)); points.add(new ImagePoint(21,12)); points.add(new ImagePoint(4,16));
		points.add(new ImagePoint(5,17)); points.add(new ImagePoint(23,18)); points.add(new ImagePoint(23,19));
		points.add(new ImagePoint(23,20)); points.add(new ImagePoint(24,21)); points.add(new ImagePoint(24,22));
		points.add(new ImagePoint(25,23)); points.add(new ImagePoint(25,24)); points.add(new ImagePoint(25,26));
		points.add(new ImagePoint(25,27)); points.add(new ImagePoint(8,28)); points.add(new ImagePoint(9,28));
		points.add(new ImagePoint(26,28)); points.add(new ImagePoint(28,34)); points.add(new ImagePoint(28,35));
		points.add(new ImagePoint(29,43)); points.add(new ImagePoint(13,44)); points.add(new ImagePoint(30,44));
		points.add(new ImagePoint(13,45)); points.add(new ImagePoint(30,45)); points.add(new ImagePoint(13,46));
		points.add(new ImagePoint(30,46)); points.add(new ImagePoint(13,47)); points.add(new ImagePoint(30,47));
		points.add(new ImagePoint(13,48)); points.add(new ImagePoint(30,48)); points.add(new ImagePoint(13,49));
		points.add(new ImagePoint(30,49));
		
		ImagePoint p0 = new ImagePoint(25,25);
		HoughPoint h = p0.estimatedHoughPoint(points);
		
		assertEquals(-0.273008703087, h.theta, 1e-7);
		assertEquals(17.3333523549, h.rho, 1e-7);
	}

	/**
	 * Test method for {@link ImagePoint#getCenterPointOfImage(ImagePlus)}.
	 */
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
	
	/**
	 * Test method for {@link ImagePoint#getImageForegroundPoints(ImagePlus, ImagePoint)}.
	 */
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
			assertEquals(1, processor.get(p.x+origin.x, p.y+origin.y));
		}
		
		// Few points
		processor.setValue(0); processor.fill();
		processor.set(2, 2, 1);
		processor.set(1, 2, 1);
		processor.set(3, 2, 1);
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(1, processor.get(p.x+origin.x, p.y+origin.y));
		}
		
		// All points
		processor.setValue(1); processor.fill();
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(1, processor.get(p.x+origin.x, p.y+origin.y));
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
			assertEquals(1, processor.get(p.x+origin.x, p.y+origin.y));
		}
		
		// Few points
		processor.setValue(0); processor.fill();
		processor.set(2, 2, 1);
		processor.set(1, 2, 1);
		processor.set(3, 2, 1);
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(1, processor.get(p.x+origin.x, p.y+origin.y));
		}
		
		// All points
		processor.setValue(1); processor.fill();
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(1, processor.get(p.x+origin.x, p.y+origin.y));
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
			assertEquals(1, processor.get(p.x+origin.x, p.y+origin.y));
		}
		
		// Few points
		processor.setValue(0); processor.fill();
		processor.set(2, 2, 1);
		processor.set(1, 2, 1);
		processor.set(3, 2, 1);
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(1, processor.get(p.x+origin.x, p.y+origin.y));
		}
		
		// All points
		processor.setValue(1); processor.fill();
		list = ImagePoint.getImageForegroundPoints(image, origin);
		if (list.isEmpty()) fail("exptected non-empty list, got nothing");
		for (ImagePoint p : list) {
			assertEquals(1, processor.get(p.x+origin.x, p.y+origin.y));
		}
	}
}
