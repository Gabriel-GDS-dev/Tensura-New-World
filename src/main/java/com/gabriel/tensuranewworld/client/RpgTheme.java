package com.gabriel.tensuranewworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

/** Original New World ornaments and shared palette, drawn at the current GUI scale. */
public final class RpgTheme {
    public static final int GOLD = 0xFFE3C58B, INK = 0xEE121D30, TEXT = 0xFFF4EBDD;
    private RpgTheme() { }
    public static boolean themed(Screen screen) {
        return screen instanceof SelectWorldScreen || screen instanceof CreateWorldScreen || screen instanceof PauseScreen;
    }
    public static void diamond(GuiGraphics g, int x, int y, int radius, int color) {
        for (int row = -radius; row <= radius; row++) {
            int half = radius - Math.abs(row);
            g.fill(x - half, y + row, x + half + 1, y + row + 1, color);
        }
    }
    public static void rule(GuiGraphics g, int left, int right, int y) {
        if (right <= left) return;
        g.fill(left, y, right, y + 1, 0x668E7959);
        diamond(g, (left + right) / 2, y, 3, GOLD);
    }
    public static void button(GuiGraphics g, int x, int y, int w, int h, boolean active,
                              boolean highlighted, int accent) {
        int border = !active ? 0xFF586075 : accent != 0 ? accent : highlighted ? GOLD : 0xFF706B65;
        g.fillGradient(x, y, x + w, y + h, highlighted ? 0xF03C4659 : 0xEE202C40, 0xF0141E30);
        g.fill(x + 3, y, x + w - 3, y + 1, border);
        g.fill(x + 3, y + h - 1, x + w - 3, y + h, border);
        g.fill(x, y + 3, x + 1, y + h - 3, border);
        g.fill(x + w - 1, y + 3, x + w, y + h - 3, border);
        if (highlighted && w > 55) diamond(g, x + 9, y + h / 2, 2, border);
    }
    public static void vanillaButton(GuiGraphics g, net.minecraft.client.gui.components.AbstractButton b) {
        button(g, b.getX(), b.getY(), b.getWidth(), b.getHeight(), b.active, b.isHoveredOrFocused(), 0);
        var font = Minecraft.getInstance().font;
        String label = font.plainSubstrByWidth(b.getMessage().getString(), Math.max(0, b.getWidth() - 20));
        g.drawString(font, label, b.getX() + (b.getWidth() - font.width(label)) / 2,
                b.getY() + (b.getHeight() - 8) / 2, b.active ? TEXT : 0xFF9299A4, false);
    }
}
