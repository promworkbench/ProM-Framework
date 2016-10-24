package org.processmining.framework.plugin.impl;

import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;

public class ProgressBarImpl implements Progress {

	private int value = 0;
	private int min = 0;
	private int max = 1;
	private boolean indeterminate = true;
	private String message = "";

	private static final long serialVersionUID = -3950799546173352932L;
	private final PluginContext context;
	private boolean canceled = false;

	public ProgressBarImpl(PluginContext context) {
		this.context = context;
	}

	public void setCaption(String message) {
		this.message = message;
		context.getProgressEventListeners().fireProgressCaptionChanged(message);
	}

	public void inc() {
		context.getProgressEventListeners().fireProgressChanged(++value);
		// Thread.yield();
	}

	public void setMinimum(int value) {
		min = value;
		context.getProgressEventListeners().fireProgressBoundsChanged(min, max);
	}

	public void setMaximum(int value) {
		max = value;
		context.getProgressEventListeners().fireProgressBoundsChanged(min, max);
	}

	public void setValue(int value) {
		this.value = value;
		context.getProgressEventListeners().fireProgressChanged(value);
		// Thread.yield();
	}

	public int getValue() {
		return value;
	}

	public void setIndeterminate(boolean makeIndeterminate) {
		indeterminate = makeIndeterminate;
		context.getProgressEventListeners().fireProgressIndeterminateChanged(makeIndeterminate);
	}

	public String getCaption() {
		return message;
	}

	public boolean isIndeterminate() {
		return indeterminate;
	}

	public int getMaximum() {
		return max;
	}

	public int getMinimum() {
		return min;
	}

	public boolean isCancelled() {
		PluginExecutionResult results = context.getResult();
		// [HV] To be safe, check the following:
		if (results == null) {
			return false;
		}
		for (int i = 0; !canceled && (i < results.getSize()); i++) {
			try {
				Object o = results.getResult(i);
				if (o instanceof ProMFuture<?>) {
					return results.<ProMFuture<?>>getResult(i).isCancelled();
				}
			} catch (Exception e) {
				// cancel on fail;
				return true;
			}
		}
		return canceled;
	}

	public void cancel() {
		canceled = true;
		// BVD: Cancel all children too!
		List<PluginContext> children = new ArrayList<>(context.getChildContexts());
		for (PluginContext child : children) {
			if (child != null && child.getProgress() != null) {
				child.getProgress().cancel();
			}
		}
		// Now cancel ProMFutures
		PluginExecutionResult results = context.getResult();
		for (int i = 0; i < results.getSize(); i++) {
			Object o = results.getResult(i);
			if (o instanceof ProMFuture<?>) {
				results.<ProMFuture<?>>getResult(i).cancel(true);
			}
		}
	}

}
