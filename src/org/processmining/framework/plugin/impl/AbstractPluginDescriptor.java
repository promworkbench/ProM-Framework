package org.processmining.framework.plugin.impl;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JOptionPane;

import org.processmining.framework.plugin.InSufficientResultException;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.RecursiveCallException;
import org.processmining.framework.plugin.events.Logger.MessageLevel;

public abstract class AbstractPluginDescriptor implements PluginDescriptor {

	@SuppressWarnings( { "unchecked" })
	private Object[] prepareAndWaitForArgs(PluginContext context, int methodIndex, List<Class<?>> parameterTypes,
			Object... args) throws CancellationException, InterruptedException, ExecutionException, FieldSetException,
			RecursiveCallException {
		Object[] result = new Object[args.length + 1];
		// copy the args to result
		System.arraycopy(args, 0, result, 1, args.length);
		boolean[] done = new boolean[result.length];
		Arrays.fill(done, false);
		boolean timeoutOccurred = false;
		do {
			timeoutOccurred = false;
			for (int i = 1; i < result.length; i++) {
				if (done[i]) {
					continue;
				}
				if (result[i] == null) {
					throw new IllegalArgumentException("Cannot pass <null> as a parameter to a plugin");
				}
				if ((result[i] instanceof ProMFuture)) {
					// synchronize on this get() method
					// any exception is forwarded.
					try {
						// try to get the result within 100 millisecond
						result[i] = ((ProMFuture) result[i]).get(100L, TimeUnit.MILLISECONDS);
						done[i] = true;
					} catch (TimeoutException e) {
						// A timeout exception occurred, no big deal,
						// just wait longer
						timeoutOccurred = true;
					}
				}
				if (result[i] instanceof Object[]) {
					// This array can contain a combination of rightly typed
					// objects, or futures on
					// rightly types objects, or futures on other arrays.
					Object[] array = (Object[]) result[i];
					boolean futureLeft = false;
					for (int j = 0; j < array.length; j++) {
						if (array[j] instanceof ProMFuture) {
							try {
								// try to get the result within 1 second
								array[j] = ((ProMFuture) array[j]).get(100L, TimeUnit.MILLISECONDS);
							} catch (TimeoutException e) {
								// A timeout exception occurred, no big deal,
								// just wait longer
								timeoutOccurred = true;
								futureLeft |= true;
							}
						}
					}
					if (!futureLeft) {
						List<Object> list = new ArrayList<Object>();
						for (int j = 0; j < array.length; j++) {
							if (array[j].getClass().isArray()) {
								for (int k = 0; k < ((Object[]) array[j]).length; k++) {
									list.add(((Object[]) array[j])[k]);
								}
							} else {
								list.add(array[j]);
							}
						}

						result[i] = list.toArray((Object[]) Array.newInstance(parameterTypes.get(i - 1)
								.getComponentType(), 0));
						done[i] = true;
					}

				}
			}
			if (timeoutOccurred) {
				// At least one of my input objects is not ready yet
				// try again in 2 seconds.
				Thread.sleep(2000);
			}
		} while (timeoutOccurred);

		context.setPluginDescriptor(this, methodIndex);
		result[0] = context;

		return result;
	}
	

