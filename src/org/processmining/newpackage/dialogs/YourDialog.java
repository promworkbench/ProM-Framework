package org.processmining.newpackage.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.newpackage.models.YourFirstInput;
import org.processmining.newpackage.models.YourSecondInput;
import org.processmining.newpackage.parameters.YourParameters;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class YourDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -60087716353524468L;

	/**
	 * The JPanel that allows the user to set (a subset of) the parameters.
	 */
	public YourDialog(UIPluginContext context, YourFirstInput input1, YourSecondInput input2,
			final YourParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, TableLayoutConstants.FILL, 30, 30 } };
		setLayout(new TableLayout(size));
		Set<String> values = new HashSet<String>();
		values.add("Option 1");
		values.add("Option 2");
		values.add("Option 3");
		values.add("Option 4");

		DefaultListModel<String> listModel = new DefaultListModel<String>();
		for (String value: values) {
			listModel.addElement(value);
		}
		final ProMList<String> list = new ProMList<String>("Select option", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final String defaultValue = "Option 1";
		list.setSelection(defaultValue);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					parameters.setYourString(selected.get(0));
				} else {
					/*
					 * Nothing selected. Revert to selection of default classifier.
					 */
					list.setSelection(defaultValue);
					parameters.setYourString(defaultValue);
				}
			}
		});
		list.setPreferredSize(new Dimension(100, 100));
		add(list, "0, 0");
		
		final NiceSlider integerSilder = SlickerFactory.instance().createNiceIntegerSlider("Select number ", -10,
				10, parameters.getYourInteger(), Orientation.HORIZONTAL);
		integerSilder.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setYourInteger(integerSilder.getSlider().getValue());
			}
		});
		add(integerSilder, "0, 1");

		final JCheckBox checkBox = SlickerFactory.instance().createCheckBox("Select Yes/No", false);
		checkBox.setSelected(parameters.isYourBoolean());
		checkBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setYourBoolean(checkBox.isSelected());
			}

		});
		checkBox.setOpaque(false);
		checkBox.setPreferredSize(new Dimension(100, 30));
		add(checkBox, "0, 2");
	}
}
