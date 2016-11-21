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
import java.util.Vector;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.NewImage;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import coordinates.*;

import main.Extract_DNA_Fibers;



/**
 * Test class for static methods of main plugin class.
 * @author julien.pontabry
 */
public class Extract_DNA_FibersTest {
	/** Maximal error allowed when testing differences of structures. */
	public static final double max_error = 1e-2;
	
	public static String testpath = "src/test/resources/";
	
	@Before
	public void setUp() throws Exception {
		// FIXME Is there a way to know where is installed Fiji for automatic testing?
		System.setProperty("plugins.dir", "/Applications/Fiji.app/plugins");
		new ImageJ();
	}
	
	/** 
	 * Compute the mean squared error of the element-wise difference between two images. 
	 * @param im1 First image to test.
	 * @param im2 Second image to test.
	 * @return The MSE of the element-wise difference between given images. 
	 */
	public static double computeMSE(ImagePlus im1, ImagePlus im2) {
		ImageProcessor ip1 = im1.getProcessor();
		ImageProcessor ip2 = im2.getProcessor();
		
		double MSE = 0.0;
				
		for (int i = 0; i < ip1.getPixelCount(); i++)
			MSE += Math.pow(ip1.get(i) - ip2.get(i), 2);
		
		MSE /= ip1.getPixelCount();
				
		return MSE;
	}

	/**
	 * Test method for {@link Extract_DNA_Fibers#extractSkeletons(ij.ImagePlus, int, int, double)}.
	 */
	@Test @Ignore("Not yet compatible with Travis-CI")
	public void testExtractSkeletons() {
		ImagePlus expected = IJ.openImage(Extract_DNA_FibersTest.testpath + "example_skeletons.zip");
		ImagePlus original = IJ.openImage(Extract_DNA_FibersTest.testpath + "example_original.zip");
		
		ImagePlus actual = Extract_DNA_Fibers.extractSkeletons(original, 1, 2, 2);
//		IJ.save(actual, testpath + "example_skeletons_actual.zip");

		double error = Extract_DNA_FibersTest.computeMSE(expected, actual);
		if (error > Extract_DNA_FibersTest.max_error)
			fail("Expected <"+ Extract_DNA_FibersTest.max_error +", got "+ error);
	}
	
	/**
	 * Test if a list contains a specific point in Hough space.
	 * @param pexp Expected point.
	 * @param list List to test.
	 * @return True if the list contains the expected point, false otherwise.
	 */
	public static boolean containsPoint(HoughPoint pexp, List<HoughPoint> list) {
		boolean contains = false;
		
		for (HoughPoint p : list) {
//			if (Double.compare(pexp.theta, p.theta) == 0 && Double.compare(pexp.rho, p.rho) == 0) {
			if (Math.abs(pexp.theta-p.theta) < 1e-10 && Math.abs(pexp.rho-p.rho) < 1e-10) {
				contains = true;
				break;
			}
		}
		
		return contains;
	}
	
	/**
	 * Compute the Hausdorff distance (maximal euclidian distance to closest points) between two lists of points.
	 * @param points1 First list.
	 * @param points2 Second list.
	 * @return Hausdorff distance of the two input lists. 
	 */
	public static double computeHausdorffDistance(List<HoughPoint> points1, List<HoughPoint> points2) {
		double maximalDistance = 0.0;
		
		for (HoughPoint p1 : points1) {
			double thetaDiff = p1.theta - points2.get(0).theta;
			double   rhoDiff = p1.rho - points2.get(0).rho;
			
			double minimalDistance = thetaDiff*thetaDiff + rhoDiff*rhoDiff;
			
			for (int i = 1; i < points2.size(); i++) {
				thetaDiff       = p1.theta - points2.get(i).theta;
				double distance = thetaDiff * thetaDiff;
				
				if (distance < minimalDistance) {
					rhoDiff  = p1.rho - points2.get(i).rho;
					distance += rhoDiff * rhoDiff;
					
					if (distance < minimalDistance)
						minimalDistance = distance;
				}
			}

			if (minimalDistance > maximalDistance)
				maximalDistance = minimalDistance;
		}
		
		return Math.sqrt(maximalDistance);
	}
	
