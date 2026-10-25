package dev.pepsi.visuals.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;

public final class R {
	public static final MinecraftClient mc = MinecraftClient.getInstance();

	public static float dt = 0.016f;
	private static long last = System.nanoTime();

	private R() {}

	/** Advance the shared frame delta (seconds). Called once per HUD frame. */
	public static void tickTime() {
		long now = System.nanoTime();
		dt = Math.min(0.1f, (now - last) / 1e9f);
		last = now;
	}

	/** Framerate-independent exponential approach. */
	public static float approach(float cur, float target, float speed) {
		return cur + (target - cur) * (1f - (float) Math.exp(-speed * dt));
	}

	public static float ease(float t) {
		float u = 1f - MathHelper.clamp(t, 0f, 1f);
		return 1f - u * u * u;
	}

	public static int alpha(int color, float a) {
		int al = (int) (((color >>> 24) & 0xFF) * MathHelper.clamp(a, 0f, 1f));
		return (al << 24) | (color & 0xFFFFFF);
	}

	public static int lerp(int a, int b, float t) {
		t = MathHelper.clamp(t, 0f, 1f);
		int al = (int) (((a >>> 24) & 0xFF) + (((b >>> 24) & 0xFF) - ((a >>> 24) & 0xFF)) * t);
		int r = (int) (((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
		int g = (int) (((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
		int bl = (int) ((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
		return (al << 24) | (r << 16) | (g << 8) | bl;
	}

	public static void rect(DrawContext c, float x, float y, float w, float h, int col) {
		c.fill(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h), col);
	}

	/** Rect with chamfered corners. */
	public static void rrect(DrawContext c, float x, float y, float w, float h, int col) {
		rect(c, x + 1, y, w - 2, h, col);
		rect(c, x, y + 1, w, h - 2, col);
	}

	public static void text(DrawContext c, String s, float x, float y, int col) {
		c.drawTextWithShadow(mc.textRenderer, s, Math.round(x), Math.round(y), col);
	}

	public static int width(String s) {
		return mc.textRenderer.getWidth(s);
	}

		public static void play(SoundEvent e, float pitch) {
		if (mc.player != null) mc.player.playSound(e, 1.0f, pitch);
	}

	public static void play(RegistryEntry<SoundEvent> e, float pitch) {
		play(e.value(), pitch);
	}
}
