package org.processmining.dialogs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.confs.YourConfiguration;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.YourFirstInput;
import org.processmining.models.YourSecondInput;

public class YourDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3587595452197538653L;
	@SuppressWarnings("unused")
	private YourConfiguration configuration;

	public YourDialog(UIPluginContext context, YourFirstInput input1, YourSecondInput input2,
			final YourConfiguration configuration) {
		this.configuration = configuration;
		final JCheckBox yourBooleanCheckBox = new JCheckBox("Your boolean: ");
		yourBooleanCheckBox.setSelected(configuration.isYourBoolean());
		yourBooleanCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				configuration.setYourBoolean(yourBooleanCheckBox.isSelected());
			}
		});
		this.add(yourBooleanCheckBox);
		final JSlider yourIntegerSlider = new JSlider();
		yourIntegerSlider.setMinimum(configuration.getYourInteger() - 50);
		yourIntegerSlider.setMaximum(configuration.getYourInteger() + 50);
		yourIntegerSlider.setValue(configuration.getYourInteger());
		yourIntegerSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				configuration.setYourInteger(yourIntegerSlider.getValue());
			}
		});
		this.add(yourIntegerSlider);
		final JTextArea yourStringTextArea = new JTextArea();
		yourStringTextArea.setText(configuration.getYourString());
		yourStringTextArea.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				configuration.setYourString(yourStringTextArea.getText());
			}
		});
		this.add(yourStringTextArea);
	}
}
