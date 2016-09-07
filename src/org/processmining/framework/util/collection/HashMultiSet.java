package org.processmining.framework.util.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HashMultiSet<T> extends AbstractMultiSet<T, Map<T, Integer>> {

	/**
	 * Constructs a new multiset, such that all elements of the given collection
	 * are added as many times as they are returned by the iterator of that
	 * collection.
	 * 
	 * @param collection
	 *            Representing the objects that should be put in a multiset
	 */
	public HashMultiSet(Collection<? extends T> collection) {
		this();
		addAll(collection);
	}

	/**
	 * Constructs a new multiset, such that all elements of the given collection
	 * are added as many times as they are in the given array.
	 * 
	 * @param collection
	 *            Representing the objects that should be put in a multiset
	 */
	public HashMultiSet(T[] collection) {
		this();
		for (T par : collection) {
			add(par);
		}
	}

	/**
	 * Constructs a new, empty multiset, such that all elements of the given
	 * collection are added as many times as they are returned by the iterator
	 * of that collection.
	 */
	public HashMultiSet() {
		size = 0;
		map = new HashMap<T, Integer>();
	}

	<S> MultiSet<S> newMultiSet(Collection<S> collection) {
		return new HashMultiSet<S>(collection);
	}

	MultiSet<T> newMultiSet() {
		return new HashMultiSet<T>();
	}
}
