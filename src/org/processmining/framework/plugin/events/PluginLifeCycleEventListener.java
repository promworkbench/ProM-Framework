package org.processmining.framework.plugin.events;

import java.util.EventListener;

import org.processmining.framework.plugin.PluginContext;

public interface PluginLifeCycleEventListener extends EventListener {

	/**
	 * This method is invoked on a parent context, if a child context of that
	 * parent is created.
	 * 
	 * @param context
	 *            the new Child context
	 */
	public void pluginCreated(PluginContext context);

	/**
	 * This method is invoked on a context, if it is started
	 * 
	 * @param context
	 *            the context
	 */
	public void pluginStarted(PluginContext context);

	/**
	 * This method is invoked on a context, if it is suspended
	 * 
	 * @param context
	 *            the context
	 */
	public void pluginSuspended(PluginContext context);

	/**
	 * This method is invoked on a context, if it is resumed
	 * 
	 * @param context
	 *            the context
	 */
	public void pluginResumed(PluginContext context);

	/**
	 * This method is invoked on a context, if it is completed
	 * 
	 * @param context
	 *            the context
	 */
	public void pluginCompleted(PluginContext context);

	/**
	 * This method is invoked on a context, if it is cancelled
	 * 
	 * @param context
	 *            the context
	 */
	public void pluginCancelled(PluginContext context);

	/**
	 * This method is invoked on a context, if it throws an exception
	 * 
	 * @param context
	 *            the context
	 */
	public void pluginTerminatedWithError(PluginContext context, Throwable t);

	/**
	 * This method is invoked on a context, if it's future was set.
	 * 
	 * @param context
	 *            the context
	 */
	public void pluginFutureCreated(PluginContext context);

	/**
	 * This method is invoked on a context, if it's deleted from its parent
	 * 
	 * @param context
	 *            the context
	 */
	public void pluginDeleted(PluginContext context);

	public class List extends ProMEventListenerList<PluginLifeCycleEventListener> {
		public void firePluginCreated(PluginContext context) {
			for (PluginLifeCycleEventListener listener : getListeners()) {
				listener.pluginCreated(context);
			}
		}

		public void firePluginStarted(PluginContext context) {
			for (PluginLifeCycleEventListener listener : getListeners()) {
				listener.pluginStarted(context);
			}
		}

		public void firePluginSuspended(PluginContext context) {
			for (PluginLifeCycleEventListener listener : getListeners()) {
				listener.pluginSuspended(context);
			}
		}

		public void firePluginResumed(PluginContext context) {
			for (PluginLifeCycleEventListener listener : getListeners()) {
				listener.pluginResumed(context);
			}
		}

		public void firePluginCompleted(PluginContext context) {
			for (PluginLifeCycleEventListener listener : getListeners()) {
				listener.pluginCompleted(context);
			}
		}

		public void firePluginCancelled(PluginContext context) {
			for (PluginLifeCycleEventListener listener : getListeners()) {
				listener.pluginCancelled(context);
			}
		}

		public void firePluginTerminatedWithError(PluginContext context, Throwable t) {
			for (PluginLifeCycleEventListener listener : getListeners()) {
				listener.pluginTerminatedWithError(context, t);
			}
		}

		public void firePluginFutureCreated(PluginContext context) {
			for (PluginLifeCycleEventListener listener : getListeners()) {
				listener.pluginFutureCreated(context);
			}
		}

		public void firePluginDeleted(PluginContext context) {
			for (PluginLifeCycleEventListener listener : getListeners()) {
				listener.pluginDeleted(context);
			}
		}
	}
}