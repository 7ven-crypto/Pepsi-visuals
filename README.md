# Pepsi Visuals (Fabric 1.21.11)

Right Shift opens the menu. LMB toggles a module, RMB expands its settings. Hold C for Zoom.

Modules: Watermark, ArrayList, Info (FPS/Ping/CPS/Coords), Keystrokes, Target HUD, Armor HUD,
ESP (glow), Fullbright, Zoom, Auto Sprint, Theme (rainbow / fixed hue).

## Build
1. Easiest wrapper setup: grab the template zip from https://fabricmc.net/develop (1.21.11),
   copy its `gradlew`, `gradlew.bat` and `gradle/` folder into this directory.
   (Or, with Gradle installed: `gradle wrapper`.)
2. `./gradlew build`  (Windows: `gradlew.bat build`)
3. Jar: `build/libs/pepsi-visuals-1.0.0.jar` (not the `-sources` one).
4. Drop it in `.minecraft/mods` with Fabric Loader + Fabric API.

Config is written to `.minecraft/config/pepsivisuals.properties`.
