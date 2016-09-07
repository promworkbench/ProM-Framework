package org.processmining.framework.plugin;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.SwingWorker;

import org.processmining.framework.plugin.events.FutureListener;
import org.processmining.framework.plugin.events.NameChangeListener;

/**
 * Class to represent a future on an object.
 * 
 * @author bfvdonge
 * 
 * @param <T>
 */
public abstract class ProMFuture<T> implements Future<T> {

	private final Class<?> classType;
	private final SwingWorker<T, Void> worker;
	private String label;
	private final NameChangeListener.ListenerList nameChangeListeners = new NameChangeListener.ListenerList();
	private final FutureListener.ListenerList futureListeners = new FutureListener.ListenerList();

	/**
	 * Instantiates a ProMFuture object of the given type and with the given
	 * label.
	 * 
	 * A SwingWorker is instantiated by the constructor. This SwingWorker is
	 * provided through the <code>getRunnable()</code> method and once executed,
	 * it will execute the <code>doInBackground()</code> method of this
	 * ProMFuture.
	 * 
	 * When finished, the <code>done()</code> method is invoked, after which any
	 * Future listeners are notified that this future is ready.
	 * 
	 * @param resultClass
	 *            Any type that extends T. However, no subtype of ProMFuture can
	 *            be provided.
	 * @param label
	 */
	public ProMFuture(Class<? extends T> resultClass, String label) {
		if (ProMFuture.class.isAssignableFrom(resultClass)) {
			// Cannot put a future inside a future.
			throw new RuntimeException("Cannot put a future in a future");
		}
		this.classType = resultClass;
		this.label = label;
		this.worker = new SwingWorker<T, Void>() {
			@Override
			protected T doInBackground() throws Exception {
				return ProMFuture.this.doInBackground();
			}

			@Override
			protected void done() {
				// invoke the setLabel() on getLabel() after finishing execution.
				// this is necessary to fire name-changed events to any 
				// listeners registered after the last call to setLabel();
				ProMFuture.this.setLabel(ProMFuture.this.getLabel());

				ProMFuture.this.done();
				ProMFuture.this.getFutureListeners().fireFutureReady(ProMFuture.this);
			}
		};
	}

	/**
	 * This method is called by the runnable of this future as soon as the
	 * computation of the result is ready, but before any listeners are notified
	 * of the completion.
	 * 
	 * Any overriding implementation can use the get() method to return the
	 * object computed, as this method is guaranteed not to throw exceptions
	 * when done() is reached.
	 */
	protected void done() {
		// Default empty implementation
	}

	/**
	 * Returns the Runnable representing the execution that needs to be
	 * performed to produce the result of this future.
	 * 
	 * @return
	 */
	public Runnable getRunnable() {
		return worker;
	}

	/**
	 * The return type of this future.
	 * 
	 * @return
	 */
	public Class<?> getReturnType() {
		return classType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get()
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		return worker.cancel(mayInterruptIfRunning);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get()
	 */
	public T get() throws InterruptedException, ExecutionException, CancellationException {
		return worker.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException,
			CancellationException {
		return worker.get(timeout, unit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isCancelled()
	 */
	public boolean isCancelled() {
		return worker.isCancelled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isDone()
	 */
	public boolean isDone() {
		return worker.isDone();
	}

	/**
	 * This method should be implemented by all subclasses of ProMFuture. Here,
	 * the object of type T is computed and returned.
	 * 
	 * @return
	 * @throws Exception
	 *             This exception should be any exception thrown by the logic of
	 *             the underlying method. Note that "wrapper"-Exceptions such as
	 *             InvocationTargetException and ExecutionException should be
	 *             unwrapped.
	 */
	protected abstract T doInBackground() throws Exception;

	/**
	 * Return a string representation of this future.
	 */
	public String toString() {
		if (isCancelled()) {
			return ("Cancelled calculation of " + getLabel());
		}
		return (isDone() ? "Processing: " : "") + getLabel();
	}

	/**
	 * returns the label of this future
	 * 
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label of this future to the given label and fires name change
	 * events in any registered name change listeners.
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
		nameChangeListeners.fireNameChanged(label);
	}

	/**
	 * Returns a ListenerList containing the registered name change listeners
	 * 
	 * @return
	 */
	public NameChangeListener.ListenerList getNameChangeListeners() {
		return nameChangeListeners;
	}

	/**
	 * Returns a ListenerList containing the registered future listeners
	 * 
	 * @return
	 */
	public FutureListener.ListenerList getFutureListeners() {
		return futureListeners;
	}

}
