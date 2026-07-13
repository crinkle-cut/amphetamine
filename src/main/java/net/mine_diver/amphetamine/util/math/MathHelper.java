package net.mine_diver.amphetamine.util.math;

public class MathHelper {
    public static int roundUpToMultiple(int value, int multiple) {
        if (multiple == 0)
            return value;
        int remainder = value % multiple;
        if (remainder == 0)
            return value;
        return value + multiple - remainder;
    }
}
