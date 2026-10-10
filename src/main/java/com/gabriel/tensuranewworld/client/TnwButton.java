package com.gabriel.tensuranewworld.client;

import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

final class TnwButton extends AbstractButton {
    private final Runnable action;
    private final BooleanSupplier selected;
    private boolean drawLabel = true;
    private int customAccent;
    private boolean muted;

    TnwButton(int x, int y, int width, int height, Component message, Runnable action) {
        this(x, y, width, height, message, action, () -> false);
    }

    TnwButton(int x, int y, int width, int height, Component message, Runnable action,
            BooleanSupplier selected) {
        super(x, y, width, height, message);
        this.action = action;
        this.selected = selected;
    }

    @Override
    public void onPress() {
        action.run();
    }

    void setDrawLabel(boolean value) { drawLabel = value; }
    void setAccent(int color, boolean muted) { this.customAccent = color; this.muted = muted; }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean highlighted = active && (isHoveredOrFocused() || selected.getAsBoolean());
        RpgTheme.button(graphics, getX(), getY(), width, height, active, highlighted, customAccent);
        int textColor = active ? RpgTheme.TEXT : 0xFF9BAEB2;
        var font = Minecraft.getInstance().font;
        String text = font.plainSubstrByWidth(getMessage().getString(), Math.max(0, width - 12));
        if (drawLabel) graphics.drawString(font, text, getX() + (width - font.width(text)) / 2,
                getY() + (height - 8) / 2, textColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
