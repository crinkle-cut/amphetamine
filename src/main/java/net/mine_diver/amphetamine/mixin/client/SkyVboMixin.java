package net.mine_diver.amphetamine.mixin.client;

import net.mine_diver.amphetamine.client.render.StaticSkyVbo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
abstract class SkyVboMixin {
    @Shadow private int starsGlList;
    @Shadow private int lightSkyGlList;
    @Shadow private int darkSkyGlList;

    @Unique
    private StaticSkyVbo amphetamine_skyVbo;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void amphetamine_replaceSkyDisplayLists(Minecraft minecraft, TextureManager textureManager, CallbackInfo ci) {
        this.amphetamine_skyVbo = new StaticSkyVbo();
        GL11.glDeleteLists(this.starsGlList, 3);
    }

    @Inject(method = "reload()V", at = @At("HEAD"))
    private void amphetamine_restoreSkyVbo(CallbackInfo ci) {
        this.amphetamine_getSkyVbo().upload();
    }

    @Inject(method = "setWorld(Lnet/minecraft/world/World;)V", at = @At("HEAD"))
    private void amphetamine_releaseSkyVbo(World world, CallbackInfo ci) {
        if (world == null && this.amphetamine_skyVbo != null)
            this.amphetamine_skyVbo.close();
    }

    @Redirect(
            method = "renderSky(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V",
                    remap = false
            )
    )
    private void amphetamine_drawSkyVbo(int list) {
        StaticSkyVbo skyVbo = this.amphetamine_getSkyVbo();
        if (list == this.lightSkyGlList)
            skyVbo.drawLightSky();
        else if (list == this.starsGlList)
            skyVbo.drawStars();
        else if (list == this.darkSkyGlList)
            skyVbo.drawDarkSky();
        else
            GL11.glCallList(list);
    }

    @Unique
    private StaticSkyVbo amphetamine_getSkyVbo() {
        if (this.amphetamine_skyVbo == null)
            this.amphetamine_skyVbo = new StaticSkyVbo();
        return this.amphetamine_skyVbo;
    }
}
