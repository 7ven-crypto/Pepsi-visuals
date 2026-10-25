package dev.pepsi.visuals.module;
 
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
 
import org.lwjgl.glfw.GLFW;
 
import dev.pepsi.visuals.util.R;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
 
public final class Modules {
	public static final Theme THEME = new Theme();
	public static final Watermark WATERMARK = new Watermark();
	public static final ArrayListHud ARRAYLIST = new ArrayListHud();
	public static final Info INFO = new Info();
	public static final Keystrokes KEYSTROKES = new Keystrokes();
	public static final TargetHud TARGET = new TargetHud();
	public static final ArmorHud ARMOR = new ArmorHud();
	public static final Esp ESP = new Esp();
	public static final Fullbright FULLBRIGHT = new Fullbright();
	public static final Zoom ZOOM = new Zoom();
	public static final Sprint SPRINT = new Sprint();
	public static final PotionHud POTION = new PotionHud();
	public static final Crosshair CROSSHAIR = new Crosshair();
	public static final HitSound HITSOUND = new HitSound();
	public static final HitParticles HITPARTICLES = new HitParticles();
	public static final AutoRespawn RESPAWN = new AutoRespawn();
	public static final Step STEP = new Step();
	public static final Flight FLIGHT = new Flight();
	public static final AutoTotem TOTEM = new AutoTotem();
	public static final AimAssist AIM = new AimAssist();
 
	public static final List<Module> ALL = List.of(
		WATERMARK, ARRAYLIST, INFO, KEYSTROKES, TARGET, ARMOR,
		POTION, CROSSHAIR, ESP, FULLBRIGHT, ZOOM,
		HITSOUND, HITPARTICLES, AIM, TOTEM, RESPAWN,
		SPRINT, STEP, FLIGHT, THEME
	);
 
	public static final List<String> CATEGORIES = List.of("Visuals", "Combat", "Movement", "Client");
 
	private Modules() {}
 
