package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import com.gabriel.tensuranewworld.gameplay.TargetInspection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.Optional;

/** Native equipped slots and live native cooldowns, painted with the pack theme. */
public final class CombatHud {
    private CombatHud() { }
    public static String seconds(int ticks) { return String.format(java.util.Locale.ROOT, "%.1fs", Math.max(0, ticks) / 20.0); }
    public static void render(GuiGraphics g) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isSpectator()) return;
        int width = Math.min(214, (g.guiWidth() - 30) / 2);
        if (width < 100) return;
        boolean compact = g.guiHeight() < 270;
        int cardHeight = compact ? 30 : 40;
        int x = 12, y = Math.max(82, g.guiHeight() - 3 * (cardHeight + 4) - 12);
        int preset = ReincarnatedSkillControls.activePreset();
        text(g, "HABILIDADES / " + (preset + 1) + "  " + ReincarnatedSkillControls.modeKey() + " MODO", x, y - 12, width, RpgTheme.GOLD);
        Object storage = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", mc.player);
        for (int index = 0; index < 3; index++) {
            int row = y + index * (cardHeight + 4);
            Object slot = ReincarnatedSkillControls.slot(preset, index);
            Object skill = ReincarnatedSkills.call(slot, "getSkill");
            Object modeValue = ReincarnatedSkills.call(slot, "getMode");
            int mode = modeValue instanceof Number n ? n.intValue() : 0;
            Object found = ReincarnatedSkills.call(storage, "getSkill", skill);
            Object instance = found instanceof Optional<?> optional ? optional.orElse(null) : null;
            Object cooldown = ReincarnatedSkills.call(instance, "getCoolDown", mode);
            int remaining = cooldown instanceof Number n ? Math.max(0, n.intValue()) : 0;
            boolean empty = skill == null || Boolean.TRUE.equals(ReincarnatedSkills.call(slot, "isEmpty"));
            String title = empty ? "Slot vazio" : component(ReincarnatedSkills.call(skill, "getName"), "Habilidade");
            String modeName = component(ReincarnatedSkills.call(skill, "getModeName", instance, mode), "Modo " + (mode + 1));
            int accent = remaining > 0 ? 0xFF92ABEA : RpgTheme.GOLD;
            TensuraUi.drawFrame(g, x, row, x + width, row + cardHeight, RpgTheme.INK, accent);
            String key = ReincarnatedSkillControls.slotKey(index);
            text(g, key, x + 6, row + 4, 32, accent);
            var icon = ResourceLocation.tryParse(String.valueOf(ReincarnatedSkills.call(skill, "getSkillIcon")));
            if (!empty && icon != null && mc.getResourceManager().getResource(icon).isPresent()) g.blit(icon, x + 9, row + (compact ? 13 : 16), compact ? 12 : 20, compact ? 12 : 20, 0, 0, 32, 32, 32, 32);
            text(g, title, x + 40, row + 5, width - 46, RpgTheme.TEXT);
            String status = empty ? "Equipe pelo Raphael [R]" : instance == null ? "Nao aprendida" : remaining > 0 ? "RECARGA " + seconds(remaining) : "PRONTA";
            Object mastery = ReincarnatedSkills.call(instance, "getMastery");
            if (!empty && mastery instanceof Number n && n.doubleValue() < 0) status = "APRENDENDO";
            Object cost = ReincarnatedSkills.call(instance, "getMagiculeCost", mc.player, mode);
            if (status.equals("PRONTA") && cost instanceof Number n) {
                Object existence = ReincarnatedSkills.callStatic("io.github.manasmods.tensura.storage.TensuraStorages", "getExistenceFrom", mc.player);
                Object mp = ReincarnatedSkills.call(existence, "getMagicule");
                if (mp instanceof Number energy && energy.doubleValue() < n.doubleValue()) status = "SEM MAGICULAS";
            }
            if (!empty && Boolean.TRUE.equals(ReincarnatedSkills.call(instance, "isToggled"))) status += " / ATIVA";
            text(g, empty || compact ? status : modeName, x + 40, row + 17, width - 46, 0xFFAFB6C4);
            if (!empty && !compact) text(g, status, x + 40, row + 29, width - 46, remaining > 0 ? 0xFF92ABEA : 0xFF91CF9A);
        }
        renderTarget(g, width);
    }
    private static void renderTarget(GuiGraphics g, int width) {
        var mc = Minecraft.getInstance();
        var vitals = ClientPlayerState.target();
        var current = TargetInspection.target(mc.player);
        if (vitals == null || vitals.entityId() < 0 || current == null || current.getId() != vitals.entityId()) return;
        int x = 12, y = 12;
        TensuraUi.drawFrame(g, x, y, x + width, y + 67, RpgTheme.INK, RpgTheme.GOLD);
        text(g, vitals.name(), x + 8, y + 7, width - 16, RpgTheme.GOLD);
        SurvivalHud.bar(g, x + 8, y + 22, width - 16, vitals.health(), vitals.maxHealth(), "VIDA",
                amount(vitals.health()) + "/" + amount(vitals.maxHealth()), 0xFF91CF9A);
        text(g, "MAGICULAS " + (vitals.magicules() < 0 ? "—" : amount(vitals.magicules())), x + 8, y + 42, width - 16, 0xFF83CDD9);
        text(g, "EP " + amount(vitals.ep()), x + 8, y + 54, width - 16, 0xFFAFB6C4);
    }
    private static String amount(double value) { return value >= 1_000_000 ? String.format(java.util.Locale.ROOT, "%.1fM", value / 1_000_000) : value >= 1000 ? String.format(java.util.Locale.ROOT, "%.1fK", value / 1000) : String.format(java.util.Locale.ROOT, "%.0f", value); }
    private static String component(Object value, String fallback) { return value instanceof Component c ? c.getString() : fallback; }
    private static void text(GuiGraphics g, String value, int x, int y, int width, int color) { g.drawString(Minecraft.getInstance().font, Minecraft.getInstance().font.plainSubstrByWidth(value, Math.max(0, width)), x, y, color, true); }
}
