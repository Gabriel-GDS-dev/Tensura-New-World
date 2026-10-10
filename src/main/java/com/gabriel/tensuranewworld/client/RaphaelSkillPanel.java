package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.data.SkillView;
import com.gabriel.tensuranewworld.registry.SkillAvailability;
import com.gabriel.tensuranewworld.registry.SkillCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Catalog and detail views share one viewport, including on small GUI scales. */
final class RaphaelSkillPanel {
    private String selectedId = "";
    private int selectedMode;
    private int categoryIndex;
    private boolean categoriesOpen;
    private int categoryOffset;
    private static final List<SkillCategory> CATEGORIES = List.of(
            SkillCategory.UNIQUE, SkillCategory.INTRINSIC, SkillCategory.COMMON, SkillCategory.EXTRA,
            SkillCategory.RESISTANCE, SkillCategory.ULTIMATE, SkillCategory.MANAS,
            SkillCategory.MAGIC, SkillCategory.BATTLEWILL, SkillCategory.OTHER);
    private boolean usableOnly;
    private String search = "";
    private int offset;
    private String feedback = "";
    private int feedbackTicks;
    private EditBox searchBox;
    private final Runnable rebuild;
    private final java.util.function.IntSupplier preset;
    private int x, top, right, bottom;
    private int lastPreset = -1;

    RaphaelSkillPanel(Runnable rebuild, java.util.function.IntSupplier preset) { this.rebuild = rebuild; this.preset = preset; }

