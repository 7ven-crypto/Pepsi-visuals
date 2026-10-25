package dev.pepsi.visuals;

import dev.pepsi.visuals.module.Module;
import dev.pepsi.visuals.module.Modules;
import dev.pepsi.visuals.util.R;
import net.minecraft.client.gui.DrawContext;

public final class Hud {
	private Hud() {}

	public static void render(DrawContext ctx) {
		R.tickTime();
		if (R.mc.player == null) return;
		for (Module m : Modules.ALL) m.anim = R.approach(m.anim, m.enabled ? 1f : 0f, 12);
		if (R.mc.options.hudHidden) return;
		for (Module m : Modules.ALL) if (m.enabled) m.onHud(ctx);
	}
}
