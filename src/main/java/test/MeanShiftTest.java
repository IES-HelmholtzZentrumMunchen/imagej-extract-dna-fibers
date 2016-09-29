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
	
	/** Instance of data point list to generate. */
	private List<HoughPoint> data = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Build a standard mean-shift
		this.process = new MeanShift(new GaussianKernel(), new HoughPoint(1, 1));
		
		// Read data files and fill the data point list
		this.data = new Vector<HoughPoint>();
		
		String testPath = "src/main/resources/test/";
		String modeFile = "modes.csv";
		String dataFile = "data.csv";
		String csvSeparator = ",";
		
		try {
			String line = "";
			
			BufferedReader buffer = new BufferedReader(new FileReader(testPath + dataFile));
			
			while((line = buffer.readLine()) != null) {
				String[] coordinates = line.split(csvSeparator);
				this.data.add(new HoughPoint(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link kernel.MeanShift#runWith(java.util.List)}.
	 */
	@Test
	public void testRunWith() {
		System.out.println(this.data.size());
		for (HoughPoint p : this.data) {
			System.out.println(p);
		}
		
		fail("Not yet implemented");
	}
}
