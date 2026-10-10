package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.gameplay.BodyRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Compact survival information for the current body, using live vanilla values. */
final class SurvivalHud {
    private SurvivalHud() { }

    static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || player.isSpectator()) return;
        var data = ClientPlayerState.get();
        var body = ClientPlayerState.body();
        boolean air = !body.breathesWater() && (player.isUnderWater() || player.getAirSupply() < player.getMaxAirSupply());
        int width = Math.min(238, graphics.guiWidth() - 24);
        if (width < 120) return;
        int x = graphics.guiWidth() - width - 12, y = 12;
        int height = 98 + (body.needsFood() ? 18 : 0) + (air ? 18 : 0);
        TensuraUi.drawFrame(graphics, x, y, x + width, y + height, 0xCA121D30, RpgTheme.GOLD);
        RpgTheme.diamond(graphics, x + 20, y + 19, 11, 0xFF283B52);
        RpgTheme.diamond(graphics, x + 20, y + 19, 8, 0xFF82D5D9);
        graphics.fill(x + 16, y + 17, x + 18, y + 19, 0xFF152538);
        graphics.fill(x + 23, y + 17, x + 25, y + 19, 0xFF152538);
        String name = body.name().isBlank() ? data.getRaceId().replace("tensura:", "").replace('_', ' ') : body.name();
        text(graphics, name + (body.transitioning() ? " / mudando" : body.canFly() ? " / VOO" : ""), x + 38, y + 9, width - 48, RpgTheme.TEXT);
        text(graphics, "EP " + format(data.getExistencePoints()) + "  /  Almas " + data.getSoulPoints(), x + 38, y + 22, width - 48, RpgTheme.GOLD);
        int row = y + 39;
        String absorption = player.getAbsorptionAmount() > 0 ? " +" + number(player.getAbsorptionAmount()) : "";
        bar(graphics, x + 10, row, width - 20, player.getHealth(), player.getMaxHealth(),
                "VIDA", number(player.getHealth()) + "/" + number(player.getMaxHealth()) + absorption,
                player.getHealth() / player.getMaxHealth() <= .25 ? 0xFFEB6876 : 0xFF91CF9A);
        row += 18;
        bar(graphics, x + 10, row, width - 20, data.getMagicules(), data.getMaxMagicules(),
                "MAGICULAS", format(data.getMagicules()) + "/" + format(data.getMaxMagicules()), 0xFF83CDD9);
        row += 18;
        if (body.needsFood()) {
            bar(graphics, x + 10, row, width - 20, player.getFoodData().getFoodLevel(), 20,
                    "ALIMENTO", player.getFoodData().getFoodLevel() + "/20", RpgTheme.GOLD);
            row += 18;
        }
        if (air) {
            bar(graphics, x + 10, row, width - 20, player.getAirSupply(), player.getMaxAirSupply(),
                    "AR", Math.max(0, Math.round(100F * player.getAirSupply() / Math.max(1, player.getMaxAirSupply()))) + "%", 0xFF92ABEA);
            row += 18;
        }
        text(graphics, "DEF " + player.getArmorValue() + "   ATQ " + number(player.getAttributeValue(Attributes.ATTACK_DAMAGE)),
                x + 10, row + 3, width - 20, 0xFFAFB6C4);
        if (graphics.guiWidth() >= 420) {
            String hint = "R  PERSONAGEM";
            graphics.drawString(minecraft.font, hint, x + width - minecraft.font.width(hint) - 10,
                    y + height + 7, RpgTheme.GOLD, true);
        }
    }

    private static void bar(GuiGraphics g, int x, int y, int width, double value, double maximum, String label, String amount, int color) {
        text(g, label, x, y, width / 2, 0xFFD3D7DF);
        var font = Minecraft.getInstance().font;
        g.drawString(font, amount, x + width - font.width(amount), y, RpgTheme.TEXT, false);
        g.fill(x, y + 11, x + width, y + 15, 0xFF303B50);
        int fill = BodyRules.barPixels(value, maximum, width);
        g.fill(x, y + 11, x + fill, y + 15, color);
        if (fill > 0) g.fill(x, y + 11, x + fill, y + 12, 0x66FFFFFF);
    }

    private static void text(GuiGraphics graphics, String value, int x, int y, int width, int color) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, font.plainSubstrByWidth(value, width), x, y, color, true);
    }

    private static String number(double value) {
        return String.format(java.util.Locale.ROOT, value == Math.floor(value) ? "%.0f" : "%.1f", value);
    }

    private static String format(long value) {
        if (value >= 1_000_000L) return String.format(java.util.Locale.ROOT, "%.1fM", value / 1_000_000.0);
        if (value >= 1_000L) return String.format(java.util.Locale.ROOT, "%.1fK", value / 1_000.0);
        return Long.toString(value);
    }
}
