package net.mine_diver.smoothbeta.mixin.client.multidraw;

import net.mine_diver.smoothbeta.client.render.Shaders;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Redirect(
            method = "applyFog(IF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glFogi(II)V",
                    remap = false
            )
    )
    private void smoothbeta_trackFogMode(int pname, int param) {
        GL11.glFogi(pname, param);
        if (pname == GL11.GL_FOG_MODE)
            Shaders.setFogMode(param);
    }
}
