package org.processmining.framework.plugin.impl;

import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.processmining.framework.plugin.IncorrectReturnTypeException;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.events.NameChangeListener;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.framework.util.Cast;

public class PluginExecutionResultImpl implements PluginExecutionResult {

	private Object[] objects;
	private Exception exception = null;
	private final Class<?>[] returnTypes;
	private final String pluginName;
	private final String[] returnNames;
	private final ProvidedObjectID[] ids;
	private Object[] parameters;
	private final PluginDescriptor plugin;

	public PluginExecutionResultImpl(Class<?>[] returnTypes, String[] returnNames, PluginDescriptor plugin) {
		this.returnNames = returnNames;
		this.plugin = plugin;
		pluginName = plugin.getName();
		objects = new Object[returnTypes.length];
		ids = new ProvidedObjectID[returnTypes.length];
		this.returnTypes = returnTypes;
		Arrays.fill(objects, null);
		Arrays.fill(ids, null);
	}

	public int getSize() {
		return returnNames.length;
	}

	void setResult(Object[] objects) throws IncorrectReturnTypeException {
		this.objects = objects;
		for (int i = 0; i < returnTypes.length; i++) {
			if (!returnTypes[i].equals(void.class)) {
				if (objects[i] == null) {
					continue;
				}
				Class<?> type = objects[i].getClass();
				if (objects[i] instanceof ProMFuture<?>) {
					type = Cast.<ProMFuture<?>>cast(objects[i]).getReturnType();
				}
				if (!returnTypes[i].isAssignableFrom(type)) {
					throw new IncorrectReturnTypeException(pluginName, i, returnTypes[i], objects[i].getClass());
				}
				if (objects[i] instanceof ProMFuture<?>) {
					final int index = i;
					((ProMFuture<?>) objects[i]).getNameChangeListeners().add(new NameChangeListener() {

						public void nameChanged(String newName) {
							returnNames[index] = newName;
						}
					});
				}
			}
		}
	}

	Object getObject(int resultIndex) {
		return objects[resultIndex];
	}

	void setException(Exception t) {
		exception = t;
	}

	Exception getException() {
		return exception;
	}

	@SuppressWarnings("unchecked")
	public <T> T getResult(int resultIndex) throws ClassCastException {
		return (T) getObject(resultIndex);
	}

	public String getResultName(int resultIndex) {
		synchronized (objects) {
			if (getObject(resultIndex) instanceof ProMFuture<?>) {
				return Cast.<ProMFuture<?>>cast(getObject(resultIndex)).getLabel();
			} else {
				return returnNames[resultIndex];
			}
		}
	}

	public String[] getResultNames() {
		return returnNames;
	}

	public Object[] getResults() {
		return objects;
	}

	public void synchronize() throws CancellationException, ExecutionException, InterruptedException {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof ProMFuture<?>) {
				Object o = Cast.<ProMFuture<?>>cast(objects[i]).get();
				synchronized (objects) {
					objects[i] = o;
				}
			}
		}
	}

	int getExpectedSize() {
		return objects.length;
	}

	public void setProvidedObjectID(int i, ProvidedObjectID id) {
		ids[i] = id;
	}

	public ProvidedObjectID getProvidedObjectID(int i) {
		return ids[i];
	}

	public void setInput(Object[] parameters) {
		this.parameters = parameters;
	}

	public Object[] getParameters() {
		return parameters;
	}

	@SuppressWarnings("unchecked")
	public <T> Class<? super T> getType(int i) {
		return (Class<? super T>) returnTypes[i];
	}

	public PluginDescriptor getPlugin() {
		return plugin;
	}

}
