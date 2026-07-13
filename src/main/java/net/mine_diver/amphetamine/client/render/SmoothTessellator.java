package net.mine_diver.amphetamine.client.render;

public interface SmoothTessellator {
    void amphetamine_startRenderingTerrain(SmoothChunkRenderer chunkRenderer);

    void amphetamine_stopRenderingTerrain();

    boolean amphetamine_isRenderingTerrain();

    void amphetamine_appendTerrainQuad(int[] vertexData);

}
