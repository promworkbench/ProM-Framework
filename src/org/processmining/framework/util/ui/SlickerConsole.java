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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.processmining.framework.plugin.events.Logger.MessageLevel;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class SlickerConsole extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1434476056589473230L;

	protected static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	protected static File logFile;
	protected static File testFile;

	protected Color colorBg = new Color(30, 30, 30);
	protected Color colorFontBg = new Color(20, 20, 20, 140);
	public static Color colorNormal = new Color(250, 250, 250, 180);
	public static Color colorWarning = new Color(240, 200, 40, 200);
	public static Color colorError = new Color(250, 40, 40, 200);
	public static Color colorDebug = new Color(170, 170, 160, 200);
	public static Color colorTest = new Color(20, 250, 20, 200);

	protected ArrayList<String> messages;
	protected ArrayList<MessageLevel> types;
	protected ArrayList<String> timestamps;
	protected int maxSize;

	protected boolean showMessages = true;
	protected boolean showWarnings = true;
	protected boolean showErrors = true;
	protected boolean showDebug = true;
	protected boolean showTest = true;

	protected boolean expanded;
	protected int lineHeight;
	protected int stringHeight;
	protected int leftBorder = 10;

	public SlickerConsole(int bufferSize) {
		setFont((new JLabel()).getFont().deriveFont(12f));
		FontMetrics fm = getFontMetrics(getFont());
		lineHeight = fm.getHeight();
		stringHeight = fm.getLeading() + fm.getAscent();
		messages = new ArrayList<String>();
		types = new ArrayList<MessageLevel>();
		timestamps = new ArrayList<String>();
		maxSize = bufferSize;
		expanded = false;
		int height = lineHeight + 10;
		setMinimumSize(new Dimension(400, height));
		setMaximumSize(new Dimension(6000, height));
		setPreferredSize(new Dimension(2000, height));

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				if (expanded == true) {
					scrollToBottom();
				}
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		int height = lineHeight + 10;
		if (expanded == true) {
			height = (messages.size() * lineHeight) + 15;
		}
		setMinimumSize(new Dimension(600, height));
		setMaximumSize(new Dimension(6000, height));
		setPreferredSize(new Dimension(4000, height));
		if (expanded == true) {
			setMaximumSize(new Dimension(6000, 2000));
			scrollToBottom();
		}
		revalidate();
		repaint();
	}

	public void receiveMessage(String text, MessageLevel type) {
		String timestamp = dateFormat.format(new Date(System.currentTimeMillis()));

		if (((type == MessageLevel.NORMAL) && (showMessages == false))
				|| ((type == MessageLevel.WARNING) && (showWarnings == false))
				|| ((type == MessageLevel.ERROR) && (showErrors == false))
				|| ((type == MessageLevel.DEBUG) && (showDebug == false))
				|| ((type == MessageLevel.TEST) && (showTest == false))) {
			return; // filtered
		}
		// add to message log
		messages.add(text);
		types.add(type);
		timestamps.add(timestamp);
		if (messages.size() <= maxSize) {
			if (expanded == true) {
				int height = messages.size() * lineHeight + 15;
				setMinimumSize(new Dimension(600, height));
				setMaximumSize(new Dimension(6000, 2000));
				setPreferredSize(new Dimension(4000, height));
				scrollToBottom();
			}
		} else {
			messages.remove(0);
			types.remove(0);
			timestamps.remove(0);
		}
		revalidate();
		repaint();
	}

	public void scrollToBottom() {
		scrollRectToVisible(new Rectangle(-23, getHeight() - 2, 25, 25));
		revalidate();
		repaint();
	}

	protected void paintComponent(Graphics g) {
		Rectangle2D clip = g.getClipBounds();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(getFont());
		if (messages.size() == 0) {
			return; // nothing to draw
		}
		if (expanded == true) {
			int startY = (int) clip.getY() - ((int) clip.getY() % lineHeight);
			int index = (startY / lineHeight);
			int endY = (int) (clip.getY() + clip.getHeight());
			while (startY <= endY) {
				drawLine(index, leftBorder, startY + stringHeight, g2d, false);
				startY += lineHeight;
				index++;
			}
		} else {
			int fontY = ((getHeight() - lineHeight) / 2) + stringHeight;
			drawLine(messages.size() - 1, leftBorder, fontY, g2d, true);
		}
	}

	protected void drawLine(int index, int x, int y, Graphics2D g2d, boolean shadow) {
		if ((index < 0) || (index >= messages.size())) {
			return;
		}
		// set appropriate font color
		String prefix = timestamps.get(index);
		Color fontColor = Color.WHITE;
		if (types.get(index) == MessageLevel.NORMAL) {
			fontColor = colorNormal;
			prefix += " [M] ";
		} else if (types.get(index) == MessageLevel.WARNING) {
			fontColor = colorWarning;
			prefix += " [W] ";
		} else if (types.get(index) == MessageLevel.ERROR) {
			fontColor = colorError;
			prefix += " [E] ";
		} else if (types.get(index) == MessageLevel.DEBUG) {
			fontColor = colorDebug;
			prefix += " [D] ";
		} else if (types.get(index) == MessageLevel.TEST) {
			fontColor = colorTest;
			prefix += " [T] ";
		}
		if (shadow == true) {
			g2d.setColor(colorFontBg);
			g2d.drawString(prefix + messages.get(index), x + 1, y + 1);
		}
		g2d.setColor(fontColor);
		g2d.drawString(prefix + messages.get(index), x, y);
	}

	/**
	 * @return the showMessages
	 */
	public boolean isShowMessages() {
		return showMessages;
	}

	/**
	 * @param showMessages
	 *            the showMessages to set
	 */
	public void setShowMessages(boolean showMessages) {
		this.showMessages = showMessages;
	}

	/**
	 * @return the showWarnings
	 */
	public boolean isShowWarnings() {
		return showWarnings;
	}

	/**
	 * @param showWarnings
	 *            the showWarnings to set
	 */
	public void setShowWarnings(boolean showWarnings) {
		this.showWarnings = showWarnings;
	}

	/**
	 * @return the showErrors
	 */
	public boolean isShowErrors() {
		return showErrors;
	}

	/**
	 * @param showErrors
	 *            the showErrors to set
	 */
	public void setShowErrors(boolean showErrors) {
		this.showErrors = showErrors;
	}

	/**
	 * @return the showDebug
	 */
	public boolean isShowDebug() {
		return showDebug;
	}

	/**
	 * @param showDebug
	 *            the showDebug to set
	 */
	public void setShowDebug(boolean showDebug) {
		this.showDebug = showDebug;
	}

	/**
	 * @return the showTest
	 */
	public boolean isShowTest() {
		return showTest;
	}

	/**
	 * @param showTest
	 *            the showTest to set
	 */
	public void setShowTest(boolean showTest) {
		this.showTest = showTest;
	}

}
