package net.mine_diver.amphetamine.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
abstract class InGameHudMixin extends DrawContext {
    @Unique
    private int amphetamine_directTextureDraws;

    @Unique
    private boolean amphetamine_iconBatchActive;

    @Unique
    private boolean amphetamine_iconBatchClosed;

    @Inject(method = "render", at = @At("HEAD"))
    private void amphetamine_resetIconBatch(float tickDelta, boolean screenOpen, int mouseX, int mouseY,
                                            CallbackInfo ci) {
        this.amphetamine_directTextureDraws = 0;
        this.amphetamine_iconBatchActive = false;
        this.amphetamine_iconBatchClosed = false;
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(IIIIII)V"
            )
    )
    private void amphetamine_batchStatusIcons(InGameHud hud, int x, int y, int u, int v, int width, int height) {
        // The hotbar, selection box, and crosshair use different texture/blend state and stay independent.
        if (this.amphetamine_directTextureDraws++ < 3 || this.amphetamine_iconBatchClosed) {
            hud.drawTexture(x, y, u, v, width, height);
            return;
        }

        Tessellator tessellator = Tessellator.INSTANCE;
        if (!this.amphetamine_iconBatchActive) {
            tessellator.startQuads();
            this.amphetamine_iconBatchActive = true;
        }

        float atlasScale = 1.0F / 256.0F;
        tessellator.vertex(x, y + height, this.zOffset, u * atlasScale, (v + height) * atlasScale);
        tessellator.vertex(x + width, y + height, this.zOffset,
                (u + width) * atlasScale, (v + height) * atlasScale);
        tessellator.vertex(x + width, y, this.zOffset, (u + width) * atlasScale, v * atlasScale);
        tessellator.vertex(x, y, this.zOffset, u * atlasScale, v * atlasScale);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE,
                    remap = false
            )
    )
    private void amphetamine_flushStatusIcons(float tickDelta, boolean screenOpen, int mouseX, int mouseY,
                                               CallbackInfo ci) {
        if (this.amphetamine_iconBatchActive) {
            Tessellator.INSTANCE.draw();
            this.amphetamine_iconBatchActive = false;
        }
        this.amphetamine_iconBatchClosed = true;
    }
}
