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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

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
	private String testPath = "src/main/resources/test/";
	
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
		// Declare a buffer for file reading
		BufferedReader buffer;
		
		// Build a standard mean-shift
		this.process = new MeanShift(new GaussianKernel(), new HoughPoint(0.6, 0.6));
		
		// Read data files and fill the data point list
		this.data = new Vector<HoughPoint>();
		
		buffer = null;
		try {
			String line = "";
			
			buffer = new BufferedReader(new FileReader(this.testPath + this.dataFile));
			
			while((line = buffer.readLine()) != null) {
				String[] coordinates = line.split(this.csvSeparator);
				this.data.add(new HoughPoint(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (buffer != null) {
				buffer.close();
			}
		}
		
		// Read modes files and fill the modes point list
		this.modes = new Vector<HoughPoint>();
		
		buffer = null;
		try {
			String line = "";
					
			buffer = new BufferedReader(new FileReader(this.testPath + this.modeFile));
					
			while((line = buffer.readLine()) != null) {
				String[] coordinates = line.split(this.csvSeparator);
				this.modes.add(new HoughPoint(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (buffer != null) {
				buffer.close();
			}
		}
		
		// Read labels files and fill the labels point list
		this.labels = new Vector<Integer>();

		buffer = null;
		try {
			String line = "";

			buffer = new BufferedReader(new FileReader(this.testPath + this.labelsFile));

			while((line = buffer.readLine()) != null) {
				String[] coordinate = line.split(this.csvSeparator);
				this.labels.add(Integer.parseInt(coordinate[0]));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (buffer != null) {
				buffer.close();
			}
		}
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
		
//		// Check labels
//		List<Integer> labels = this.process.getLabels();
//		assertEquals(this.labels.size(), labels.size());
//		
//		for (int i = 0; i < labels.size(); i++)
//			assertEquals(this.labels.get(i), labels.get(i));
	}
}
