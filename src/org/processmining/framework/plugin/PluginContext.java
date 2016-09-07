package org.processmining.framework.plugin;

import java.util.List;
import java.util.concurrent.Executor;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.plugin.events.Logger;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.plugin.events.PluginLifeCycleEventListener;
import org.processmining.framework.plugin.events.ProgressEventListener;
import org.processmining.framework.plugin.impl.FieldNotSetException;
import org.processmining.framework.plugin.impl.FieldSetException;
import org.processmining.framework.util.Pair;

public interface PluginContext extends GlobalContext, ObjectConstructor {

	/**
	 * Returns a new plugin context instance, which can be used to invoke other
	 * plugins.
	 * 
	 * @return the new plugin context instance
	 */
	PluginContext createChildContext(String label);

	/* === Getters ==================================================== */

	/**
	 * Returns the progress object corresponding to this context
	 * 
	 * @return the progress object corresponding to this context
	 */
	Progress getProgress();

	/**
	 * Returns the list of registered progress listeners
	 * 
	 * @return the list of registered progress listeners
	 */
	ProgressEventListener.ListenerList getProgressEventListeners();

	/**
	 * Returns the list of registered plugin life cycle listeners.
	 * 
	 * @return the list of registered plugin life cycle listeners.
	 */
	PluginLifeCycleEventListener.List getPluginLifeCycleEventListeners();

	/**
	 * Each PluginContext should carry an ID. This ID is unique within this
	 * plugin context's global context.
	 * 
	 * @return the ID of this context
	 */
	PluginContextID getID();

	/**
	 * Returns the label of this context.
	 * 
	 * @return
	 */
	String getLabel();

	/**
	 * Return the plugin descriptor and method index of the plugin which is
	 * invoked in this context. This descriptor is set by the
	 * PluginDescriptor.invoke() method and will not be set yet before
	 * PluginManager.invoke() is called.
	 * 
	 * @return the descriptor of the plugin which is invoked in this context If
	 *         the plugin is not set yet, a pair of (null,-1) is returned
	 */
	Pair<PluginDescriptor, Integer> getPluginDescriptor();

	/**
	 * Returns the context which created this context or null if it has no
	 * parent.
	 * 
	 * @return
	 */
	PluginContext getParentContext();

	/**
	 * Returns a list of all child contexts which have been created with
	 * createChildContext().
	 * 
	 * @return
	 */
	List<PluginContext> getChildContexts();

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
	PluginExecutionResult getResult();

	/**
	 * This method should only be used by a plugin, in the body of that plugin.
	 * That is the only location, where it is guaranteed that each result object
	 * in getResults() can safely be cast to a ProMFuture.
	 * 
	 * @param i
	 * @return
	 */
	ProMFuture<?> getFutureResult(int i);

	/**
	 * Returns an executor which can be used to execute plugins in child
	 * contexts.
	 * 
	 * @return
	 */
	Executor getExecutor();

	/**
	 * Returns true if this is a distant child of context, i.e. true if
	 * getParent.getID().equals(context.getID()) ||
	 * getParent().isDistantChildOf(context);
	 * 
	 * @param context
	 * @return
	 */
	boolean isDistantChildOf(PluginContext context);

	/*
	 * === Setters: should only be called by the framework!
	 * ===============================
	 */

	void setFuture(PluginExecutionResult resultToBe);

	void setPluginDescriptor(PluginDescriptor descriptor, int methodIndex) throws FieldSetException,
			RecursiveCallException;

	boolean hasPluginDescriptorInPath(PluginDescriptor descriptor, int methodIndex);

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
	void log(String message, MessageLevel level);

	/**
	 * Same as calling log(message, MessageLevel.NORMAL);
	 * 
	 * @param message
	 *            The message
	 */
	void log(String message);

	/**
	 * The provided Exception is provided to the context. It signals the context
	 * about an error in the plugin, that specifically lead to abnormal
	 * termination. The plugin signaling the exception is no longer executing!
	 * 
	 * @param exception
	 *            the exception thrown
	 */
	void log(Throwable exception);

	/**
	 * Returns the list of logging listeners registered to this context.
	 * 
	 * @return
	 */
	Logger.ListenerList getLoggingListeners();

	/**
	 * Returns the root plugin context. This is an instance of PluginContext of
	 * which all other contexts are distant childs.
	 * 
	 * @return
	 */
	PluginContext getRootContext();

	/**
	 * Delete this child from this context.
	 * 
	 * @param child
	 * @returns true if this child was a child of the context and has now been
	 *          deleted. false otherwise
	 */
	boolean deleteChild(PluginContext child);

	/**
	 * Registers the given connection in the global context. The implementation
	 * is
	 * 
	 * addConnection(this,c);
	 * 
	 * @param c
	 */
	<T extends Connection> T addConnection(T c);

	void clear();
}
