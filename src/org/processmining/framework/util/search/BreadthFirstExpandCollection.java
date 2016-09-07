package org.processmining.framework.util.search;

import java.util.Collection;

public class BreadthFirstExpandCollection<N> extends DepthFirstExpandCollection<N> {

	public void add(Collection<? extends N> newElements) {
		stack.addAll(0, newElements);
	}

}
