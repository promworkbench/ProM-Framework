package org.processmining.framework.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.GlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.RecursiveCallException;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.events.Logger;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.plugin.events.PluginLifeCycleEventListener;
import org.processmining.framework.plugin.events.ProgressEventListener;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.framework.util.Cast;
import org.processmining.framework.util.Pair;

public abstract class AbstractPluginContext implements PluginContext {

	private final GlobalContext context;
	private final PluginContext parentPluginContext;

	private final PluginLifeCycleEventListener.List lifeCycleEventListeners = new PluginLifeCycleEventListener.List();
	private final ProgressEventListener.ListenerList progressEventListeners = new ProgressEventListener.ListenerList();
	private final Logger.ListenerList loggers = new Logger.ListenerList();

	protected Progress progress;
	private PluginDescriptor descriptor;
	private final java.util.List<PluginContext> childContexts = new ArrayList<PluginContext>(0);
	private PluginExecutionResult futures = null;
	private final PluginContextID id;
	private String label = "";
	private int methodIndex = -1;

	/**
	 * Create a new root plugin instance context.
	 * 
	 * @param context
	 *            the global context in which this instance context is used
	 */
	public AbstractPluginContext(GlobalContext context, String label) {
		assert (context != null);
		this.context = context;
		id = context.createNewPluginContextID();
		this.label = label;
		parentPluginContext = null;
		progress = new ProgressBarImpl(this);
	}

	protected AbstractPluginContext(AbstractPluginContext context, String label) {
		assert (context != null);
		this.context = context.getGlobalContext();
		id = this.context.createNewPluginContextID();
		this.label = label;
		parentPluginContext = context;
		progress = new ProgressBarImpl(this);
	}

	public Pair<PluginDescriptor, Integer> getPluginDescriptor() {
		return new Pair<PluginDescriptor, Integer>(descriptor, methodIndex);
	}

	public GlobalContext getGlobalContext() {
		return context;
	}

	public PluginLifeCycleEventListener.List getPluginLifeCycleEventListeners() {
		return lifeCycleEventListeners;
	}

	public ProgressEventListener.ListenerList getProgressEventListeners() {
		return progressEventListeners;
	}

	public Progress getProgress() {
		return progress;
	}

	public PluginContextID getID() {
		return id;
	}

	public ConnectionManager getConnectionManager() {
		return context.getConnectionManager();
	}

	public String getLabel() {
		return label;
	}

	public boolean hasPluginDescriptorInPath(PluginDescriptor plugin, int methodIndex) {
		return ((methodIndex == this.methodIndex) && (plugin == descriptor))
				|| (getParentContext() == null ? false : getParentContext().hasPluginDescriptorInPath(plugin,
						methodIndex));
	}

	public void setPluginDescriptor(PluginDescriptor descriptor, int methodIndex) throws FieldSetException,
			RecursiveCallException {
		if ((getParentContext() != null) && getParentContext().hasPluginDescriptorInPath(descriptor, methodIndex)) {
			throw new RecursiveCallException(this, descriptor, methodIndex);
		}
		if (this.descriptor == null) {
			this.methodIndex = methodIndex;
			this.descriptor = descriptor;
		} else {
			throw new FieldSetException("descriptor",
					"Use getGlobalContext().createInstanceContext() to create a new instance context");
		}
	}

	public PluginContext createChildContext(String label) {
		PluginContext context = createTypedChildContext(label);
		childContexts.add(context);
		return context;
	}

	public java.util.List<PluginContext> getChildContexts() {
		return Collections.unmodifiableList(childContexts);
	}

	public PluginContext getParentContext() {
		return parentPluginContext;
	}

	protected abstract PluginContext createTypedChildContext(String label);

	/**
	 * This method returns the PluginExecutionResult of the plugin which is
	 * invoked in this context. This future result is set by
	 * PluginManager.invoke() and will not be available (will be null) until the
	 * invoke() method is called.
	 * 
	 * @return The PluginExecutionResult that represents the result of this
	 *         plugin invocation
	 * @throws FieldNotSetException
	 *             If the future is not know to this context
	 */
	public PluginExecutionResult getResult() {
		return futures;
	}

	public ProMFuture<?> getFutureResult(int i) {
		assert (futures != null);
		Object o = futures.getResult(i);
		assert (o instanceof ProMFuture<?>);
		// Note that if this cast cannot be performed,
		// an exception should be thrown. This method should
		// only be called from the body of a plugin
		return futures.<ProMFuture<?>>getResult(i);
	}

	public void setFuture(PluginExecutionResult futureToBe) {
		assert (futures == null);
		futures = futureToBe;
	}

