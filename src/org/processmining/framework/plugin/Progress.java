package org.processmining.framework.plugin;

/**
 * Interface for progress indicator
 * 
 * @author bfvdonge
 * 
 */
public interface Progress {

	void setMinimum(int value);

	void setMaximum(int value);

	void setValue(int value);

	void setCaption(String message);

	String getCaption();

	int getValue();

	void inc();

	void setIndeterminate(boolean makeIndeterminate);

	boolean isIndeterminate();

	int getMinimum();

	int getMaximum();

	boolean isCancelled();

	void cancel();
}
