package org.processmining.framework.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.processmining.framework.util.ArrayUtils;

/**
 * This class represents a binding between a method of a plugin and a set of
 * parameters. The semantics of this object are as follows.
 * 
 * First, the {@code PluginParameterBinding.Factory.tryToBind()} should be used
 * to construct a list of PluginParameterBindings. This method is called with a
 * variable array of parameter types, such that for each returned binding, the
 * <code>invoke()</code> method can be called with a variable array of
 * instantiations of these types, in that order.
 * 
 * Any reordering that needs to be done (if <code>tryToBind()</code> was called
 * with <code>orderedParameters == false</code>) is handled by the binding.
 * 
 * A PluginParameterBinding is only guaranteed to be executable, if the factory
 * method was called with <code>mustBeTotal</code> set to true.
 * 
 * @author bfvdonge
 * 
 */
public class PluginParameterBinding implements Comparable<PluginParameterBinding> {

	/**
	 * Factory for instantiating PluginParameterBindings binding plugins with a
	 * given input.
	 * 
	 * @author bfvdonge
	 * 
	 */
	public static class Factory {

		private Factory() {
		}

		/**
		 * This method instantiates binding objects binding the method and index
		 * <code>methodIndex</code> of the given plugin to the given set of
		 * parameter types, if possible. If no binding is possible, an empty
		 * list is returned.
		 * 
		 * If a total binding is requested (indicated by
		 * <code>mustBeTotal == true</code>), then the returned binding assigns
		 * objects from the given parameters to all inputParameters of the
		 * plugin method.
		 * 
		 * If no total binding is required, the resulting bindings is not
		 * necessarily executable, as the method bound might require more input.
		 * However, all provided parameterTypes are bound to a parameter.
		 * 
		 * Using the flag <code>orderedParameters</code> the user can request
		 * bindings that consider the given parameters, in the given order only,
		 * i.e. no reordering is done. Any binding <code>b</code> returned with
		 * <code>orderedParameters == true</code> will satisfy the property that
		 * <code>b.getBinding()[i] == i</code> for all <code>i</code>.
		 * 
		 * @param plugin
		 *            the plugin for which to try to bind the parameters
		 * @param mustBeTotal
		 *            indicating whether the resulting binding should be total
		 * @param orderedParameters
		 *            whether or not the parameters are ordered.
		 * @param parameterTypes
		 *            the available types to be distributed over the parameters.
		 *            Note that if one of the objects is a Object[] then, this
		 *            array will be kept as one. Multiple objects of type T,
		 *            might end up in an array of type S super T, i.e. they can
		 *            be bound into one S[] to be passed to a single element of
		 *            plugin.getTypes()
		 * @return A list of length 0 if no binding exists, or a list of length
		 *         1 if a binding exists. Note that this may change in the
		 *         future to lists of length n.
		 */
		public static List<PluginParameterBinding> tryToBind(PluginManager manager, PluginDescriptor plugin,
				int methodIndex, boolean mustBeTotal, boolean orderedParameters, Class<?>... parameterTypes) {

			List<PluginParameterBinding> result = new ArrayList<PluginParameterBinding>();

			if (mustBeTotal && (parameterTypes.length < plugin.getParameterTypes(methodIndex).size())) {
				// Quick check. the result is empty if it should be total, but their
				// are less provided parameters than required.
				return result;
			}
			if (!mustBeTotal && (parameterTypes.length > 0) && (plugin.getParameterTypes(methodIndex).size() <= 1)) {
				// Quick check. A partial mapping to one type is not possible, when
				// having
				// to assign all parameters
				return result;
			}
			if (orderedParameters && (parameterTypes.length != plugin.getParameterTypes(methodIndex).size())) {
				// can't do this, parameters.size should match required params

				return result;
			}

			// We are dealing with multiple parameters, and we are sure that
			// there are more parameters than types if the mapping needs to be
			// total.
			if ((parameterTypes.length == plugin.getParameterTypes(methodIndex).size())
					&& ((parameterTypes.length == 1) || orderedParameters)) {
				// if ordered, or only 1 to match
				int[] list = new int[parameterTypes.length];
				for (int i = 0; i < parameterTypes.length; i++) {
					// This single parameter matches the required type
					Class<?> parType = plugin.getParameterTypes(methodIndex).get(i);
					//				if (parType.isAssignableFrom(parameters.get(i))
					//						|| (parType.isArray() && parType.getComponentType().isAssignableFrom(parameters.get(i)))) {
					if (manager.isParameterAssignable(parameterTypes[i], parType)) {
						list[i] = i;
					} else {
						return result;
					}

				}
				PluginParameterBinding binding = new PluginParameterBinding(plugin, methodIndex, true, list);
				result.add(binding);

				return result;
			}

			boolean[] fullyUsed = new boolean[plugin.getParameterNames(methodIndex).size()];
			boolean[] used = new boolean[plugin.getParameterNames(methodIndex).size()];
			Arrays.fill(fullyUsed, false);
			Arrays.fill(used, false);

			int[] list = new int[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				Class<?> par = parameterTypes[i];
				boolean done = false;
				int matchingIndex = -1;
				Class<?> parType = null;
				// First try to match to an unused input parameter
				for (int j = 0; (j < plugin.getParameterNames(methodIndex).size()) && !done; j++) {
					if (used[j]) {
						continue;
					}
					parType = plugin.getParameterTypes(methodIndex).get(j);
					boolean match = manager.isParameterAssignable(par, parType);
					if (match) {
						matchingIndex = j;
						done = true;
					}
				}
				// Then, to a used, but not fully used parameter
				for (int j = 0; (j < plugin.getParameterNames(methodIndex).size()) && !done; j++) {
					if (fullyUsed[j]) {
						continue;
					}
					parType = plugin.getParameterTypes(methodIndex).get(j);
					boolean match = manager.isParameterAssignable(par, parType);
					if (match) {
						matchingIndex = j;
						done = true;
					}
				}

				if (done) {
					assert ((matchingIndex >= 0) && (parType != null));
					list[i] = matchingIndex;
					used[matchingIndex] = true;
					fullyUsed[matchingIndex] = !parType.isArray();
				} else {
					// Could not assign this parameter
					return result;
				}
				// }
			}
			boolean complete = true;
			for (int i = 0; (i < used.length) && complete; i++) {
				complete &= used[i];
			}

			if (mustBeTotal == complete) {
				PluginParameterBinding binding = new PluginParameterBinding(plugin, methodIndex, complete, list);
				result.add(binding);
			}

			return result;
		}

	}

