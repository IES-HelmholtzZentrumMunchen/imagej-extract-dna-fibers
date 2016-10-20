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

/**
 * Defines the Gaussian kernel.
 * @author julien.pontabry
 */
public class GaussianKernel extends Kernel {
	/** Normalization constant of the Gaussian kernel. */
	public static final double normConst = 1.0/Math.sqrt(2*Math.PI);
	
	/**
	 * @see kernel.Kernel#getNormConst()
	 */
	@Override
	public double getNormConst() {
		return GaussianKernel.normConst;
	}

	/**
	 * @see kernel.Kernel#evaluate(double)
	 */
	@Override
	public double evaluate(double u) {
		return this.evaluateSquared(u * u);
	}
	
	/**
	 * @see kernel.Kernel#evaluateSquared(double)
	 */
	@Override
	public double evaluateSquared(double u2) {
		return Math.exp(-0.5 * u2);
	}

	/**
	 * @see kernel.Kernel#derivative(double)
	 */
	@Override
	public double derivative(double u) {
		return -u * this.evaluate(u);
	}

}
