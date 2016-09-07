package org.processmining.framework.connections.impl;

import java.util.UUID;

import org.processmining.framework.ProMID;
import org.processmining.framework.connections.ConnectionID;

public class ConnectionIDImpl implements ConnectionID {

	private final UUID id = UUID.randomUUID();

	public String toString() {
		return id.toString();
	}

	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof ConnectionIDImpl)) {
			return false;
		} else {
			return ((ConnectionIDImpl) o).id.equals(id);
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