	private final PluginDescriptor plugin;
	private final int methodIndex;
	private final boolean isComplete;

	// Binding represents the index of the parameter to which
	// an object should be bound
	private final int[] binding;

	/**
	 * Constructs a binding on the given plugin, method and binding.
	 * 
	 * @param plugin
	 * @param methodIndex
	 * @param isComplete
	 * @param binding
	 */
	private PluginParameterBinding(PluginDescriptor plugin, int methodIndex, boolean isComplete, int[] binding) {
		this.plugin = plugin;
		this.methodIndex = methodIndex;
		this.isComplete = isComplete;
		this.binding = binding;

	}

	/**
	 * Invokes the method of the plugin referenced by this binding on the given
	 * parameterObjects. It should be noted that the number of given parameter
	 * object should be the same as the number of parameter types provided to
	 * the <code>tryToBind()</code> method of the factory. Furthermore, the
	 * types of these parameters should be right, i.e. each object should be of
	 * the right type, or should be a <code>ProMFuture</code> on that type.
	 * 
	 * In this method, the given parameters are first re-ordered according to
	 * the given binding and then
	 * <code>getPlugin().invoke(getMethodIndex(), context, ...)</code> is
	 * called, on the newly ordered parameters.
	 * 
	 * No checks are done if this plugin is executable or not on the given
	 * input, it's up to the plugin to handle this
	 * 
	 * @param context
	 *            Note that the plugin should be executable in this context.
	 *            However, since PluginContext's should be kept uniformly typed
	 *            within one instance of ProM, no checks are done here.
	 * @param parameterObjects
	 * @return
	 */
	public PluginExecutionResult invoke(PluginContext context, Object... parameterObjects) {
		Object[] args = prepareArguments(parameterObjects);
		return plugin.invoke(methodIndex, context, args);
	}

