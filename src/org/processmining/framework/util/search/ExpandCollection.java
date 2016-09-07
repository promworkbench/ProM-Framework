package org.processmining.framework.util.search;

import java.util.Collection;

/**
 * 
 * @author bfvdonge
 * 
 * @param <N>
 */
public interface ExpandCollection<N> {

	/**
	 * Gets and removes the first element to be expanded by the node expander.
	 * 
	 * Implementing classes may assume that isEmpty() returns false before any
	 * call to pop, i.e. the first line of the implementation can be:
	 * <code>assert (!isEmpty())</code>
	 * 
	 * @return the first element to be investigated by the searcher.
	 */
	public N pop();

	/**
	 * Adds the given elements to the collection. Note that implementing classes
	 * may safely assume that the calling thread owns the monitor, i.e. there is
	 * no need to synchronize on the collection retained by this
	 * expandcollection and removals are allowed. Furthermore, the implementing
	 * class may decide not to include any of the given newElements if it
	 * decides that they are not to be investigated further.
	 * 
	 * @param newElements
	 *            the nodes to expand further
	 */
	public void add(Collection<? extends N> newElements);

	/**
	 * Checks whether or not the collection retained by this ExpandCollection is
	 * empty. If this method returns false, a call to pop() can be made.
	 * 
	 * @return
	 */
	public boolean isEmpty();

}
