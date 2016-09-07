package org.processmining.framework.util.search;

import java.util.Collection;
import java.util.Stack;

public class DepthFirstExpandCollection<N> implements ExpandCollection<N> {

	protected Stack<N> stack = new Stack<N>();

	public void add(Collection<? extends N> newElements) {
		stack.addAll(newElements);
	}

	public N pop() {
		return stack.pop();
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

}
