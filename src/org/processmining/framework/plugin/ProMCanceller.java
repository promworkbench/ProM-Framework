package org.processmining.framework.plugin;

/**
 * Interface that can be used by visualizations to receive a notification from
 * the framework when they are removed. {@link #isCancelled()} will return true
 * in this case.
 * 
 * @author F. Mannhardt, S.J.J Leemans
 *
 */
public interface ProMCanceller {

	/**
	 * {@link ProMCanceller} that never returns true for {@link #isCancelled()}
	 */
	public final static ProMCanceller NEVER_CANCEL = new ProMCanceller() {

		public boolean isCancelled() {
			return false;
		}
	};

	boolean isCancelled();

}
