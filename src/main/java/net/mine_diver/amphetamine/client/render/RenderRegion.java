package net.mine_diver.amphetamine.client.render;

import net.mine_diver.amphetamine.client.render.gl.GlUniform;
import net.mine_diver.amphetamine.client.render.gl.VertexBuffer;
import net.mine_diver.amphetamine.mixin.client.multidraw.RenderListAccessor;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.world.ChunkRenderer;

import java.util.ArrayList;
import java.util.List;

public class RenderRegion extends ChunkRenderer {

    private final RenderListAccessor _super = (RenderListAccessor) this;
    private final SmoothWorldRenderer stationWorldRenderer;
    private final List<VertexBuffer> buffers = new ArrayList<>();
    private int renderPass;

    public RenderRegion(WorldRenderer worldRenderer) {
        stationWorldRenderer = ((SmoothWorldRenderer) worldRenderer);
    }

    @Override
    public void init(int i, int j, int k, double d, double e, double f) {
        super.init(i, j, k, d, e, f);
        buffers.clear();
    }

    @Override
    public void addGlList(int i) {
        throw new UnsupportedOperationException("Call lists can't be added to VBO regions!");
    }

    public void addBuffer(VertexBuffer buffer, int renderPass) {
        buffers.add(buffer);
        this.renderPass = renderPass;
    }

    public void render() {
        if (!_super.amphetamine_getInitialized() || buffers.isEmpty())
            return;
        Shader shader = Shaders.getTerrainShader();
        GlUniform chunkOffset = shader.chunkOffset;
        chunkOffset.set(_super.amphetamine_getX() - _super.amphetamine_getOffsetX(),
                _super.amphetamine_getY() - _super.amphetamine_getOffsetY(),
                _super.amphetamine_getZ() - _super.amphetamine_getOffsetZ());
        chunkOffset.upload();
        for (VertexBuffer vertexBuffer : buffers)
            vertexBuffer.uploadToPool();
        stationWorldRenderer.amphetamine_getTerrainVboPool(renderPass).drawAll();
    }
}
