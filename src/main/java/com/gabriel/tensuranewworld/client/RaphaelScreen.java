package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.data.CharacterAttribute;
import com.gabriel.tensuranewworld.data.MimicForm;
import com.gabriel.tensuranewworld.data.SubordinateData;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.registry.EvolutionPath;
import com.gabriel.tensuranewworld.registry.EvolutionRegistry;
import com.gabriel.tensuranewworld.registry.RaceDefinition;
import com.gabriel.tensuranewworld.registry.RaceRegistry;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.ModList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RaphaelScreen extends Screen {
    private static final int MIN_MARGIN = 10;
    private Tab tab = Tab.STATUS;
    private int scrollOffset;
    private boolean showingHelp;
    private TensuraPlayerData renderedSnapshot;
    private final RaphaelSkillPanel skillPanel = new RaphaelSkillPanel(
            this::rebuildInterface, ReincarnatedSkillControls::activePreset);

    public RaphaelScreen() {
        super(Component.translatable("screen.tensuranewworld.raphael"));
    }

    @Override
    protected void init() {
        rebuildInterface();
    }

    private void rebuildInterface() {
        var previousFocus = getFocused() instanceof net.minecraft.client.gui.components.AbstractWidget widget ? widget : null;
        boolean typing = !showingHelp && tab == Tab.SKILLS && skillPanel.typing();
        int cursor = typing ? skillPanel.searchInput().getCursorPosition() : 0;
        clearWidgets();
        Layout layout = layout();
        int gap = layout.panelHeight < 220 ? 0 : 2;
        int buttonHeight = Math.max(8, Math.min(22,
                (layout.panelHeight - 60 - gap * (Tab.values().length - 1)) / Tab.values().length));
        int y = layout.top + 53;
        for (Tab value : Tab.values()) {
            addRenderableWidget(new TnwButton(layout.left + 8, y, layout.navWidth - 16, buttonHeight,
                    Component.translatable(value.translationKey), () -> {
                        tab = value;
                        showingHelp = false;
                        scrollOffset = 0;
                        rebuildInterface();
                    }, () -> tab == value));
            y += buttonHeight + gap;
        }

        TensuraPlayerData data = ClientPlayerState.get();
        renderedSnapshot = data;
        addRenderableWidget(new TnwButton(layout.right - 76, layout.top + 12, 62, 22,
                Component.translatable(showingHelp ? "ui.tensuranewworld.back" : "ui.tensuranewworld.help"), () -> {
                    showingHelp = !showingHelp; scrollOffset = 0; rebuildInterface();
                }, () -> showingHelp));
        if (showingHelp) return;
        if (tab == Tab.EVOLUTION) {
            TnwButton evolve = new TnwButton(layout.right - 132, layout.bottom - 30, 116, 22,
                    Component.literal("EVOLUIR"), this::sendCommand);
            evolve.active = !data.getPendingEvolution().isBlank();
            addRenderableWidget(evolve);
        } else if (tab == Tab.ATTRIBUTES) {
            addAttributeButtons(layout, data);
        } else if (tab == Tab.FORMS) {
            addFormButtons(layout, data);
        } else if (tab == Tab.SKILLS) {
            skillPanel.build(contentX(layout), layout.top + 55, layout.right - 14, layout.bottom - 12,
                    this::addRenderableWidget);
            if (typing && skillPanel.searchInput() != null) {
                setFocused(skillPanel.searchInput());
                skillPanel.searchInput().moveCursorTo(cursor, false);
            }
        }
        if (!typing && previousFocus != null) {
            for (var child : children()) {
                if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget
                        && widget.getX() == previousFocus.getX() && widget.getY() == previousFocus.getY()
                        && widget.getMessage().equals(previousFocus.getMessage())) {
                    setFocused(widget);
                    break;
                }
            }
        }
    }

    private void addAttributeButtons(Layout layout, TensuraPlayerData data) {
        int x = contentX(layout);
        int width = contentWidth(layout);
        int buttonWidth = Math.min(68, Math.max(34, width / 3));
        int startY = layout.top + 121;
        int rows = attributeRows(layout);
        scrollOffset = RaphaelViewport.clamp(scrollOffset, CharacterAttribute.values().length, rows);
        for (int index = scrollOffset; index < Math.min(CharacterAttribute.values().length, scrollOffset + rows); index++) {
            CharacterAttribute attribute = CharacterAttribute.values()[index];
            int y = startY + (index - scrollOffset) * 31;
            TnwButton button = new TnwButton(layout.right - 12 - buttonWidth, y, buttonWidth, 20,
                    Component.literal("+ 1"), () -> sendCommand("tnw attribute " + attribute.id()));
            button.active = data.getAttributePoints() > 0 && data.getAttribute(attribute) < 100;
            addRenderableWidget(button);
        }
    }

    private void addFormButtons(Layout layout, TensuraPlayerData data) {
        if (ModList.get().isLoaded("morph")) {
            if (!data.getActiveMimicForm().isBlank()) {
                addRenderableWidget(new TnwButton(layout.right - 112, layout.top + 56, 96, 21,
                        Component.literal("LIMPAR NW"), () -> sendCommand("tnw form clear")));
            }
            return;
        }
        int width = contentWidth(layout);
        int rowHeight = 32;
        int startY = layout.top + 86;
        int visibleRows = formRows(layout);
        List<MimicForm> forms = data.getMimicForms();
        int first = RaphaelViewport.clamp(scrollOffset, forms.size(), visibleRows);
        for (int index = first; index < Math.min(forms.size(), first + visibleRows); index++) {
            MimicForm form = forms.get(index);
            int y = startY + (index - first) * rowHeight;
            int buttonWidth = Math.min(84, Math.max(40, width / 3));
            String command = "tnw form " + form.entityId();
            addRenderableWidget(new TnwButton(layout.right - 12 - buttonWidth, y, buttonWidth, 21,
                    Component.literal(form.entityId().equals(data.getActiveMimicForm()) ? "ATIVA" : "ASSUMIR"),
                    () -> sendCommand(command), () -> form.entityId().equals(data.getActiveMimicForm())));
        }
        if (!data.getActiveMimicForm().isBlank()) {
            TnwButton clear = new TnwButton(layout.right - 112, layout.top + 56, 96, 21,
                    Component.literal("ABANDONAR"), () -> sendCommand("tnw form clear"));
            addRenderableWidget(clear);
        }
    }

    private void sendCommand() {
        sendCommand("tnw evolve");
    }

    private void sendCommand(String command) {
        if (minecraft != null && minecraft.player != null && minecraft.player.connection != null) {
            minecraft.player.connection.sendCommand(command);
        }
    }

    @Override
    public void tick() {
        if (tab == Tab.SKILLS && !showingHelp) skillPanel.tick();
        if (renderedSnapshot != ClientPlayerState.get()) {
            rebuildInterface();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Screen.render calls this before its widgets. The custom background is
        // already drawn in render; vanilla blur here would blur our text too.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Layout layout = layout();
        int contentLeft = layout.left + layout.navWidth;
        TensuraUi.drawArtworkBackground(graphics, width, height, 0xB008101A);
        TensuraUi.drawFrame(graphics, layout.left, layout.top, layout.right, layout.bottom,
                0xF0121D30, 0xFFE3C58B);
        graphics.fill(layout.left + 4, layout.top + 4, contentLeft - 2, layout.bottom - 4, 0xF0182438);
        graphics.fill(contentLeft + 1, layout.top + 49, layout.right - 4, layout.bottom - 4, 0xD91A263B);
        graphics.fill(layout.left + 4, layout.top + 4, layout.right - 4, layout.top + 47, 0xF02A3448);
        graphics.fill(layout.left, layout.top + 47, layout.right, layout.top + 48, 0xFF72634F);
        graphics.fill(contentLeft, layout.top + 48, contentLeft + 1, layout.bottom, 0xFF72634F);
        TensuraUi.drawCircuitAccents(graphics, contentLeft + 3, layout.top + 49, layout.right - 4, layout.bottom - 4);

        drawReadable(graphics, "CRÔNICAS", layout.left + 12, layout.top + 9,
                0xFFE3C58B);
        drawReadable(graphics, "NEW WORLD", layout.left + 12, layout.top + 26,
                0xFFE7DECE);
        drawReadable(graphics, Component.translatable(tab.translationKey).getString().toUpperCase(java.util.Locale.ROOT),
                contentLeft + 14, layout.top + 16, 0xFFFFFFFF);
        drawReadable(graphics, font.plainSubstrByWidth("MAG  " + format(ClientPlayerState.get().getMagicules()),
                contentWidth(layout)), contentLeft + 14, layout.top + 31, 0xFF89D8D3);

        int contentTop = layout.top + 50;
        int contentBottom = layout.bottom - (showingHelp ? 28 : tab == Tab.EVOLUTION ? 34 : 10);
        graphics.enableScissor(contentLeft + 1, contentTop, layout.right - 8, contentBottom);
        renderContent(graphics, layout, ClientPlayerState.get());
        graphics.disableScissor();

        drawReadable(graphics, "R  FECHAR", layout.left + 10, layout.bottom - 15, 0xFFE7DECE);
        if (showingHelp) drawReadable(graphics, font.plainSubstrByWidth("ROLE PARA LER / ESC VOLTAR", contentWidth(layout)),
                contentLeft + 14, layout.bottom - 15, RpgTheme.GOLD);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (tab == Tab.SKILLS && !showingHelp) skillPanel.render(graphics);
    }

    private void renderContent(GuiGraphics graphics, Layout layout, TensuraPlayerData data) {
        if (tab == Tab.SKILLS && !showingHelp) return;
        int x = contentX(layout);
        int y = layout.top + 61;
        int contentWidth = contentWidth(layout);
        if (!showingHelp && tab == Tab.STATUS && scrollOffset == 0 && layout.panelHeight >= 275 && contentWidth >= 240) {
            renderCharacterCards(graphics, layout, data);
            return;
        }
        if (tab == Tab.ATTRIBUTES && !showingHelp) {
            renderAttributes(graphics, x, y, contentWidth, data);
            return;
        }
        if (tab == Tab.FORMS && !showingHelp) {
            renderForms(graphics, x, y, contentWidth, data);
            return;
        }
        List<DisplayLine> lines = linesFor(data);
        int lineOffset = 0;
        for (DisplayLine line : lines) {
            List<net.minecraft.util.FormattedCharSequence> wrapped =
                    font.split(Component.literal(line.text), contentWidth);
            for (net.minecraft.util.FormattedCharSequence part : wrapped) {
                if (lineOffset++ < scrollOffset) {
                    continue;
                }
                if (y + font.lineHeight > layout.bottom - (showingHelp ? 28 : tab == Tab.EVOLUTION ? 34 : 10)) {
                    return;
                }
                drawReadable(graphics, part, x, y, line.color);
                y += 16;
            }
            if (lineOffset++ >= scrollOffset) {
                y += line.gap;
            }
        }
    }

    private void renderAttributes(GuiGraphics graphics, int x, int y, int contentWidth,
            TensuraPlayerData data) {
        RaceDefinition race = RaceRegistry.get(data.getRaceId()).orElse(null);
        drawReadable(graphics, "Pontos de crescimento: " + data.getAttributePoints(),
                x, y, data.getAttributePoints() > 0 ? 0xFF65E59C : 0xFFE7DECE);
        y += 15;
        drawReadable(graphics, "Essencia: " + format(data.getEssence()), x, y, 0xFFE3C58B);
        y += 13;
        int threshold = (int) Math.max(1L, data.getEssenceForNextPoint());
        int progress = (int) Math.min(threshold, data.getEssenceProgress());
        graphics.fill(x, y, x + Math.max(20, contentWidth - 4), y + 7, 0xFF263941);
        graphics.fill(x, y, x + (int) ((long) Math.max(20, contentWidth - 4) * progress / threshold),
                y + 7, 0xFF2CAAB8);
        y += 14;
        drawReadable(graphics, format(data.getEssenceProgress()) + " / " + format(threshold)
                + " para o proximo ponto", x, y, 0xFFE7DECE);
        y += 18;
        Layout layout = layout();
        int rows = attributeRows(layout);
        int first = RaphaelViewport.clamp(scrollOffset, CharacterAttribute.values().length, rows);
        y = layout.top + 126;
        int labelWidth = Math.max(20, contentWidth - Math.min(68, Math.max(34, contentWidth / 3)) - 10);
        for (int index = first; index < Math.min(CharacterAttribute.values().length, first + rows); index++) {
            CharacterAttribute attribute = CharacterAttribute.values()[index];
            String favored = race != null && race.favoredAttribute() == attribute ? "  * racial" : "";
            drawReadable(graphics, font.plainSubstrByWidth(attribute.displayName() + "  " + data.getAttribute(attribute) + favored, labelWidth),
                    x, y, 0xFFE7DECE);
            y += 31;
        }
        if (race != null) {
            for (var line : font.split(Component.literal("Passiva: " + race.passive()), contentWidth)) {
                if (y + font.lineHeight >= layout.bottom - 10) break;
                drawReadable(graphics, line, x, y, 0xFFE7DECE);
                y += 12;
            }
        }
        if (CharacterAttribute.values().length > rows) {
            drawReadable(graphics, "Role para ver os outros atributos", x, layout.bottom - 19, 0xFFE3C58B);
        }
    }

    private void renderForms(GuiGraphics graphics, int x, int y, int contentWidth, TensuraPlayerData data) {
        if (ModList.get().isLoaded("morph")) {
            drawReadable(graphics, "MORPH // TRANSFORMACAO ANIMADA ATIVA", x, y, 0xFFE3C58B);
            drawReadable(graphics, "Use [ ou ] para abrir a selecao do Morph.", x, y + 18, 0xFFFFFFFF);
            drawReadable(graphics, "Enter/clique transforma; Esc fecha a selecao.", x, y + 36, 0xFFE7DECE);
            var body = ClientPlayerState.body();
            var player = minecraft == null ? null : minecraft.player;
            java.util.List<String> descriptions = new java.util.ArrayList<>();
            descriptions.add("Corpo atual: " + (body.name().isBlank() ? data.getRaceId() : body.name()));
            if (player != null) {
                descriptions.add(String.format(java.util.Locale.ROOT, "Vida %.1f / %.1f | Ataque %.1f | Armadura %d",
                        player.getHealth(), player.getMaxHealth(),
                        player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), player.getArmorValue()));
                descriptions.add(String.format(java.util.Locale.ROOT, "Velocidade x%.2f | %s",
                        player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) / .1,
                        body.needsFood() ? "Precisa se alimentar" : "Nao precisa se alimentar"));
            }
            descriptions.add("Atributos do corpo + seus investimentos e equipamentos.");
            descriptions.add("Voo: aves voadoras, morcegos e criaturas aereas. Dois toques em Espaco; Shift desce.");
            descriptions.add("Galinha plana; formas terrestres nao voam. Ctrl ativa a corrida.");
            descriptions.add("Transformar preserva a proporcao da vida e a alimentacao anterior.");
            y += 57;
            for (String description : descriptions) {
                for (var line : font.split(Component.literal(description), contentWidth)) {
                    if (y + 10 > layout().bottom - 10) return;
                    graphics.drawString(font, line, x, y, 0xFFE7DECE, false);
                    y += 12;
                }
                y += 4;
            }
            return;
        }
        if (!RaceRegistry.isSlime(data.getRaceId())) {
            drawReadable(graphics, "Mimetizacao New World disponivel para linhagens Slime.", x, y, 0xFFFFFFFF);
            drawReadable(graphics, "Derrote criaturas com Predator para registrar formas.", x, y + 18, 0xFFE7DECE);
            return;
        }
        drawReadable(graphics, "FORMAS REGISTRADAS: " + data.getMimicForms().size(), x, y, 0xFFE3C58B);
        y += 21;
        List<MimicForm> forms = data.getMimicForms();
        int rowHeight = 32;
        int startY = layout().top + 86;
        int visibleRows = formRows(layout());
        int first = RaphaelViewport.clamp(scrollOffset, forms.size(), visibleRows);
        y = startY;
        for (int index = first; index < Math.min(forms.size(), first + visibleRows); index++) {
            MimicForm form = forms.get(index);
            String active = form.entityId().equals(data.getActiveMimicForm()) ? "  [ATIVA]" : "";
            int labelWidth = Math.max(20, contentWidth - Math.min(84, Math.max(40, contentWidth / 3)) - 10);
            drawReadable(graphics, font.plainSubstrByWidth(form.displayName() + active, labelWidth), x, y, 0xFFFFFFFF);
            drawReadable(graphics, "x" + form.defeatedCount() + "  HP "
                    + format(Math.round(form.maxHealth())) + "  ATQ " + format(Math.round(form.attackDamage()))
                    + "  ARM " + format(Math.round(form.armor())), x, y + 15, 0xFFE7DECE);
            y += rowHeight;
        }
        if (forms.isEmpty()) {
            drawReadable(graphics, "Nenhuma forma registrada. Cace mobs para absorve-los.",
                    x, y, 0xFFE7DECE);
        }
    }

    private void renderCharacterCards(GuiGraphics g, Layout layout, TensuraPlayerData data) {
        int x = contentX(layout), y = layout.top + 61, w = contentWidth(layout);
        var race = RaceRegistry.get(data.getRaceId()).orElse(null);
        TensuraUi.drawFrame(g, x, y, x + w, y + 47, 0xFF26344A, RpgTheme.GOLD);
        RpgTheme.diamond(g, x + 23, y + 23, 14, 0xFF415069);
        g.drawCenteredString(font, rank(data.getExistencePoints()), x + 23, y + 20, RpgTheme.GOLD);
        drawReadable(g, font.plainSubstrByWidth(race == null ? data.getRaceId() : race.displayName(), w - 55), x + 48, y + 9, RpgTheme.TEXT);
        drawReadable(g, font.plainSubstrByWidth(displayStage(data.getEvolutionStage()), w - 55), x + 48, y + 26, RpgTheme.GOLD);
        String[][] cards = {
            {"EXISTÊNCIA", format(data.getExistencePoints()) + " EP"},
            {"MAGÍCULAS", format(data.getMagicules()) + " / " + format(data.getMaxMagicules())},
            {"ESSÊNCIA", format(data.getEssence())}, {"ALMAS", Long.toString(data.getSoulPoints())},
            {"BATALHAS", Long.toString(data.getBattlesWon())}, {"CRESCIMENTO", Integer.toString(data.getAttributePoints())}
        };
        int cardWidth = (w - 8) / 2;
        for (int i = 0; i < cards.length; i++) {
            int cx = x + (i % 2) * (cardWidth + 8), cy = y + 57 + (i / 2) * 42;
            g.fillGradient(cx, cy, cx + cardWidth, cy + 36, 0xF02A374E, 0xE01D293D);
            g.fill(cx, cy + 35, cx + cardWidth, cy + 36, 0x667C6C52);
            drawReadable(g, cards[i][0], cx + 9, cy + 6, 0xFFAFB6C4);
            drawReadable(g, font.plainSubstrByWidth(cards[i][1], cardWidth - 18), cx + 9, cy + 21, RpgTheme.TEXT);
        }
        int storyY = y + 187;
        RpgTheme.rule(g, x, x + w, storyY);
        drawReadable(g, "CAPÍTULO " + data.getStoryChapter() + "  /  SUA JORNADA", x, storyY + 10, RpgTheme.GOLD);
        int textY = storyY + 25;
        for (var line : font.split(Component.literal(data.getStoryObjective()), w)) {
            if (textY + 9 > layout.bottom - 12) break;
            g.drawString(font, line, x, textY, RpgTheme.TEXT, false); textY += 12;
        }
    }

    private List<DisplayLine> linesFor(TensuraPlayerData data) {
        List<DisplayLine> lines = new ArrayList<>();
        if (showingHelp) {
            var guide = MenuGuide.lines(tab.name());
            lines.add(DisplayLine.heading(guide.getFirst()));
            guide.stream().skip(1).forEach(text -> lines.add(DisplayLine.normal(text)));
            var race = RaceRegistry.get(data.getRaceId()).orElse(null);
            if (race != null) {
                lines.add(DisplayLine.heading("SUA ESPÉCIE: " + race.displayName()));
                lines.add(DisplayLine.normal("Linhagem " + race.lineage() + "; atributo favorecido: " + race.favoredAttribute().displayName()
                        + "; Essência base por ponto: " + race.essencePerGrowthPoint() + "."));
                lines.add(DisplayLine.normal("Descrição racial: " + race.passive()));
                lines.add(DisplayLine.normal("Status, Atributos, Evolução, Habilidades, Seguidores e Nação usam estas mesmas abas para todas as espécies. As condições raciais ficam nas fichas de evolução/habilidade."));
            }
            return lines;
        }
        switch (tab) {
            case EVOLUTION -> {
                lines.add(DisplayLine.normal("ESTAGIO ATUAL"));
                lines.add(DisplayLine.heading(displayStage(data.getEvolutionStage())));
                List<EvolutionPath> next = EvolutionRegistry.next(data.getEvolutionStage());
                if (next.isEmpty()) {
                    lines.add(DisplayLine.normal("Esta linhagem alcancou seu apice conhecido."));
                    break;
                }
                EvolutionPath path = next.get(0);
                lines.add(DisplayLine.heading("PROXIMA FORMA  " + path.displayName()));
                lines.add(DisplayLine.normal(path.description()));
                for (var condition : path.conditions()) {
                    boolean complete = condition.isMet(data);
                    lines.add(new DisplayLine((complete ? "[OK] " : "[  ] ") + condition.label(data),
                            complete ? 0xFF65E59C : 0xFFE7DECE, 4));
                }
                if (path.harvestOnly()) {
                    lines.add(new DisplayLine("Concluida pelo Festival da Colheita.", 0xFFFFCC73, 2));
                } else if (!data.getPendingEvolution().isBlank()) {
                    lines.add(DisplayLine.accent("Evolucao pronta: confirme no botao abaixo."));
                }
            }
            case STATUS -> {
                RaceDefinition race = RaceRegistry.get(data.getRaceId()).orElse(null);
                lines.add(DisplayLine.heading(displayStage(data.getEvolutionStage()) + "  //  Rank " + rank(data.getExistencePoints())));
                lines.add(DisplayLine.normal("Raca  " + (race == null ? data.getRaceId() : race.displayName())));
                if (race != null) lines.add(DisplayLine.normal("Passiva  " + race.passive()));
                lines.add(DisplayLine.accent("EP  " + format(data.getExistencePoints())));
                lines.add(DisplayLine.accent("Essencia  " + format(data.getEssence())
                        + "  |  crescimento " + data.getAttributePoints()));
                lines.add(DisplayLine.normal("Magicules  " + format(data.getMagicules()) + " / " + format(data.getMaxMagicules())));
                lines.add(DisplayLine.normal("Almas humanas  " + data.getSoulPoints()));
                lines.add(DisplayLine.normal("Batalhas vencidas  " + data.getBattlesWon()));
                lines.add(DisplayLine.normal("Semente de Lorde Demonio  " + yesNo(data.isDemonLordSeed())));
                lines.add(DisplayLine.normal("Lorde Demonio Verdadeiro  " + yesNo(data.isTrueDemonLord())));
                lines.add(DisplayLine.heading("CAPITULO " + data.getStoryChapter()));
                lines.add(DisplayLine.normal(data.getStoryObjective()));
            }
            case STOMACH -> {
                lines.add(DisplayLine.heading(Component.translatable("ui.tensuranewworld.stomach.partial_heading").getString()));
                lines.add(DisplayLine.normal(Component.translatable("ui.tensuranewworld.stomach.partial_help").getString()));
                if (data.getStomach().isEmpty()) {
                    lines.add(DisplayLine.normal(Component.translatable("ui.tensuranewworld.stomach.empty").getString()));
                }
                data.getStomach().forEach(entry -> lines.add(DisplayLine.normal(entry.itemId() + "  x" + entry.count())));
            }
            case SUBORDINATES -> {
                lines.add(DisplayLine.heading("CRIATURAS NOMEADAS / TODAS AS RAÇAS"));
                lines.add(DisplayLine.normal("Registre com /tnw naming_nearest Nome a até 8 blocos. Nomear custa magículas e não faz um inimigo seguir ou ficar aliado. AJUDA explica custo, alvo e Dom."));
                if (data.getSubordinates().isEmpty()) lines.add(DisplayLine.normal("Nenhum subordinado nomeado."));
                for (SubordinateData value : data.getSubordinates()) {
                    lines.add(DisplayLine.normal(value.name() + "  //  " + value.raceId() + "  //  EP "
                            + format(value.existencePoints()) + (value.giftGranted() ? "  //  Dom concedido" : "")));
                }
            }
            case NATION -> {
                lines.add(DisplayLine.normal("Para todas as raças: nomeie pelo menos 5 criaturas e use /tnw nation_found Nome da Nação. Este painel é um registro de progressão; AJUDA explica as funções disponíveis."));
                lines.add(DisplayLine.heading(data.getNationName().isBlank() ? "NENHUMA NACAO FUNDADA" : data.getNationName()));
                lines.add(DisplayLine.normal("Alinhamento  " + data.getNationAlignment()));
                data.getDiplomacy().forEach((nation, score) -> lines.add(DisplayLine.normal(nation + "  " + score)));
            }
            default -> { }
        }
        return lines;
    }

    private Layout layout() {
        int panelWidth = Math.max(1, Math.min(860, width - MIN_MARGIN * 2));
        int panelHeight = Math.max(1, Math.min(500, height - MIN_MARGIN * 2));
        int left = Math.max(0, (width - panelWidth) / 2);
        int top = Math.max(0, (height - panelHeight) / 2);
        int navWidth = Math.min(142, Math.max(72, panelWidth / 3));
        return new Layout(left, top, left + panelWidth, top + panelHeight, navWidth, panelHeight);
    }

    private int contentX(Layout layout) {
        return layout.left + layout.navWidth + 14;
    }

    private int contentWidth(Layout layout) {
        return Math.max(20, layout.right - contentX(layout) - 14);
    }

    private void drawReadable(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.drawString(font, text, 0, 0, color, true);
        graphics.pose().popPose();
    }

    private void drawReadable(GuiGraphics graphics, net.minecraft.util.FormattedCharSequence text,
            int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.drawString(font, text, 0, 0, color, true);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (tab == Tab.SKILLS && !showingHelp) return skillPanel.scroll(mouseX, mouseY, scrollY);
        Layout layout = layout();
        if (scrollY == 0 || mouseX < contentX(layout) || mouseX >= layout.right
                || mouseY < layout.top + 50 || mouseY >= layout.bottom) return false;
        int requested = Math.max(0, scrollOffset + (scrollY < 0 ? 1 : -1));
        if (!showingHelp && (tab == Tab.FORMS || tab == Tab.ATTRIBUTES)) {
            int count = tab == Tab.FORMS ? ClientPlayerState.get().getMimicForms().size() : CharacterAttribute.values().length;
            int rows = tab == Tab.FORMS ? formRows(layout) : attributeRows(layout);
            scrollOffset = RaphaelViewport.clamp(requested, count, rows);
            rebuildInterface();
        } else {
            int count = 0;
            for (DisplayLine line : linesFor(ClientPlayerState.get())) {
                count += font.split(Component.literal(line.text), contentWidth(layout)).size() + 1;
            }
            scrollOffset = RaphaelViewport.clamp(requested, Math.max(0, count - 1), 1);
        }
        return true;
    }

    private int attributeRows(Layout layout) {
        return RaphaelViewport.rows(layout.top + 121, layout.bottom - 26, 31, 20);
    }

    private int formRows(Layout layout) {
        return RaphaelViewport.rows(layout.top + 86, layout.bottom - 10, 32, 24);
    }

    private static String displayStage(String stage) {
        String value = stage.replace('_', ' ');
        return value.isEmpty() ? "Unknown" : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static String rank(long ep) {
        if (ep >= 10_000_000L) return "S";
        if (ep >= 1_000_000L) return "A";
        if (ep >= 500_000L) return "B";
        if (ep >= 100_000L) return "C";
        if (ep >= 50_000L) return "D";
        if (ep >= 10_000L) return "E";
        return "F";
    }

    private static String format(long value) {
        if (value >= 1_000_000L) return String.format(java.util.Locale.ROOT, "%.1fM", value / 1_000_000.0D);
        if (value >= 1_000L) return String.format(java.util.Locale.ROOT, "%.1fK", value / 1_000.0D);
        return Long.toString(value);
    }

    private static String yesNo(boolean value) {
        return value ? "Sim" : "Nao";
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showingHelp && keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            showingHelp = false; scrollOffset = 0; rebuildInterface(); return true;
        }
        if (tab == Tab.SKILLS && !showingHelp && keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && skillPanel.closeCategories()) return true;
        if (tab == Tab.SKILLS && !showingHelp && skillPanel.typing() && keyCode != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (ClientKeyMappings.OPEN_RAPHAEL.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Layout(int left, int top, int right, int bottom, int navWidth, int panelHeight) { }

    private record DisplayLine(String text, int color, int gap) {
        static DisplayLine normal(String text) { return new DisplayLine(text, 0xFFE7DECE, 2); }
        static DisplayLine accent(String text) { return new DisplayLine(text, 0xFFE3C58B, 2); }
        static DisplayLine heading(String text) { return new DisplayLine(text, 0xFFFFFFFF, 7); }
    }

    private enum Tab {
        STATUS("status"),
        ATTRIBUTES("attributes"),
        EVOLUTION("evolution"),
        FORMS("forms"),
        SKILLS("skills"),
        STOMACH("stomach"),
        SUBORDINATES("subordinates"),
        NATION("nation");

        private final String translationKey;
        Tab(String translationKey) {
            this.translationKey = "ui.tensuranewworld.tab." + translationKey;
        }
    }
}