	public static void init() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (world.isClient() && entity instanceof LivingEntity le) {
				HITSOUND.attack(le);
				HITPARTICLES.attack(le);
			}
			return ActionResult.PASS;
		});
	}
 
	private static boolean down(int key) {
		return GLFW.glfwGetKey(R.mc.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
	}
 
	private static boolean mouseDown(int button) {
		return GLFW.glfwGetMouseButton(R.mc.getWindow().getHandle(), button) == GLFW.GLFW_PRESS;
	}
 
	// ------------------------------------------------------------------ Client
 
	public static final class Theme extends Module {
		public final Setting rainbow = add(new Setting("Rainbow", true));
		public final Setting hue = add(new Setting("Hue", 0, 360, 200, 1));
		public final Setting speed = add(new Setting("Speed", 1, 10, 3, 1));
 
		Theme() {
			super("Theme", "Client", true);
			toggleable = false;
			hidden = true;
		}
 
		public int color(float offset) {
			float h;
			if (rainbow.bool()) {
				h = ((System.currentTimeMillis() % 20000L) / 20000f) * speed.value + offset;
			} else {
				h = hue.value / 360f;
			}
			h = h - (float) Math.floor(h);
			return 0xFF000000 | (MathHelper.hsvToRgb(h, 0.55f, 1f) & 0xFFFFFF);
		}
	}
 
	// ----------------------------------------------------------------- Visuals
 
	public static final class Watermark extends Module {
		Watermark() {
			super("Watermark", "Visuals", true);
			hidden = true;
		}
 
		@Override
		public void onHud(DrawContext ctx) {
			String text = "Pepsi Visuals";
			float x = 6, y = 6;
			float w = R.width(text) + 12;
			R.rrect(ctx, x, y, w, 15, 0xC00C0C14);
			R.rect(ctx, x + 1, y, w - 2, 1, THEME.color(0f));
			float cx = x + 6;
			for (int i = 0; i < text.length(); i++) {
				String ch = String.valueOf(text.charAt(i));
				R.text(ctx, ch, cx, y + 4, THEME.color(i * 0.04f));
				cx += R.width(ch);
			}
		}
	}
 
	public static final class ArrayListHud extends Module {
		ArrayListHud() {
			super("ArrayList", "Visuals", true);
			hidden = true;
		}
 
		@Override
		public void onHud(DrawContext ctx) {
			List<Module> list = new ArrayList<>();
			for (Module m : ALL) {
				if (!m.hidden && m.anim > 0.01f) list.add(m);
			}
			list.sort(Comparator.comparingInt((Module m) -> -R.width(m.name)));
 
			int sw = ctx.getScaledWindowWidth();
			float y = 5;
			int i = 0;
			for (Module m : list) {
				float a = R.ease(m.anim);
				float w = R.width(m.name) + 9;
				float x = sw - 4 - w + (1f - a) * (w + 6);
				int col = THEME.color(i * 0.06f);
				R.rect(ctx, x, y, w, 11, R.alpha(0xB00C0C14, a));
				R.rect(ctx, x + w - 2, y, 2, 11, R.alpha(col, a));
				R.text(ctx, m.name, x + 3, y + 2, R.alpha(col, a));
				y += 11 * a;
				i++;
			}
		}
	}
 
	public static final class Info extends Module {
		final Setting fps = add(new Setting("FPS", true));
		final Setting ping = add(new Setting("Ping", true));
		final Setting cps = add(new Setting("CPS", true));
		final Setting coords = add(new Setting("Coords", true));
		final Setting speed = add(new Setting("Speed", true));
		final Setting server = add(new Setting("Server", false));
 
		private double px, pz, bps;
 
		private final Deque<Long> clicks = new ArrayDeque<>();
		private boolean lastDown;
 
		Info() {
			super("Info", "Visuals", true);
		}
 
		@Override
		public void onTick() {
			if (!enabled || mc.player == null) return;
			double dx = mc.player.getX() - px, dz = mc.player.getZ() - pz;
			bps = bps * 0.8 + Math.hypot(dx, dz) * 20 * 0.2;
			px = mc.player.getX();
			pz = mc.player.getZ();
		}
 
		@Override
		public void onHud(DrawContext ctx) {
			long now = System.currentTimeMillis();
			boolean d = mc.currentScreen == null && mouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT);
			if (d && !lastDown) clicks.addLast(now);
			lastDown = d;
			while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) clicks.pollFirst();
 
			List<String> lines = new ArrayList<>();
			if (fps.bool()) lines.add("FPS " + mc.getCurrentFps());
			if (ping.bool() && mc.getNetworkHandler() != null && mc.player != null) {
				PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
				if (e != null) lines.add("Ping " + e.getLatency() + "ms");
			}
			if (cps.bool()) lines.add("CPS " + clicks.size());
			if (coords.bool() && mc.player != null) {
				lines.add(String.format(Locale.ROOT, "XYZ %.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
			}
 
			if (speed.bool()) lines.add(String.format(Locale.ROOT, "%.1f b/s", bps));
			if (server.bool()) lines.add(mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer");
 
			float y = 26;
			int i = 0;
			for (String s : lines) {
				float w = R.width(s) + 9;
				int col = THEME.color(0.1f + i * 0.05f);
				R.rect(ctx, 6, y, w, 11, 0xB00C0C14);
				R.rect(ctx, 6, y, 2, 11, col);
				R.text(ctx, s, 11, y + 2, 0xFFFFFFFF);
				y += 12;
				i++;
			}
		}
	}
 
	public static final class Keystrokes extends Module {
		private final float[] anims = new float[6];
 
		Keystrokes() {
			super("Keystrokes", "Visuals", true);
		}
 
		private void key(DrawContext ctx, int idx, String label, boolean pressed, float x, float y, float w) {
			anims[idx] = R.approach(anims[idx], pressed ? 1f : 0f, 18);
			int bg = R.lerp(0xA00C0C14, R.alpha(THEME.color(idx * 0.08f), 0.75f), anims[idx]);
			R.rrect(ctx, x, y, w, 22, bg);
			R.text(ctx, label, x + (w - R.width(label)) / 2f, y + 7, R.lerp(0xFFC8C8D8, 0xFF10101A, anims[idx]));
		}
 
		@Override
		public void onHud(DrawContext ctx) {
			if (mc.player == null) return;
			float x0 = 8;
			float y0 = ctx.getScaledWindowHeight() / 2f - 30;
			key(ctx, 0, "W", mc.options.forwardKey.isPressed(), x0 + 24, y0, 22);
			key(ctx, 1, "A", mc.options.leftKey.isPressed(), x0, y0 + 24, 22);
			key(ctx, 2, "S", mc.options.backKey.isPressed(), x0 + 24, y0 + 24, 22);
			key(ctx, 3, "D", mc.options.rightKey.isPressed(), x0 + 48, y0 + 24, 22);
			boolean free = mc.currentScreen == null;
			key(ctx, 4, "LMB", free && mouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT), x0, y0 + 48, 34);
			key(ctx, 5, "RMB", free && mouseDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT), x0 + 36, y0 + 48, 34);
		}
	}
 
	public static final class TargetHud extends Module {
		final Setting crosshair = add(new Setting("Crosshair", true));
 
		private LivingEntity target;
		private long lastHit;
		private float alpha, hp, trail;
 
		TargetHud() {
			super("Target HUD", "Visuals", true);
		}
 
		public void set(LivingEntity e) {
			if (e != target) {
				hp = e.getHealth();
				trail = hp;
			}
			target = e;
			lastHit = System.currentTimeMillis();
		}
 
		@Override
		public void onTick() {
			if (!enabled || !crosshair.bool() || mc.player == null) return;
			if (mc.currentScreen == null && mc.targetedEntity instanceof LivingEntity le && le != mc.player) set(le);
		}
 
		@Override
		public void onHud(DrawContext ctx) {
			boolean show = target != null && target.isAlive() && System.currentTimeMillis() - lastHit < 4000;
			alpha = R.approach(alpha, show ? 1f : 0f, 10);
			if (alpha < 0.02f || target == null) return;
 
			float max = Math.max(1f, target.getMaxHealth());
			hp = R.approach(hp, target.getHealth(), 14);
			trail = R.approach(trail, hp, 3);
			float frac = MathHelper.clamp(hp / max, 0f, 1f);
			float trailFrac = MathHelper.clamp(trail / max, 0f, 1f);
 
			float x = ctx.getScaledWindowWidth() / 2f + 14;
			float y = ctx.getScaledWindowHeight() / 2f + 14;
			float w = 140, h = 42;
			float a = R.ease(alpha);
			int accent = THEME.color(0f);
 
			R.rrect(ctx, x - 1, y - 1, w + 2, h + 2, R.alpha(0x40000000, a));
			R.rrect(ctx, x, y, w, h, R.alpha(0xD00C0C14, a));
			R.rect(ctx, x, y + 1, 2, h - 2, R.alpha(accent, a));
 
			String name = target.getName().getString();
			R.rrect(ctx, x + 7, y + 7, 28, 28, R.alpha(R.lerp(0xFF2A2A3C, accent, 0.35f), a));
			String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT);
			R.text(ctx, initial, x + 7 + (28 - R.width(initial)) / 2f, y + 17, R.alpha(0xFFFFFFFF, a));
 
			R.text(ctx, name, x + 41, y + 5, R.alpha(0xFFFFFFFF, a));
 
			float bx = x + 41, by = y + 17, bw = 92;
			R.rect(ctx, bx, by, bw, 5, R.alpha(0xFF1A1A26, a));
			R.rect(ctx, bx, by, bw * trailFrac, 5, R.alpha(0xFFFFFFFF, a * 0.55f));
			R.rect(ctx, bx, by, bw * frac, 5, R.alpha(R.lerp(0xFFE04A4A, 0xFF4AE08A, frac), a));
 
			String info = String.format(Locale.ROOT, "%.1f HP   %d AR   %.1fm",
				target.getHealth(), target.getArmor(), mc.player.distanceTo(target));
			var m = ctx.getMatrices();
			m.pushMatrix();
			m.translate(bx, y + 27);
			m.scale(0.8f, 0.8f);
			R.text(ctx, info, 0, 0, R.alpha(0xFFB8B8C8, a));
			m.popMatrix();
		}
	}
 
	public static final class ArmorHud extends Module {
		private static final EquipmentSlot[] SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
		};
 
		ArmorHud() {
			super("Armor HUD", "Visuals", true);
		}
 
		@Override
		public void onHud(DrawContext ctx) {
			if (mc.player == null) return;
			float x = ctx.getScaledWindowWidth() - 24;
			float y = ctx.getScaledWindowHeight() / 2f - 38;
			for (EquipmentSlot slot : SLOTS) {
				ItemStack st = mc.player.getEquippedStack(slot);
				R.rrect(ctx, x - 1, y - 1, 18, 18, 0x900C0C14);
				if (!st.isEmpty()) {
					ctx.drawItem(st, Math.round(x), Math.round(y));
					ctx.drawStackOverlay(mc.textRenderer, st, Math.round(x), Math.round(y));
				}
				y += 20;
			}
		}
	}
 
	// ------------------------------------------------------------------ Utility
 
	public static final class Esp extends Module {
		Esp() {
			super("ESP", "Visuals", false);
		}
 
		@Override
		public void onTick() {
			if (!enabled || mc.world == null) return;
			for (PlayerEntity p : mc.world.getPlayers()) {
				if (p != mc.player && !p.isGlowing()) p.setGlowing(true);
			}
		}
 
		@Override
		public void onDisable() {
			if (mc.world == null) return;
			for (PlayerEntity p : mc.world.getPlayers()) {
				if (p != mc.player) p.setGlowing(false);
			}
		}
	}
 
	public static final class Fullbright extends Module {
		Fullbright() {
			super("Fullbright", "Visuals", false);
		}
 
		@Override
		public void onTick() {
			if (!enabled || mc.player == null) return;
			StatusEffectInstance cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
			if (cur == null || cur.getDuration() < 240) {
				mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
			}
		}
 
		@Override
		public void onDisable() {
			if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
		}
	}
 
	public static final class Zoom extends Module {
		final Setting fov = add(new Setting("FOV", 30, 70, 30, 1));
		private Integer saved;
 
		Zoom() {
			super("Zoom", "Visuals", false);
		}
 
		private void restore() {
			if (saved != null) {
				mc.options.getFov().setValue(saved);
				saved = null;
			}
		}
 
		@Override
		public void onTick() {
			if (!enabled || mc.player == null) return;
			if (mc.currentScreen == null && down(GLFW.GLFW_KEY_C)) {
				if (saved == null) saved = mc.options.getFov().getValue();
				mc.options.getFov().setValue((int) fov.value);
			} else {
				restore();
			}
		}
 
		@Override
		public void onDisable() {
			restore();
		}
	}
 
	public static final class Sprint extends Module {
		Sprint() {
			super("Auto Sprint", "Movement", false);
		}
 
		@Override
		public void onTick() {
			if (!enabled || mc.player == null || mc.currentScreen != null) return;
			if (mc.options.forwardKey.isPressed()) mc.options.sprintKey.setPressed(true);
		}
 
		@Override
		public void onDisable() {
			mc.options.sprintKey.setPressed(false);
		}
	}
 
	// ------------------------------------------------------------ More visuals
 
	public static final class PotionHud extends Module {
		PotionHud() {
			super("Potion HUD", "Visuals", true);
		}
 
		@Override
		public void onHud(DrawContext ctx) {
			if (mc.player == null) return;
			float y = 90;
			int i = 0;
			for (StatusEffectInstance e : mc.player.getStatusEffects()) {
				String name = e.getEffectType().value().getName().getString();
				String time = e.isInfinite() ? "**" : String.format(Locale.ROOT, "%d:%02d", e.getDuration() / 1200, (e.getDuration() / 20) % 60);
				String s = name + " " + (e.getAmplifier() + 1) + "  " + time;
				float w = R.width(s) + 9;
				int col = THEME.color(0.5f + i * 0.05f);
				R.rect(ctx, 6, y, w, 11, 0xB00C0C14);
				R.rect(ctx, 6, y, 2, 11, col);
				R.text(ctx, s, 11, y + 2, 0xFFFFFFFF);
				y += 12;
				i++;
			}
		}
	}
 
	public static final class Crosshair extends Module {
		final Setting gap = add(new Setting("Gap", 6, 20, 9, 1));
		final Setting length = add(new Setting("Length", 2, 10, 4, 1));
		final Setting dynamic = add(new Setting("Dynamic", true));
		private float spread;
 
		Crosshair() {
			super("Crosshair", "Visuals", false);
		}
 
		@Override
		public void onHud(DrawContext ctx) {
			if (mc.player == null || mc.currentScreen != null) return;
			boolean moving = mc.player.getVelocity().horizontalLengthSquared() > 0.001;
			float target = dynamic.bool() ? (moving ? 3f : 0f) + (mc.player.isSprinting() ? 2f : 0f) : 0f;
			spread = R.approach(spread, target, 12);
			float cx = ctx.getScaledWindowWidth() / 2f, cy = ctx.getScaledWindowHeight() / 2f;
			float g = gap.value + spread, l = length.value;
			int col = THEME.color(0.3f);
			R.rect(ctx, cx, cy - g - l, 1, l, col);
			R.rect(ctx, cx, cy + g, 1, l, col);
			R.rect(ctx, cx - g - l, cy, l, 1, col);
			R.rect(ctx, cx + g, cy, l, 1, col);
		}
	}
 
	public static final class HitSound extends Module {
		final Setting pitch = add(new Setting("Pitch", 0.5f, 2f, 1.2f, 0.1f));
 
		HitSound() {
			super("Hit Sound", "Visuals", false);
		}
 
		public void attack(LivingEntity target) {
			if (enabled) R.play(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, pitch.value);
		}
	}
 
	public static final class HitParticles extends Module {
		final Setting count = add(new Setting("Count", 4, 30, 12, 1));
 
		HitParticles() {
			super("Hit Particles", "Visuals", false);
		}
 
		public void attack(LivingEntity t) {
			if (!enabled) return;
			for (int i = 0; i < (int) count.value; i++) {
				double x = t.getX() + (Math.random() - 0.5) * t.getWidth();
				double y = t.getY() + Math.random() * t.getHeight();
				double z = t.getZ() + (Math.random() - 0.5) * t.getWidth();
				mc.particleManager.addParticle(ParticleTypes.CRIT, x, y, z,
					(Math.random() - 0.5) * 0.4, Math.random() * 0.3, (Math.random() - 0.5) * 0.4);
			}
		}
	}
 
	// ------------------------------------------------------------------ Combat
 
	public static final class AutoRespawn extends Module {
		AutoRespawn() {
			super("Auto Respawn", "Combat", false);
		}
 
		@Override
		public void onTick() {
			if (enabled && mc.player != null && mc.currentScreen instanceof DeathScreen) mc.player.requestRespawn();
		}
	}
 
	public static final class AutoTotem extends Module {
		final Setting delay = add(new Setting("Delay", 0, 10, 2, 1));
		private int timer;
 
		AutoTotem() {
			super("Auto Totem", "Combat", false);
		}
 
		@Override
		public void onTick() {
			if (!enabled || mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
			if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
				timer = 0;
				return;
			}
			if (timer++ < (int) delay.value) return;
			var handler = mc.player.playerScreenHandler;
			for (int i = 9; i <= 44; i++) {
				if (handler.slots.get(i).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
					mc.interactionManager.clickSlot(handler.syncId, i, 40, SlotActionType.SWAP, mc.player);
					timer = 0;
					return;
				}
			}
		}
	}
 
	public static final class AimAssist extends Module {
		final Setting range = add(new Setting("Range", 2, 6, 4.5f, 0.5f));
		final Setting fov = add(new Setting("FOV", 10, 180, 60, 5));
		final Setting speed = add(new Setting("Speed", 1, 20, 6, 1));
 
		AimAssist() {
			super("Aim Assist", "Combat", false);
		}
 
		@Override
		public void onTick() {
			if (!enabled || mc.player == null || mc.world == null || mc.currentScreen != null) return;
			if (!mouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) return;
 
			float bestYaw = 0, bestPitch = 0;
			double best = fov.value / 2.0;
			boolean found = false;
			Vec3d eye = mc.player.getEyePos();
			for (PlayerEntity p : mc.world.getPlayers()) {
				if (p == mc.player || !p.isAlive() || mc.player.distanceTo(p) > range.value) continue;
				Vec3d d = p.getBoundingBox().getCenter().subtract(eye);
				float yaw = (float) (MathHelper.atan2(d.z, d.x) * 180.0 / Math.PI) - 90f;
				float pitch = (float) -(MathHelper.atan2(d.y, Math.hypot(d.x, d.z)) * 180.0 / Math.PI);
				float dy = MathHelper.wrapDegrees(yaw - mc.player.getYaw());
				float dp = pitch - mc.player.getPitch();
				double ang = Math.hypot(dy, dp);
				if (ang < best) {
					best = ang;
					bestYaw = dy;
					bestPitch = dp;
					found = true;
				}
			}
			if (!found) return;
			float k = MathHelper.clamp(speed.value / 40f, 0.02f, 0.5f);
			mc.player.setYaw(mc.player.getYaw() + bestYaw * k);
			mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + bestPitch * k, -90f, 90f));
		}
	}
 
	// ---------------------------------------------------------------- Movement
 
	public static final class Step extends Module {
		final Setting height = add(new Setting("Height", 1f, 2.5f, 1f, 0.5f));
 
		Step() {
			super("Step", "Movement", false);
		}
 
		@Override
		public void onTick() {
			if (!enabled || mc.player == null) return;
			EntityAttributeInstance inst = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
			if (inst != null) inst.setBaseValue(height.value);
		}
 
		@Override
		public void onDisable() {
			if (mc.player == null) return;
			EntityAttributeInstance inst = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
			if (inst != null) inst.setBaseValue(0.6);
		}
	}
 
	public static final class Flight extends Module {
		final Setting speed = add(new Setting("Speed", 0.5f, 5f, 1f, 0.5f));
 
		Flight() {
			super("Flight", "Movement", false);
		}
 
		@Override
		public void onTick() {
			if (!enabled || mc.player == null) return;
			PlayerAbilities ab = mc.player.getAbilities();
			ab.allowFlying = true;
			ab.setFlySpeed(0.05f * speed.value);
		}
 
		@Override
		public void onDisable() {
			if (mc.player == null) return;
			PlayerAbilities ab = mc.player.getAbilities();
			ab.setFlySpeed(0.05f);
			if (!mc.player.isCreative() && !mc.player.isSpectator()) {
				ab.allowFlying = false;
				ab.flying = false;
			}
		}
	}
}
 
