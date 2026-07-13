package net.mine_diver.amphetamine.mixin.client.multidraw;

import java.util.Arrays;

public final class TessellatorMixinVerification {
    public static void main(String[] args) throws ReflectiveOperationException {
        int[] source = new int[32];
        for (int i = 0; i < source.length; ++i)
            source[i] = i;
        int[] destination = new int[26];
        Arrays.fill(destination, -1);

        var copyQuad = TessellatorMixin.class.getDeclaredMethod("amphetamine_copyTerrainQuad", int[].class, int[].class, int.class);
        copyQuad.setAccessible(true);
        copyQuad.invoke(null, source, destination, 1);

        int[] expected = {
                -1,
                0, 1, 2, 3, 4, 5,
                8, 9, 10, 11, 12, 13,
                16, 17, 18, 19, 20, 21,
                24, 25, 26, 27, 28, 29,
                -1
        };
        if (!Arrays.equals(destination, expected))
            throw new AssertionError("StationAPI quad was not compacted to the terrain vertex format");
    }
}
