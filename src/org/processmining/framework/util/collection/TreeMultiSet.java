package org.processmining.framework.util.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class TreeMultiSet<T> extends AbstractMultiSet<T, TreeMap<T, Integer>> implements SortedMultiSet<T> {

	private Comparator<? super T> comparator;

	/**
	 * Constructs a new multiset, such that all elements of the given collection
	 * are added as many times as they are returned by the iterator of that
	 * collection.
	 * 
	 * All elements inserted into the multiset must implement the Comparable
	 * interface. Furthermore, all such elements must be mutually comparable:
	 * e1.compareTo(e2) must not throw a ClassCastException for any elements e1
	 * and e2 in the set.
	 * 
	 * @param collection
	 *            Representing the objects that should be put in a multiset
	 */
	public TreeMultiSet(Collection<T> collection) {
		this((Comparator<T>) null);
		addAll(collection);
	}

	/**
	 * Constructs a new multiset, such that all elements of the given collection
	 * are added as many times as they are in the given array.
	 * 
	 * All elements inserted into the multiset must implement the Comparable
	 * interface. Furthermore, all such elements must be mutually comparable:
	 * e1.compareTo(e2) must not throw a ClassCastException for any elements e1
	 * and e2 in the set.
	 * 
	 * @param collection
	 *            Representing the objects that should be put in a multiset
	 */
	public TreeMultiSet(T... collection) {
		this((Comparator<T>) null);
		for (T par : collection) {
			add(par);
		}
	}

	/**
	 * Constructs a new, empty multiset, such that all elements of the given
	 * collection are added as many times as they are returned by the iterator
	 * of that collection.
	 * 
	 * All elements inserted into the multiset must implement the Comparable
	 * interface. Furthermore, all such elements must be mutually comparable:
	 * e1.compareTo(e2) must not throw a ClassCastException for any elements e1
	 * and e2 in the set.
	 * 
	 */
	public TreeMultiSet() {
		this((Comparator<T>) null);
	}

	/**
	 * Constructs a new multiset, such that all elements of the given collection
	 * are added as many times as they are returned by the iterator of that
	 * collection.
	 * 
	 * @param comp
	 *            A comparator providing sorting on the elements of the multiset
	 * @param collection
	 *            Representing the objects that should be put in a multiset
	 */
	public TreeMultiSet(Collection<T> collection, Comparator<? super T> comp) {
		this(comp);
		addAll(collection);
	}

	/**
	 * Constructs a new multiset, such that all elements of the given collection
	 * are added as many times as they are in the given array.
	 * 
	 * @param comp
	 *            A comparator providing sorting on the elements of the multiset
	 * @param collection
	 *            Representing the objects that should be put in a multiset
	 */
	public TreeMultiSet(Comparator<? super T> comp, T... collection) {
		this(comp);
		for (T par : collection) {
			add(par);
		}
	}

	/**
	 * Constructs a new, empty multiset, such that all elements of the given
	 * collection are added as many times as they are returned by the iterator
	 * of that collection.
	 * 
	 * @param comp
	 *            A comparator providing sorting on the elements of the multiset
	 */
	public TreeMultiSet(Comparator<? super T> comp) {
		size = 0;
		TreeMap<T, Integer> newMap = new TreeMap<T, Integer>(comp);
		comparator = newMap.comparator();
		map = newMap;
	}

	<S> MultiSet<S> newMultiSet(Collection<S> collection) {
		return new TreeMultiSet<S>(collection);
	}

	MultiSet<T> newMultiSet() {
		return new TreeMultiSet<T>();
	}

	public Comparator<? super T> comparator() {
		return comparator;
	}

	/**
	 * returns an unmodifiable set of unique objects in the multiset.
	 * 
	 * @return an unmodifiable set of unique objects in the multiset.
	 */
	public SortedSet<T> baseSet() {
		// JAVA 5 CODE:
		SortedSet<T> set = new TreeSet<T>(comparator);
		set.addAll(map.keySet());
		// JAVA 6 CODE:
		// SortedSet<T> set = map.navigableKeySet());
		return Collections.unmodifiableSortedSet(set);
	}
}
