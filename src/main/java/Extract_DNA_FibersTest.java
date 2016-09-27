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
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;



/**
 * @author julien.pontabry
 *
 */
public class Extract_DNA_FibersTest {
	/** Maximal error allowed when testing differences of structures. */
	public static final double max_error = 1e-2;
	
	public String testpath = "src/main/resources/test/";
	
	@Before
	public void setUp() throws Exception {
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
	 * Test method for {@link Extract_DNA_Fibers#extractSkeletons(ij.ImagePlus)}.
	 */
	@Test
	public void testExtractSkeletons() {
		ImagePlus expected = IJ.openImage(testpath + "example_skeletons.zip");
		ImagePlus original = IJ.openImage(testpath + "example_original.zip");
		
		ImagePlus actual = Extract_DNA_Fibers.extractSkeletons(original, 1, 2, 2);
//		IJ.save(actual, testpath + "example_skeletons_actual.zip");

		double error = Extract_DNA_FibersTest.computeMSE(expected, actual);
		if (error > Extract_DNA_FibersTest.max_error)
			fail("Expected <"+ Extract_DNA_FibersTest.max_error +", got "+ error);
	}

}
