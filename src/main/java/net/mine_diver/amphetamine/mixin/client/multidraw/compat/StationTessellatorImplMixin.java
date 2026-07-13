package net.mine_diver.amphetamine.mixin.client.multidraw.compat;

import net.mine_diver.amphetamine.client.render.SmoothTessellator;
import net.minecraft.client.render.Tessellator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.modificationstation.stationapi.impl.client.render.StationTessellatorImpl", remap = false)
abstract class StationTessellatorImplMixin {
    @Shadow @Final private Tessellator self;
    @Shadow @Final private int[] fastVertexData;

    @Inject(
            method = "quad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/modificationstation/stationapi/mixin/render/client/TessellatorAccessor;setHasNormals(Z)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void amphetamine_appendTerrainQuad(CallbackInfo ci) {
        SmoothTessellator tessellator = (SmoothTessellator) self;
        if (!tessellator.amphetamine_isRenderingTerrain()) return;
        tessellator.amphetamine_appendTerrainQuad(fastVertexData);
        ci.cancel();
    }
}
