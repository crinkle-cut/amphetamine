package net.mine_diver.amphetamine.client.render.gl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface GlShader {
	int getProgramRef();

	Program getVertexShader();

	Program getFragmentShader();

	void attachReferencedShaders();
}