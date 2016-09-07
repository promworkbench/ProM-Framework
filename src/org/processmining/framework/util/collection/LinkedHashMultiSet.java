package org.processmining.framework.util.collection;

import java.util.Collection;
import java.util.LinkedHashMap;

public class LinkedHashMultiSet<T> extends HashMultiSet<T> {

	/**
	 * Constructs a new multiset, such that all elements of the given collection
	 * are added as many times as they are returned by the iterator of that
	 * collection.
	 * 
	 * @param collection
	 *            Representing the objects that should be put in a multiset
	 */
	public LinkedHashMultiSet(Collection<? extends T> collection) {
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
	public LinkedHashMultiSet(T... collection) {
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
	public LinkedHashMultiSet() {
		size = 0;
		map = new LinkedHashMap<T, Integer>();
	}

}