	public boolean equals(Object o) {
		if (o instanceof PluginContext) {
			return ((PluginContext) o).getID().equals(id);
		}
		return false;

	}

	public String toString() {
		return getID() + ": " + getLabel();
	}

	public boolean isDistantChildOf(PluginContext context) {
		if (getParentContext() == null) {
			return false;
		}
		return getParentContext().getID().equals(context.getID()) || getParentContext().isDistantChildOf(context);
	}

	/**
	 * The provided String is provided to the context for information. It can
	 * for example signal a state change of a plugin. Note that some contexts
	 * can completely ignore this message.
	 * 
	 * @param message
	 *            the message to log
	 * @param level
	 *            the message level
	 */
	public void log(String message, MessageLevel level) {
		loggers.fireLog(message, id, level);
	}

	/**
	 * Same as calling log(message, MessageLevel.NORMAL);
	 * 
	 * @param message
	 *            The message
	 */
	public void log(String message) {
		loggers.fireLog(message, id, MessageLevel.NORMAL);
	}

	/**
	 * The provided Exception is provided to the context. It signals the context
	 * about an error in the plugin, that specifically lead to abnormal
	 * termination. The plugin signaling the exception is no longer executing!
	 * 
	 * @param exception
	 *            the exception thrown
	 */
	public void log(Throwable exception) {
		loggers.fireLog(exception, id);
		System.err.println("-------- The following exception was logged by the framework: ");
		System.err.println("         The exception was probalby handled properly. ");
		exception.printStackTrace();
		System.err.println("--------------------------------------------------------------");
		
	}

	/**
	 * Returns the plugin manager. The plugin manager can be used to query for
	 * plugins which are registered in ProM.
	 * 
	 * @return the plugin manager
	 */
	public PluginManager getPluginManager() {
		return context.getPluginManager();
	}

	/**
	 * Returns the providedObject manager. The providedObject manager can be
	 * used to query for providedObjects which are registered in ProM. The
	 * manager should be a ProvidedObjectListener on all PluginInstanceContexts
	 * created by createRootInstanceContext.
	 * 
	 * @return the providedObject manager
	 */
	public ProvidedObjectManager getProvidedObjectManager() {
		return context.getProvidedObjectManager();
	}

	/**
	 * The GlobalContext implementation should create IDs for all PluginContexts
	 * instantiated for it.
	 * 
	 * @return
	 */
	public PluginContextID createNewPluginContextID() {
		return context.createNewPluginContextID();
	}

	/**
	 * This method invokes the specified plugin in a context which is a child of
	 * the main plugin context maintained by this globalContext;
	 * 
	 * @param plugin
	 * @param objects
	 *            The objects to serve as input. Note that all elements should
	 *            implement Object, or be a ProMFuture<?>.
	 */
	public void invokePlugin(PluginDescriptor plugin, int index, Object... objects) {
		context.invokePlugin(plugin, index, objects);
	}

	public void invokeBinding(PluginParameterBinding binding, Object... objects) {
		context.invokeBinding(binding, objects);
	}

	public Logger.ListenerList getLoggingListeners() {
		return loggers;
	}

	public PluginContext getRootContext() {
		if (parentPluginContext == null) {
			return this;
		} else {
			return parentPluginContext.getRootContext();
		}
	}

	public boolean deleteChild(PluginContext child) {
		if (childContexts.contains(child)) {
			childContexts.remove(child);
			for (PluginContext context : new ArrayList<PluginContext>(child.getChildContexts())) {
				child.deleteChild(context);
			}
			child.getPluginLifeCycleEventListeners().firePluginDeleted(child);
			return true;
		}
		return false;
	}

	public <T extends Connection> T addConnection(T c) {
		return getConnectionManager().addConnection(c);
	}

