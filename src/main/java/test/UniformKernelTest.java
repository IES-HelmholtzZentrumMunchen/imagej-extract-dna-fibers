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

import org.junit.Before;
import org.junit.Test;

import kernel.UniformKernel;


/**
 * Test case for uniform kernel class.
 * @author julien.pontabry
 */
public class UniformKernelTest {
	/** Instance of uniform kernel. */
	private UniformKernel kernel = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.kernel = new UniformKernel();
	}

	/**
	 * Test method for {@link kernel.UniformKernel#getNormConst()}.
	 */
	@Test
	public void testGetNormConst() {
		assertEquals(0.5, this.kernel.getNormConst(), 1e-7);
	}

	/**
	 * Test method for {@link kernel.UniformKernel#evaluate(double)}.
	 */
	@Test
	public void testEvaluate() {
		assertEquals(0.5, this.kernel.getNormConst()*this.kernel.evaluate(0), 1e-7);
		assertEquals(0.5, this.kernel.getNormConst()*this.kernel.evaluate(0.5), 1e-7);
		assertEquals(0.5, this.kernel.getNormConst()*this.kernel.evaluate(-0.5), 1e-7);
		assertEquals(0, this.kernel.getNormConst()*this.kernel.evaluate(2), 1e-7);
		assertEquals(0, this.kernel.getNormConst()*this.kernel.evaluate(-1.1), 1e-7);
		assertEquals(0, this.kernel.getNormConst()*this.kernel.evaluate(1.1), 1e-7);
	}

	/**
	 * Test method for {@link kernel.UniformKernel#derivative(double)}.
	 */
	@Test
	public void testDerivative() {
		assertEquals(0, this.kernel.getNormConst()*this.kernel.derivative(0), 1e-7);
		assertEquals(0, this.kernel.getNormConst()*this.kernel.derivative(0.5), 1e-7);
		assertEquals(0, this.kernel.getNormConst()*this.kernel.derivative(-0.5), 1e-7);
		assertEquals(0, this.kernel.getNormConst()*this.kernel.derivative(2), 1e-7);
		assertEquals(0, this.kernel.getNormConst()*this.kernel.derivative(-1.1), 1e-7);
		assertEquals(0, this.kernel.getNormConst()*this.kernel.derivative(1.1), 1e-7);
	}

}
