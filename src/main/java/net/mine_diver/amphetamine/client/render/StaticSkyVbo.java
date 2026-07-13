package net.mine_diver.amphetamine.client.render;

import net.mine_diver.amphetamine.client.render.gl.GlStateManager;
import net.minecraft.client.util.GlAllocationUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.util.Random;

/**
 * Retains vanilla's light sky, stars, and dark sky in one position-only VBO.
 */
public final class StaticSkyVbo implements AutoCloseable {
    private static final int POSITION_COMPONENTS = 3;
    private static final int SKY_TILE_SIZE = 64;
    private static final int SKY_GRID_RADIUS = SKY_TILE_SIZE * (256 / SKY_TILE_SIZE + 2);
    private static final int SKY_GRID_TILES = SKY_GRID_RADIUS * 2 / SKY_TILE_SIZE + 1;
    private static final int SKY_PLANE_VERTEX_COUNT = SKY_GRID_TILES * SKY_GRID_TILES * 6;
    private static final int MAX_STAR_VERTEX_COUNT = 1500 * 6;
    private static final int MAX_VERTEX_COUNT = SKY_PLANE_VERTEX_COUNT * 2 + MAX_STAR_VERTEX_COUNT;

    private int vertexBufferId;
    private int lightSkyFirst;
    private int lightSkyCount;
    private int starsFirst;
    private int starsCount;
    private int darkSkyFirst;
    private int darkSkyCount;

    public StaticSkyVbo() {
        this.upload();
    }

    public void upload() {
        if (this.vertexBufferId != 0)
            return;

        FloatBuffer vertices = GlAllocationUtils.allocateFloatBuffer(MAX_VERTEX_COUNT * POSITION_COMPONENTS);

        this.lightSkyFirst = vertexCount(vertices);
        appendSkyPlane(vertices, 16.0F, false);
        this.lightSkyCount = vertexCount(vertices) - this.lightSkyFirst;

        this.starsFirst = vertexCount(vertices);
        appendStars(vertices);
        this.starsCount = vertexCount(vertices) - this.starsFirst;

        this.darkSkyFirst = vertexCount(vertices);
        appendSkyPlane(vertices, -16.0F, true);
        this.darkSkyCount = vertexCount(vertices) - this.darkSkyFirst;

        vertices.flip();
        this.vertexBufferId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBufferId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void drawLightSky() {
        this.draw(this.lightSkyFirst, this.lightSkyCount);
    }

    public void drawStars() {
        this.draw(this.starsFirst, this.starsCount);
    }

    public void drawDarkSky() {
        this.draw(this.darkSkyFirst, this.darkSkyCount);
    }

    private void draw(int first, int count) {
        if (this.vertexBufferId == 0)
            this.upload();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBufferId);
        GL11.glVertexPointer(POSITION_COMPONENTS, GL11.GL_FLOAT, 0, 0L);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, first, count);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void close() {
        if (this.vertexBufferId != 0) {
            GlStateManager._glDeleteBuffers(this.vertexBufferId);
            this.vertexBufferId = 0;
        }
    }

    private static void appendSkyPlane(FloatBuffer vertices, float y, boolean reverseWinding) {
        for (int x = -SKY_GRID_RADIUS; x <= SKY_GRID_RADIUS; x += SKY_TILE_SIZE) {
            for (int z = -SKY_GRID_RADIUS; z <= SKY_GRID_RADIUS; z += SKY_TILE_SIZE) {
                if (reverseWinding) {
                    vertex(vertices, x + SKY_TILE_SIZE, y, z);
                    vertex(vertices, x, y, z);
                    vertex(vertices, x, y, z + SKY_TILE_SIZE);
                    vertex(vertices, x + SKY_TILE_SIZE, y, z);
                    vertex(vertices, x, y, z + SKY_TILE_SIZE);
                    vertex(vertices, x + SKY_TILE_SIZE, y, z + SKY_TILE_SIZE);
                } else {
                    vertex(vertices, x, y, z);
                    vertex(vertices, x + SKY_TILE_SIZE, y, z);
                    vertex(vertices, x + SKY_TILE_SIZE, y, z + SKY_TILE_SIZE);
                    vertex(vertices, x, y, z);
                    vertex(vertices, x + SKY_TILE_SIZE, y, z + SKY_TILE_SIZE);
                    vertex(vertices, x, y, z + SKY_TILE_SIZE);
                }
            }
        }
    }

    private static void appendStars(FloatBuffer vertices) {
        Random random = new Random(10842L);
        double[] starX = new double[4];
        double[] starY = new double[4];
        double[] starZ = new double[4];
        for (int i = 0; i < 1500; ++i) {
            double x = (double) (random.nextFloat() * 2.0F - 1.0F);
            double y = (double) (random.nextFloat() * 2.0F - 1.0F);
            double z = (double) (random.nextFloat() * 2.0F - 1.0F);
            double size = (double) (0.25F + random.nextFloat() * 0.25F);
            double distance = x * x + y * y + z * z;
            if (distance >= 1.0 || distance <= 0.01)
                continue;

            distance = 1.0 / Math.sqrt(distance);
            x *= distance;
            y *= distance;
            z *= distance;
            double centerX = x * 100.0;
            double centerY = y * 100.0;
            double centerZ = z * 100.0;
            double longitude = Math.atan2(x, z);
            double sinLongitude = Math.sin(longitude);
            double cosLongitude = Math.cos(longitude);
            double latitude = Math.atan2(Math.sqrt(x * x + z * z), y);
            double sinLatitude = Math.sin(latitude);
            double cosLatitude = Math.cos(latitude);
            double rotation = random.nextDouble() * Math.PI * 2.0;
            double sinRotation = Math.sin(rotation);
            double cosRotation = Math.cos(rotation);

            for (int corner = 0; corner < 4; ++corner) {
                double zero = 0.0;
                double cornerX = (double) ((corner & 2) - 1) * size;
                double cornerY = (double) ((corner + 1 & 2) - 1) * size;
                double rotatedX = cornerX * cosRotation - cornerY * sinRotation;
                double rotatedY = cornerY * cosRotation + cornerX * sinRotation;
                double latitudeX = rotatedX * sinLatitude + zero * cosLatitude;
                double latitudeZ = zero * sinLatitude - rotatedX * cosLatitude;
                double worldX = latitudeZ * sinLongitude - rotatedY * cosLongitude;
                double worldZ = rotatedY * sinLongitude + latitudeZ * cosLongitude;
                starX[corner] = centerX + worldX;
                starY[corner] = centerY + latitudeX;
                starZ[corner] = centerZ + worldZ;
            }

            starVertex(vertices, starX, starY, starZ, 0);
            starVertex(vertices, starX, starY, starZ, 1);
            starVertex(vertices, starX, starY, starZ, 2);
            starVertex(vertices, starX, starY, starZ, 0);
            starVertex(vertices, starX, starY, starZ, 2);
            starVertex(vertices, starX, starY, starZ, 3);
        }
    }

    private static void starVertex(FloatBuffer vertices, double[] x, double[] y, double[] z, int corner) {
        vertex(vertices, x[corner], y[corner], z[corner]);
    }

    private static int vertexCount(FloatBuffer vertices) {
        return vertices.position() / POSITION_COMPONENTS;
    }

    private static void vertex(FloatBuffer vertices, double x, double y, double z) {
        vertices.put((float) x);
        vertices.put((float) y);
        vertices.put((float) z);
    }
}
