package net.mine_diver.amphetamine.mixin.client.multidraw;

import net.mine_diver.amphetamine.client.render.SmoothChunkRenderer;
import net.mine_diver.amphetamine.client.render.SmoothTessellator;
import net.minecraft.client.render.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(Tessellator.class)
abstract class TessellatorMixin implements SmoothTessellator {
    @Shadow protected abstract void reset();

    @Shadow public abstract void draw();

    @Shadow private ByteBuffer byteBuffer;
    @Shadow private int[] buffer;
    @Shadow private int vertexCount;
    @Shadow private double u;
    @Shadow private double v;
    @Shadow private int color;
    @Shadow private boolean hasColor;
    @Shadow private boolean hasTexture;
    @Shadow private int bufferPosition;
    @Shadow private int addedVertexCount;
    @Shadow private double xOffset;
    @Shadow private double yOffset;
    @Shadow private double zOffset;
    @Shadow private boolean drawing;
    @Shadow private int bufferSize;
    @Unique
    private static final int amphetamine_TERRAIN_VERTEX_SIZE = 6;
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

    @Inject(
            method = "vertex(DDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void amphetamine_vertex(double x, double y, double z, CallbackInfo ci) {
        if (!amphetamine_renderingTerrain) return;

        ++addedVertexCount;
        if (hasTexture) {
            buffer[bufferPosition + 3] = Float.floatToRawIntBits((float) u);
            buffer[bufferPosition + 4] = Float.floatToRawIntBits((float) v);
        }
        if (hasColor)
            buffer[bufferPosition + 5] = color;
        buffer[bufferPosition] = Float.floatToRawIntBits((float) (x + xOffset));
        buffer[bufferPosition + 1] = Float.floatToRawIntBits((float) (y + yOffset));
        buffer[bufferPosition + 2] = Float.floatToRawIntBits((float) (z + zOffset));
        bufferPosition += amphetamine_TERRAIN_VERTEX_SIZE;
        ++vertexCount;

        if (vertexCount % 4 == 0 && bufferPosition >= bufferSize - amphetamine_TERRAIN_VERTEX_SIZE * 4) {
            draw();
            drawing = true;
        }
        ci.cancel();
    }
}
