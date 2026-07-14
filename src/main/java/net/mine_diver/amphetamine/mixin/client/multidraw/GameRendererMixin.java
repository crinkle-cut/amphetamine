package net.mine_diver.amphetamine.mixin.client.multidraw;

import net.mine_diver.amphetamine.client.render.Shaders;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Unique
    private static final double amphetamine_WORLD_DEPTH_NEAR = 0.1;
    @Unique
    private static final double amphetamine_HAND_DEPTH_NEAR = 0.05;
    @Unique
    private static final double amphetamine_HAND_DEPTH_FAR = 0.099;
    @Unique
    private static final double amphetamine_HUD_DEPTH_FAR = 0.049;

    @Redirect(
            method = "applyFog(IF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glFogi(II)V",
                    remap = false
            )
    )
    private void amphetamine_trackFogMode(int pname, int param) {
        GL11.glFogi(pname, param);
        if (pname == GL11.GL_FOG_MODE)
            Shaders.setFogMode(param);
    }

    @Redirect(
            method = "renderFrame(FJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glClear(I)V",
                    ordinal = 0,
                    remap = false
            )
    )
    private void amphetamine_beginWorldDepthLayer(int mask) {
        GL11.glClear(mask);
        GL11.glDepthRange(amphetamine_WORLD_DEPTH_NEAR, 1.0);
    }

    @Redirect(
            method = "renderFrame(FJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glClear(I)V",
                    ordinal = 1,
                    remap = false
            )
    )
    private void amphetamine_beginHandDepthLayer(int mask) {
        if (mask != GL11.GL_DEPTH_BUFFER_BIT) {
            GL11.glClear(mask);
            return;
        }
        // Keep first-person geometry in front of world depth without ending the render pass for a clear.
        GL11.glDepthRange(amphetamine_HAND_DEPTH_NEAR, amphetamine_HAND_DEPTH_FAR);
    }

    @Redirect(
            method = "setupHudRender()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glClear(I)V",
                    remap = false
            )
    )
    private void amphetamine_beginHudDepthLayer(int mask) {
        if (mask != GL11.GL_DEPTH_BUFFER_BIT) {
            GL11.glClear(mask);
            return;
        }
        // HUD depth remains closer than the hand layer and is reset by the next frame's full clear.
        GL11.glDepthRange(0.0, amphetamine_HUD_DEPTH_FAR);
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("RETURN"))
    private void amphetamine_restoreDepthRange(float tickDelta, CallbackInfo ci) {
        GL11.glDepthRange(0.0, 1.0);
    }
}
