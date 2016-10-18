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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import coordinates.HoughPoint;

/**
 * Csv management tool.
 * @author julien.pontabry
 */
public class CsvManager {
	/**
	 * Read list of points in Hough space from csv file.
	 * @param path Input path.
	 * @param delimiter Csv delimiter.
	 * @return List of points in Hough space.
	 * @throws Exception
	 */
	public static List<HoughPoint> readHoughPoints(String path, String delimiter) throws Exception {
		List<HoughPoint> points = new Vector<HoughPoint>();

		BufferedReader buffer = null;
		
		try {
			String line = "";
			
			buffer = new BufferedReader(new FileReader(path));
			
			while((line = buffer.readLine()) != null) {
				String[] coordinates = line.split(delimiter);
				points.add(new HoughPoint(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
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
		
		return points;
	}
	
	/**
	 * Read list of integers from csv file.
	 * @param path Input path.
	 * @param delimiter Csv delimiter.
	 * @return List of integers.
	 * @throws Exception
	 */
	public static List<Integer> readIntegers(String path, String delimiter) throws Exception {
		List<Integer> points = new Vector<Integer>();

		BufferedReader buffer = null;
		
		try {
			String line = "";
			
			buffer = new BufferedReader(new FileReader(path));
			
			while((line = buffer.readLine()) != null) {
				String[] coordinates = line.split(delimiter);
				points.add(Integer.parseInt(coordinates[0]));
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
		
		return points;
	}
}
