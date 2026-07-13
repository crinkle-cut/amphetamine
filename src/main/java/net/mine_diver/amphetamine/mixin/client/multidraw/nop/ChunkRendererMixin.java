package net.mine_diver.amphetamine.mixin.client.multidraw.nop;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkBuilder.class)
public class ChunkRendererMixin {
    @Redirect(
            method = "setPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V",
                    remap = false
            )
    )
    private void amphetamine_nopOcclusionListStart(int list, int mode) {}

    @Redirect(
            method = "setPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/ItemRenderer;renderShapeFlat(Lnet/minecraft/util/math/Box;)V"
            )
    )
    private void amphetamine_nopOcclusionBox(Box box) {}

    @Redirect(
            method = "setPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glEndList()V",
                    remap = false
            )
    )
    private void amphetamine_nopOcclusionListEnd() {}

    @Redirect(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V",
                    remap = false
            )
    )
    private void amphetamine_nop_GL11_glNewList(int list, int mode) {}

    @Redirect(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glEndList()V",
                    remap = false
            )
    )
    private void amphetamine_nop_GL11_glEndList() {}

    @Redirect(
            method = {
                    "translateToRenderPosition",
                    "rebuild"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V",
                    remap = false
            )
    )
    private void amphetamine_nop_GL11_glTranslatef(float x, float y, float z) {}

    @Redirect(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V",
                    remap = false
            )
    )
    private void amphetamine_nop_GL11_glPushMatrix() {}

    @Redirect(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V",
                    remap = false
            )
    )
    private void amphetamine_nop_GL11_glScalef(float x, float y, float z) {}

    @Redirect(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V",
                    remap = false
            )
    )
    private void amphetamine_nop_GL11_glPopMatrix() {}
}
