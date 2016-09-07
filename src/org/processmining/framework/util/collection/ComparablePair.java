package org.processmining.framework.util.collection;

import org.processmining.framework.util.Pair;

public class ComparablePair<F extends Comparable<? super F>, S extends Comparable<? super S>> extends Pair<F, S>
		implements Comparable<ComparablePair<F, S>> {

	public ComparablePair(F first, S second) {
		super(first, second);
	}

	public <T extends Comparable<? super T>> int compareTo(T x, T y) {
		if (x == null) {
			return y == null ? 0 : -1;
		} else {
			return x.compareTo(y);
		}
	}

	public int compareTo(ComparablePair<F, S> other) {
		if (other == null) {
			return 1;
		}
		int result = compareTo(first, other.first);
		return result == 0 ? compareTo(second, other.second) : result;
	}

}
