package org.processmining.framework.connections.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionAnnotation;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.connections.annotations.ConnectionDoesntExistMessage;
import org.processmining.framework.plugin.annotations.KeepInProMCache;
import org.processmining.framework.util.collection.HashMultiSet;
import org.processmining.framework.util.collection.MultiSet;

/**
 * Note that for all implementations of Connection, it is essential to consider
 * memory consumption. Connections are kept in the framework for as long as the
 * isRemoved() method returns false, hence this method should return true at the
 * earliest occasion.
 * 
 * The AbstractConnection keeps weak references to the objects added through the
 * put methods. As soon as one of these objects is collected by the garbage
 * collector, then this connection becomes removed. Therefore, it is essential
 * that subclasses of the AbstractConnection do not keep pointers to these
 * objects, or at least only keep weak references.
 * 
 * This class is annotated with @ConnectionAnnotation. Therefore, none of the
 * implementing classes have to do so explicitly.
 * 
 * @author bfvdonge
 * 
 */
@KeepInProMCache
@ConnectionAnnotation
@ConnectionDoesntExistMessage(message = "Connection Doesn't Exist")
public abstract class AbstractConnection implements Connection {

	private final Map<String, WeakReference<?>> mapping;
	private static final long serialVersionUID = -9049359040527952685L;

	private String label;

	private final ConnectionID id;
	protected transient ConnectionManager manager = null;

	protected AbstractConnection(String label) {
		if (label == null) {
			throw new NullPointerException("Connection label should not be NULL");
		}
		this.label = label;
		id = new ConnectionIDImpl();
		mapping = new HashMap<String, WeakReference<?>>();
	}

	public void setManager(ConnectionManager manager) {
		this.manager = manager;
	}

	public String getLabel() {
		return label;
	}

	public boolean containsObjects(Object... objects) {
		return containsObjects(Arrays.asList(objects));
	}

	public boolean containsObjects(Collection<?> objects) {
		Collection<WeakReference<?>> references = new ArrayList<WeakReference<?>>(mapping.values());

		for (Object o : objects) {
			boolean found = false;
			Iterator<WeakReference<?>> it = references.iterator();
			while (!found && it.hasNext()) {
				Object referenced = it.next().get();
				if (o.getClass().isArray() && referenced.getClass().isArray()) {
					// We are dealing with an array type
					if (Arrays.equals((Object[]) o, (Object[]) referenced)) {
						// Found a match for this object.
						found = true;
					}
				} else {
					if (referenced.equals(o)) {
						// Found a match for this object.
						found = true;
					}
				}
				if (found) {
					it.remove();
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	public boolean isRemoved() {
//		System.gc(); // By doing this, object are removed earlier from the workspace.
		for (Map.Entry<String, WeakReference<?>> t : mapping.entrySet()) {
			Object o = t.getValue().get();
//			System.out.println("[AbstractionConnection] isRemoved " + this.getClass().getName() + "@" + t.getKey() + ": " + o);			
			if (o == null) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		return "Connection labelled " + label + ", connecting " + super.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	public MultiSet<Object> getObjects() {
		MultiSet<Object> result = new HashMultiSet<Object>();
		for (Map.Entry<String, WeakReference<?>> t : mapping.entrySet()) {
			Object o = t.getValue().get();
			if (o != null) {
				result.add(o);
			} else {
				return new HashMultiSet<Object>();
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof AbstractConnection)) {
			return false;
		}
		AbstractConnection a = (AbstractConnection) o;
		if (id == null) {
			if (a.id != null) {
				return false;
			}
		} else if (!id.equals(a.id)) {
			return false;
		}
		return true;
	}

	public ConnectionID getID() {
		return id;
	}

	@SuppressWarnings("unchecked")
	public <T> T getObjectWithRole(String role) {
		assert (get(role) != null);
		return (T) get(role);
	}

	public Set<String> getRoles() {
		return mapping.keySet();
	}

	protected <T> WeakReference<T> put(String role, T o) {
		WeakReference<T> ref = new WeakReference<T>(o);
		mapping.put(role, ref);
		return ref;
	}

	protected void remove(String role) {
		mapping.remove(role);
	}

	protected Object get(String role) {
		return mapping.get(role).get();
	}

	protected Map<String, WeakReference<?>> getMapping() {
		return mapping;
	}

	/**
	 * Removes the connection. After calling this method, isRemoved()returns
	 * true;
	 */
	public void remove() {
		for (Map.Entry<String, WeakReference<?>> t : mapping.entrySet()) {
			t.getValue().clear();
		}
	}

	/**
	 * sets the label of the connection to the new name
	 * 
	 * @param name
	 */
	public void setLabel(String name) {
		if (name == null) {
			throw new NullPointerException("Connection label should not be NULL");
		}
		boolean changed = name.equals(label);
		this.label = name;
		if (changed) {
			updated();
		}
	}

	public void updated() {
		if (manager != null) {
			manager.getConnectionListeners().fireConnectionUpdated(id);
		}
	}

	private Object readResolve() {
		manager = null;
		return this;
	}
}
