package net.mine_diver.amphetamine.mixin.client;

import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
abstract class HeldItemRendererMixin {
    @Unique
    private static final int amphetamine_EXTRUDED_ITEM_BATCH_COUNT = 6;

    @Unique
    private int amphetamine_extrudedItemStarts;

    @Unique
    private int amphetamine_extrudedItemDraws;

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void amphetamine_beginExtrudedItemBatch(LivingEntity entity, ItemStack stack, CallbackInfo ci) {
        this.amphetamine_extrudedItemStarts = 0;
        this.amphetamine_extrudedItemDraws = 0;
    }

    @Redirect(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Tessellator;startQuads()V"
            )
    )
    private void amphetamine_continueExtrudedItemBatch(Tessellator tessellator) {
        if (this.amphetamine_extrudedItemStarts++ == 0
                || this.amphetamine_extrudedItemDraws >= amphetamine_EXTRUDED_ITEM_BATCH_COUNT)
            tessellator.startQuads();
    }

    @Redirect(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Tessellator;draw()V"
            )
    )
    private void amphetamine_finishExtrudedItemBatch(Tessellator tessellator) {
        if (++this.amphetamine_extrudedItemDraws >= amphetamine_EXTRUDED_ITEM_BATCH_COUNT)
            tessellator.draw();
    }
}
