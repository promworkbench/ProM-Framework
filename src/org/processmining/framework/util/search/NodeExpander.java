package org.processmining.framework.util.search;

import java.util.Collection;

import org.processmining.framework.plugin.Progress;

/**
 * This class is used by the MultiThreadedSearcher to handle callbacks. Each
 * time the searcher considers a node, it asks the registered expander to expand
 * the node. If a node cannot be expanded, then a call to processLeaf is made.
 * The expander should not process any leafs from within the expandNode method.
 * 
 * @author bfvdonge
 * 
 * @param <N>
 */
public interface NodeExpander<N> {

	/**
	 * This method is called by the searcher when a node in the search tree has
	 * to be considered for expansion. The node to expand is given as a
	 * parameter and the method should return a collection of nodes representing
	 * the children of this node. If the node turns out to be a leaf, an empty
	 * collection has to be returned.
	 * 
	 * @param toExpand
	 *            The node to expand.
	 * @param progress
	 *            The progress indicator provided to the searcher in which this
	 *            expander is registered. The expander may increment the
	 *            progress, but it should check for cancellation, especially
	 *            when doing long computations.
	 * @param unmodifiableResultCollection
	 *            the leafs of the search space that have been added to the
	 *            resultCollection by the processLeaf method. Note that any
	 *            access to this collection should be synchronized and this
	 *            collection should not be modified in this method. However the
	 *            provided collection is modifiable, as for synchronization
	 *            purposes, the actual resultCollection is provided.
	 * @return A collection of child nodes, or an empty collection if this node
	 *         turns out to be a leaf.
	 */
	public Collection<N> expandNode(N toExpand, Progress progress, Collection<N> unmodifiableResultCollection);

	/**
	 * This method is called by the searcher to which this expander is
	 * registered each time a leaf was encountered. The provided
	 * resultCollection was specified in the startSearch method of the searcher
	 * and can be used to store this result. A typical implementation is:
	 * 
	 * <code>
	 * synchronized(resultCollection) {
	 *     resultCollection.add(leaf);
	 * }
	 * </code>
	 * 
	 * However, more advanced implementations are possible.
	 * 
	 * Finally, note that calls to this method are not thread-safe, i.e. the
	 * implementing class should take care of synchronization on the
	 * resultCollection if necessary (as in the example above).
	 * 
	 * @param leaf
	 *            The node that was found to be a leaf by the expandNode method
	 *            (i.e. the expandNode method returned and empty collection)
	 * @param progress
	 *            The progress indicator provided to the searcher in which this
	 *            expander is registered. The expander may increment the
	 *            progress, but it should check for cancellation, especially
	 *            when doing long computations.
	 * @param resultCollection
	 *            The collection to which to add the leaf node. More advanced
	 *            computations are allowed here, i.e. the resultCollection can
	 *            be changed. Note however that this requires syncrhonization on
	 *            the collection, as calls to this method are not thread-safe.
	 */
	public void processLeaf(N leaf, Progress progress, Collection<N> resultCollection);

}
