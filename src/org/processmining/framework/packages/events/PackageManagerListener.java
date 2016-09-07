package org.processmining.framework.packages.events;

import java.io.File;
import java.net.URL;
import java.util.EventListener;

import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.events.ProMEventListenerList;

public interface PackageManagerListener extends EventListener {

	public class ListenerList extends ProMEventListenerList<PackageManagerListener> {
		public void fireSessionStart() {
			for (PackageManagerListener listener : getListeners()) {
				listener.sessionStart();
			}
		}

		public void fireSessionComplete(boolean error) {
			for (PackageManagerListener listener : getListeners()) {
				listener.sessionComplete(error);
			}
		}

		public void fireStartDownload(String packageName, URL url, PackageDescriptor pack) {
			for (PackageManagerListener listener : getListeners()) {
				listener.startDownload(packageName, url, pack);
			}
		}

		public void fireStartInstall(String packageName, File folder, PackageDescriptor pack) {
			for (PackageManagerListener listener : getListeners()) {
				listener.startInstall(packageName, folder, pack);
			}
		}

		public void fireFinishedInstall(String packageName, File folder, PackageDescriptor pack) {
			for (PackageManagerListener listener : getListeners()) {
				listener.finishedInstall(packageName, folder, pack);
			}
		}

		public void fireException(String exception) {
			for (PackageManagerListener listener : getListeners()) {
				listener.exception(exception);
			}
		}

		public void fireException(Throwable t) {
			for (PackageManagerListener listener : getListeners()) {
				listener.exception(t);
			}
		}
	}

	public void sessionStart();

	public void exception(Throwable t);

	public void exception(String exception);

	public void startDownload(String packageName, URL url, PackageDescriptor pack);

	public void startInstall(String packageName, File folder, PackageDescriptor pack);

	public void finishedInstall(String packageName, File folder, PackageDescriptor pack);

	public void sessionComplete(boolean error);

}
