package org.processmining.framework.util.search;

import java.rmi.server.UID;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import javax.swing.SwingWorker;

import org.processmining.framework.plugin.Progress;

/**
 * This class implements a multi-threaded search algorithm.
 * 
 * Internally, each instance of this class keeps a stack of objects of type N,
 * representing the nodes in the tree. Each thread pops an element off the stack
 * and asks the registered NodeExpander<N> to expand it. If the node turns out
 * to be a leaf, the expander is asked to process the leaf.
 * 
 * The use of this class is as follows (in pseudo-code):
 * 
 * <code>
 * Collection<N> initialSearchNodes;
 * Collection<N> resultCollection;
 * NodeExpander<N> expander = new NodeExpander<N>();
 * MultiThreadedSearcher<N> searcher = new MultiThreadedSearcher<N>(expander, BREADTHFIRST);
 * searcher.addInitialNodes(initialSearchNodes);
 * 
 * searcher.startSearch(executor, progress, resultCollection); 
 * </code>
 * 
 * Note that if you use this code from a plugin, you can also use:
 * 
 * <code>
 * searcher.startSearch(context.getExecutor(), context.getProgress(), resultCollection);
 * </code>
 * 
 * The progress object given to the startSearch method is only used for
 * cancellation checks, i.e. progress is never incremented.
 * 
 * @author bfvdonge
 * 
 * @param <N>
 */
public class MultiThreadedSearcher<N> {

	/**
	 * Constant representing a DEPTH-FIRST search.
	 */
	public final static int DEPTHFIRST = 0;

	/**
	 * Constant representing a BREADTH-FIRST search.
	 */
	public final static int BREADTHFIRST = 1;

	private final NodeExpander<N> expander;
	private final int threads;
	private Map<MultiThreadedSearchWorker<N>, Boolean> waiting;
	private final ExpandCollection<N> stack;

	/**
	 * Instantiates a searcher. The searcher will use as many threads as
	 * specified. Furthermore, the given expander is used for the expansion of
	 * search nodes and the processing of leaf nodes.
	 * 
	 * The searchtype can be either DEPTHFIRST, or BREADTHFIRST. In the first
	 * case, new nodes produced by the expand method of the expander are added
	 * to the bottom of the stack, whereas in the latter case, the new nodes are
	 * pushed to the top of the stack.
	 * 
	 * @param numberOfThreads
	 *            specifies the number of threads to use. If unsure how to set
	 *            this value, use the other constructor without this parameter
	 * @param expander
	 *            The expander that will be used to expand each search node and
	 *            process the leafs of the search tree
	 * @param searchType
	 *            the type of search, either DEPTHFIRST or BREADTHFIRST
	 */
	public MultiThreadedSearcher(int numberOfThreads, NodeExpander<N> expander, int searchType) {
		this.threads = numberOfThreads;
		this.expander = expander;
		if (searchType == DEPTHFIRST) {
			this.stack = new DepthFirstExpandCollection<N>();
		} else if (searchType == BREADTHFIRST) {
			this.stack = new BreadthFirstExpandCollection<N>();
		} else {
			throw new IllegalArgumentException("Wrong search type specified.");
		}
	}

	/**
	 * Instantiates a searcher. The searcher will use as many threads as the
	 * virtual machine reports to have CPUs. Furthermore, the given expander is
	 * used for the expansion of search nodes and the processing of leaf nodes.
	 * 
	 * The searchtype can be either DEPTHFIRST, or BREADTHFIRST. In the first
	 * case, new nodes produced by the expand method of the expander are added
	 * to the bottom of the stack, whereas in the latter case, the new nodes are
	 * pushed to the top of the stack.
	 * 
	 * By default, as many threads are used as there are CPUs reported by
	 * <code>Runtime.getRuntime().availableProcessors()</code>
	 * 
	 * @param expander
	 *            The expander that will be used to expand each search node and
	 *            process the leafs of the search tree
	 * @param searchType
	 *            the type of search, either DEPTHFIRST or BREADTHFIRST
	 */
	public MultiThreadedSearcher(NodeExpander<N> expander, int searchType) {
		this(Runtime.getRuntime().availableProcessors(), expander, searchType);
	}

