package net.mine_diver.amphetamine.mixin.client;

import net.mine_diver.amphetamine.client.render.HudRenderSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
abstract class HudRenderMixin {
    @Shadow
    private Minecraft client;

    @Unique
    private int amphetamine_hudDisplayList;
    @Unique
    private boolean amphetamine_hudDisplayListReady;
    @Unique
    private long amphetamine_nextHudFrameNanos;
    @Unique
    private int amphetamine_hudDisplayWidth = -1;
    @Unique
    private int amphetamine_hudDisplayHeight = -1;
    @Unique
    private int amphetamine_hudGuiScale = -1;
    @Unique
    private boolean amphetamine_hudScreenOpen;

    @Redirect(
            method = "onFrameUpdate(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;render(FZII)V"
            )
    )
    private void amphetamine_renderHudAtConfiguredFps(
            InGameHud hud,
            float tickDelta,
            boolean screenOpen,
            int mouseX,
            int mouseY
    ) {
        if (this.amphetamine_hudDisplayList == 0) {
            this.amphetamine_hudDisplayList = GL11.glGenLists(1);
            if (this.amphetamine_hudDisplayList == 0) {
                hud.render(tickDelta, screenOpen, mouseX, mouseY);
                return;
            }
        }

        long now = System.nanoTime();
        boolean renderShapeChanged = this.client.displayWidth != this.amphetamine_hudDisplayWidth
                || this.client.displayHeight != this.amphetamine_hudDisplayHeight
                || this.client.options.guiScale != this.amphetamine_hudGuiScale
                || screenOpen != this.amphetamine_hudScreenOpen;
        if (!this.amphetamine_hudDisplayListReady
                || renderShapeChanged
                || now - this.amphetamine_nextHudFrameNanos >= 0L) {
            GL11.glNewList(this.amphetamine_hudDisplayList, GL11.GL_COMPILE_AND_EXECUTE);
            try {
                hud.render(tickDelta, screenOpen, mouseX, mouseY);
            } finally {
                GL11.glEndList();
            }

            this.amphetamine_hudDisplayListReady = true;
            this.amphetamine_hudDisplayWidth = this.client.displayWidth;
            this.amphetamine_hudDisplayHeight = this.client.displayHeight;
            this.amphetamine_hudGuiScale = this.client.options.guiScale;
            this.amphetamine_hudScreenOpen = screenOpen;
            this.amphetamine_scheduleNextHudFrame(now, renderShapeChanged);
        } else {
            GL11.glCallList(this.amphetamine_hudDisplayList);
        }
    }

    @Unique
    private void amphetamine_scheduleNextHudFrame(long now, boolean resetSchedule) {
        if (!this.amphetamine_hudDisplayListReady
                || resetSchedule
                || now - this.amphetamine_nextHudFrameNanos >= HudRenderSettings.FRAME_INTERVAL_NANOS * 4L) {
            this.amphetamine_nextHudFrameNanos = now + HudRenderSettings.FRAME_INTERVAL_NANOS;
            return;
        }

        long elapsedIntervals = (now - this.amphetamine_nextHudFrameNanos)
                / HudRenderSettings.FRAME_INTERVAL_NANOS;
        this.amphetamine_nextHudFrameNanos += (elapsedIntervals + 1L) * HudRenderSettings.FRAME_INTERVAL_NANOS;
    }
}
