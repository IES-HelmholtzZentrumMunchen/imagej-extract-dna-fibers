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
package gui;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author julien.pontabry
 *
 */
public class SeparatorPanel extends Panel {
    /** Serial number. */
	private static final long serialVersionUID = -2843210795677051028L;

	/**  Title of the panel. */
    private String m_title;

    /** Insets of the panel. */
    private Insets insets = new Insets( 10, 100, 10, 100 );
    
	/**
	 * Default constructor.
	 */
	public SeparatorPanel() {
		this("");
	}

	/**
     * Constructor.
     * @param title Title of the panel
     */
    public SeparatorPanel(String title) {
        m_title = title;
    }
    
    /**
     * Insets getter.
     * @return The current insets
     */
    public Insets getInsets() {
        return insets;
    }

    /**
     * Paint the panel on the provided graphics.
     * @param g Graphics to paint on.
     */
    public void paint(Graphics g) {
        super.paint(g);

        int width = g.getFontMetrics().stringWidth(m_title);

        g.setColor(this.getForeground());
        g.drawLine(5, 20, 15, 20);
        g.drawLine(width+25, 20, this.getWidth() - 5, 20);
        g.drawString(m_title, 20, 23);
    }

    /**
     * Test main function.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        Frame f = new Frame("SeparatorPanel Tester");

        SeparatorPanel p = new SeparatorPanel("Title of Panel");
        p.add(new Label("Label 1"));
        p.add(new Label("Label 2"));
        p.add(new Label("Label 3"));
        f.add(p);

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        } );
        f.setBounds(300, 300, 300, 300);
        f.setVisible(true);
    }
}
