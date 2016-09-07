package org.processmining.framework.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public interface HTMLToString {

	public String toHTMLString(boolean includeHTMLTags);

	public static class HTMLCellRenderer extends JLabel implements ListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -808355468668630456L;

		public HTMLCellRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			if (value instanceof HTMLToString) {
				setText(Cast.<HTMLToString>cast(value).toHTMLString(true));
			} else {
				setText(value.toString());
			}

			setBackground(isSelected ? Color.RED : Color.WHITE);
			setForeground(isSelected ? Color.WHITE : Color.BLACK);
			return this;

		}
	}

}
