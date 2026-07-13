package net.mine_diver.amphetamine.mixin.client;

import net.mine_diver.amphetamine.client.render.SmoothTextRenderer;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntityRenderer.class)
abstract class SignBlockEntityRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/block/entity/SignBlockEntity;DDDF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER,
                    remap = false))
    private void amphetamine_beginTextBatch(SignBlockEntity sign, double x, double y, double z, float tickDelta,
                                           CallbackInfo ci) {
        ((SmoothTextRenderer) ((SignBlockEntityRenderer) (Object) this).getTextRenderer()).amphetamine_beginBatch();
    }

    @Inject(
            method = "render(Lnet/minecraft/block/entity/SignBlockEntity;DDDF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V",
                    ordinal = 1,
                    remap = false))
    private void amphetamine_endTextBatch(SignBlockEntity sign, double x, double y, double z, float tickDelta,
                                         CallbackInfo ci) {
        ((SmoothTextRenderer) ((SignBlockEntityRenderer) (Object) this).getTextRenderer()).amphetamine_endBatch();
    }
}
