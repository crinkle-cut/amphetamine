package net.mine_diver.amphetamine.client.lwjgl;

public final class MacOSXContextUpdateGate {
    public static final long GEOMETRY_POLL_INTERVAL_NANOS = 16_666_667L;
    public static final long SAFETY_UPDATE_INTERVAL_NANOS = 1_000_000_000L;

    private boolean contextDirty = true;
    private boolean contextUpdated;
    private boolean geometryKnown;
    private boolean geometryPollStarted;
    private long lastGeometryPollNanos;
    private long lastContextUpdateNanos;
    private int x;
    private int y;
    private int width;
    private int height;

    public void reset() {
        contextDirty = true;
        contextUpdated = false;
        geometryKnown = false;
        geometryPollStarted = false;
    }

    public void markDirty() {
        contextDirty = true;
    }

    public boolean shouldPollGeometry(long now) {
        if (!geometryPollStarted) {
            geometryPollStarted = true;
            lastGeometryPollNanos = now;
            return true;
        }
        if (now - lastGeometryPollNanos < GEOMETRY_POLL_INTERVAL_NANOS)
            return false;
        lastGeometryPollNanos = now;
        return true;
    }

    public void recordGeometry(int x, int y, int width, int height) {
        if (geometryKnown && (this.x != x || this.y != y || this.width != width || this.height != height))
            contextDirty = true;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        geometryKnown = true;
    }

    public boolean shouldUpdate(long now, boolean cursorUpdateRequired) {
        if (!contextDirty
                && !cursorUpdateRequired
                && contextUpdated
                && now - lastContextUpdateNanos < SAFETY_UPDATE_INTERVAL_NANOS)
            return false;
        contextDirty = false;
        contextUpdated = true;
        lastContextUpdateNanos = now;
        return true;
    }
}
