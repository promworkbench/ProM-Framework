package org.processmining.framework.plugin.annotations;

public enum PluginQuality {
	VeryPoor( //
			"VeryPoor", //
			0), //
	Poor( //
			"Poor", //
			1), //
	Fair( //
			"Fair", //
			2), //
	Good( //
			"Good", //
			3), //
	VeryGood( //
			"VeryGood", //
			4);

	private final String name;
	private final int value;

	private PluginQuality(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}
}
