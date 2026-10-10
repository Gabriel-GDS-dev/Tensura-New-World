package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

final class ReincarnatedSkillControls {
    private static final String PACKETS = "io.github.manasmods.tensura.network.c2s.RequestAbilityModeChangePacket";
    private static final String KEYS = "io.github.manasmods.tensura.client.TensuraKeybinds";

    private ReincarnatedSkillControls() { }

    static boolean equip(String id, int slot, int mode) {
        return equip(id, slot, mode, activePreset());
    }
    static boolean equip(String id, int slot, int mode, int preset) {
        var player = Minecraft.getInstance().player;
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (player == null || slot < 0 || slot > 2 || mode < 0 || location == null) return false;
        if (preset < 0 || preset >= presetCount()) return false;
        return send(ReincarnatedSkills.callStatic(PACKETS, "changeAbilityPacket", slot, preset,
                location, mode));
    }
    static Object ability() {
        return ReincarnatedSkills.callStatic("io.github.manasmods.tensura.storage.TensuraStorages", "getAbilityFrom", Minecraft.getInstance().player);
    }
    static int activePreset() { Object index = ReincarnatedSkills.call(ability(), "getActivePreset"); return index instanceof Number n ? n.intValue() : 0; }
    static int presetCount() { Object all = ReincarnatedSkills.call(ability(), "getPresets"); return all instanceof java.util.List<?> list ? Math.min(9, list.size()) : 0; }
    static String presetName(int preset) { Object name = ReincarnatedSkills.call(ability(), "getPresetName", preset); return name instanceof String s ? s : ""; }
    static boolean activatePreset(int preset) { return preset >= 0 && preset < presetCount() && send(ReincarnatedSkills.callStatic(PACKETS, "changePresetPacket", (double)(preset + 1), false)); }
    static boolean renamePreset(int preset, String name) { return preset >= 0 && preset < presetCount() && !name.isBlank() && name.length() <= 20 && send(ReincarnatedSkills.callStatic(PACKETS, "renamePresetPacket", preset, name)); }
    static Object slot(int preset, int slot) { return ReincarnatedSkills.call(ability(), "getAbilitySlot", preset, slot); }
    static String slotName(int preset, int index) {
        Object skill = ReincarnatedSkills.call(slot(preset, index), "getSkill");
        Object name = ReincarnatedSkills.call(skill, "getName");
        return name instanceof Component c ? c.getString()
                : Component.translatable("ui.tensuranewworld.skills.empty_slot").getString();
    }
    static boolean equipped(int preset, int index, String id, int mode) {
        Object slot = slot(preset, index); Object skill = ReincarnatedSkills.call(slot, "getSkill");
        Object actualMode = ReincarnatedSkills.call(slot, "getMode");
        return id.equals(String.valueOf(ReincarnatedSkills.call(skill, "getRegistryName"))) && actualMode instanceof Number n && n.intValue() == mode;
    }
    static boolean clearSlot(int preset, int index) { return preset >= 0 && preset < presetCount() && index >= 0 && index < 3 && send(ReincarnatedSkills.callStatic(PACKETS, "changeAbilityPacket", index, preset, ResourceLocation.withDefaultNamespace("empty"), 0)); }

    static boolean toggle(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location != null && send(ReincarnatedSkills.callStatic(PACKETS, "toggleAbilityPacket", location));
    }

    private static boolean send(Object packet) {
        var player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().getConnection() == null) return false;
        if (packet instanceof CustomPacketPayload payload) {
            try {
                ArchitecturySkillTransport.send(payload);
                return true;
            } catch (ReflectiveOperationException | LinkageError | RuntimeException failure) {
                org.slf4j.LoggerFactory.getLogger(ReincarnatedSkillControls.class)
                        .warn("Unable to send native Tensura skill packet through Architectury", failure);
            }
        }
        player.displayClientMessage(Component.translatable("message.tensuranewworld.reincarnated.send_failed"), false);
        return false;
    }

    static String slotKey(int slot) {
        return key("ACTIVATE_SLOT_" + (slot + 1), slot == 0 ? "Z" : slot == 1 ? "X" : "C");
    }

    static String modeKey() { return key("NEXT_ABILITY_MODE", Component.translatable(
            "ui.tensuranewworld.skills.left_alt").getString()); }

    private static String key(String field, String fallback) {
        try {
            Object keybind = Class.forName(KEYS).getField(field).get(null);
            Object name = ReincarnatedSkills.call(keybind, "getTranslatedKeyMessage");
            if (!(name instanceof Component)) {
                Object mapping = ReincarnatedSkills.call(keybind, "getKeyMapping");
                name = ReincarnatedSkills.call(mapping, "getTranslatedKeyMessage");
            }
            return name instanceof Component component ? component.getString()
                    : Component.translatable("ui.tensuranewworld.skills.default_key", fallback).getString();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return Component.translatable("ui.tensuranewworld.skills.default_key", fallback).getString();
        }
    }
}
