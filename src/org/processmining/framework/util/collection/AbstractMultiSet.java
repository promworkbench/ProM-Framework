package org.processmining.framework.util.collection;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.processmining.framework.util.Cast;

/**
 * This class implements a mutliset. The implementation is synchronized.
 * 
 * @author bfvdonge
 * 
 * @param <T>
 *            the type of the objects in this multiset.
 */
public abstract class AbstractMultiSet<T, M extends Map<T, Integer>> extends AbstractCollection<T> implements
		MultiSet<T> {

	protected M map;
	protected int size;
	private int hashCode;
	private boolean hashValid = false;

	private static final long serialVersionUID = -6521454214767452945L;

	abstract <S> MultiSet<S> newMultiSet(Collection<S> collection);

	abstract MultiSet<T> newMultiSet();

	/**
	 * Keeps all elements of the given collection in this multiset. Multicities
	 * are taken into account, i.e. as many of the same objects are kept as
	 * returned by the collections iterator.
	 * 
	 * @return true if the multiset changed from calling this method.
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		hashValid = false;
		return retainAll(newMultiSet(c));
	}

	/**
	 * adds one object to the multiset. If the object was not contained before,
	 * then it now has one occerrence, otherwise, the number of occurrences is
	 * increased.
	 * 
	 * @return true, since the collection is always modified.
	 */
	@Override
	public boolean add(T par) {
		add(par, 1);
		hashValid = false;
		return true;
	}

	/**
	 * Keeps all elements of the given collection in this multiset.
	 * Multiplicities are taken into account.
	 * 
	 * @return true if the multiset changed from calling this method.
	 */
	public boolean retainAll(MultiSet<?> c) {
		boolean changed = false;
		Iterator<T> it = map.keySet().iterator();
		while (it.hasNext()) {
			T key = it.next();
			
			Integer occToRetain = c.occurrences(key);
			Integer occInThis = occurrences(key);
			if (occInThis >= occToRetain) {
				// keep occToRetain
				size -= (occInThis - occToRetain);
				if (occToRetain == 0) {
					it.remove();
				} else {
					map.put(key, occToRetain);
				}
				changed = true;
				hashValid = false;
			}
		}
		return changed;
	}

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
	public Integer add(T par, Integer weight) {
		if (weight == 0) {
			return weight;
		}
		hashValid = false;
		long newSize = (long) size + (long) weight;
		if (newSize > Integer.MAX_VALUE) {
			size = Integer.MAX_VALUE;
		} else {
			size = (int) newSize;
		}
		if (!map.containsKey(par)) {
			assert (weight > 0);
			map.put(par, weight);
			return weight;
		} else {
			long num = (long) map.get(par) + (long) weight;
			if (num > Integer.MAX_VALUE) {
				num = Integer.MAX_VALUE;
			}
			if (num == 0) {
				map.remove(par);
			} else {
				assert (num > 0);
				map.put(par, (int) num);
			}
			return (int) num;
		}
	}

	/**
	 * Adds the given collection to this multiset. If the given collection is
	 * not a multiset, then the implementation is diverted to
	 * abstractcollection.
	 * 
	 * @param collection
	 *            the collection to add
	 * @return true if the multiset changed due to this method call.
	 */
	@Override
	public boolean addAll(Collection<? extends T> collection) {
		if (collection.isEmpty()) {
			return false;
		}
		hashValid = false;
		if (collection instanceof MultiSet<?>) {
			MultiSet<? extends T> mset = Cast.<MultiSet<? extends T>>cast(collection);
			for (T key : mset.baseSet()) {
				add(key, mset.occurrences(key));
			}
		} else {
			for (T key : collection) {
				add(key);
			}
		}
		return true;
	}

	/**
	 * Converts this multiset to a list, such that each element occurs as often
	 * as returned by the iterator of the multiset (its number of occurrences).
	 * 
	 * @return a list of objects as returned by the iterator
	 */
	public List<T> toList() {
		List<T> list = new ArrayList<T>(size);
		for (T occ : this) {
			list.add(occ);
		}
		return list;
	}

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
	public boolean isLessOrEqual(MultiSet<T> multiSet) {
		for (T element : baseSet()) {
			if (multiSet.occurrences(element) < occurrences(element)) {
				return false;
			}
		}
		return true;
	}

	protected boolean containsAtLeast(T element, int occ) {
		return occurrences(element) >= occ;
	}

	/**
	 * returns true if the multisets are equal, i.e. if they contain the same
	 * objects with the same number of occurrences.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof AbstractMultiSet<?, ?>) {
			return map.equals(((AbstractMultiSet<?, ?>) o).map);
		}
		return false;
	}

	/**
	 * returns the size of the multiset, i.e. the sum over the multiplicities of
	 * all contained objects.
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * returns a string representing this multiset. The string contains, between
	 * brackets, pairs of objects and their multiplicities.
	 */
	@Override
	public String toString() {
		String s = "[";
		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			if (!s.equals("[")) {
				s += " ";
			}
			s += "(" + entry.getKey() + "," + entry.getValue() + ")";
		}
		return s + "]";
	}

	/**
	 * returns a hashCode for this multiset.
	 */
	@Override
	public int hashCode() {
		if (!hashValid) {
			hashCode = map.hashCode();
			hashValid = true;
		}
		return hashCode;

	}

	/**
	 * returns the number of occurrences of the given object in this multiset.
	 * 
	 * @param source
	 *            the object to get the occurrences for
	 * @return the number of occurrences, 0 if the object does not occur.
	 */
	public Integer occurrences(Object source) {
		return (map.keySet().contains(source) ? map.get(source) : 0);
	}

	/**
	 * returns an iterator over the elements of the multiset. Note that if an
	 * object appears n times in the multiset, it is returned n times by the
	 * iterator.
	 * 
	 * For an iterator over unique elements of the multiset, use the toSet()
	 * method.
	 */
	@Override
	public Iterator<T> iterator() {
		return new MultiSetIterator<T, M>(this);
	}

	/**
	 * returns an unmodifiable set of unique objects in the multiset.
	 * 
	 * @return an unmodifiable set of unique objects in the multiset.
	 */
	public Set<T> baseSet() {
		return Collections.unmodifiableSet(map.keySet());
	}

	/**
	 * checks whether the number of occurrences of the given object is greater
	 * or equal to 1.
	 */
	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	/**
	 * Checks whether the number of occurrences of elements in the given
	 * collection is at most what is specified in this collection, i.e., this
	 * method returns (new MultiSet(c)).isLessOrEqual(this)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(Collection<?> c) {
		MultiSet set;
		if (c instanceof MultiSet) {
			set = (MultiSet) c;
		} else {
			set = newMultiSet(c);
		}
		return set.isLessOrEqual(this);
	}

	/**
	 * removes the given object from this multiset, if it is in there. Only one
	 * occurrence is removed, i.e. contains(o) can still be true after calling
	 * remove(o)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if (occurrences(o) == 0) {
			return false;
		} else {
			hashValid = false;
			// removing 1 occurrence
			size--;
			Integer occ = map.get(o);
			if (occ == 1) {
				map.remove(o);
				return true;
			} else {
				// since o is in this multiset, it can safely
				// be cast to T
				map.put((T) o, occ - 1);
				return true;
			}
		}
	}

	/**
	 * removes the elements in the given multiset from this multiset.
	 * 
	 * @param mset
	 *            the multiset of elements needing to be removed.
	 * @return a new multiset where the occurrences are the occurrences in this
	 *         multiset, minus the occurrences in the given multiset
	 */
	@Override
	public boolean removeAll(Collection<?> collection) {
		if (collection instanceof AbstractMultiSet<?, ?>) {
			return !removeAllMultiSet(Cast.<AbstractMultiSet<?, ?>>cast(collection), newMultiSet()).isEmpty();
		} else {
			boolean b = false;
			for (Object o : collection) {
				b |= remove(o);
			}
			return b;
		}
	}

	protected <S extends MultiSet<T>> S removeAllMultiSet(AbstractMultiSet<?, ?> mset, S removed) {
		for (Map.Entry<?, Integer> entry : mset.map.entrySet()) {
			if (!map.containsKey(entry.getKey())) {
				continue;
			}
			// Since map.containsKey(entry.getKey()), this is a safe cast
			T key = Cast.<T>cast(entry.getKey());
			Integer val = map.get(key);
			// What's the minimum of the amount I have and the amount I have to remove
			Integer toRemove = Math.min(entry.getValue(), val);
			removed.add(key, toRemove);

			size -= toRemove;
			if (val - toRemove == 0) {
				map.remove(key);
			} else {
				assert (val - toRemove > 0);
				map.put(key, val - toRemove);
			}
		}
		hashValid = false;
		return removed;
	}

	public String toHTMLString(boolean includeHTMLTags) {

		String s = (includeHTMLTags ? "<html>" : "") + "[";
		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			if (!s.endsWith("[")) {
				s += ",";
			}
			s += entry.getKey();
			if (entry.getValue() > 1) {
				s += "<sup>" + entry.getValue() + "</sup>";
			}
		}
		return s + "]" + (includeHTMLTags ? "</html>" : "");
	}

	@Override
	public void clear() {
		map.clear();
		hashValid = false;
		size = 0;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

}

