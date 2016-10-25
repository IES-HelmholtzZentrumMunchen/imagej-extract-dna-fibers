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

import org.junit.Before;
import org.junit.Test;

import coordinates.HoughPoint;
import kernel.GaussianKernel;
import kernel.MeanShift;


/**
 * Test case for MeanShift class.
 * @author julien.pontabry
 */
public class MeanShiftTest {
	/** Instance of the class under test. */
	private MeanShift process = null;
	
	/** Instance of dataset. */
	private List<HoughPoint> data = null;
	
	/** Instance of expected modes list. */
	private List<HoughPoint> modes = null;
	
	/** Instance of expected labels. */
	private List<Integer> labels = null;
	
	/** Path to test resources. */
	private String testPath = "src/test/resources/";
	
	/** Filename of expected modes. */
	private String modeFile = "modes.csv";
	
	/** Filename of testing dataset. */
	private String dataFile = "data.csv";
	
	/** Filename of expected labels. */
	private String labelsFile = "labels.csv";
	
	/** CSV separator. */
	private String csvSeparator = ",";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Build a standard mean-shift
		this.process = new MeanShift(new GaussianKernel(), new HoughPoint(0.6, 0.6));
		
		// Read data files and fill the data point list
		this.data = CsvManager.readHoughPoints(this.testPath+this.dataFile, this.csvSeparator);
		
		// Read modes files and fill the modes point list
		this.modes = CsvManager.readHoughPoints(this.testPath+this.modeFile, this.csvSeparator);
		
		// Read labels files and fill the labels point list
		this.labels = CsvManager.readIntegers(this.testPath+this.labelsFile, this.csvSeparator);
	}

	/**
	 * Test method for {@link kernel.MeanShift#runWith(java.util.List)}.
	 */
	@Test
	public void testRunWith() {
		// Run process
		this.process.runWith(this.data);
		
		// Check modes
		List<HoughPoint> modes = this.process.getModes();
		assertEquals(this.modes.size(), modes.size());
		
		for (int i = 0; i < modes.size(); i++) {
			HoughPoint expmode = this.modes.get(i);
			HoughPoint mode = modes.get(i);
			assertEquals(expmode.getX(), mode.getX(), 1e-7);
			assertEquals(expmode.getY(), mode.getY(), 1e-7);
		}
		
		// Check labels
		List<Integer> labels = this.process.getLabels();
		assertEquals(this.labels.size(), labels.size());
		
		for (int i = 0; i < labels.size(); i++)
			assertEquals("element " + i+1 + ": ", this.labels.get(i), labels.get(i));
	}
}
