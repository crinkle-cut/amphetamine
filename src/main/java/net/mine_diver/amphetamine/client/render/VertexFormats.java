package net.mine_diver.amphetamine.client.render;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexFormats {
    public static final VertexFormatElement POSITION_ELEMENT = new VertexFormatElement(0, VertexFormatElement.ComponentType.FLOAT, VertexFormatElement.Type.POSITION, 3);
    public static final VertexFormatElement COLOR_ELEMENT = new VertexFormatElement(0, VertexFormatElement.ComponentType.UBYTE, VertexFormatElement.Type.COLOR, 4);
    public static final VertexFormatElement TEXTURE_0_ELEMENT = new VertexFormatElement(0, VertexFormatElement.ComponentType.FLOAT, VertexFormatElement.Type.UV, 2);
    public static final VertexFormat POSITION_TEXTURE_COLOR = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position", POSITION_ELEMENT)
                    .put("UV0", TEXTURE_0_ELEMENT)
                    .put("Color", COLOR_ELEMENT)
                    .build()
    );
}
