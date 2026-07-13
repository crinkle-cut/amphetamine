package net.mine_diver.smoothbeta.client.render;

import net.mine_diver.smoothbeta.client.render.gl.Program;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.mine_diver.smoothbeta.SmoothBeta.LOGGER;

public class Shaders {

    private static Shader terrainShader;
    private static boolean initialized = false;
    private static int fogMode;

    public static void init() {
        // Don't load shaders here - OpenGL context not available yet
        // Shaders will be loaded lazily on first access
    }

    private static void loadShaders() {
        if (initialized)
            return;
        initialized = true;

        // Clear old program cache
        List<Program> list = new ArrayList<>();
        list.addAll(Program.Type.FRAGMENT.getProgramCache().values());
        list.addAll(Program.Type.VERTEX.getProgramCache().values());
        list.forEach(Program::release);

        if (terrainShader != null) {
            terrainShader.close();
        }

        try {
            terrainShader = new Shader("terrain", VertexFormats.POSITION_TEXTURE_COLOR);
        } catch (IOException e) {
            LOGGER.error("Could not load terrain shader", e);
            throw new RuntimeException("Could not load terrain shader", e);
        }
    }

    public static Shader getTerrainShader() {
        if (!initialized) {
            loadShaders();
        }
        return terrainShader;
    }

    public static void setFogMode(int mode) {
        fogMode = switch (mode) {
            case GL11.GL_EXP -> 0;
            case GL11.GL_EXP2 -> 1;
            case GL11.GL_LINEAR -> 2;
            default -> throw new IllegalArgumentException("Unexpected fog mode: " + mode);
        };
    }

    public static int getFogMode() {
        return fogMode;
    }
}
