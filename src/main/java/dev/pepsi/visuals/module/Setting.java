package dev.pepsi.visuals.module;

public final class Setting {
	public final String name;
	public final float min, max, step;
	public final boolean isBool;
	public float value;

	public Setting(String name, float min, float max, float value, float step) {
		this.name = name;
		this.min = min;
		this.max = max;
		this.value = value;
		this.step = step;
		this.isBool = false;
	}

	public Setting(String name, boolean value) {
		this.name = name;
		this.min = 0;
		this.max = 1;
		this.step = 1;
		this.value = value ? 1 : 0;
		this.isBool = true;
	}

	public boolean bool() {
		return value > 0.5f;
	}
}
