package org.processmining.framework.plugin.events;

import java.util.EventListener;

public interface ProgressEventListener extends EventListener {

	void changeProgressCaption(String newCaption);

	void changeProgress(int progress);

	void changeProgressBounds(int lowBo, int upBo);

	void changeProgressIndeterminate(boolean indeterminate);

	public class ListenerList extends ProMEventListenerList<ProgressEventListener> {
		public void fireProgressCaptionChanged(String newCaption) {
			for (ProgressEventListener listener : getListeners()) {
				listener.changeProgressCaption(newCaption);
			}
		}

		public void fireProgressChanged(int progress) {
			for (ProgressEventListener listener : getListeners()) {
				listener.changeProgress(progress);
			}
		}

		public void fireProgressBoundsChanged(int lowBo, int upBo) {
			for (ProgressEventListener listener : getListeners()) {
				listener.changeProgressBounds(lowBo, upBo);
			}
		}

		public void fireProgressIndeterminateChanged(boolean indeterminate) {
			for (ProgressEventListener listener : getListeners()) {
				listener.changeProgressIndeterminate(indeterminate);
			}
		}
	}
}