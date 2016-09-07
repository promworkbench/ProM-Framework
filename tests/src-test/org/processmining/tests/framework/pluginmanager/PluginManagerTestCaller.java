package org.processmining.tests.framework.pluginmanager;

import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.annotations.Plugin;

@Ignore // no JUnit test
public class PluginManagerTestCaller {
	
	@Plugin(name = "test_invoke_other_plugins1", parameterLabels = {}, //
	returnLabels = { "test result" }, returnTypes = { String.class }, userAccessible = false)
	public static String test_Invoke_Other_Plugin_1(PluginContext context) throws Throwable {

		Integer input = new Integer(context.hashCode());
		Integer output = context.tryToFindOrConstructFirstNamedObject(Integer.class, "Test Plugin (called by PluginManagerTests - test_invoke_other_plugins)", 
				null, null, input);
		
		Assert.assertEquals("Expecting same output as input", input, output);

		return "success";
	}
	
	@Plugin(name = "test_invoke_other_plugins2", parameterLabels = {}, //
	returnLabels = { "test result" }, returnTypes = { String.class }, userAccessible = false)
	public static String test_Invoke_Other_Plugin_2(PluginContext context) throws Throwable {

		Integer input = new Integer(context.hashCode());
		
		
		PluginDescriptor plugin2 = context.getPluginManager().getPlugin("org.processmining.tests.framework.pluginmanager.PluginManagerTestCallee");
		List<PluginParameterBinding> plugin2Bindings = PluginParameterBinding.Factory.tryToBind(context.getPluginManager(), plugin2, 0, true, true, Integer.class);
		
		Assert.assertTrue("Could not find a parameter binding", !plugin2Bindings.isEmpty());
		
		PluginParameterBinding pluginBinding = plugin2Bindings.get(0);

		PluginContext child_context = context.createChildContext("CNet mining");
		context.getPluginLifeCycleEventListeners().firePluginCreated(child_context);
		PluginExecutionResult pluginResult = pluginBinding.invoke(child_context, input);
		pluginResult.synchronize();
		
		Integer output1 = pluginResult.<Integer> getResult(0);
		String output2 = pluginResult.<String> getResult(1);
		Double output3 = pluginResult.<Double> getResult(2);
		Character output4 = pluginResult.<Character> getResult(3);
		
		Assert.assertNotNull(output1);
		Assert.assertNotNull(output2);
		Assert.assertNotNull(output3);
		Assert.assertNotNull(output4);
		
		Assert.assertEquals("Expecting same output as input", input, output1);
		
		return "success";
	}

}
