package net.mine_diver.amphetamine.client.render;

import java.nio.IntBuffer;
import java.util.Arrays;

public final class VboPoolVerification {
    private VboPoolVerification() {
    }

    public static void main(String[] args) {
        Range[] ranges = {
                new Range(2, 0, 4),
                new Range(1, 12, 4),
                new Range(1, 4, 8)
        };
        Arrays.sort(ranges, (left, right) -> VboPool.compareRangeKeys(
                left.page, left.first, right.page, right.first));

        IntBuffer firsts = IntBuffer.allocate(ranges.length);
        IntBuffer counts = IntBuffer.allocate(ranges.length);
        int page = ranges[0].page;
        for (Range range : ranges) {
            if (range.page != page || !VboPool.mergeTouchingRange(firsts, counts, range.first, range.count)) {
                page = range.page;
                firsts.put(range.first);
                counts.put(range.count);
            }
        }

        if (firsts.position() != 2
                || firsts.get(0) != 4 || counts.get(0) != 12
                || firsts.get(1) != 0 || counts.get(1) != 4)
            throw new AssertionError("Opaque VBO ranges were not sorted and coalesced by page and offset");
    }

    private record Range(int page, int first, int count) {
    }
}