	public Class<? extends PluginContext> getPluginContextType() {
		return this.getClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.util.ObjectConstructorInterface#
	 * tryToFindOrConstructAllObjects(java.lang.Class,
	 * org.processmining.framework.plugin.PluginContext, java.lang.Class,
	 * java.lang.String, java.lang.Object)
	 */
	public <T, C extends Connection> Collection<T> tryToFindOrConstructAllObjects(Class<T> type,
			Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
		return findOrConstructAllObjects(false, type, null, connectionType, role, input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.util.ObjectConstructorInterface#
	 * tryToFindOrConstructFirstObject(java.lang.Class,
	 * org.processmining.framework.plugin.PluginContext, java.lang.Class,
	 * java.lang.String, java.lang.Object)
	 */
	public <T, C extends Connection> T tryToFindOrConstructFirstObject(Class<T> type, Class<C> connectionType,
			String role, Object... input) throws ConnectionCannotBeObtained {
		return findOrConstructAllObjects(true, type, null, connectionType, role, input).iterator().next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.util.ObjectConstructorInterface#
	 * tryToFindOrConstructFirstNamedObject(java.lang.Class, java.lang.String,
	 * org.processmining.framework.plugin.PluginContext, java.lang.Class,
	 * java.lang.String,
	 * 
	 * java.lang.Object)
	 */
	public <T, C extends Connection> T tryToFindOrConstructFirstNamedObject(Class<T> type, String name,
			Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
		return findOrConstructAllObjects(true, type, name, connectionType, role, input).iterator().next();
	}

	private <T, C extends Connection> Collection<T> constructAllObjects(boolean stopAtFirst, Class<T> type,
			String name, Object... input) throws CancellationException, InterruptedException, ExecutionException {
		Class<?>[] types;
		if (input != null) {
			types = new Class<?>[input.length];
			for (int i = 0; i < input.length; i++) {
				types[i] = input[i].getClass();
			}
		} else {
			types = new Class<?>[0];
			input = new Object[0];
		}

		// Find available plugins
		Set<Pair<Integer, PluginParameterBinding>> set = getPluginManager().find(Plugin.class, type,
				getPluginContextType(), true, false, false, types);

		if (set.isEmpty()) {
			throw new RuntimeException("No plugin available to build this type of object: " + type.toString());
		}

		// Filter on the given name, if given.
		if (name != null) {
			Set<Pair<Integer, PluginParameterBinding>> filteredSet = new HashSet<Pair<Integer, PluginParameterBinding>>();
			for (Pair<Integer, PluginParameterBinding> pair : set) {
				if (name.equals(pair.getSecond().getPlugin().getName())) {
					filteredSet.add(pair);
				}
			}
			set.clear();
			set.addAll(filteredSet);
		}

		if (set.isEmpty()) {
			throw new RuntimeException("No named plugin available to build this type of object: " + name + ", "
					+ type.toString());
		}

		SortedSet<Pair<Integer, PluginParameterBinding>> plugins = new TreeSet<Pair<Integer, PluginParameterBinding>>(
				new Comparator<Pair<Integer, PluginParameterBinding>>() {

					public int compare(Pair<Integer, PluginParameterBinding> arg0,
							Pair<Integer, PluginParameterBinding> arg1) {
						int c = arg0.getSecond().getPlugin().getReturnNames().size()
								- arg1.getSecond().getPlugin().getReturnNames().size();
						if (c == 0) {
							c = arg0.getSecond().compareTo(arg1.getSecond());
						}
						if (c == 0) {
							c = arg0.getFirst() - arg1.getFirst();
						}
						return c;
					}

				});
		plugins.addAll(set);

		Collection<T> result = new ArrayList<T>(stopAtFirst ? 1 : plugins.size());

		// get the first available plugin
		ExecutionException ex = null;
		for (Pair<Integer, PluginParameterBinding> pair : plugins) {
			PluginParameterBinding binding = pair.getSecond();
			// create a context to execute this plugin in
			PluginContext child = createChildContext("Computing: " + type.toString());
			getPluginLifeCycleEventListeners().firePluginCreated(child);

			// Invoke the binding
			PluginExecutionResult pluginResult = binding.invoke(child, input);

			// synchronize on the required result and continue
			try {
				pluginResult.synchronize();

				// get all results and pass them to the framework as provided objects
				getProvidedObjectManager().createProvidedObjects(child);
				result.add(pluginResult.<T>getResult(pair.getFirst()));
				if (stopAtFirst) {
					break;
				}
			} catch (ExecutionException e) {
				// Try next plugin if stop at first, otherwise rethrow
				ex = e;
			} finally {
				child.getParentContext().deleteChild(child);
			}
		}
		if (result.isEmpty()) {
			assert (ex != null);
			throw ex;
		}
		return result;
	}

	private <T, C extends Connection> Collection<T> findOrConstructAllObjects(boolean stopAtFirst, Class<T> type,
			String name, Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {

		Collection<T> accepted = new ArrayList<T>();
		try {
			for (C conn : getConnectionManager().getConnections(connectionType, this, input)) {
				Object object = conn.getObjectWithRole(role);
				if (type.isAssignableFrom(object.getClass())) {
					accepted.add(Cast.<T>cast(object));
				}
			}
		} catch (Exception e) {
			// Don't care, let's try to construct later
		}
		if (!accepted.isEmpty()) {
			return accepted;
		}
		try {
			return constructAllObjects(stopAtFirst, type, name, input);
		} catch (Exception e) {
			throw new ConnectionCannotBeObtained(e.getMessage(), connectionType);
		}
	}

	public void clear() {
		getProvidedObjectManager().clear();
		getConnectionManager().clear();
	}
}