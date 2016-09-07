package org.processmining.framework.providedobjects.impl;

import java.util.UUID;

import org.processmining.framework.ProMID;
import org.processmining.framework.providedobjects.ProvidedObjectID;

public class ProvidedObjectIDImpl implements ProvidedObjectID {

	private final UUID id = UUID.randomUUID();

	public String toString() {
		return id.toString();
	}

	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof ProvidedObjectIDImpl)) {
			return false;
		} else {
			return ((ProvidedObjectIDImpl) o).id.equals(id);
		}
	}

	public boolean equalsIgnoreType(Object o) {
		return toString().equals(o.toString());
	}

	public int compareTo(ProMID o) {
		return o.getUUID().compareTo(id);
	}

	public UUID getUUID() {
		return id;
	}
}
