package net.mine_diver.amphetamine.mixin.client.multidraw;

import net.minecraft.client.render.world.ChunkRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChunkRenderer.class)
abstract class RenderListMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 65536))
    private int amphetamine_removeUnusedListBuffer(int size) {
        return 0;
    }
}
