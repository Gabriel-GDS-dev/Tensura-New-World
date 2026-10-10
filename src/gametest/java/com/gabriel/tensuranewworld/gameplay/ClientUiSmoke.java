package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.client.RaphaelScreen;
import com.gabriel.tensuranewworld.client.TensuraTitleScreen;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in captures actual rendered screens in an isolated run directory. */
@EventBusSubscriber(modid = "tensuranewworld", value = Dist.CLIENT)
public final class ClientUiSmoke {
    private static int stage, ticks;
    private static final String[] NAMES = {"title", "character", "skills", "attributes", "worlds", "create-world", "hud", "character-live", "skills-live", "title-small", "stomach", "stomach-help", "stomach-help-end", "followers", "followers-help", "nation", "nation-help"};
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("tnw.uiSmoke") || (mc.screen == null && stage < 7) || mc.getOverlay() != null) return;
        if (stage == 0) {
            if (mc.screen.getClass().getSimpleName().contains("Accessibility")) mc.setScreen(new TensuraTitleScreen());
            if (!(mc.screen instanceof TensuraTitleScreen)) return;
            mc.options.guiScale().set(2); mc.resizeDisplay(); stage = 1; ticks = 0;
            mc.options.languageCode = "pt_br";
            mc.getLanguageManager().setSelected("pt_br"); mc.reloadResourcePacks();
        }
        if (stage == 6 && !(mc.screen instanceof CreateWorldScreen)) return;
        if (stage == 7) {
            if (mc.player == null || mc.level == null) return;
            if (mc.screen != null) { mc.setScreen(null); ticks = 0; return; }
        }
        if (++ticks < 20) return;
        Path folder = Path.of("screenshots", "rpg-review"); Files.createDirectories(folder);
        try (var screenshot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            screenshot.writeToFile(folder.resolve(NAMES[stage - 1] + ".png"));
        }
        ticks = 0;
        switch (stage++) {
            case 1 -> mc.setScreen(new RaphaelScreen());
            case 2 -> press(mc, "Habilidades");
            case 3 -> press(mc, "Atributos");
            case 4 -> mc.setScreen(new SelectWorldScreen(new TensuraTitleScreen()));
            case 5 -> CreateWorldScreen.openFresh(mc, mc.screen);
            case 6 -> {
                mc.createWorldOpenFlows().openWorld("UI Preview", () -> { throw new AssertionError("Preview world did not load"); });
            }
            case 7 -> {
                var camera = mc.gameRenderer.getMainCamera();
                for (boolean detached : new boolean[]{false, true}) for (boolean reverse : new boolean[]{false, true}) {
                    camera.setup(mc.level, mc.player, detached, reverse, 1);
                    if (camera.getPosition().y < mc.player.getY() + mc.player.getBbHeight() * 0.25F)
                        throw new AssertionError("Camera at feet after perspective switch");
                }
                mc.setScreen(new RaphaelScreen());
            }
            case 8 -> press(mc, "Habilidades");
            case 9 -> {
                mc.setScreen(new TensuraTitleScreen()); mc.options.guiScale().set(4); mc.resizeDisplay();
            }
            case 10 -> {
                mc.options.guiScale().set(2); mc.resizeDisplay(); mc.setScreen(new RaphaelScreen()); press(mc, "Estomago");
            }
            case 11 -> press(mc, "AJUDA");
            case 12 -> { for (int i = 0; i < 80; i++) mc.screen.mouseScrolled(mc.screen.width - 100, 100, 0, -1); }
            case 13 -> press(mc, "Seguidores");
            case 14 -> press(mc, "AJUDA");
            case 15 -> press(mc, "Nacao");
            case 16 -> press(mc, "AJUDA");
            case 17 -> {
                org.slf4j.LoggerFactory.getLogger(ClientUiSmoke.class).info("TNW RPG UI SMOKE PASSED: HUD, menus and scrollable stomach/followers/nation guides");
                mc.stop();
            }
        }
    }
    private static void press(Minecraft mc, String label) {
        mc.screen.children().stream().filter(c -> c instanceof AbstractButton b && b.getMessage().getString().equals(label))
                .map(c -> (AbstractButton)c).findFirst().orElseThrow().onPress();
    }
}
