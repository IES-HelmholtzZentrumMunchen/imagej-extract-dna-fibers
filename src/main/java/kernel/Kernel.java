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
 * Base class for mathematical kernel.
 * @author julien.pontabry
 */
public abstract class Kernel {
	/**
	 * Get the normalization constant of the kernel.
	 * @return Normalization constant as double precision number.
	 */
	public abstract double getNormConst();
	
	/**
	 * Evaluate the kernel at <code>u</code>.
	 * @param u Point where to evaluate the kernel.
	 * @return Kernel value at <code>u</code>.
	 */
	public abstract double evaluate(double u);
	
	/**
	 * Evaluate the kernel at <code>u^2</code>.
	 * @param u Point where to evaluate the kernel.
	 * @return Kernel value at <code>u^2</code>.
	 */
	public abstract double evaluateSquared(double u2);
	
	/**
	 * Evaluate the kernel derivative at <code>u</code>.
	 * @param u Point where to evaluate the kernel derivative.
	 * @return Kernel derivative value at <code>u</code>.
	 */
	public abstract double derivative(double u);
}
