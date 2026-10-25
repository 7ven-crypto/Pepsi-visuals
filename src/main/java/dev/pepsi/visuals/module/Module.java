package dev.pepsi.visuals.module;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class Module {
	protected static final MinecraftClient mc = MinecraftClient.getInstance();

	public final String name;
	public final String category;
	public final List<Setting> settings = new ArrayList<>();

	public boolean enabled;
	public boolean expanded;
	public boolean toggleable = true;
	public boolean hidden;

	// Animation state, driven by the HUD / GUI.
	public float anim, expandAnim, hoverAnim;

	protected Module(String name, String category, boolean defaultOn) {
		this.name = name;
		this.category = category;
		this.enabled = defaultOn;
		this.anim = defaultOn ? 1f : 0f;
	}

	protected Setting add(Setting s) {
		settings.add(s);
		return s;
	}

	public void setEnabled(boolean e) {
		if (e == enabled) return;
		enabled = e;
		if (e) onEnable(); else onDisable();
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public void onEnable() {}
	public void onDisable() {}
	public void onTick() {}
	public void onHud(DrawContext ctx) {}
}
