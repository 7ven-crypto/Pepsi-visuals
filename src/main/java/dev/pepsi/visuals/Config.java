package dev.pepsi.visuals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import dev.pepsi.visuals.module.Module;
import dev.pepsi.visuals.module.Modules;
import dev.pepsi.visuals.module.Setting;
import net.fabricmc.loader.api.FabricLoader;

public final class Config {
	private Config() {}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("pepsivisuals.properties");
	}

	private static String key(String s) {
		return s.replace(' ', '_');
	}

	public static void load() {
		Path f = file();
		if (!Files.exists(f)) return;
		Properties p = new Properties();
		try (var r = Files.newBufferedReader(f)) {
			p.load(r);
		} catch (IOException e) {
			return;
		}
		for (Module m : Modules.ALL) {
			String k = key(m.name);
			String v = p.getProperty(k);
			if (v != null) {
				m.enabled = Boolean.parseBoolean(v);
				m.anim = m.enabled ? 1f : 0f;
			}
			for (Setting s : m.settings) {
				String sv = p.getProperty(k + "." + key(s.name));
				if (sv == null) continue;
				try {
					s.value = Float.parseFloat(sv);
				} catch (NumberFormatException ignored) {
				}
			}
		}
	}

	public static void save() {
		Properties p = new Properties();
		for (Module m : Modules.ALL) {
			String k = key(m.name);
			p.setProperty(k, Boolean.toString(m.enabled));
			for (Setting s : m.settings) p.setProperty(k + "." + key(s.name), Float.toString(s.value));
		}
		try (var w = Files.newBufferedWriter(file())) {
			p.store(w, "Pepsi Visuals");
		} catch (IOException ignored) {
		}
	}
}
