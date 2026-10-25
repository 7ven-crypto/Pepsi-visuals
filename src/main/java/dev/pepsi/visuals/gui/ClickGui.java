package dev.pepsi.visuals.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import dev.pepsi.visuals.Config;
import dev.pepsi.visuals.module.Module;
import dev.pepsi.visuals.module.Modules;
import dev.pepsi.visuals.module.Setting;
import dev.pepsi.visuals.util.R;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class ClickGui extends Screen {
	private static final float PANEL_W = 124, HEADER_H = 20, ROW_H = 18, SET_H = 15, GAP = 10;

	private float anim;
	private boolean closing;
	private boolean lastL, lastR, lastShift;
	private Setting dragging;
	private float dragX, dragW;

	public ClickGui() {
		super(Text.literal("Pepsi Visuals"));
		this.lastShift = GLFW.glfwGetKey(R.mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
		this.lastL = true;
		this.lastR = true;
		R.play(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.3f);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void removed() {
		Config.save();
	}

	private void beginClose() {
		if (closing) return;
		closing = true;
		R.play(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f);
	}

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		anim = R.approach(anim, closing ? 0f : 1f, 16);
		if (closing && anim < 0.03f) {
			R.mc.setScreen(null);
			return;
		}

		long win = R.mc.getWindow().getHandle();
		boolean lmb = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean rmb = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
		boolean lp = lmb && !lastL;
		boolean rp = rmb && !lastR;
		if (shift && !lastShift) beginClose();
		lastL = lmb;
		lastR = rmb;
		lastShift = shift;
		if (!lmb) dragging = null;

		boolean live = !closing && anim > 0.9f;
		float a = R.ease(anim);
		int sw = ctx.getScaledWindowWidth();
		int sh = ctx.getScaledWindowHeight();

		if (dragging != null && lmb) {
			float ratio = MathHelper.clamp((mx - dragX) / dragW, 0f, 1f);
			float v = dragging.min + ratio * (dragging.max - dragging.min);
			if (dragging.step > 0) v = Math.round(v / dragging.step) * dragging.step;
			dragging.value = MathHelper.clamp(v, dragging.min, dragging.max);
		}

		ctx.fill(0, 0, sw, sh, R.alpha(0xFF05060C, 0.55f * a));

		var m = ctx.getMatrices();
		float cx = sw / 2f, cy = sh / 2f, s = 0.88f + 0.12f * a;
		m.pushMatrix();
		m.translate(cx, cy);
		m.scale(s, s);
		m.translate(-cx, -cy);

		String title = "PEPSI VISUALS";
		R.text(ctx, title, (sw - R.width(title)) / 2f, 16, R.alpha(Modules.THEME.color(0f), a));
		String hint = "LMB toggle  |  RMB settings  |  Right Shift close";
		R.text(ctx, hint, (sw - R.width(hint)) / 2f, 28, R.alpha(0xFF9A9AB0, a));

		int n = Modules.CATEGORIES.size();
		float total = n * PANEL_W + (n - 1) * GAP;
		float px = (sw - total) / 2f;

		for (int c = 0; c < n; c++) {
			String cat = Modules.CATEGORIES.get(c);
			List<Module> mods = new ArrayList<>();
			for (Module mod : Modules.ALL) if (mod.category.equals(cat)) mods.add(mod);

			float y = 44;
			int accent = Modules.THEME.color(c * 0.12f);
			R.rrect(ctx, px, y, PANEL_W, HEADER_H, R.alpha(0xF0141420, a));
			R.rect(ctx, px, y + HEADER_H - 1, PANEL_W, 1, R.alpha(accent, a));
			R.text(ctx, cat, px + (PANEL_W - R.width(cat)) / 2f, y + 6, R.alpha(0xFFFFFFFF, a));
			y += HEADER_H;

			for (Module mod : mods) {
				boolean hover = live && mx >= px && mx < px + PANEL_W && my >= y && my < y + ROW_H;
				mod.hoverAnim = R.approach(mod.hoverAnim, hover ? 1f : 0f, 14);
				mod.expandAnim = R.approach(mod.expandAnim, mod.expanded ? 1f : 0f, 14);

				if (hover && lp && mod.toggleable) {
					mod.toggle();
					R.play(SoundEvents.UI_BUTTON_CLICK, mod.enabled ? 1.4f : 1.0f);
				}
				if (hover && rp && !mod.settings.isEmpty()) {
					mod.expanded = !mod.expanded;
					R.play(SoundEvents.UI_BUTTON_CLICK, 0.8f);
				}

				float on = mod.toggleable ? mod.anim : 0f;
				int base = R.lerp(0xE00E0E16, 0xE01A1A28, mod.hoverAnim);
				int bg = R.lerp(base, accent, 0.30f * on);
				R.rect(ctx, px, y, PANEL_W, ROW_H, R.alpha(bg, a));
				if (on > 0.01f) R.rect(ctx, px, y, 2, ROW_H, R.alpha(accent, a * on));
				int tc = R.lerp(0xFFB8B8C8, 0xFFFFFFFF, Math.max(on, mod.hoverAnim * 0.6f));
				R.text(ctx, mod.name, px + 7, y + 5, R.alpha(tc, a));
				if (!mod.settings.isEmpty()) {
					String dots = mod.expanded ? "-" : "+";
					R.text(ctx, dots, px + PANEL_W - 11, y + 5, R.alpha(0xFF8A8AA0, a));
				}
				y += ROW_H;

				float setH = mod.settings.size() * SET_H * R.ease(mod.expandAnim);
				if (setH > 0.5f) {
					R.rect(ctx, px, y, PANEL_W, setH, R.alpha(0xD0080810, a));
					if (mod.expandAnim > 0.97f) {
						float sy = y;
						for (Setting st : mod.settings) {
							boolean sHover = live && mx >= px && mx < px + PANEL_W && my >= sy && my < sy + SET_H;
							if (st.isBool) {
								if (sHover && lp) {
									st.value = st.bool() ? 0 : 1;
									R.play(SoundEvents.UI_BUTTON_CLICK, st.bool() ? 1.4f : 1.0f);
								}
								R.text(ctx, st.name, px + 7, sy + 4, R.alpha(0xFFC8C8D8, a));
								float bx = px + PANEL_W - 15;
								R.rect(ctx, bx, sy + 3, 9, 9, R.alpha(0xFF22222E, a));
								if (st.bool()) R.rect(ctx, bx + 1, sy + 4, 7, 7, R.alpha(accent, a));
							} else {
								if (sHover && lp) {
									dragging = st;
									dragX = px;
									dragW = PANEL_W;
								}
								float ratio = (st.value - st.min) / (st.max - st.min);
								R.rect(ctx, px, sy, PANEL_W * ratio, SET_H, R.alpha(accent, a * 0.40f));
								R.text(ctx, st.name, px + 7, sy + 4, R.alpha(0xFFC8C8D8, a));
								String val = st.step >= 1 ? Integer.toString(Math.round(st.value))
									: String.format(Locale.ROOT, "%.1f", st.value);
								R.text(ctx, val, px + PANEL_W - 7 - R.width(val), sy + 4, R.alpha(0xFFFFFFFF, a));
							}
							sy += SET_H;
						}
					}
				}
				y += setH;
			}
			px += PANEL_W + GAP;
		}

		m.popMatrix();
	}
}
