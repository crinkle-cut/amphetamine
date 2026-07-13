package net.mine_diver.amphetamine.util.math;

public class Vec3f {
    public static final Vec3f ZERO = new Vec3f(0, 0, 0);

    private final float x;
    private final float y;
    private final float z;

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