	/**
	 * Instantiates a searcher. The searcher will use as many threads as
	 * specified. Furthermore, the given expander is used for the expansion of
	 * search nodes and the processing of leaf nodes and the given
	 * expandCollection to store nodes that need to be expanded further.
	 * 
	 * @param numberOfThreads
	 *            specifies the number of threads to use. If unsure how to set
	 *            this value, use the other constructor without this parameter
	 * @param expander
	 *            The expander that will be used to expand each search node and
	 *            process the leafs of the search tree
	 * @param expandCollection
	 *            the collection to store nodes that need to be expanded
	 */
	public MultiThreadedSearcher(int numberOfThreads, NodeExpander<N> expander, ExpandCollection<N> expandCollection) {
		this.threads = numberOfThreads;
		this.expander = expander;
		this.stack = expandCollection;
	}

	/**
	 * Instantiates a searcher. The searcher will use as many threads as
	 * specified. Furthermore, the given expander is used for the expansion of
	 * search nodes and the processing of leaf nodes and the given
	 * expandCollection to store nodes that need to be expanded further.
	 * 
	 * By default, as many threads are used as there are CPUs reported by
	 * <code>Runtime.getRuntime().availableProcessors()</code>
	 * 
	 * @param expander
	 *            The expander that will be used to expand each search node and
	 *            process the leafs of the search tree
	 * @param expandCollection
	 *            the collection to store nodes that need to be expanded
	 */
	public MultiThreadedSearcher(NodeExpander<N> expander, ExpandCollection<N> expandCollection) {
		this(Runtime.getRuntime().availableProcessors(), expander, expandCollection);
	}

	/**
	 * Sets the initial nodes of the search tree. Note that you can provide an
	 * empty collection, in which case the search returns immediately, without
	 * calling any methods in the expander.
	 * 
	 * @param initialNodes
	 *            the collection of initial nodes.
	 */
	public void addInitialNodes(Collection<N> initialNodes) {
		synchronized (stack) {
			stack.add(initialNodes);
		}
	}

	/**
	 * Sets the initial nodes of the search tree. Note that you don't have to
	 * provide any nodes, in which case the search returns immediately, without
	 * calling any methods in the expander.
	 * 
	 * @param initialNodes
	 *            zero or more initial nodes.
	 */
	public void addInitialNodes(N... initialNodes) {
		synchronized (stack) {
			stack.add(Arrays.asList(initialNodes));
		}
	}

	/**
	 * A call to this method initiates the search. The calling thread is
	 * suspended until the search is completed, or the progress was canceled.
	 * The resultCollection given to this method is passes through to the
	 * processLeaf method of the NodeExpander, i.e. no changes to this
	 * collection are made by the searcher.
	 * 
	 * @param executor
	 *            The executor in which the searcher can schedule it's threads.
	 *            If called from a plugin, use context.getExector() to pass to
	 *            this method.
	 * @param progress
	 *            The progress which is polled for cancellation. Note that no
	 *            other changes are made to the progress. If changes are
	 *            necessary, this has to be handled by the NodeExpander. If
	 *            called from a plugin, use context.getProgress() to pass to
	 *            this method.
	 * @param resultCollection
	 *            The collection in which the final result is stored by the
	 *            processLeaf method of the node expander. Note that the
	 *            searcher does not change this collection in any way, nor does
	 *            it handle any necessary synchronization.
	 * @throws InterruptedException
	 *             If one of the threads was interupted;
	 * @throws ExecutionException
	 *             If one of the threads threw an exception;
	 */
	public void startSearch(final Executor executor, final Progress progress, final Collection<N> resultCollection)
			throws InterruptedException, ExecutionException {

		// Set the number of waiting threads to 0;
		waiting = new HashMap<MultiThreadedSearchWorker<N>, Boolean>();

		MultiThreadedSearchWorker<N> worker = null;

		// Syncrhonize on the object "waiting" to make sure that first all
		// workers are registered to this map, before any of them accesses it.
		synchronized (waiting) {
			for (int i = 0; i < threads; i++) {
				worker = new MultiThreadedSearchWorker<N>(this, progress, resultCollection);
				waiting.put(worker, false);
			}
		}

		for (MultiThreadedSearchWorker<N> w : waiting.keySet()) {
			executor.execute(w);
		}

		// Just synchronize on the last worker. It stops only when all workers are finished.
		worker.get();

	}

