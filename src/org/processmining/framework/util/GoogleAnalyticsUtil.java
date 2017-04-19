package org.processmining.framework.util;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.GoogleAnalytics;

public class GoogleAnalyticsUtil {

//	private GoogleAnalytics ga = new GoogleAnalytics("UA-1999775-7"); // www.promtools.org
	private GoogleAnalytics ga = new GoogleAnalytics("UA-1999775-1"); // www.win.tue.nl/~hverbeek

	public void runPluginEvent(String pluginName, String packageName) {
		EventHit eh = new EventHit();
		// Anonymize the IP. We're not that interested in it. 
		eh.anonymizeIp(true);
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
		
//		ga.post(eh);
		// Use https if possible. 
		ga.getConfig().setUseHttps(true);
		ga.postAsync(eh);
		/*
		 * if you uncomment the next line, make sure you're using post() and not postAsync() in the previous lines.
		 * Both post() and postAsync() may change the content of variable eh, which may cause problesm when writing
		 * out the value of this variable.
		 * 
		 */
//		System.out.println("[GoogleAnalyticsUtil] " + eh);
	}
}
