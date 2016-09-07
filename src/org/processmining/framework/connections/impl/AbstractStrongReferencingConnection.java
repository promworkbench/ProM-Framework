package org.processmining.framework.connections.impl;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public abstract class AbstractStrongReferencingConnection extends AbstractConnection {

	// Keep a strong reference to some objects, as these
	// would otherwise fault the connection.
	// When checking for removal, these objects are ignored, i.e. the connection
	// is considered to be removed if any of the weak references has been nullified.
	private final Collection<Object> objectReferences = new HashSet<Object>();

	public AbstractStrongReferencingConnection(String label) {
		super(label);
	}

	protected <T> WeakReference<T> putStrong(String role, T o) {
		objectReferences.add(o);
		return super.put(role, o);
	}

	@Override
	public boolean isRemoved() {
		for (Map.Entry<String, WeakReference<?>> t : getMapping().entrySet()) {
			Object o = t.getValue().get();
			if (objectReferences.contains(o)) {
				// Skip the objects to which Hard links need to be kept.
				continue;
			}
			if (o == null) {
				return true;
			}
		}
		return false;
	}

}