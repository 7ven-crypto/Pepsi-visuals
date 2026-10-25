package dev.pepsi.visuals;

import org.lwjgl.glfw.GLFW;

import dev.pepsi.visuals.gui.ClickGui;
import dev.pepsi.visuals.module.Module;
import dev.pepsi.visuals.module.Modules;
import dev.pepsi.visuals.util.R;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public final class PepsiVisuals implements ClientModInitializer {
	public static final String ID = "pepsivisuals";

	private boolean lastShift;

	@Override
	public void onInitializeClient() {
		Modules.init();
		Config.load();

		HudElementRegistry.attachElementAfter(
			VanillaHudElements.MISC_OVERLAYS,
			Identifier.of(ID, "hud"),
			(ctx, tickCounter) -> Hud.render(ctx)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			for (Module m : Modules.ALL) m.onTick();

			// Open the menu with Right Shift when no screen is up.
			boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
			if (shift && !lastShift && client.currentScreen == null && client.player != null) {
				client.setScreen(new ClickGui());
			}
			lastShift = shift;
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (world.isClient() && entity instanceof LivingEntity le) Modules.TARGET.set(le);
			return ActionResult.PASS;
		});
	}
}
