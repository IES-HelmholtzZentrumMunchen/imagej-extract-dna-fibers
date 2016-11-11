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
package main;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import coordinates.HoughPoint;
import ij.IJ;


/**
 * Defines the mean-shift algorithm on 2D points.
 * The <code>Point2D</code> class is not coded to make it
 * generic enough, so here we are using HoughPoint class instead.
 * @author julien.pontabry
 */
public class MeanShift {
	/** Max domain value of the Gaussian kernel (approximation). */
	public static final double maxDomain = 5.0;
	
	/** Bandwidths for each component. */
	protected HoughPoint h;
	
	/** Output labels of input data points.*/
	protected List<Integer> labels;
	
	/** Output modes of kernel density estimate from intput data points. */
	protected List<HoughPoint> modes;
	
	/** Numerical tolerance for convergence. */
	protected final double tolerance = 1e-10;
	
	/** Numerical precision for merging close modes. */
	protected final double mergeEpsilon = 1e-2;
	
	/** Maximum number of iterations for mean shift convergence. */
	protected final int max_iterations = 1000;
	
	/**
	 * Default constructor.
	 * Isotropic standard (bandwidth equal to 1 in both 
	 * directions) Gaussian kernel is used as default.
	 */
	public MeanShift() {
		this(new HoughPoint(1,1));
	}
	
	/**
	 * Full constructor.
	 * @param k The kernel to use for density estimate.
	 * @param h The bandwidths for both components as a <code>HoughPoint</code>
	 */
	public MeanShift(HoughPoint h) {
		this.h = h;
		
		this.labels = null;
		this.modes = null;
		this.labels = null;
	}
	
	/**
	 * Set the bandwidths used within kernel for density estimate.
	 * @param h Any bandwidth vector as HoughPoint.
	 */
	public void setBandwidth(HoughPoint h) {
		this.h = h;
	}
	
	/**
	 * Get the bandwidths used within kernel for density estimate.
	 * @return Currently used bandwidths.
	 */
	public HoughPoint getBandwidth() {
		return this.h;
	}
	
	/**
	 * Inner class to encapsulate a data point and its position in the list.
	 * This is used at the reduce step in parallelization.
	 * @author julien.pontabry
	 */
	protected class DataPoint {
		public HoughPoint point = null;
		public int position = 0;
		
		public DataPoint(HoughPoint point, int position) {
			this.point = point;
			this.position = position;
		}
	}
	
	/**
	 * Run the mean-shift procedure.
	 * @param data Input data points.
	 */
	public void runWith(List<HoughPoint> data) {
		// Initialize output
		Integer[] labels = new Integer[data.size()];
		this.modes = new Vector<HoughPoint>();
		
		// Setup mean-shift for data points to be executed in parallel
		List<Callable<Vector<DataPoint>>> tasks = new Vector<>();
		
		// Cut the process in C+1 pieces (where C is the number of cores)
		// to accelerate the process (avoid many thread creations/destructions
		// and use maximum CPU for computing only.
		int groupSize = data.size()/(Runtime.getRuntime().availableProcessors()+1);
		System.out.println("\tgroupSize = "+groupSize);
		for (int i = 0; i < data.size(); i+=groupSize) {
			final int startIndex = i;

			tasks.add(() -> {
				Vector<DataPoint> dataPoints = new Vector<>();
				
				for (int j = startIndex; j < startIndex+groupSize && j < data.size(); j++) {
					// Initialization of the mean shift
					HoughPoint p = new HoughPoint(data.get(j));

					double error;
					int iteration = 0;
						
					// Push iteratively point to closest mode
					do {
						double sumOfWeights = 0.0;
						double x = 0.0, y = 0.0;

						for (HoughPoint q : data) {
							// Compute Gaussian kernel distance only for close points
							// Use partial distance to speed up the process
							double  tmpx = (p.getX() - q.getX()) / this.h.getX();
							double tmpx2 = tmpx*tmpx;
							
							if (Double.compare(tmpx2, MeanShift.maxDomain) < 0) { // x < 5
								double tmpy = (p.getY() - q.getY()) / this.h.getY();
								double tmpu2 = tmpx2 + tmpy*tmpy;
								
								if (Double.compare(tmpu2, MeanShift.maxDomain) < 0) { 
									double weight = Math.exp(-0.5 * tmpu2);
									
									sumOfWeights += weight;
									x += q.getX() * weight;
									y += q.getY() * weight;
								}
							}
						} // for each point

						HoughPoint mean = new HoughPoint(x/sumOfWeights, y/sumOfWeights);
						double x_diff = mean.getX()-p.getX(), y_diff = mean.getY()-p.getY();
						error = x_diff*x_diff + y_diff*y_diff;
						p.setLocation(mean);

						iteration++;
					} while (Double.compare(error, this.tolerance) > 0 && iteration < max_iterations);
					
					// The final mode is the updated point
					dataPoints.add(new DataPoint(p, j));
				}
				
				return dataPoints;
			});
		}
		
		// Run threads in parallel and reduce results
		ExecutorService executor = Executors.newWorkStealingPool();
		
	    try {
	        executor.invokeAll(tasks)
	        	.stream()
	        	.map(future -> {
	        		try {
	        			return future.get();
	        		}
	        		catch (Exception e) {
	        			throw new IllegalStateException(e);
	        		}
	        	})
	        	.forEach(result -> {
	        		for (int i = 0; i < result.size(); i++)
	        			labels[result.get(i).position] = this.mergeOrAddMode(result.get(i).point);
	        	});
	        
	        this.labels = new Vector<Integer>(Arrays.asList(labels));
	    }
	    catch (Exception e) {
	    	this.modes = new Vector<HoughPoint>();
	    	this.labels = new Vector<Integer>();
	    	IJ.error("Exception", "An exception occured!\n" + e.getMessage());
	    }
	}
	
	/**
	 * Add a candidate mode or merge it with a close existing mode.
	 * @param p The candidate mode.
	 * @return The position of the added/merged mode in the list.
	 */
	protected Integer mergeOrAddMode(HoughPoint p) {
		boolean found = false;
		int i = 0;

		
		for (i = 0; i < this.modes.size(); i++) {
			HoughPoint mode = this.modes.get(i);
			
			double xDiff = mode.getX() - p.getX();
			double yDiff = mode.getY() - p.getY();

			
			if (Double.compare(Math.sqrt(xDiff*xDiff + yDiff*yDiff), this.mergeEpsilon) < 0) {
				found = true;
				break;
			}
		}
		
		if (!found)
			this.modes.add(p);
		
		return i;
	}
	
	/**
	 * Get the labels of data points.
	 * The algorithm must be ran on some data points before (output is null otherwise).
	 * @return List of integers (labels).
	 */
	public List<Integer> getLabels() {
		return this.labels;
	}
	
	/**
	 * Get the output modes of the kernel density estimate of data points.
	 * The algorithm must be ran on some data points before (output is null otherwise).
	 * @return List of modes as points.
	 */
	public List<HoughPoint> getModes() {
		return this.modes;
	}
}