/**
 * Provides an iterator over a MultiSet. This iterator returns objects as many
 * times as they are contained in the multiset.
 * 
 * @author bfvdonge
 * 
 * @param <T>
 */
class MultiSetIterator<T, M extends Map<T, Integer>> implements Iterator<T> {

	private final AbstractMultiSet<T, M> multiset;
	private final Iterator<Map.Entry<T, Integer>> iterator;
	private Entry<T, Integer> currentEntry = null;
	private Integer toGiveCount = 0;
	private boolean removed = false;

	public MultiSetIterator(AbstractMultiSet<T, M> multiset) {
		this.multiset = multiset;
		this.iterator = multiset.map.entrySet().iterator();
	}

	public boolean hasNext() {
		// No next object, if toGiveCount ==0 and iterator has no next object
		return !((toGiveCount == 0) && !iterator.hasNext());
	}

	public T next() throws NoSuchElementException {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		if (toGiveCount == 0) {
			this.currentEntry = iterator.next();
			this.toGiveCount = currentEntry.getValue();
		}
		// reduce the toGiveCount by 1.
		toGiveCount--;
		removed = false;
		return currentEntry.getKey();
	}

	public void remove() {
		if (removed) {
			throw new IllegalStateException();
		}
		int val = currentEntry.getValue();
		if (val > 1) {
			currentEntry.setValue(val - 1);
		} else {
			iterator.remove();
		}
		multiset.size--;
		removed = true;
	}

}