	/**
	 * Test method for {@link Extract_DNA_Fibers#buildHoughSpaceFromSkeletons(ij.ImagePlus, int)}.
	 * @throws Exception
	 */
	@Test
	public void testBuildHoughSpaceFromSkeletons() throws Exception {
		//
		// Simulations
		//
		
		HoughPoint pexp;
		ImagePlus skeletons = NewImage.createByteImage("", 16, 16, 1, NewImage.FILL_BLACK);
		skeletons.setRoi(0, 0, skeletons.getWidth(), skeletons.getHeight());
		ImageProcessor processor = skeletons.getProcessor();
		
		processor.set(8, 1, 1);
		processor.set(13, 10, 1);
		processor.set(2, 13, 1);
		
		ImagePoint origin = ImagePoint.getCenterPointOfImage(skeletons);
		List<HoughPoint> list = Extract_DNA_Fibers.buildHoughSpaceFromSkeletons(skeletons, skeletons.getRoi(), 100, 25);
		
		if (list.isEmpty())
			fail("Expected non-empty list, got nothing");
		
		pexp = ImagePoint.convertImagePointsToHoughPoint(new ImagePoint(8, 1).subtract(origin), new ImagePoint(13, 10).subtract(origin));
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, list));
		
		pexp = ImagePoint.convertImagePointsToHoughPoint(new ImagePoint(8, 1).subtract(origin), new ImagePoint(2, 13).subtract(origin));
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, list));
		
		pexp = ImagePoint.convertImagePointsToHoughPoint(new ImagePoint(13, 10).subtract(origin), new ImagePoint(2, 13).subtract(origin));
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, list));
		
		//
		// Simulations with ROI
		//
		
		skeletons.setRoi(0, 0, 14, 11);
		
		processor.set(8, 1, 1);
		processor.set(13, 10, 1);
		processor.set(2, 13, 1);
		
		list = Extract_DNA_Fibers.buildHoughSpaceFromSkeletons(skeletons, skeletons.getRoi(), 100, 25);
		
		if (list.isEmpty())
			fail("Expected non-empty list, got nothing");
		
		pexp = ImagePoint.convertImagePointsToHoughPoint(new ImagePoint(8, 1).subtract(origin), new ImagePoint(13, 10).subtract(origin));
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, list));
		
		pexp = ImagePoint.convertImagePointsToHoughPoint(new ImagePoint(8, 1).subtract(origin), new ImagePoint(2, 13).subtract(origin));
		assertTrue(!Extract_DNA_FibersTest.containsPoint(pexp, list));
		
		pexp = ImagePoint.convertImagePointsToHoughPoint(new ImagePoint(13, 10).subtract(origin), new ImagePoint(2, 13).subtract(origin));
		assertTrue(!Extract_DNA_FibersTest.containsPoint(pexp, list));

		//
		// Real dataset
		//
		
		skeletons = IJ.openImage(Extract_DNA_FibersTest.testpath + "example_skeletons.zip");
		skeletons.setRoi(0, 0, skeletons.getWidth(), skeletons.getHeight());
		List<HoughPoint> nearlyExpected = CsvManager.readHoughPoints(Extract_DNA_FibersTest.testpath+"hough_points.csv", ",");
		
		long time_start = System.nanoTime();
		List<HoughPoint> points = Extract_DNA_Fibers.buildHoughSpaceFromSkeletons(skeletons, skeletons.getRoi(), 3000, 25);
		long time_end = System.nanoTime();
		System.out.println("Elasped time for real Hough space building: "+ (time_end-time_start)/1000000. +" ms.");
