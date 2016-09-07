package org.processmining.framework.plugin.annotations;

public enum PluginLevel {
	Local( //
			"Local", //
			1), //
	NightlyBuild( //
			"NightlyBuild", //
			2), //
	Regular( //
			"Regular", //
			3), //
	PeerReviewed( //
			"PeerReviewed", //
			4), //
	BulletProof( //
			"BulletProof", //
			5);

	private final String name;
	private final int value;

	private PluginLevel(String name, int value) {
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
