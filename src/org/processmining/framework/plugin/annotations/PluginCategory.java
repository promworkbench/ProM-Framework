package org.processmining.framework.plugin.annotations;

/**
 * Plugin Category
 * 
 * @author mleemans
 * 
 *         Possible categories specifying the 'type' of functionality the plugin
 *         provides.
 */
public enum PluginCategory {
	Discovery( //
			"Discovery", //
			"Discovery plugin constructs a model from a given event log", //
			"action_filter_discovery_20x20.png"), //
	ConformanceChecking( //
			"Conformance Checking", //
			"Conformance checking plugin check conformance between a given a model and a given event log", //
			"action_filter_conformance_20x20.png"), //
	Enhancement( //
			"Enhancement", //
			"Enhancement plugin enhances a given model using a given log", //
			"action_filter_enhancement_20x20.png"), //
	Filtering( //
			"Filtering", //
			"Filtering plugin filters or clusters a given log", //
			"action_filter_filtering_20x20.png"), //
	Analytics( //
			"Analytics", //
			"Analytic plugin provides additional analysis for a given log", //
			"action_filter_analytics_20x20.png");

	private final String name;
	private final String description;
	private final String imageFilterFilename;

	private PluginCategory(String name, String description, String imageFilterFilename) {
		this.name = name;
		this.description = description;
		this.imageFilterFilename = imageFilterFilename;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getImageFilterFilename() {
		return imageFilterFilename;
	}

}
