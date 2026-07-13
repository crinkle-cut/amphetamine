package net.mine_diver.amphetamine.mixin.client.multidraw;

import net.mine_diver.amphetamine.client.render.SmoothChunkRenderer;
import net.mine_diver.amphetamine.client.render.SmoothTessellator;
import net.minecraft.client.render.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(Tessellator.class)
abstract class TessellatorMixin implements SmoothTessellator {
    @Shadow protected abstract void reset();

    @Shadow private ByteBuffer byteBuffer;
    @Unique
    private boolean amphetamine_renderingTerrain;
    @Unique
    private SmoothChunkRenderer amphetamine_chunkRenderer;

    @Override
    @Unique
    public void amphetamine_startRenderingTerrain(SmoothChunkRenderer chunkRenderer) {
        amphetamine_renderingTerrain = true;
        amphetamine_chunkRenderer = chunkRenderer;
    }

    @Override
    @Unique
    public void amphetamine_stopRenderingTerrain() {
        amphetamine_renderingTerrain = false;
        amphetamine_chunkRenderer = null;
    }

    @Override
    public boolean amphetamine_isRenderingTerrain() {
        return amphetamine_renderingTerrain;
    }

    @Inject(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/ByteBuffer;limit(I)Ljava/nio/Buffer;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void amphetamine_uploadTerrain(CallbackInfo ci) {
        if (!amphetamine_renderingTerrain) return;
        amphetamine_chunkRenderer.amphetamine_getCurrentBuffer().upload(byteBuffer);
        reset();
        ci.cancel();
    }

    @ModifyConstant(
            method = "vertex(DDD)V",
            constant = @Constant(intValue = 7)
    )
    private int amphetamine_prohibitExtraVertices(int constant) {
        return amphetamine_renderingTerrain ? -1 : constant;
    }

    @ModifyConstant(
            method = "vertex(DDD)V",
            constant = @Constant(
                    intValue = 8,
                    ordinal = 2
            )
    )
    private int amphetamine_compactVertices(int constant) {
        // Position (3 ints), UV (2 ints), and packed color (1 int).
        return amphetamine_renderingTerrain ? 6 : constant;
    }
}