    void build(int x, int top, int right, int bottom, Consumer<AbstractWidget> add) {
        this.x = x; this.top = top; this.right = right; this.bottom = bottom;
        searchBox = null;
        int width = right - x;
        lastPreset = preset.getAsInt();
        SkillView selected = selected();
        if (selected == null) {
            selectedId = "";
            int filterWidth = Math.max(42, (width - 6) * 2 / 3);
            add.accept(new TnwButton(x, top, filterWidth, 20,
                    Component.literal(categoryLabel(categoryIndex) + (categoriesOpen ? " ^" : " v")),
                    () -> { categoriesOpen = !categoriesOpen; categoryOffset = 0; rebuild.run(); }));
            add.accept(new TnwButton(x + filterWidth + 4, top, width - filterWidth - 4, 20,
                    Component.translatable(usableOnly ? "ui.tensuranewworld.skills.usable"
                            : "ui.tensuranewworld.skills.all"),
                    () -> { usableOnly = !usableOnly; offset = 0; rebuild.run(); }, () -> usableOnly));
            if (categoriesOpen) {
                categoryOffset = RaphaelViewport.clamp(categoryOffset, CATEGORIES.size() + 1, categoryRows());
                for (int index = categoryOffset; index < Math.min(CATEGORIES.size() + 1, categoryOffset + categoryRows()); index++) {
                    int choice = index;
                    TnwButton option = new TnwButton(x, top + 27 + (index - categoryOffset) * 23, width, 20,
                            Component.translatable("ui.tensuranewworld.skills.category_count",
                                    categoryLabel(index), categoryCount(index)), () -> {
                                categoryIndex = choice; categoriesOpen = false; offset = 0; rebuild.run();
                            }, () -> categoryIndex == choice);
                    if (index > 0) option.setAccent(CATEGORIES.get(index - 1).color, false);
                    add.accept(option);
                }
                return;
            }
            int presetWidth = presetButtonWidth(width);
            int searchWidth = ReincarnatedSkillControls.presetCount() > 0
                    ? Math.max(1, width - presetWidth - 9) : width - 6;
            searchBox = new EditBox(Minecraft.getInstance().font, x + 3, top + 27, searchWidth, 16,
                    Component.translatable("ui.tensuranewworld.skills.search"));
            searchBox.setMaxLength(100);
            searchBox.setHint(Component.translatable("ui.tensuranewworld.skills.search_hint"));
            searchBox.setValue(search);
            searchBox.setResponder(value -> {
                search = value; offset = 0;
                // Deferred rebuild preserves text input focus and cursor.
                needsRebuild = true;
            });
            add.accept(searchBox);
            if (ReincarnatedSkillControls.presetCount() > 0) {
                addPresetButton(x + width - presetWidth, top + 25, presetWidth, add);
            }
            List<SkillView> values = filtered();
            int rows = rows();
            offset = RaphaelViewport.clamp(offset, values.size(), rows);
            for (int index = offset; index < Math.min(values.size(), offset + rows); index++) {
                SkillView skill = values.get(index);
                int y = top + catalogStart() + (index - offset) * cardStride();
                TnwButton card = new TnwButton(x, y, width, cardHeight(), Component.literal(name(skill)), () -> {
                    selectedId = skill.id(); selectedMode = 0; offset = 0; feedback = ""; rebuild.run();
                });
                // Card text is drawn separately so long names remain within the panel.
                card.setDrawLabel(false);
                boolean vivid = skill.modes().stream().anyMatch(mode -> mode.availability().vivid);
                card.setAccent(vivid ? skill.category().color : 0xFF596873, !vivid);
                card.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(name(skill)
                        + "\n" + categoryLabel(skill.category()) + " / " + summary(skill)
                        + (skill.modes().isEmpty() ? "" : "\n" + skill.modes().get(0).hint()))));
                add.accept(card);
            }
        } else {
            selectedMode = Math.max(0, Math.min(selectedMode, selected.modes().size() - 1));
            int backWidth = Math.min(72, width / 3);
            add.accept(new TnwButton(x, top, backWidth, 20,
                    Component.translatable("ui.tensuranewworld.skills.back"), () -> {
                selectedId = ""; selectedMode = 0; offset = 0; rebuild.run();
            }));
            boolean hasModeControls = selected.modes().size() > 1;
            if (hasModeControls) {
                add.accept(new TnwButton(right - 54, top, 24, 20, Component.literal("<"), () -> cycleMode(-1)));
                add.accept(new TnwButton(right - 26, top, 24, 20, Component.literal(">"), () -> cycleMode(1)));
            }
            int presetControlWidth = width - backWidth - (hasModeControls ? 56 : 0) - 8;
            if (ReincarnatedSkillControls.presetCount() > 0 && presetControlWidth >= 34) {
                addPresetButton(x + backWidth + 4, top, presetControlWidth, add);
            }
            SkillView.Mode mode = mode(selected);
            int equipY = bottom - (compact() && !selected.toggleable() ? 21 : 44);
            if (mode != null && mode.slottable()) {
                int buttonWidth = (width - 8) / 3;
                for (int slot = 0; slot < 3; slot++) {
                    int index = slot;
                    TnwButton equip = new TnwButton(x + slot * (buttonWidth + 4), equipY, buttonWidth, 20,
                            Component.translatable("ui.tensuranewworld.skills.slot", slot + 1), () -> {
                                if (ReincarnatedSkillControls.equip(selected.id(), index, mode.index(), preset.getAsInt())) {
                                    feedback = Component.translatable("ui.tensuranewworld.skills.equip_requested",
                                            ReincarnatedSkillControls.slotKey(index)).getString();
                                    feedbackTicks = 100;
                                }
                            }, () -> ReincarnatedSkillControls.equipped(preset.getAsInt(), index, selected.id(), mode.index()));
                    equip.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(
                            Component.translatable("ui.tensuranewworld.skills.equip_tooltip", modeName(mode),
                                    slot + 1, ReincarnatedSkillControls.slotKey(slot)).getString())));
                    add.accept(equip);
                }
            }
            if (selected.toggleable()) {
                TnwButton toggle = new TnwButton(x, bottom - 21, width, 20,
                        Component.translatable(selected.toggled() ? "ui.tensuranewworld.skills.passive_off"
                                : "ui.tensuranewworld.skills.passive_on"),
                        () -> {
                            if (ReincarnatedSkillControls.toggle(selected.id())) {
                                feedback = Component.translatable("ui.tensuranewworld.skills.toggle_requested").getString();
                                feedbackTicks = 100;
                            }
                        }, selected::toggled);
                toggle.active = selected.canToggle();
                add.accept(toggle);
            }
        }
    }

    private boolean needsRebuild;

    void tick() {
        if (feedbackTicks > 0 && --feedbackTicks == 0) feedback = "";
        int currentPreset = preset.getAsInt();
        if (currentPreset != lastPreset) {
            lastPreset = currentPreset;
            rebuild.run();
        }
        if (needsRebuild) {
            needsRebuild = false;
            boolean focused = searchBox != null && searchBox.isFocused();
            int cursor = searchBox == null ? 0 : searchBox.getCursorPosition();
            rebuild.run();
            if (searchBox != null && focused) { searchBox.setFocused(true); searchBox.moveCursorTo(cursor, false); }
        }
    }

    boolean typing() { return searchBox != null && searchBox.isFocused(); }
    EditBox searchInput() { return searchBox; }

    private void cycleMode(int direction) {
        SkillView selected = selected();
        if (selected == null || selected.modes().isEmpty()) return;
        selectedMode = Math.floorMod(selectedMode + direction, selected.modes().size());
        offset = 0; rebuild.run();
    }

    private void addPresetButton(int buttonX, int buttonY, int buttonWidth, Consumer<AbstractWidget> add) {
        int count = ReincarnatedSkillControls.presetCount();
        int activePreset = Math.floorMod(preset.getAsInt(), count);
        String name = presetDisplayName(activePreset);
        TnwButton button = new TnwButton(buttonX, buttonY, buttonWidth, 20,
                Component.translatable("ui.tensuranewworld.skills.preset", activePreset + 1, count),
                () -> {
                    int nextPreset = (activePreset + 1) % count;
                    if (ReincarnatedSkillControls.activatePreset(nextPreset)) {
                        feedback = Component.translatable("ui.tensuranewworld.skills.preset_requested",
                                presetDisplayName(nextPreset)).getString();
                    } else {
                        feedback = Component.translatable("ui.tensuranewworld.skills.preset_failed").getString();
                    }
                    feedbackTicks = 100;
                });
        button.active = count > 1;
        button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable(
                "ui.tensuranewworld.skills.preset_tooltip", name)));
        add.accept(button);
    }

    private static String presetDisplayName(int presetIndex) {
        String name = ReincarnatedSkillControls.presetName(presetIndex);
        return name.isBlank()
                ? Component.translatable("ui.tensuranewworld.skills.preset_name", presetIndex + 1).getString()
                : name;
    }

    private static int presetButtonWidth(int panelWidth) {
        return Math.min(panelWidth, Math.min(104, Math.max(48, panelWidth / 3)));
    }

    void render(GuiGraphics graphics) {
        SkillView selected = selected();
        if (selected == null) renderCatalog(graphics);
        else renderDetails(graphics, selected);
    }

    // Text is drawn after widgets in RaphaelScreen, so card backgrounds never cover labels.
    private void renderCatalog(GuiGraphics graphics) {
        if (categoriesOpen) {
            text(graphics, clip(Component.translatable("ui.tensuranewworld.skills.category_hint").getString(),
                    right - x), x, bottom - 11, 0xFF91B2C4);
            return;
        }
        List<SkillView> values = filtered();
        int end = Math.min(values.size(), offset + rows());
        if (!compact()) text(graphics, Component.translatable("ui.tensuranewworld.skills.catalog_count",
                values.size()).getString(), x, top + 51, 0xFF91B2C4);
        for (int index = offset; index < end; index++) {
            SkillView skill = values.get(index);
            int y = top + catalogStart() + (index - offset) * cardStride();
            boolean vivid = skill.modes().stream().anyMatch(mode -> mode.availability().vivid);
            int accent = vivid ? skill.category().color : 0xFF82909A;
            graphics.fill(x + 2, y + 2, x + 4, y + cardHeight() - 2, accent);
            int iconSize = compact() ? 20 : 28;
            RpgTheme.diamond(graphics, x + 22, y + cardHeight() / 2, compact() ? 12 : 17, 0xFF35445B);
            var icon = net.minecraft.resources.ResourceLocation.tryParse(skill.icon());
            if (icon != null && Minecraft.getInstance().getResourceManager().getResource(icon).isPresent()) {
                graphics.blit(icon, x + 22 - iconSize / 2, y + (cardHeight() - iconSize) / 2,
                        0, 0, iconSize, iconSize, iconSize, iconSize);
            }
            int labelX = x + 45;
            int labelWidth = right - labelX - 7;
            text(graphics, clip(name(skill), labelWidth), labelX, y + 5, vivid ? 0xFFF2F8FF : 0xFF97A4AE);
            text(graphics, clip(categoryLabel(skill.category()) + "  /  " + summary(skill), labelWidth),
                    labelX, y + (compact() ? 13 : 17), accent);
            text(graphics, clip(costSummary(skill), labelWidth), labelX, y + (compact() ? 23 : 29), vivid ? 0xFFBBD6E4 : 0xFF85939E);
        }
        if (values.isEmpty()) {
            wrapped(graphics, Component.translatable("ui.tensuranewworld.skills.no_results").getString(),
                    x + 4, top + 69, right - x - 8, bottom - 17, 0xFFA8BDCA);
        }
        text(graphics, Component.translatable("ui.tensuranewworld.skills.scroll_range",
                values.isEmpty() ? 0 : offset + 1, end, values.size()).getString(), x, bottom - 11, 0xFF7B9EAE);
    }

    private void renderDetails(GuiGraphics graphics, SkillView skill) {
        SkillView.Mode mode = mode(skill);
        if (mode == null) return;
        int accent = mode.availability().vivid ? skill.category().color : 0xFF8796A3;
        int start = top + 24;
        graphics.fill(x, start, right, start + 2, accent);
        int iconSpace = !compact() && right - x >= 220 && !skill.icon().isBlank() ? 27 : 0;
        if (iconSpace > 0) {
            var location = net.minecraft.resources.ResourceLocation.tryParse(skill.icon());
            if (location != null) graphics.blit(location, x + 2, start + 6, 24, 24, 0, 0, 32, 32, 32, 32);
        }
        text(graphics, clip(name(skill), right - x - iconSpace - 4), x + iconSpace, start + 6, accent);
        if (!compact()) text(graphics, clip(categoryLabel(skill.category()) + "  //  "
                + availabilityLabel(mode.availability()), right - x - iconSpace - 4), x + iconSpace, start + 19, accent);
        List<String> paragraphs = new ArrayList<>();
        if (compact()) paragraphs.add(categoryLabel(skill.category()) + " / " + availabilityLabel(mode.availability()));
        paragraphs.add(Component.translatable("ui.tensuranewworld.skills.mode",
                selectedMode + 1, skill.modes().size(), modeName(mode)).getString());
        paragraphs.add(Component.translatable("ui.tensuranewworld.skills.use_cost",
                energy(mode.magiculeCost()), mode.auraCost() > 0
                        ? Component.translatable("ui.tensuranewworld.skills.aura_cost", energy(mode.auraCost())).getString()
                        : "").getString());
        paragraphs.add(Component.translatable("ui.tensuranewworld.skills.variable_costs").getString());
        if (mode.availability() == SkillAvailability.LEARNING || mode.availability() == SkillAvailability.MODE_LEARNING) {
            paragraphs.add(Component.translatable("ui.tensuranewworld.skills.training_cost",
                    energy(mode.trainingMagiculeCost()), mode.trainingAuraCost() > 0
                            ? Component.translatable("ui.tensuranewworld.skills.aura_cost",
                                    energy(mode.trainingAuraCost())).getString()
                            : "").getString());
        }
        if (mode.cooldownTicks() > 0) paragraphs.add(Component.translatable("ui.tensuranewworld.skills.cooldown",
                energy(Math.ceil(mode.cooldownTicks() / 20.0))).getString());
        if (skill.mastery() >= 0 && skill.maxMastery() > 0) paragraphs.add(Component.translatable(
                "ui.tensuranewworld.skills.mastery", energy(skill.mastery()), skill.maxMastery()).getString());
        if (skill.temporary()) paragraphs.add(Component.translatable("ui.tensuranewworld.skills.temporary",
                energy(Math.max(0, skill.remainingTicks()) / 20.0)).getString());
        if (skill.toggleable()) paragraphs.add(Component.translatable("ui.tensuranewworld.skills.passive_state",
                Component.translatable(skill.toggled() ? "ui.tensuranewworld.skills.enabled"
                        : "ui.tensuranewworld.skills.disabled").getString()).getString());
        paragraphs.add(mode.hint());
        if (!skill.unlockGuide().isBlank()) {
            paragraphs.addAll(List.of(skill.unlockGuide().split("\\n")));
        }
        if (mode.slottable()) {
            paragraphs.add(mode.slot() >= 0 ? Component.translatable("ui.tensuranewworld.skills.equipped_slot",
                    mode.slot() + 1, ReincarnatedSkillControls.slotKey(mode.slot())).getString()
                    : Component.translatable("ui.tensuranewworld.skills.choose_slot").getString());
            paragraphs.add(Component.translatable("ui.tensuranewworld.skills.slot_keys",
                    ReincarnatedSkillControls.slotKey(0), ReincarnatedSkillControls.slotKey(1),
                    ReincarnatedSkillControls.slotKey(2)).getString());
            paragraphs.add(Component.translatable("ui.tensuranewworld.skills.change_mode",
                    ReincarnatedSkillControls.modeKey()).getString());
        }
        String description = localized(skill.descriptionKey(), skill.description());
        if (!description.isBlank()) paragraphs.add(Component.translatable(
                "ui.tensuranewworld.skills.description", description).getString());
        paragraphs.add(Component.translatable("ui.tensuranewworld.skills.id", skill.id()).getString());
        List<FormattedCharSequence> lines = new ArrayList<>();
        var font = Minecraft.getInstance().font;
        for (String paragraph : paragraphs) {
            lines.addAll(font.split(Component.literal(paragraph), right - x - 12));
            lines.add(FormattedCharSequence.EMPTY);
        }
        int viewportTop = start + (compact() ? 18 : 36);
        int footerHeight = mode.slottable() && skill.toggleable() ? 59 : compact() ? 35 : 59;
        int viewportBottom = bottom - footerHeight;
        int visible = Math.max(1, (viewportBottom - viewportTop) / 12);
        offset = RaphaelViewport.clamp(offset, lines.size(), visible);
        graphics.enableScissor(x, viewportTop, right, Math.max(viewportTop, viewportBottom));
        for (int i = offset; i < Math.min(lines.size(), offset + visible); i++) {
            graphics.drawString(font, lines.get(i), x + 5, viewportTop + (i - offset) * 12,
                    mode.availability().vivid ? 0xFFDCE8F2 : 0xFF9CA9B4, false);
        }
        graphics.disableScissor();
        text(graphics, clip(feedback.isBlank() ? Component.translatable(
                "ui.tensuranewworld.skills.scroll_detail", offset + 1, lines.size()).getString() : feedback, right - x),
                x, bottom - footerHeight + 3, feedback.isBlank() ? 0xFF7B9EAE : 0xFF70E7C1);
    }

    boolean scroll(double mouseX, double mouseY, double delta) {
        if (delta == 0 || mouseX < x || mouseX >= right || mouseY < top || mouseY >= bottom) return false;
        if (categoriesOpen && selected() == null) {
            categoryOffset = Math.max(0, categoryOffset + (delta < 0 ? 1 : -1));
            rebuild.run();
            return true;
        }
        offset = Math.max(0, offset + (delta < 0 ? 1 : -1));
        if (selected() == null) rebuild.run();
        return true;
    }

    private boolean compact() { return bottom - top < 160; }
    private int catalogStart() { return compact() ? 46 : 65; }
    private int cardStride() { return compact() ? 35 : 47; }
    private int cardHeight() { return compact() ? 32 : 42; }
    private int rows() { return RaphaelViewport.rows(top + catalogStart(), bottom - 15, cardStride(), cardHeight()); }

    private int categoryRows() { return RaphaelViewport.rows(top + 27, bottom - 15, 23, 20); }
    private static String categoryLabel(int index) {
        return index == 0 ? Component.translatable("ui.tensuranewworld.skills.all_categories").getString()
                : categoryLabel(CATEGORIES.get(index - 1));
    }
    private static String categoryLabel(SkillCategory category) {
        return Component.translatable("ui.tensuranewworld.skills.category."
                + category.name().toLowerCase(Locale.ROOT)).getString();
    }
    private static String availabilityLabel(SkillAvailability availability) {
        return Component.translatable("ui.tensuranewworld.skills.availability."
                + availability.name().toLowerCase(Locale.ROOT)).getString();
    }
    private static long categoryCount(int index) {
        return ClientPlayerState.skills().stream()
                .filter(skill -> index == 0 || skill.category() == CATEGORIES.get(index - 1)).count();
    }
    boolean closeCategories() {
        if (!categoriesOpen) return false;
        categoriesOpen = false; rebuild.run(); return true;
    }

    private SkillView selected() {
        return ClientPlayerState.skills().stream().filter(skill -> skill.id().equals(selectedId)).findFirst().orElse(null);
    }

    private SkillView.Mode mode(SkillView skill) {
        return skill.modes().isEmpty() ? null : skill.modes().get(Math.min(selectedMode, skill.modes().size() - 1));
    }

    private List<SkillView> filtered() {
        String query = search.toLowerCase(Locale.ROOT).trim();
        return ClientPlayerState.skills().stream()
                .filter(skill -> categoryIndex == 0 || skill.category() == CATEGORIES.get(categoryIndex - 1))
                .filter(skill -> !usableOnly || skill.modes().stream().anyMatch(mode -> mode.availability().vivid))
                .filter(skill -> query.isBlank() || name(skill).toLowerCase(Locale.ROOT).contains(query)
                        || skill.id().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private static String summary(SkillView skill) {
        long available = skill.modes().stream().filter(mode -> mode.availability().vivid).count();
        return available > 0 ? Component.translatable("ui.tensuranewworld.skills.available_modes",
                available, skill.modes().size()).getString()
                : skill.modes().isEmpty() ? Component.translatable("ui.tensuranewworld.skills.no_modes").getString()
                        : availabilityLabel(skill.modes().get(0).availability());
    }

    private static String costSummary(SkillView skill) {
        double min = skill.modes().stream().mapToDouble(SkillView.Mode::magiculeCost).min().orElse(-1);
        double max = skill.modes().stream().mapToDouble(SkillView.Mode::magiculeCost).max().orElse(-1);
        return min < 0 ? Component.translatable("ui.tensuranewworld.skills.cost_unavailable").getString()
                : Component.translatable("ui.tensuranewworld.skills.cost_range",
                        min == max ? energy(min) : energy(min) + "-" + energy(max)).getString();
    }

    private static String name(SkillView skill) { return localized(skill.nameKey(), skill.fallbackName()); }
    private static String modeName(SkillView.Mode mode) { return localized(mode.nameKey(), mode.name()); }
    private static String localized(String key, String fallback) {
        return !key.isBlank() && net.minecraft.client.resources.language.I18n.exists(key)
                ? Component.translatable(key).getString() : fallback;
    }

    private static String energy(double value) {
        return value < 0 ? Component.translatable("ui.tensuranewworld.skills.not_reported").getString()
                : String.format(Locale.ROOT, value == Math.floor(value) ? "%.0f" : "%.1f", value);
    }

    private static String clip(String text, int width) {
        var font = Minecraft.getInstance().font;
        return font.width(text) <= width ? text : font.plainSubstrByWidth(text, Math.max(0, width - font.width("..."))) + "...";
    }

    private static void text(GuiGraphics graphics, String value, int x, int y, int color) {
        graphics.drawString(Minecraft.getInstance().font, value, x, y, color, false);
    }

    private static void wrapped(GuiGraphics graphics, String value, int x, int y, int width, int bottom, int color) {
        var font = Minecraft.getInstance().font;
        for (var line : font.split(Component.literal(value), width)) {
            if (y + font.lineHeight > bottom) break;
            graphics.drawString(font, line, x, y, color, false); y += 12;
        }
    }
}