	/**
	 * Sorts the parameters according the ordering specified in the binding
	 * array. Produces arrays if multiple objects are bound to the same
	 * parameter.
	 * 
	 * @param parameterObjects
	 * @return
	 */
	private Object[] prepareArguments(Object... parameterObjects) {
		Object[] args = new Object[plugin.getParameterNames(methodIndex).size()];
		Arrays.fill(args, null);
		for (int i = 0; i < parameterObjects.length; i++) {
			int index = binding[i];
			if (index < 0) {
				continue;
			}
			if (!plugin.getParameterTypes(methodIndex).get(index).isArray()) {
				// Single parameter
				args[index] = parameterObjects[i];
				continue;
			}
			// Array Parameter (there might be more added to this array).
			Object[] arg;
			if (args[index] == null) {
				arg = new Object[0];
			} else {
				arg = (Object[]) args[index];
			}
			Object[] newArg = ArrayUtils.copyOf(arg, arg.length + 1);
			newArg[arg.length] = parameterObjects[i];
			args[index] = newArg;
		}
		return args;
	}

	/**
	 * Returns the Plugin which is bound by this binding.
	 * 
	 * @return
	 */
	public PluginDescriptor getPlugin() {
		return plugin;
	}

	/**
	 * This method returns an array of integers, of which the length corresponds
	 * to the length of the list of parameters types provided to the
	 * PluginParameterBinding factory. For each parameter type, this array
	 * indicates to which parameter of the plugin it is bound.
	 * 
	 * For each element <code>x</code> of this array is holds that
	 * <code>0 <= x < getPlugin().getParameterNames(getMethodIndex())</code>
	 * 
	 * If the same parameter index is provided for different parameters (i.e.
	 * <code>getBinding()[x] == getBinding()[y]</code> with <code>x != y</code>,
	 * then this impliest that the parameter is an array type, to which multiple
	 * objects can be connected. In other words, this implies that
	 * 
	 * <code>getPlugin().getParameterType(getMethodIndex(),getBinding()[x]).isArray() == true</code>
	 * 
	 * @return
	 */
	public int[] getBinding() {
		return binding;
	}

	/**
	 * Equality of bindings is based on the binding, plugin and complete status.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof PluginParameterBinding)) {
			return false;
		}
		PluginParameterBinding b = (PluginParameterBinding) o;
		return (isComplete == b.isComplete) && plugin.equals(b.plugin) && (methodIndex == b.methodIndex)
				&& Arrays.equals(b.binding, binding);
	}

	/**
	 * Returns a hashcode based on the binding, plugin and complete status
	 */
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + Arrays.hashCode(binding);
		hash = 31 * hash + plugin.hashCode();
		hash = 31 * hash + methodIndex;
		return 31 * hash + (isComplete ? 13 : 17);

	}

	/**
	 * Returns the method index of the method which is bound by this binding.
	 * 
	 * When this binding is invoked, this method inside the plugin provided by
	 * getPlugin() is invoked.
	 * 
	 * @return
	 */
	public int getMethodIndex() {
		return methodIndex;
	}

	public int compareTo(PluginParameterBinding other) {
		if (other == this) {
			return 0;
		}
		int c = plugin.compareTo(other.plugin);
		if (c == 0) {
			c = methodIndex - other.methodIndex;
		}
		if (c == 0) {
			return Arrays.toString(binding).compareTo(Arrays.toString(other.binding));
		}
		return c;
	}

}