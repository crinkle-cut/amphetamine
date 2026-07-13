package net.mine_diver.amphetamine.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class VertexFormat {
    private final ImmutableMap<String, VertexFormatElement> elementMap;
    private final int vertexSizeByte;

    public VertexFormat(ImmutableMap<String, VertexFormatElement> elementMap) {
        this.elementMap = elementMap;
        int i = 0;
        for (VertexFormatElement vertexFormatElement : elementMap.values())
            i += vertexFormatElement.getByteLength();
        this.vertexSizeByte = i;
    }

    public String toString() {
        return "format: " + this.elementMap.size() + " elements: "
                + this.elementMap.entrySet().stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    public int getVertexSizeByte() {
        return this.vertexSizeByte;
    }

    public ImmutableList<String> getAttributeNames() {
        return this.elementMap.keySet().asList();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        VertexFormat vertexFormat = (VertexFormat) o;
        if (this.vertexSizeByte != vertexFormat.vertexSizeByte) {
            return false;
        }
        return this.elementMap.equals(vertexFormat.elementMap);
    }

    public int hashCode() {
        return this.elementMap.hashCode();
    }

    @Environment(value = EnvType.CLIENT)
    public enum DrawMode {
        LINES(4, 2, 2, false),
        LINE_STRIP(5, 2, 1, true),
        DEBUG_LINES(1, 2, 2, false),
        DEBUG_LINE_STRIP(3, 2, 1, true),
        TRIANGLES(4, 3, 3, false),
        TRIANGLE_STRIP(5, 3, 1, true),
        TRIANGLE_FAN(6, 3, 1, true),
        QUADS(7, 4, 4, false);

        public final int glMode;
        public final int firstVertexCount;
        public final int additionalVertexCount;
        public final boolean shareVertices;

        DrawMode(int glMode, int firstVertexCount, int additionalVertexCount, boolean shareVertices) {
            this.glMode = glMode;
            this.firstVertexCount = firstVertexCount;
            this.additionalVertexCount = additionalVertexCount;
            this.shareVertices = shareVertices;
        }

        public int getIndexCount(int vertexCount) {
            return switch (this) {
                case LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN, QUADS ->
                    vertexCount;
                case LINES -> vertexCount / 4 * 6;
            };
        }
    }
}
