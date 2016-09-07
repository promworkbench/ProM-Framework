package org.processmining.framework.util.ui;

/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which are not
 * licensed under the terms of the GPL, given that they satisfy one or more of
 * the following conditions: 1) Explicit license is granted to the ProM and
 * ProMimport programs for usage, linking, and derivative work. 2) Carte blance
 * license is granted to all programs developed at Eindhoven Technical
 * University, The Netherlands, or under the umbrella of STW Technology
 * Foundation, The Netherlands. For further exemptions not covered by the above
 * conditions, please contact the author of this code.
 */

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ExpandButton extends JButton {

	private static final long serialVersionUID = -8427659617273150458L;

	protected Color colorTriangle = new Color(220, 220, 220, 220);

	protected boolean isExpanded = false;
	protected boolean mouseOver = false;

	public ExpandButton() {
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder());
		Dimension size = new Dimension(21, 21);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) { /* ignore */
			}

			public void mouseEntered(MouseEvent arg0) {
				mouseOver = true;
				repaint();
			}

			public void mouseExited(MouseEvent arg0) {
				mouseOver = false;
				repaint();
			}

			public void mousePressed(MouseEvent arg0) { /* ignore */
			}

			public void mouseReleased(MouseEvent arg0) { /* ignore */
			}
		});
	}

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setExpanded(boolean expanded) {
		isExpanded = expanded;
		repaint();
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int y[] = new int[] { 5, 15, 5 };
		int x[] = new int[] { 5, 10, 15 };
		g2d.setColor(colorTriangle);
		if (mouseOver == false) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
		}
		if (isExpanded == true) {
			g2d.fillPolygon(x, y, 3);
		} else {
			g2d.fillPolygon(y, x, 3);
		}
		g2d.dispose();
	}

}
