package org.processmining.framework.util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.processmining.framework.plugin.events.Logger.MessageLevel;

import com.fluxicon.slickerbox.components.GradientPanel;
import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

public class SlickerMessageBar extends GradientPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3098268894613400200L;
	protected static Color colorTop = new Color(90, 90, 90);
	protected static Color colorBottom = new Color(40, 40, 40);
	protected static Color colorBorder = new Color(60, 60, 60);

	protected SlickerConsole console;
	protected RoundedPanel largeConsolePanel;
	protected JPanel expandPanel;
	protected ExpandButton expand;
	protected JScrollPane largeConsoleScrollPane;
	protected JPanel filterPanel;
	private final TypeToggleButton messageButton;
	private final TypeToggleButton warningButton;
	private final TypeToggleButton errorButton;
	private final TypeToggleButton debugButton;
	private final TypeToggleButton testButton;

	/**
	 * @param colorTop
	 * @param colorBottom
	 */
	public SlickerMessageBar() {
		super(colorTop, colorBottom);
		//this.setBorder(BorderFactory.createLineBorder(colorBorder));
		console = new SlickerConsole(300);
		largeConsolePanel = new RoundedPanel(10, 6, 0);
		largeConsolePanel.setBackground(new Color(20, 20, 20, 160));
		largeConsolePanel.setLayout(new BorderLayout());
		largeConsoleScrollPane = new JScrollPane();
		largeConsoleScrollPane.setOpaque(false);
		largeConsoleScrollPane.getViewport().setOpaque(false);
		largeConsoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		largeConsoleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		JScrollBar vBar = largeConsoleScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0), new Color(140, 140, 140), new Color(110, 110,
				110), 4, 12));
		largeConsoleScrollPane.setBorder(BorderFactory.createEmptyBorder());
		largeConsolePanel.add(largeConsoleScrollPane, BorderLayout.CENTER);
		expandPanel = new JPanel();
		expandPanel.setLayout(new BoxLayout(expandPanel, BoxLayout.Y_AXIS));
		expandPanel.setMinimumSize(new Dimension(30, 23));
		expandPanel.setMaximumSize(new Dimension(30, 1000));
		expandPanel.setPreferredSize(new Dimension(30, 500));
		expandPanel.setOpaque(false);
		expand = new ExpandButton();
		expand.setExpanded(false);
		expand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				toggleExpanded();
			}
		});
		expandPanel.add(Box.createVerticalGlue());
		setLayout(new BorderLayout());
		this.add(expandPanel, BorderLayout.WEST);
		this.add(console, BorderLayout.CENTER);
		setMinimumSize(new Dimension(600, 23));
		setMaximumSize(new Dimension(3000, 23));
		setPreferredSize(new Dimension(2000, 23));
		filterPanel = new JPanel();
		filterPanel.setOpaque(false);
		filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
		messageButton = new TypeToggleButton("messages", "M", SlickerConsole.colorNormal);
		messageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				console.setShowMessages(!console.isShowMessages());
			}
		});
		warningButton = new TypeToggleButton("warnings", "W", SlickerConsole.colorWarning);
		warningButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				console.setShowWarnings(!console.isShowWarnings());
			}
		});
		errorButton = new TypeToggleButton("errors", "E", SlickerConsole.colorError);
		errorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				console.setShowErrors(!console.isShowErrors());
			}
		});
		debugButton = new TypeToggleButton("debug messages", "D", SlickerConsole.colorDebug);
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				console.setShowDebug(!console.isShowDebug());
			}
		});
		testButton = new TypeToggleButton("test messages", "T", SlickerConsole.colorTest);
		testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				console.setShowTest(!console.isShowTest());
			}
		});
		filterPanel.add(messageButton);
		filterPanel.add(warningButton);
		filterPanel.add(errorButton);
		filterPanel.add(debugButton);
		filterPanel.add(testButton);
		filterPanel.add(Box.createVerticalGlue());
		expand.doClick();
	}

	public void toggleExpanded() {
		expand.setExpanded(!expand.isExpanded());
		toggleDimension();
	}

	public void receiveMessage(String text, MessageLevel type) {
		console.receiveMessage(text, type);
	}

	protected void toggleDimension() {
		removeAll();
		this.add(expandPanel, BorderLayout.WEST);
		if (expand.isExpanded() == true) {
			setMinimumSize(new Dimension(600, 200));
			setMaximumSize(new Dimension(3000, 200));
			setPreferredSize(new Dimension(1000, 200));
			console.setExpanded(true);
			largeConsolePanel = new RoundedPanel(10, 6, 0);
			largeConsolePanel.setBackground(new Color(20, 20, 20, 160));
			largeConsolePanel.setLayout(new BorderLayout());
			largeConsoleScrollPane = new JScrollPane();
			largeConsoleScrollPane.setOpaque(false);
			largeConsoleScrollPane.getViewport().setOpaque(false);
			largeConsoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			largeConsoleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			JScrollBar vBar = largeConsoleScrollPane.getVerticalScrollBar();
			vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0), new Color(140, 140, 140), new Color(110,
					110, 110), 4, 12));
			vBar.setOpaque(false);
			largeConsoleScrollPane.setBorder(BorderFactory.createEmptyBorder());
			largeConsolePanel.add(largeConsoleScrollPane, BorderLayout.CENTER);
			expandPanel.removeAll();
			expandPanel.add(expand);
			expandPanel.add(filterPanel);
			expandPanel.add(Box.createVerticalGlue());
			this.add(largeConsolePanel, BorderLayout.CENTER);
			revalidate();
			largeConsoleScrollPane.getViewport().setView(console);
			revalidate();
			console.scrollToBottom();
			repaint();
		} else {
			setMinimumSize(new Dimension(600, 23));
			setMaximumSize(new Dimension(3000, 23));
			setPreferredSize(new Dimension(2000, 23));
			console.setExpanded(false);
			this.add(console, BorderLayout.CENTER);
			expandPanel.removeAll();
			expandPanel.add(expand);
			expandPanel.add(Box.createVerticalGlue());
			revalidate();
			repaint();
		}
		revalidate();
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width = getWidth();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(new Color(0, 0, 0, 180));
		g2d.drawLine(0, 0, width - 1, 0);
		g2d.setColor(new Color(0, 0, 0, 90));
		g2d.drawLine(0, 1, width - 1, 1);
	}

	/**
	 * @return the showMessages
	 */
	public boolean isShowMessages() {
		return console.isShowMessages();
	}

	/**
	 * @param showMessages
	 *            the showMessages to set
	 */
	public void setShowMessages(boolean showMessages) {
		if (isShowMessages() != showMessages) {
			messageButton.doClick();
		}
	}

	/**
	 * @return the showWarnings
	 */
	public boolean isShowWarnings() {
		return console.isShowWarnings();
	}

	/**
	 * @param showWarnings
	 *            the showWarnings to set
	 */
	public void setShowWarnings(boolean showWarnings) {
		if (isShowWarnings() != showWarnings) {
			warningButton.doClick();
		}
	}

	/**
	 * @return the showErrors
	 */
	public boolean isShowErrors() {
		return console.isShowErrors();
	}

	/**
	 * @param showErrors
	 *            the showErrors to set
	 */
	public void setShowErrors(boolean showErrors) {
		if (isShowErrors() != showErrors) {
			errorButton.doClick();
		}
	}

	/**
	 * @return the showDebug
	 */
	public boolean isShowDebug() {
		return console.isShowDebug();
	}

	/**
	 * @param showDebug
	 *            the showDebug to set
	 */
	public void setShowDebug(boolean showDebug) {
		if (isShowDebug() != showDebug) {
			debugButton.doClick();
		}
	}

	/**
	 * @return the showTest
	 */
	public boolean isShowTest() {
		return console.isShowTest();
	}

	/**
	 * @param showTest
	 *            the showTest to set
	 */
	public void setShowTest(boolean showTest) {
		if (isShowTest() != showTest) {
			testButton.doClick();
		}
	}

}
