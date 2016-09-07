package org.processmining.framework.util.collection;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.processmining.framework.util.HTMLToString;

/**
 * This class implements a mutliset. The implementation is synchronized.
 * 
 * @author bfvdonge
 * 
 * @param <T>
 *            the type of the objects in this multiset.
 */
public interface MultiSet<T> extends Collection<T>, HTMLToString {

	/**
	 * Adds the given object to the multiset, as many times as indicated by the
	 * given weight parameter.
	 * 
	 * @param par
	 *            the object to add
	 * @param weight
	 *            the number of times to add it
	 * @return the new number of occurrences of the object (>0)
	 */
	public Integer add(T par, Integer weight);

	/**
	 * Converts this multiset to a list, such that each element occurs as often
	 * as returned by the iterator of the multiset (its number of occurrences).
	 * 
	 * @return a list of objects as returned by the iterator
	 */
	public List<T> toList();

	/**
	 * returns true if this multiset is less or equal to the given multiset,
	 * i.e. all objects in this multiset should be contained in the given set
	 * and the number of occurrences in the given set is at least the number of
	 * occurrences in this multiset.
	 * 
	 * @param multiSet
	 *            the multiset to test
	 * @return true if the given multiset is less or equal.
	 */
	public boolean isLessOrEqual(MultiSet<T> multiSet);

	/**
	 * returns the number of occurrences of the given object in this multiset.
	 * 
	 * @param source
	 *            the object to get the occurrences for
	 * @return the number of occurrences, 0 if the object does not occur.
	 */
	public Integer occurrences(Object source);

	/**
	 * returns an unmodifiable set of unique objects in the multiset.
	 * 
	 * @return an unmodifiable set of unique objects in the multiset.
	 */
	public Set<T> baseSet();

}
