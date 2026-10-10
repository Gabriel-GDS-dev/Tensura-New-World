package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.TensuraNewWorld;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class TensuraUi {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            TensuraNewWorld.MOD_ID, "textures/gui/title_background.png");
    private static final int TEXTURE_WIDTH = 1672;
    private static final int TEXTURE_HEIGHT = 941;

    private TensuraUi() {
    }

    static void drawArtworkBackground(GuiGraphics graphics, int width, int height, int shadeColor) {
        float textureAspect = (float) TEXTURE_WIDTH / TEXTURE_HEIGHT;
        float screenAspect = (float) width / height;
        if (screenAspect > textureAspect) {
            int cropHeight = Math.round(TEXTURE_WIDTH / screenAspect);
            int cropTop = (TEXTURE_HEIGHT - cropHeight) / 2;
            graphics.blit(BACKGROUND, 0, 0, width, height, 0, cropTop,
                    TEXTURE_WIDTH, cropHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        } else {
            int cropWidth = Math.round(TEXTURE_HEIGHT * screenAspect);
            int cropLeft = (TEXTURE_WIDTH - cropWidth) / 2;
            graphics.blit(BACKGROUND, 0, 0, width, height, cropLeft, 0,
                    cropWidth, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        graphics.fill(0, 0, width, height, shadeColor);
    }

    static void drawFrame(GuiGraphics graphics, int left, int top, int right, int bottom,
            int fillColor, int accentColor) {
        graphics.fill(left, top, right, bottom, fillColor);
        graphics.fill(left, top, right, top + 1, 0x9976654D);
        graphics.fill(left, bottom - 1, right, bottom, 0x9976654D);
        graphics.fill(left, top, left + 1, bottom, 0x9976654D);
        graphics.fill(right - 1, top, right, bottom, 0x9976654D);
        int corner = Math.min(12, Math.min((right - left) / 8, (bottom - top) / 4));
        graphics.fill(left + 4, top + 4, left + 4 + corner, top + 5, 0xFFE3C58B);
        graphics.fill(right - 4 - corner, bottom - 5, right - 4, bottom - 4, 0xFFE3C58B);
        RpgTheme.diamond(graphics, left + 4, top + 4, 3, accentColor);
        RpgTheme.diamond(graphics, right - 5, bottom - 5, 3, accentColor);
    }

    static void drawCircuitAccents(GuiGraphics graphics, int left, int top, int right, int bottom) {
        // Original geometric motif: restrained technical lines behind readable content.
        for (int y = top + 12; y < bottom; y += 24) {
            graphics.fill(right - 18, y, right - 5, y + 1, 0x335F6785);
            graphics.fill(right - 6, y, right - 5, Math.min(bottom, y + 8), 0x335F6785);
        }
        int corner = Math.min(26, Math.max(0, (right - left) / 6));
        graphics.fill(left + 3, top + 4, left + 3 + corner, top + 5, 0x9976654D);
        graphics.fill(right - corner - 4, bottom - 5, right - 4, bottom - 4, 0x9976654D);
        graphics.fill(right - 5, bottom - corner - 4, right - 4, bottom - 4, 0x9976654D);
    }
}
