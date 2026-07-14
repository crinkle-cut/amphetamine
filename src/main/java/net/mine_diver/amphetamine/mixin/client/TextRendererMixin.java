package net.mine_diver.amphetamine.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.CharacterUtils;
import net.mine_diver.amphetamine.client.render.SmoothTextRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextRenderer.class)
abstract class TextRendererMixin implements SmoothTextRenderer {
    @Shadow private int[] characterWidths;
    @Shadow public int boundTexture;

    @Unique private final int[] amphetamine_formatColors = new int[32];
    @Unique private boolean amphetamine_batching;

    @Override
    public void amphetamine_beginBatch() {
        if (this.amphetamine_batching)
            throw new IllegalStateException("Text batch is already active");
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.boundTexture);
        Tessellator.INSTANCE.startQuads();
        this.amphetamine_batching = true;
    }

    @Override
    public void amphetamine_endBatch() {
        if (!this.amphetamine_batching)
            throw new IllegalStateException("No text batch is active");
        this.amphetamine_batching = false;
        Tessellator.INSTANCE.draw();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void amphetamine_buildFormatColors(GameOptions options, String fontPath, TextureManager textureManager,
                                               CallbackInfo ci) {
        for (int i = 0; i < this.amphetamine_formatColors.length; ++i) {
            int offset = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + offset;
            int green = (i >> 1 & 1) * 170 + offset;
            int blue = (i & 1) * 170 + offset;
            if (i == 6)
                red += 85;
            if (options.anaglyph3d) {
                int anaglyphRed = (red * 30 + green * 59 + blue * 11) / 100;
                int anaglyphGreen = (red * 30 + green * 70) / 100;
                int anaglyphBlue = (red * 30 + blue * 70) / 100;
                red = anaglyphRed;
                green = anaglyphGreen;
                blue = anaglyphBlue;
            }
            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            this.amphetamine_formatColors[i] = red << 16 | green << 8 | blue;
        }
    }

    /**
     * Draws all glyphs in one tessellator batch instead of one display-list call per glyph.
     *
     * @author Nitch
     * @reason Avoid driver-side display-list interpretation for every glyph.
     */
    @Overwrite
    public void draw(String text, int x, int y, int color, boolean shadow) {
        if (text == null)
            return;

        if (shadow)
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;

        int alpha = color >>> 24;
        if (alpha == 0)
            alpha = 255;

        Tessellator tessellator = Tessellator.INSTANCE;
        if (!this.amphetamine_batching) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.boundTexture);
            tessellator.startQuads();
        }
        amphetamine_setColor(tessellator, color, alpha);
        int cursorX = x;

        for (int i = 0; i < text.length(); ++i) {
            if (text.charAt(i) == '\u00a7' && i + 1 < text.length()) {
                int format = "0123456789abcdef".indexOf(Character.toLowerCase(text.charAt(++i)));
                if (format < 0)
                    format = 15;
                amphetamine_setColor(tessellator,
                        this.amphetamine_formatColors[format + (shadow ? 16 : 0)], alpha);
                continue;
            }

            int glyph = CharacterUtils.VALID_CHARACTERS.indexOf(text.charAt(i));
            if (glyph < 0)
                continue;

            glyph += 32;
            float u = (glyph % 16 * 8) / 128.0F;
            float v = (glyph / 16 * 8) / 128.0F;
            float size = 7.99F;
            tessellator.vertex(cursorX, y + size, 0.0, u, v + size / 128.0F);
            tessellator.vertex(cursorX + size, y + size, 0.0, u + size / 128.0F, v + size / 128.0F);
            tessellator.vertex(cursorX + size, y, 0.0, u + size / 128.0F, v);
            tessellator.vertex(cursorX, y, 0.0, u, v);
            cursorX += this.characterWidths[glyph];
        }

        if (!this.amphetamine_batching)
            tessellator.draw();
    }

    /**
     * Emits a text shadow and its foreground in one batch.
     *
     * @author Nitch
     * @reason Avoid a second small draw and redundant font-texture bind for every shadowed string.
     */
    @Overwrite
    public void drawWithShadow(String text, int x, int y, int color) {
        boolean ownsBatch = !this.amphetamine_batching;
        if (ownsBatch)
            this.amphetamine_beginBatch();
        this.draw(text, x + 1, y + 1, color, true);
        this.draw(text, x, y, color, false);
        if (ownsBatch)
            this.amphetamine_endBatch();
    }

    @Unique
    private static void amphetamine_setColor(Tessellator tessellator, int color, int alpha) {
        tessellator.color(color >> 16 & 255, color >> 8 & 255, color & 255, alpha);
    }
}
