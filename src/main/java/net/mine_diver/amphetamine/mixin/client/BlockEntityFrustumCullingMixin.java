package net.mine_diver.amphetamine.mixin.client;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Culler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
abstract class BlockEntityFrustumCullingMixin {
    /**
     * Block-entity renderers can extend outside their owning block. A full block
     * of padding covers rotated signs and a moving piston's complete travel while
     * keeping the test conservative at the edge of the view.
     */
    @Unique
    private static final double amphetamine_BLOCK_ENTITY_PADDING = 1.0;

    @Unique
    private final Box amphetamine_blockEntityBounds = Box.create(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

    @Redirect(
            method = "renderEntities(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/client/render/Culler;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;F)V"
            )
    )
    private void amphetamine_renderVisibleBlockEntity(BlockEntityRenderDispatcher dispatcher,
                                                     BlockEntity blockEntity,
                                                     float tickDelta,
                                                     Vec3d ignoredCameraPos,
                                                     Culler culler,
                                                     float ignoredTickDelta) {
        // Avoid doing the more expensive plane tests for block entities that
        // the dispatcher's unchanged 64-block check will reject anyway.
        if (blockEntity.distanceFrom(dispatcher.cameraX, dispatcher.cameraY, dispatcher.cameraZ) >= 4096.0)
            return;

        double padding = amphetamine_BLOCK_ENTITY_PADDING;
        this.amphetamine_blockEntityBounds.set(
                (double) blockEntity.x - padding,
                (double) blockEntity.y - padding,
                (double) blockEntity.z - padding,
                (double) blockEntity.x + 1.0 + padding,
                (double) blockEntity.y + 1.0 + padding,
                (double) blockEntity.z + 1.0 + padding
        );

        if (culler.isVisible(this.amphetamine_blockEntityBounds))
            dispatcher.render(blockEntity, tickDelta);
    }
}
