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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class TypeToggleButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8489141352439288484L;
	protected Color colorPassive = new Color(90, 90, 90, 200);
	protected Color colorTextPassive = new Color(180, 180, 180, 200);
	protected Color colorTextActive = new Color(10, 10, 10, 200);
	protected FontMetrics metrics;
	protected int size = 20;

	protected boolean mouseOver = false;
	protected boolean buttonIsEnabled = true;

	protected String type;
	protected String letter;
	protected Color active;

	public TypeToggleButton(String type, String letter, Color active) {
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder());
		setToolTipText("Click to disable " + type);
		this.type = type;
		this.letter = letter;
		this.active = active;
		setFont(getFont().deriveFont(12f));
		metrics = getFontMetrics(getFont());
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setButtonEnabled(!buttonIsEnabled);
			}
		});
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
		Dimension dim = new Dimension(size + 4, size + 4);
		setMinimumSize(dim);
		setMaximumSize(dim);
		setPreferredSize(dim);
	}

	public boolean isEnabled() {
		return buttonIsEnabled;
	}

	public void setButtonEnabled(boolean enabled) {
		buttonIsEnabled = enabled;
		if (buttonIsEnabled == true) {
			setToolTipText("Click to disable " + type);
		} else {
			setToolTipText("Click to enable " + type);
		}
		repaint();
	}

	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (mouseOver == false) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
		}
		if (buttonIsEnabled == true) {
			g2d.setColor(active);
		} else {
			g2d.setColor(colorPassive);
		}
		g2d.fillRoundRect(((width - size) / 2), ((height - size) / 2), size - 1, size - 1, 8, 8);
		if (buttonIsEnabled == true) {
			g2d.setColor(colorTextActive);
		} else {
			g2d.setColor(colorTextPassive);
		}
		Rectangle2D stringBounds = metrics.getStringBounds(letter, g2d);
		int fontX = (width - (int) stringBounds.getWidth()) / 2;
		int fontY = ((height - (int) stringBounds.getHeight()) / 2) + metrics.getAscent();
		g2d.drawString(letter, fontX, fontY);
		g2d.dispose();
	}

}
