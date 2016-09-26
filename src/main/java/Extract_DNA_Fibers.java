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

import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Vector;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.PointRoi;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

/**
 * Plugin for DNA fibers extraction and unfolding.
 *
 * After DNA fibers detection (manually or automatically), the fibers are
 * located by their centerline (represented by a line ROI). They can be
 * unfolded (to make them straight) with this plugin.
 *
 * @author Julien Pontabry
 */
public class Extract_DNA_Fibers implements PlugInFilter {
	/** The input image */
	protected ImagePlus image = null;

	/**
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus imp) {
		// Get inputs
		this.image = imp;

		// Finish the setup
		return DOES_8G | DOES_16 | DOES_32 | NO_CHANGES;
	}

	/**
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		if (this.showAndCheckDialog()) {
			// TODO processing
		}
	}

	/**
	 * Show the dialog box for input parameters.
	 * @return True if the dialog box has been filled and accepted, false otherwise.
	 */
	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("DNA Fibers - extract and unfold");

		// TODO make input parameters dialog

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		// TODO get back input parameters

		return true;
	}

	private boolean showAndCheckDialog() {
		// Call dialog
		boolean notCanceled = this.showDialog();

		// Check parameters
//		while(notCanceled && !this.checkWidth(this.radius)) {
//			IJ.showMessage("Width must be strictly positive!");
//			notCanceled = this.showDialog();
//		}
		// TODO check parameters

		return notCanceled;
	}

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads an
	 * image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = Extract_DNA_Fibers.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// open the Clown sample
		ImagePlus image = IJ.openImage("http://imagej.net/images/blobs.gif");
		image.show();

		/*// run the plugin
		IJ.runPlugIn(clazz.getName(), "");*/
	}
}
