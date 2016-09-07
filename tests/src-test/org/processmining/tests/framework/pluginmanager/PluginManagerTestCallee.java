package org.processmining.tests.framework.pluginmanager;

import org.junit.Ignore;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;


/*
 * A test plugin with 2 plugin variants where the second variants uses a
 * superset of the parameters of the first variant
 * 
 * @author dfahland
 */
@Plugin(name = "Test Plugin (called by PluginManagerTests - test_invoke_other_plugins)", 
	parameterLabels = {"Input1", "Input2"},
	returnLabels = {"Output1", "Output2", "Output3", "Output4"},
	returnTypes = {Integer.class, String.class, Double.class, Character.class},
	userAccessible = true,
	help = "some help text")
@Ignore // no JUnit test
public class PluginManagerTestCallee {
	
	@PluginVariant(requiredParameterLabels = { 0 })
	public static Object[] runPlugin(PluginContext context, Integer input1) {
		return runPlugin(context, input1, input1.toString());
	}
	
	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public static Object[] runPlugin(PluginContext context, Integer input1, String input2) {
		Object[] result = new Object[4];
		result[0] = input1;
		result[1] = input2;
		result[2] = new Double(input1);
		result[3] = (input2.length() == 0) ? new Character(' ') : new Character(input2.charAt(0));
		return result;
	}

}
