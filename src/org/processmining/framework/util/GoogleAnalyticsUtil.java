package org.processmining.framework.util;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.GoogleAnalytics;

public class GoogleAnalyticsUtil {

//	private GoogleAnalytics ga = new GoogleAnalytics("UA-1999775-7"); // www.promtools.org
	private GoogleAnalytics ga = new GoogleAnalytics("UA-1999775-1"); // www.win.tue.nl/~hverbeek

	public void runPluginEvent(String pluginName, String packageName) {
		EventHit eh = new EventHit();
		eh.eventAction("Run ProM Plug-in");
		if (pluginName == null) {
			eh.eventLabel("<No plug-in name>");
		} else {
			eh.eventLabel(pluginName);
		}
		if (packageName == null) {
			eh.eventCategory("<No package name>");
		} else {
			eh.eventCategory(packageName);
		}
		
		ga.postAsync(eh);
//		ga.close();
		System.out.println("[GoogleAnalyticsUtil] " + eh);
	}
}
