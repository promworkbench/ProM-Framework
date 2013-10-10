package org.processmining.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.confs.YourConfiguration;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.YourDialog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.YourFirstInput;
import org.processmining.models.YourOutput;
import org.processmining.models.YourSecondInput;

@Plugin(name = "Your plug-in name", parameterLabels = { "Name of your first input", "Name of your second input", "Name of your configuration" }, 
	    returnLabels = { "Name of your output" }, returnTypes = { YourOutput.class })
public class YourPlugin {

	/**
	 * The method that does the heavy lifting for your plug-in.
	 * 
	 * Note that this method only uses the boolean which is stored in the configuration.
	 * Nevertheless, it could have used the integer and/or the String as well.
	 * 
	 * @param context The context where to run in.
	 * @param input1 The first input.
	 * @param input2 The second input.
	 * @param configuration The configuration to use.
	 * @return The output.
	 */
	private YourOutput yourPrivatePlugin(PluginContext context, YourFirstInput input1, YourSecondInput input2, YourConfiguration configuration) {
	    return configuration.isYourBoolean() ? new YourOutput(input1) : new YourOutput(input2);
	}
	
	/**
	 * The plug-in variant that runs in any context and requires a configuration.
	 * 
	 * @param context The context to run in.
	 * @param input1 The first input.
	 * @param input2 The second input.
	 * @param configuration The configuration to use.
	 * @return The output.
	 */
	@UITopiaVariant(affiliation = "Your affiliation", author = "Your name", email = "Your e-mail address")
	@PluginVariant(variantLabel = "Your plug-in name, parameters", requiredParameterLabels = { 0, 1, 2 })
	public YourOutput yourConfiguredPlugin(PluginContext context, YourFirstInput input1, YourSecondInput input2, YourConfiguration configuration) {
		// Do the heavy lifting.
	    return yourPrivatePlugin(context, input1, input2, configuration);
	}
	
	/**
	 * The plug-in variant that runs in any context and uses the default configuration.
	 * 
	 * @param context The context to run in.
	 * @param input1 The first input.
	 * @param input2 The second input.
	 * @return The output.
	 */
	@UITopiaVariant(affiliation = "Your affiliation", author = "Your name", email = "Your e-mail address")
	@PluginVariant(variantLabel = "Your plug-in name, parameters", requiredParameterLabels = { 0, 1 })
	public YourOutput yourDefaultPlugin(PluginContext context, YourFirstInput input1, YourSecondInput input2) {
		// Get the default configuration.
	    YourConfiguration configuration = new YourConfiguration(input1, input2);
		// Do the heavy lifting.
	    return yourPrivatePlugin(context, input1, input2, configuration);
	}
	
	/**
	 * The plug-in variant that runs in a UI context and uses a dialog to get the configuration.
	 * 
	 * @param context The context to run in.
	 * @param input1 The first input.
	 * @param input2 The second input.
	 * @return The output.
	 */
	@UITopiaVariant(affiliation = "Your affiliation", author = "Your name", email = "Your e-mail address")
	@PluginVariant(variantLabel = "Your plug-in name, dialog", requiredParameterLabels = { 0, 1 })
	public YourOutput yourDefaultPlugin(UIPluginContext context, YourFirstInput input1, YourSecondInput input2) {
		// Get the default configuration.
	    YourConfiguration configuration = new YourConfiguration(input1, input2);
	    // Get a dialog for this configuration.
	    YourDialog dialog = new YourDialog(context, input1, input2, configuration);
	    // Show the dialog. User can now change the configuration.
	    InteractionResult result = context.showWizard("Your dialog title", true, true, dialog);
	    // User has close the dialog.
	    if (result == InteractionResult.FINISHED) {
			// Do the heavy lifting.
	    	return yourPrivatePlugin(context, input1, input2, configuration);
	    }
	    // Dialog got canceled.
	    return null;
	}	
}