	NodeExpander<N> getExpander() {
		return expander;
	}

	ExpandCollection<N> getStack() {
		return stack;
	}

	boolean setWaiting(MultiThreadedSearchWorker<N> worker, Boolean state) {
		synchronized (waiting) {
			waiting.put(worker, state);
			return !waiting.containsValue(false);
		}
	}
}

class MultiThreadedSearchWorker<N> extends SwingWorker<Object, Void> {

	private final MultiThreadedSearcher<N> owner;
	private final Progress progress;

	private final Collection<N> resultCollection;

	private final ExpandCollection<N> stack;
	private final UID id = new UID();

	public MultiThreadedSearchWorker(MultiThreadedSearcher<N> owner, Progress progress, Collection<N> resultCollection) {
		this.owner = owner;
		this.progress = progress;
		this.resultCollection = resultCollection;
		this.stack = owner.getStack();
	}

	public int hashCode() {
		return id.hashCode();
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o instanceof MultiThreadedSearchWorker) {
			return id.equals(((MultiThreadedSearchWorker) o).id);
		}
		return false;
	}

	@Override
	protected Object doInBackground() throws Exception {
		while (!progress.isCancelled()) {
			// First get the node to expand
			N toExpand = getNodeToExpand(progress);
			if (toExpand == null) {
				// Cancellation occurred, or the search is finished.
				// stop execution
				synchronized (stack) {
					stack.notifyAll();
				}
				break /* while */;
			}

			// Ask the expander for new nodes. Note that this call does not 
			// synchronize on stack, hence multiple of these calls can be 
			// processed in parallel.
			Collection<N> expandFurther = owner.getExpander().expandNode(toExpand, progress, resultCollection);

			// Let the searcher process the newly found nodes. Note that this is
			// synchronized as it requires to change the stack.
			processNewNodes(toExpand, expandFurther, resultCollection, progress);
		}
		// all threads are done, so quit.
		return null;
	}

	private N getNodeToExpand(Progress progress) throws InterruptedException {
		synchronized (stack) {
			while (!progress.isCancelled()) {
				if (stack.isEmpty()) {
					// Notify the searcher that we enter the waiting state
					if (owner.setWaiting(this, true)) {
						// All threads are waiting for the stack and this is the last
						// that will start waiting. Hence, there is nothing to do anymore.
						// Stop the execution by returning null.
						return null;
					} else {
						// Let's sleep until stack is filled again.
						stack.wait();
						continue;
					}
				} else {
					// Notify the searcher that we leave the waiting state
					owner.setWaiting(this, false);
					// get the node to expand.
					N toExpand = stack.pop();
					stack.notifyAll();
					return toExpand;
				}
			}
		}
		// In case of a cancellation, throw an exception;
		owner.setWaiting(this, true);
		return null;
	}

	private void processNewNodes(N toExpand, Collection<N> expandFurther, final Collection<N> resultCollection,
			Progress progress) {
		synchronized (stack) {
			if (!expandFurther.isEmpty()) {
				// There are tuples that need to be expanded further.
				stack.add(expandFurther);
			} else {
				synchronized (resultCollection) {
					owner.getExpander().processLeaf(toExpand, progress, resultCollection);
				}
			}
			// notify threads waiting on stack
			stack.notifyAll();
		}

	}

}
