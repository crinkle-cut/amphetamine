package net.mine_diver.amphetamine.client.render;

/**
 * Tests an axis-aligned box against the six planes of a view frustum.
 */
public final class FrustumIntersection {
    private static final int PLANE_COUNT = 6;

    private FrustumIntersection() {
    }

    public static boolean intersects(float[][] frustum,
                                     double minX, double minY, double minZ,
                                     double maxX, double maxY, double maxZ) {
        for (int i = 0; i < PLANE_COUNT; ++i) {
            float[] plane = frustum[i];
            float normalX = plane[0];
            float normalY = plane[1];
            float normalZ = plane[2];
            double x = normalX >= 0.0F ? maxX : minX;
            double y = normalY >= 0.0F ? maxY : minY;
            double z = normalZ >= 0.0F ? maxZ : minZ;

            // The selected point maximizes this plane equation over the AABB.
            // Preserve vanilla's strict comparison so boxes on a plane remain outside.
            if (!((double) normalX * x
                    + (double) normalY * y
                    + (double) normalZ * z
                    + (double) plane[3] > 0.0))
                return false;
        }
        return true;
    }
}
