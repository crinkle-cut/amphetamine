package net.mine_diver.amphetamine.client.render;

import java.util.Random;

public final class FrustumIntersectionVerification {
    private static final int SINGLE_PLANE_CASES = 500_000;
    private static final int FULL_FRUSTUM_CASES = 100_000;

    private FrustumIntersectionVerification() {
    }

    public static void main(String[] args) {
        verifyBoundaries();

        Random random = new Random(0x5EEDC011L);
        float[][] frustum = new float[16][16];
        setAlwaysVisible(frustum);

        for (int i = 0; i < SINGLE_PLANE_CASES; ++i) {
            randomPlane(random, frustum[0]);
            verifyRandomBox(random, frustum, "single plane", i);
        }

        for (int i = 0; i < FULL_FRUSTUM_CASES; ++i) {
            for (int plane = 0; plane < 6; ++plane)
                randomPlane(random, frustum[plane]);
            verifyRandomBox(random, frustum, "full frustum", i);
        }

        System.out.println("Verified " + (SINGLE_PLANE_CASES + FULL_FRUSTUM_CASES)
                + " optimized frustum intersections against the vanilla algorithm.");
    }

    private static void verifyBoundaries() {
        float[][] frustum = new float[16][16];
        setAlwaysVisible(frustum);

        frustum[0][0] = 1.0F;
        frustum[0][3] = 0.0F;
        verify(frustum, -1.0, -1.0, -1.0, 0.0, 1.0, 1.0, false, "touching positive plane");
        verify(frustum, -1.0, -1.0, -1.0, Double.MIN_VALUE, 1.0, 1.0, true, "past positive plane");

        frustum[0][0] = -1.0F;
        verify(frustum, 0.0, -1.0, -1.0, 1.0, 1.0, 1.0, false, "touching negative plane");
        verify(frustum, -Double.MIN_VALUE, -1.0, -1.0, 1.0, 1.0, 1.0, true, "past negative plane");

        frustum[0][0] = Float.NaN;
        verify(frustum, -1.0, -1.0, -1.0, 1.0, 1.0, 1.0, false, "NaN plane");
    }

    private static void setAlwaysVisible(float[][] frustum) {
        for (int plane = 0; plane < 6; ++plane) {
            frustum[plane][0] = 0.0F;
            frustum[plane][1] = 0.0F;
            frustum[plane][2] = 0.0F;
            frustum[plane][3] = 1.0F;
        }
    }

    private static void randomPlane(Random random, float[] plane) {
        plane[0] = random.nextFloat() * 2.0F - 1.0F;
        plane[1] = random.nextFloat() * 2.0F - 1.0F;
        plane[2] = random.nextFloat() * 2.0F - 1.0F;
        plane[3] = random.nextFloat() * 512.0F - 256.0F;
    }

    private static void verifyRandomBox(Random random, float[][] frustum, String kind, int iteration) {
        double minX = random.nextDouble() * 1024.0 - 512.0;
        double minY = random.nextDouble() * 256.0 - 128.0;
        double minZ = random.nextDouble() * 1024.0 - 512.0;
        double maxX = minX + random.nextDouble() * 64.0;
        double maxY = minY + random.nextDouble() * 64.0;
        double maxZ = minZ + random.nextDouble() * 64.0;
        boolean expected = legacyIntersects(frustum, minX, minY, minZ, maxX, maxY, maxZ);
        verify(frustum, minX, minY, minZ, maxX, maxY, maxZ, expected, kind + " #" + iteration);
    }

    private static void verify(float[][] frustum,
                               double minX, double minY, double minZ,
                               double maxX, double maxY, double maxZ,
                               boolean expected, String description) {
        boolean actual = FrustumIntersection.intersects(frustum, minX, minY, minZ, maxX, maxY, maxZ);
        if (actual != expected)
            throw new AssertionError(description + ": expected " + expected + " but got " + actual);
    }

    private static boolean legacyIntersects(float[][] frustum,
                                            double minX, double minY, double minZ,
                                            double maxX, double maxY, double maxZ) {
        for (int planeIndex = 0; planeIndex < 6; ++planeIndex) {
            float[] plane = frustum[planeIndex];
            boolean anyInside = false;
            for (int corner = 0; corner < 8; ++corner) {
                double x = (corner & 1) == 0 ? minX : maxX;
                double y = (corner & 2) == 0 ? minY : maxY;
                double z = (corner & 4) == 0 ? minZ : maxZ;
                if ((double) plane[0] * x
                        + (double) plane[1] * y
                        + (double) plane[2] * z
                        + (double) plane[3] > 0.0) {
                    anyInside = true;
                    break;
                }
            }
            if (!anyInside)
                return false;
        }
        return true;
    }
}
