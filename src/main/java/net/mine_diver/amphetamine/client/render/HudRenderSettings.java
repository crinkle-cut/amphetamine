package net.mine_diver.amphetamine.client.render;

import net.mine_diver.amphetamine.Amphetamine;

public final class HudRenderSettings {
    public static final String FPS_PROPERTY = "amp.hudFps";
    public static final int DEFAULT_FPS = 60;
    public static final int FPS = readFps();
    public static final long FRAME_INTERVAL_NANOS = Math.max(1L, 1_000_000_000L / FPS);

    private HudRenderSettings() {
    }

    private static int readFps() {
        String value = System.getProperty(FPS_PROPERTY);
        if (value == null) {
            return DEFAULT_FPS;
        }

        try {
            int fps = Integer.parseInt(value);
            if (fps > 0) {
                return fps;
            }
        } catch (NumberFormatException ignored) {
        }

        Amphetamine.LOGGER.warn(
                "Invalid -D{}={} launch option; using the default HUD cap of {} FPS",
                FPS_PROPERTY,
                value,
                DEFAULT_FPS
        );
        return DEFAULT_FPS;
    }
}
