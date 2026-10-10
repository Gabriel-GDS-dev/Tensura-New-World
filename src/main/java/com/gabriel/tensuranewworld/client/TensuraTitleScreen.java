package com.gabriel.tensuranewworld.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

public final class TensuraTitleScreen extends Screen {
    public TensuraTitleScreen() { super(Component.literal("Tensura - New World")); }
    private TitleLayout layout() { return TitleLayout.fit(width, height); }
    private int panelWidth() { return layout().width(); }
    private int panelX() { return layout().left(); }
    private int menuTop() { return layout().menuTop(); }
    private int buttonHeight() { return layout().buttonHeight(); }
    @Override protected void init() {
        int x = panelX() + 22, w = panelWidth() - 44, y = menuTop(), step = buttonHeight() + layout().gap();
        addRenderableWidget(new TnwButton(x, y, w, buttonHeight(), Component.literal("INICIAR JORNADA"),
                () -> minecraft.setScreen(new SelectWorldScreen(this))));
        addRenderableWidget(new TnwButton(x, y + step, w, buttonHeight(), Component.literal("MULTIJOGADOR"),
                () -> minecraft.setScreen(new JoinMultiplayerScreen(this))));
                addRenderableWidget(new TnwButton(x, y + step * 2, w, buttonHeight(), Component.literal("CONFIGURAÇÕES"),
                () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))));
        addRenderableWidget(new TnwButton(x, y + step * 3, w, buttonHeight(), Component.literal("SAIR"), minecraft::stop));
    }
    @Override public void renderBackground(GuiGraphics g, int mx, int my, float delta) {
        TensuraUi.drawArtworkBackground(g, width, height, 0x330B1220);
        g.fillGradient(0, 0, width, height, 0x22091525, 0xCB0D1729);
        int x = panelX(), w = panelWidth();
        TensuraUi.drawFrame(g, x, 18, x + w, height - 18, RpgTheme.INK, RpgTheme.GOLD);
        if (height >= 240) RpgTheme.rule(g, x + 22, x + w - 22, menuTop() - 15);
        if (width > 580) {
            int heroX = 34, heroY = height / 2;
            RpgTheme.diamond(g, heroX + 12, heroY - 38, 9, RpgTheme.GOLD);
            g.drawString(font, "UMA NOVA EXISTÊNCIA", heroX, heroY - 14, RpgTheme.GOLD, false);
            float scale = Math.min(2, (panelX() - heroX - 25F) / font.width("Seu destino começa aqui."));
            g.pose().pushPose(); g.pose().translate(heroX, heroY + 5, 0); g.pose().scale(scale, scale, 1);
            g.drawString(font, "Seu destino começa aqui.", 0, 0, RpgTheme.TEXT, false); g.pose().popPose();
            g.drawString(font, "Explore. Aprenda. Evolua.", heroX, heroY + 36, 0xFFD2D7DE, false);
        }
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        super.render(g, mx, my, delta);
        int x = panelX(), w = panelWidth(), titleY = height < 240 ? 30 : Math.max(35, menuTop() - 77);
        RpgTheme.diamond(g, x + w / 2, titleY - 9, 4, RpgTheme.GOLD);
        g.pose().pushPose(); g.pose().translate(x + w / 2 - font.width("TENSURA"), titleY + 6, 0);
        g.pose().scale(2, 2, 1); g.drawString(font, "TENSURA", 0, 0, RpgTheme.TEXT, false); g.pose().popPose();
        g.drawCenteredString(font, "N E W  W O R L D", x + w / 2, titleY + 30, RpgTheme.GOLD);
        String version = net.neoforged.fml.ModList.get().getModContainerById("tensuranewworld")
                .map(container -> container.getModInfo().getVersion().toString()).orElse("dev");
        g.drawCenteredString(font, version.toUpperCase(java.util.Locale.ROOT).replace('-', ' ') + "  /  CRÔNICAS DE TEMPEST",
                width / 2, height - 11, 0xFFD3C9B8);
    }
    @Override public boolean isPauseScreen() { return false; }
}