//		CsvManager.writeHoughPoints(points, Extract_DNA_FibersTest.testpath+"hough_points.csv", ",");
		
		double hausdorffDistance = Extract_DNA_FibersTest.computeHausdorffDistance(nearlyExpected, points);
		double minHausdorffDistance = 30;
		assertTrue("Hausdorff distance test failed (expected below <"+minHausdorffDistance+">, actual <"+hausdorffDistance+">).", minHausdorffDistance > hausdorffDistance);
	}
	
	/**
	 * Test method for {@link Extract_DNA_Fibers#replicateHoughSpaceBorders(java.util.List<coordinates.HoughPoint>, double}
	 */
	@Test
	public void testReplicateHoughSpaceBorders() {
		// Dataset
		List<HoughPoint> points = new Vector<HoughPoint>();
		points.add(new HoughPoint(1,3)); points.add(new HoughPoint(2,3));
		points.add(new HoughPoint(1,-2)); points.add(new HoughPoint(-1,2));
		points.add(new HoughPoint(1,2)); points.add(new HoughPoint(1,0));
		points.add(new HoughPoint(-3,3)); points.add(new HoughPoint(-1,1));
		
		double supBoundX = 3.0, infBoundX = -3.0;
		
		// Test with bandwidth equals 0.2
		List<HoughPoint> rep = Extract_DNA_Fibers.replicateHoughSpaceBorders(points, 0.2, supBoundX, infBoundX, false);
		
		assertEquals(points.size()+2, rep.size());
		
		for (HoughPoint p : points)
			assertTrue(Extract_DNA_FibersTest.containsPoint(p, rep));
		
		HoughPoint pexp = new HoughPoint(-4,3);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
		pexp = new HoughPoint(3,3);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
		
		// Test with bandwidth equals 0.4
		rep = Extract_DNA_Fibers.replicateHoughSpaceBorders(points, 0.4, supBoundX, infBoundX, true);

		assertEquals(points.size()+8, rep.size());

		for (HoughPoint p : points)
			assertTrue(Extract_DNA_FibersTest.containsPoint(p, rep));

		pexp = new HoughPoint(-4,-3);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
		pexp = new HoughPoint(-5,-3);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
		pexp = new HoughPoint(-5,2);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
		pexp = new HoughPoint(-5,0);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
		pexp = new HoughPoint(-5,2);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
		pexp = new HoughPoint(3,-3);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
		pexp = new HoughPoint(5,-1);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
		pexp = new HoughPoint(5,-2);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, rep));
	}
	
	/**
	 * Test method for {@link Extract_DNA_Fibers#selectHoughPoints(List, double, double, double)}
	 * @throws Exception 
	 */
	@Test
	public void testSelectHoughPoints() throws Exception {
		//
		// Simulations
		//

		List<HoughPoint> points = new Vector<HoughPoint>();
		points.add(new HoughPoint(0.5235988,1.5707963)); points.add(new HoughPoint(1.0471976,1.5707963));
		points.add(new HoughPoint(0.5235988,-1.0471976)); points.add(new HoughPoint(-0.5235988,1.0471976));
		points.add(new HoughPoint(0.5235988,1.0471976)); points.add(new HoughPoint(0.5235988,0.0000000));
		points.add(new HoughPoint(-1.5707963,1.5707963)); points.add(new HoughPoint(-0.5235988,0.5235988));
				
		
		List<HoughPoint> selectedPoints = Extract_DNA_Fibers.selectHoughPoints(points, 0, 15, 5);
		
		assertEquals(3, selectedPoints.size());

		HoughPoint pexp = new HoughPoint(0.5433148078406361,0.4444145062305863);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, selectedPoints));

		pexp = new HoughPoint(-0.5235988,0.7853982053946021);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, selectedPoints));
		
		pexp = new HoughPoint(1.4399509862664805,-0.7857240764529543);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, selectedPoints));
		
		
		selectedPoints = Extract_DNA_Fibers.selectHoughPoints(points, 0, 30, 5);
		
		assertEquals(1, selectedPoints.size());
		
		pexp = new HoughPoint(0.579964441507301,0.5143353639215483);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, selectedPoints));
		
		
		selectedPoints = Extract_DNA_Fibers.selectHoughPoints(points, 0, 2.5, 5);
		
		assertEquals(4, selectedPoints.size());
		
		pexp = new HoughPoint(0.5235988,0.39850718301113264);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, selectedPoints));

		pexp = new HoughPoint(1.0471976,1.5707963);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, selectedPoints));
		
		pexp = new HoughPoint(-0.5235988,0.7853982053946021);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, selectedPoints));

		pexp = new HoughPoint(-1.5707963,1.5707963);
		assertTrue(Extract_DNA_FibersTest.containsPoint(pexp, selectedPoints));
		
		
		//
		// Real dataset
		//
		
		points = CsvManager.readHoughPoints(Extract_DNA_FibersTest.testpath+"hough_points_lowsample.csv", ",");
		List<HoughPoint> expectedPoints = CsvManager.readHoughPoints(Extract_DNA_FibersTest.testpath+"selected_points.csv", ",");
		
		long startTime = System.nanoTime();
		selectedPoints = Extract_DNA_Fibers.selectHoughPoints(points, 0.33, 2.5, 5);

		long endTime = System.nanoTime();
		System.out.println("Elapsed time for real Hough points selection: "+(endTime-startTime)/1000000.+ "ms.");
