package com.gabriel.tensuranewworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;

/** Crosshair block inspection in the same card style as the combat target. */
public final class BlockInspectionHud {
    private BlockInspectionHud() { }

    public static void render(GuiGraphics graphics) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.player.isSpectator()
                || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        var state = mc.level.getBlockState(hit.getBlockPos());
        if (state.isAir()) return;
        int width = Math.min(214, (graphics.guiWidth() - 30) / 2);
        if (width < 100) return;
        int x = 12, y = 12;
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String mod = ModList.get().getModContainerById(id.getNamespace())
                .map(container -> container.getModInfo().getDisplayName()).orElse(id.getNamespace());
        boolean ore = state.is(Tags.Blocks.ORES);
        boolean correct = mc.player.getMainHandItem().isCorrectToolForDrops(state);
        boolean harvest = !state.requiresCorrectToolForDrops() || correct;
        TensuraUi.drawFrame(graphics, x, y, x + width, y + (ore ? 79 : 65), RpgTheme.INK, RpgTheme.GOLD);
        graphics.renderItem(new ItemStack(state.getBlock().asItem()), x + 7, y + 7);
        text(graphics, state.getBlock().getName().getString(), x + 28, y + 7, width - 36, RpgTheme.TEXT);
        text(graphics, mod, x + 28, y + 20, width - 36, 0xFF83CDD9);
        text(graphics, id.toString(), x + 8, y + 34, width - 16, 0xFFAFB6C4);
        text(graphics, harvest ? "Ferramenta adequada" : "Requer ferramenta melhor", x + 8, y + 48,
                width - 16, harvest ? 0xFF91CF9A : 0xFFEB9898);
        if (ore && ModList.get().isLoaded("veinminer")) {
            String sneak = mc.options.keyShift.getTranslatedKeyMessage().getString();
            text(graphics, "VeinMiner: segure " + sneak + " + mine", x + 8, y + 62, width - 16, RpgTheme.GOLD);
        }
    }

    private static void text(GuiGraphics graphics, String text, int x, int y, int width, int color) {
        graphics.drawString(Minecraft.getInstance().font,
                Minecraft.getInstance().font.plainSubstrByWidth(text, Math.max(0, width)), x, y, color, true);
    }
}
