package net.mine_diver.amphetamine.mixin.client.multidraw;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.world.ChunkRenderer;

@Mixin(ChunkRenderer.class)
public interface RenderListAccessor {
    @Accessor("initialized")
    boolean amphetamine_getInitialized();

    @Accessor("x")
    int amphetamine_getX();

    @Accessor("y")
    int amphetamine_getY();

    @Accessor("z")
    int amphetamine_getZ();

    @Accessor("offsetX")
    float amphetamine_getOffsetX();

    @Accessor("offsetY")
    float amphetamine_getOffsetY();

    @Accessor("offsetZ")
    float amphetamine_getOffsetZ();
}
