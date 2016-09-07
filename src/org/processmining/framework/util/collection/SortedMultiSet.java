package org.processmining.framework.util.collection;

import java.util.Comparator;
import java.util.SortedSet;

public interface SortedMultiSet<T> extends MultiSet<T> {

	Comparator<? super T> comparator();

	/**
	 * returns an unmodifiable set of unique objects in the multiset.
	 * 
	 * @return an unmodifiable set of unique objects in the multiset.
	 */
	SortedSet<T> baseSet();

}
