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
package kernel;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import coordinates.HoughPoint;
import coordinates.ImagePoint;
import ij.IJ;


/**
 * Defines the mean-shift algorithm on 2D points.
 * The <code>Point2D</code> class is not coded to make it
 * generic enough, so here we are using HoughPoint class instead.
 * @author julien.pontabry
 */
public class MeanShift {
	/** Kernel used for density estimate. */
	protected Kernel k;
	
	/** Bandwidths for each component. */
	protected HoughPoint h;
	
	/** Output labels of input data points.*/
	protected List<Integer> labels;
	
	/** Output modes of kernel density estimate from intput data points. */
	protected List<HoughPoint> modes;
	
	/** Numerical tolerance for convergence. */
	protected final double tolerance = 1e-20;
	
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
		this(new GaussianKernel());
	}
	
	/**
	 * Constructor.
	 * The kernel is by default isotropic with bandwidth
	 * equal to 1 in both directions.
	 * @param k The kernel to use for density estimate.
	 */
	public MeanShift(Kernel k) {
		this(k, new HoughPoint(1, 1));
	}
	
	/**
	 * Full constructor.
	 * @param k The kernel to use for density estimate.
	 * @param h The bandwidths for both components as a <code>HoughPoint</code>
	 */
	public MeanShift(Kernel k, HoughPoint h) {
		this.k = k;
		this.h = h;
		
		this.labels = null;
		this.modes = null;
		this.labels = null;
	}
	
	/**
	 * Set the kernel to use for density estimate.
	 * @param k Any kernel.
	 */
	public void setKernel(Kernel k) {
		this.k = k;
	}
	
	/**
	 * Get the kernel used for density estimate.
	 * @return Currently used kernel.
	 */
	public Kernel getKernel() {
		return this.k;
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
		this.labels = new Vector<Integer>(data.size());
		this.modes = new Vector<HoughPoint>();
		
		// Setup mean-shift for data points to be executed in parallel
		List<Callable<DataPoint>> tasks = new Vector<>();
				
		IntStream.range(0, data.size()).forEach(i -> {
			tasks.add(() -> {
					// Initialization of the mean shift
					HoughPoint p = data.get(i);
					
					double error;
					int iteration = 0;
						
					// Push iteratively point to closest mode
					do {
						double sumOfWeights = 0.0;
						double x = 0.0, y = 0.0;

						for (HoughPoint q : data) {
							// TODO do not process points outside of kernel domain
//							double weight = this.kernelDerivative(p, q);
							double weight = this.kernel(p, q);
							sumOfWeights += weight;
							x += q.getX() * weight;
							y += q.getY() * weight;
						}

						HoughPoint mean = new HoughPoint(x/sumOfWeights, y/sumOfWeights);
						double x_diff = mean.getX()-p.getX(), y_diff = mean.getY()-p.getY();
						error = x_diff*x_diff + y_diff*y_diff;
						p.setLocation(mean);
						iteration++;
					} while (Double.compare(error, this.tolerance) > 0 && iteration < max_iterations);
					
					// The final mode is the updated point
					return new DataPoint(p, i);
			});
		});
		
		// Run threads in parallel and reduce results
		ExecutorService executor = Executors.newWorkStealingPool(1);
		
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
	        		this.mergeOrAddMode(result.point);
	        		// FIXME why when setting labels, there is only one loop running?
//	        		this.labels.set(result.position, this.mergeOrAddMode(result.point));
	        	});
	    }
	    catch (Exception e) {
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
			
			if (Double.compare(xDiff*xDiff + yDiff*yDiff, this.mergeEpsilon) <= 0) {
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
	
	/**
	 * Estimate kernel distance for 2D vectors with bandwidths.
	 * @param p First point of the kernel distance.
	 * @param q Second point of the kernel distance.
	 * @return Kernel distance between the two specified points <code>p</code> and <code>q</code>.
	 */
	protected double kernel(HoughPoint p, HoughPoint q) {
		double x = (p.getX() - q.getX()) / this.h.getX();
		double y = (p.getY() - q.getY()) / this.h.getY();
		
		return this.k.evaluate(Math.sqrt(x*x + y*y));
	}
	
	/**
	 * Estimate kernel derivative distance for 2D vectors with bandwidths.
	 * @param p First point of the kernel distance.
	 * @param q Second point of the kernel distance.
	 * @return Kernel distance between the two specified points <code>p</code> and <code>q</code>.
	 */
	protected double kernelDerivative(HoughPoint p, HoughPoint q) {
		double x = (p.getX() - q.getX()) / this.h.getX();
		double y = (p.getY() - q.getY()) / this.h.getY();
		
		return this.k.derivative(Math.sqrt(x*x + y*y));
	}
}
