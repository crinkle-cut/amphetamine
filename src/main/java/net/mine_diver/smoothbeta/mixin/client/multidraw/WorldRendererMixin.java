package net.mine_diver.smoothbeta.mixin.client.multidraw;

import net.mine_diver.smoothbeta.client.render.*;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.OpenGlCapabilities;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.world.ChunkRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldRenderer.class)
abstract class WorldRendererMixin implements SmoothWorldRenderer {
        @Shadow
        private ChunkRenderer[] chunkRenderers;

        @Unique
        private VboPool smoothbeta_vboPool;

        @ModifyArg(
                        method = "<init>",
                        at = @At(
                                        value = "INVOKE",
                                        target = "Lnet/minecraft/client/util/GlAllocationUtils;generateDisplayLists(I)I",
                                        ordinal = 0),
                        index = 0)
        private int smoothbeta_rightSizeChunkDisplayLists(int original) {
                return 18 * 18 * 18 * 3;
        }

        @Redirect(
                        method = "<init>",
                        at = @At(
                                        value = "INVOKE",
                                        target = "Lnet/minecraft/client/render/OpenGlCapabilities;glArbOcclusionQuery()Z"))
        private boolean smoothbeta_disableOcclusionQueries(OpenGlCapabilities capabilities) {
                return false;
        }

        @Override
        @Unique
        public VboPool smoothbeta_getTerrainVboPool() {
                return smoothbeta_vboPool;
        }

        @Inject(method = "reload()V", at = @At("HEAD"))
        private void smoothbeta_resetVboPool(CallbackInfo ci) {
                if (smoothbeta_vboPool != null)
                        smoothbeta_vboPool.deleteGlBuffers();
                smoothbeta_vboPool = new VboPool(VertexFormats.POSITION_TEXTURE_COLOR);
                Shaders.getTerrainShader();
        }

        @Inject(method = "setWorld", at = @At("HEAD"))
        private void smoothbeta_releaseVboPoolOnUnload(net.minecraft.world.World world, CallbackInfo ci) {
                if (world == null && smoothbeta_vboPool != null) {
                        smoothbeta_vboPool.deleteGlBuffers();
                        smoothbeta_vboPool = null;
                }
        }

        @Redirect(method = "<init>", at = @At(value = "NEW", target = "()Lnet/minecraft/client/render/world/ChunkRenderer;"))
        private ChunkRenderer smoothbeta_injectRenderRegion() {
                return new RenderRegion((WorldRenderer) (Object) this);
        }

        @Inject(method = "renderChunks(IIID)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/world/ChunkRenderer;addGlList(I)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
        private void smoothbeta_addBufferToRegion(int j, int k, int d, double par4, CallbackInfoReturnable<Integer> cir,
                        int var6, LivingEntity var7, double var8, double var10, double var12, int var14, int var15,
                        ChunkBuilder var16, int var17) {
                ((RenderRegion) this.chunkRenderers[var17])
                                .addBuffer(((SmoothChunkRenderer) var16).smoothbeta_getBuffer(d));
        }

        @Redirect(method = "renderChunks(IIID)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/world/ChunkRenderer;addGlList(I)V"))
        private void smoothbeta_stopCallingRenderList(ChunkRenderer instance, int i) {
        }

        @Inject(method = "renderLastChunks(ID)V", at = @At("HEAD"))
        public void smoothbeta_beforeRenderRegion(int d, double par2, CallbackInfo ci) {
                smoothbeta_vboPool.setPageBatching(d == 0);
                Shader shader = Shaders.getTerrainShader();
                shader.fogMode.set(Shaders.getFogMode());
                shader.bind();
        }

        @Inject(method = "renderLastChunks(ID)V", at = @At("RETURN"))
        public void smoothbeta_afterRenderRegion(int d, double par2, CallbackInfo ci) {
                smoothbeta_vboPool.finishDrawing();
                Shaders.getTerrainShader().unbind();
        }
}
