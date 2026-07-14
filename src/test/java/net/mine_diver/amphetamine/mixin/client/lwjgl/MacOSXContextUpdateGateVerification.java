package net.mine_diver.amphetamine.mixin.client.lwjgl;

import net.mine_diver.amphetamine.client.lwjgl.MacOSXContextUpdateGate;

public final class MacOSXContextUpdateGateVerification {
    private MacOSXContextUpdateGateVerification() {
    }

    public static void main(String[] args) {
        MacOSXContextUpdateGate gate = new MacOSXContextUpdateGate();
        long now = 1_000_000_000L;

        require(gate.shouldPollGeometry(now), "Initial geometry must be sampled");
        gate.recordGeometry(10, 20, 1280, 720);
        require(gate.shouldUpdate(now, false), "The first context update must run");
        require(!gate.shouldUpdate(now + 1L, false), "A static frame must skip the context update");

        long beforePoll = now + MacOSXContextUpdateGate.GEOMETRY_POLL_INTERVAL_NANOS - 1L;
        require(!gate.shouldPollGeometry(beforePoll), "Geometry polling must be rate limited");
        now += MacOSXContextUpdateGate.GEOMETRY_POLL_INTERVAL_NANOS;
        require(gate.shouldPollGeometry(now), "Geometry must be polled after the interval");
        gate.recordGeometry(11, 20, 1280, 720);
        require(gate.shouldUpdate(now, false), "Moving the window must update the context");

        gate.markDirty();
        require(gate.shouldUpdate(now + 1L, false), "Explicit drawable changes must update the context");
        require(gate.shouldUpdate(now + 2L, true), "Cursor changes must retain LWJGL cursor handling");

        long safetyUpdate = now + 2L + MacOSXContextUpdateGate.SAFETY_UPDATE_INTERVAL_NANOS;
        require(gate.shouldUpdate(safetyUpdate, false), "Unobserved display changes need a safety update");
        require(!gate.shouldUpdate(safetyUpdate + 1L, false), "The safety update must reset its interval");

        gate.reset();
        require(gate.shouldPollGeometry(safetyUpdate + 2L), "Reset must invalidate cached geometry");
        gate.recordGeometry(0, 0, 1920, 1080);
        require(gate.shouldUpdate(safetyUpdate + 2L, false), "Reset must require a context update");
    }

    private static void require(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
