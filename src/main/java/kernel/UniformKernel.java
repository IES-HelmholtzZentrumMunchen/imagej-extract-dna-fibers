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
 * Defines the uniform kernel.
 * @author julien.pontabry
 */
public class UniformKernel extends Kernel {
	/** Normalization constant of the uniform kernel. */
	public static final double normConst = 0.5;
	
	/**
	 * @see kernel.Kernel#getNormConst()
	 */
	@Override
	public double getNormConst() {
		return UniformKernel.normConst;
	}

	/**
	 * @see kernel.Kernel#evaluate(java.lang.Double)
	 */
	@Override
	public double evaluate(double u) {
		if (Double.compare(Math.abs(u), 1.0) <= 0)
			return 1.0;
		else
			return 0.0;
	}

	/**
	 * @see kernel.Kernel#derivative(java.lang.Double)
	 */
	@Override
	public double derivative(double u) {
		return 0.0;
	}

}
