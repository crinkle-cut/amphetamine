package net.mine_diver.amphetamine.client.render;

import net.mine_diver.amphetamine.client.render.gl.VertexBuffer;

public interface SmoothChunkRenderer {
    VertexBuffer amphetamine_getBuffer(int pass);

    VertexBuffer amphetamine_getCurrentBuffer();
}
