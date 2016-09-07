package org.processmining.framework.plugin.events;

import java.util.EventListener;

import org.processmining.framework.plugin.PluginContextID;

public interface Logger extends EventListener {

	public enum MessageLevel {
		/**
		 * Normal message level, for information during runtime
		 */
		NORMAL(" ", "Normal"),
		/**
		 * Warning message level, for warning messages during runtime
		 */
		WARNING("W", "Warning"),
		/**
		 * Error message level, for error information during ruintime. Use in
		 * case error can be recovered from, i.e. not in case of exception
		 * handling.
		 */
		ERROR("E", "Error"),
		/**
		 * All TEST messages are omitted in the release version.
		 */
		TEST("T", "Test"),
		/**
		 * All DEBUG messages are omitted in the release version.
		 */
		DEBUG("D", "Debug");

		private final String shortName;
		private final String longName;

		MessageLevel(String shortName, String longName) {
			this.shortName = shortName;
			this.longName = longName;
		}

		public String getShortName() {
			return shortName;
		}

		public String getLongName() {
			return longName;
		}
	}

	public class ListenerList extends ProMEventListenerList<Logger> {

		public void fireLog(String message, PluginContextID contextID, MessageLevel messageLevel) {
			for (Logger listener : getListeners()) {
				listener.log(message, contextID, messageLevel);
			}
		}

		public void fireLog(Throwable t, PluginContextID contextID) {
			for (Logger listener : getListeners()) {
				listener.log(t, contextID);
			}
		}
	}

	public void log(String message, PluginContextID contextID, MessageLevel messageLevel);

	public void log(Throwable t, PluginContextID contextID);

}