//		CsvManager.writeHoughPoints(selectedPoints, Extract_DNA_FibersTest.testpath+"selected_points.csv", ",");
		
		assertEquals(expectedPoints.size(), selectedPoints.size());
		
		for (HoughPoint p : selectedPoints)
			assertTrue(p+" is not contained in expected list", Extract_DNA_FibersTest.containsPoint(p, expectedPoints));
		
		ImagePlus skeletons = IJ.openImage(Extract_DNA_FibersTest.testpath + "example_skeletons.zip");
		ImagePoint   origin = ImagePoint.getCenterPointOfImage(skeletons);
		RoiManager manager = new RoiManager();
		for (HoughPoint p : selectedPoints) {
			manager.addRoi(p.convertHoughPointToImageLine(skeletons, origin));
		}
//		manager.runCommand("Save", Extract_DNA_FibersTest.testpath+"lines.zip");
	}
	
	/**
	 * Test if a list contains a specific line ROI.
	 * @param pexp Expected line ROI.
	 * @param list List to test.
	 * @return True if the list contains the expected line ROI, false otherwise.
	 */
	public static boolean containsLineRoi(Line lexp, List<Line> list) {
		boolean contains = false;
		
		for (Line l : list) {
			if (l.x1 == lexp.x1 && l.x2 == lexp.x2 && l.y1 == lexp.y1 && l.y2 == lexp.y2) {
				contains = true;
				break;
			}
		}
		
		return contains;
	}
	
	/**
	 * Test method for {@link Extract_DNA_Fibers#buildSegments(ImagePlus, List, double, double, double)}
	 * @throws Exception 
	 */
	@Test
	public void testBuildSegments() throws Exception {
		ImagePlus             skeletons = IJ.openImage(Extract_DNA_FibersTest.testpath + "example_skeletons.zip");
		List<HoughPoint> selectedPoints = CsvManager.readHoughPoints(Extract_DNA_FibersTest.testpath+"selected_points.csv", ",");
		
		skeletons.setRoi(0, 0, skeletons.getWidth(), skeletons.getHeight());
		
		List<Line> segments = Extract_DNA_Fibers.buildSegments(skeletons, skeletons.getRoi(), selectedPoints, 50, 30, 2.0);
		
//		RoiManager manager = new RoiManager();
//		for (Line segment : segments)
//			manager.addRoi(segment);
//		
//		manager.runCommand("Save", Extract_DNA_FibersTest.testpath+"segments.zip");

		Line lexp = new Line(355, 470, 333, 375);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(323, 316, 306, 247);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(281, 108, 255, 0);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(369, 597, 328, 425);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(316, 372, 247, 70);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(275, 356, 267, 265);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(262, 137, 257, 69);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(275, 190, 308, 223);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(520, 401, 548, 429);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(587, 618, 578, 566);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(564, 498, 481, 115);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		
		// With ROI
		skeletons.setRoi(300, 100, 600, 500);
		segments = Extract_DNA_Fibers.buildSegments(skeletons, skeletons.getRoi(), selectedPoints, 50, 30, 2.0);
		
//		RoiManager manager = new RoiManager();
//		for (Line segment : segments)
//			manager.addRoi(segment);
//		
//		manager.runCommand("Save", Extract_DNA_FibersTest.testpath+"segments_roi.zip");

		lexp = new Line(355, 470, 333, 375);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(323, 316, 306, 247);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(281, 108, 255, 0);
		assertTrue("Not expected to find <"+lexp+"> in list", !Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(369, 597, 328, 425);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(316, 372, 247, 70);
		assertTrue("Not expected to find <"+lexp+"> in list", !Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(275, 356, 267, 265);
		assertTrue("Not expected to find <"+lexp+"> in list", !Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(262, 137, 257, 69);
		assertTrue("Not expected to find <"+lexp+"> in list", !Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(275, 190, 308, 223);
		assertTrue("Not expected to find <"+lexp+"> in list", !Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(520, 401, 548, 429);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(587, 618, 578, 566);
		assertTrue("Not expected to find <"+lexp+"> in list", !Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
		
		lexp = new Line(564, 498, 481, 115);
		assertTrue("Expected to find <"+lexp+"> in list", Extract_DNA_FibersTest.containsLineRoi(lexp, segments));
	}
}