	public PluginExecutionResult invoke(final int methodIndex, final PluginContext context, final Object... args) {

		ProMFuture<?>[] futures = new ProMFuture[Math.max(1, getReturnTypes().size())];

		Class<?> returnType;
		String name;
		final PluginExecutionResultImpl lock;
		if (getReturnTypes().size() == 0) {
			returnType = void.class;
			name = "nothing";
			lock = new PluginExecutionResultImpl(new Class<?>[] { returnType }, new String[0], this);
		} else {
			returnType = getReturnTypes().get(0);
			name = getReturnNames().get(0);
			lock = new PluginExecutionResultImpl(getReturnTypes().toArray(new Class<?>[0]), getReturnNames().toArray(
					new String[0]), this);
		}

		futures[0] = new ProMFuture<Object>(returnType, name) {

			@Override
			protected Object doInBackground() throws Exception {
				context.getPluginLifeCycleEventListeners().firePluginStarted(context);
				synchronized (lock) {
					// First, do a get on all Future objects in the args list

					Object[] allArgs = prepareAndWaitForArgs(context, methodIndex, getParameterTypes(methodIndex), args);

					// copy the result back into parameters, i.e. all futures
					// are unwrapped at this point and arrays are correctly typed.
					Object[] parameters = new Object[allArgs.length - 1];
					System.arraycopy(allArgs, 1, parameters, 0, parameters.length);
					lock.setInput(parameters);

					// All objects are available, now any exceptions
					// are forwarded, so start the computation of
					// this plugin
					try {
						System.out.println("Start plug-in " + getName());
						long time = -System.currentTimeMillis();
						Object[] result = execute(context, methodIndex, allArgs);
						time += System.currentTimeMillis();
						System.out.println("End plug-in " + getName() + ", took " + time + " milliseconds");

						if ((result == null) && !getReturnType().equals(void.class)) {
							throw new InSufficientResultException(getName(), lock.getExpectedSize(), 0);
						} else if (result.length < lock.getExpectedSize()) {
							throw new InSufficientResultException(getName(), lock.getExpectedSize(), result.length);
						} else if (result.length > lock.getExpectedSize()) {
							context.log("Plugin " + getName() + " produced " + result.length + " results, while "
									+ lock.getExpectedSize() + " results were declared. Extra results are ignored.",
									MessageLevel.WARNING);
						}

						lock.setResult(result);
						lock.notifyAll();
						Object object = lock.getObject(0);
						return object;
					} catch (Exception ex) {
						if (ex.getCause() instanceof Exception) {
							ex = (Exception) ex.getCause();
						}
						lock.setException(ex);
						lock.notifyAll();
						//						context.getPluginLifeCycleEventListeners().firePluginTerminatedWithError(context, ex);
						//						context.log(ex);
						throw ex;
					}
				}
			}

			@Override
			protected void done() {
				if (context != null) {
					if (isCancelled()) {
						context.getPluginLifeCycleEventListeners().firePluginCancelled(context);
					} else {
						try {
							get();
							context.getPluginLifeCycleEventListeners().firePluginCompleted(context);
						} catch (Exception e) {
							if (GraphicsEnvironment.isHeadless()) {
								System.err.println("Exception happened: "+e.getMessage());
							} else {
								JOptionPane.showMessageDialog(null, "Exception happened: "+e.getMessage());
							}
							context.getPluginLifeCycleEventListeners().firePluginTerminatedWithError(context, e);
							context.log(e);
						}
					}

				}

			}
		};

		for (int i = 1; i < getReturnTypes().size(); i++) {
			final int j = i;
			returnType = getReturnTypes().get(i);
			name = getReturnNames().get(i);

			futures[i] = new ProMFuture<Object>(returnType, name) {
				@Override
				protected Object doInBackground() throws Exception {
					synchronized (lock) {
						while ((lock.getObject(j) instanceof ProMFuture<?>) && (lock.getException() == null)) {
							lock.wait();
						}
						Object object = lock.getObject(j);
						if ((object != null) && (lock.getException() == null)) {
							return object;
						}
						throw (lock.getException());
					}

				}
			};
		}

		lock.setResult(futures);

		context.setFuture(lock);
		context.getPluginLifeCycleEventListeners().firePluginFutureCreated(context);
		assert (context.getParentContext() != null);
		for (int i = 0; i < futures.length; i++) {
			context.getParentContext().getExecutor().execute(futures[i].getRunnable());
		}

		return lock;
	}

	/**
	 * In this method, the pluginDescriptor should do the actual work of
	 * concstructing the result. Note that no objects passed in the allArgs
	 * argument are futures anymore.
	 * 
	 * @param context
	 * @param methodIndex
	 * @param allArgs
	 * @return
	 * @throws Exception
	 */
	protected abstract Object[] execute(PluginContext context, int methodIndex, Object... allArgs) throws Exception;

}
