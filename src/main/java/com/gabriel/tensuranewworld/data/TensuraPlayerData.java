package com.gabriel.tensuranewworld.data;

import com.mojang.serialization.Codec;
import com.gabriel.tensuranewworld.registry.EvolutionRegistry;
import com.gabriel.tensuranewworld.registry.RaceRegistry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class TensuraPlayerData {
    public static final Codec<TensuraPlayerData> CODEC = CompoundTag.CODEC.xmap(TensuraPlayerData::load, TensuraPlayerData::save);

    private String raceId = "tensura:slime";
    private String evolutionStage = "";
    private String pendingEvolution = "";
    private long magicules = 1_000L;
    private long maxMagicules = 1_000L;
    private long basePhysicalPower = 400L;
    private long existencePoints;
    private long essence;
    private long essenceProgress;
    private int attributePoints;
    private int soulPoints;
    private int battlesWon;
    private int notableActs;
    private int harvestSleepTicks;
    private int storyChapter;
    private String storyObjective = "Awaken in the new world";
    private String nationName = "";
    private String nationAlignment = "Neutral";
    private boolean demonLordSeed;
    private boolean trueDemonLord;
    private boolean heroEgg;
    private boolean trueHero;
    private boolean harvestFestivalActive;
    private boolean raphaelAwakened;
    private boolean cielAwakened;
    private boolean receivedName;
    private boolean masteredCombatSkill;
    private boolean elementalBlessing;
    private boolean survivedCriticalHunger;
    private boolean absorbedHungerFactor;
    private boolean soulContract;
    private boolean wonNetherDuel;
    private final Set<String> skills = new LinkedHashSet<>();
    private final Set<String> titles = new LinkedHashSet<>();
    private final Map<CharacterAttribute, Integer> attributes = new EnumMap<>(CharacterAttribute.class);
    private final List<StomachEntry> stomach = new ArrayList<>();
    private final Map<String, MimicForm> mimicForms = new LinkedHashMap<>();
    private final List<SubordinateData> subordinates = new ArrayList<>();
    private final Map<String, Integer> diplomacy = new LinkedHashMap<>();
    private String activeMimicForm = "";

    public TensuraPlayerData() {
        skills.add("tensuranewworld:predator");
        skills.add("tensuranewworld:great_sage");
        recalculateExistencePoints();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("race_id", raceId);
        tag.putString("evolution_stage", evolutionStage);
        tag.putString("pending_evolution", pendingEvolution);
        tag.putLong("magicules", magicules);
        tag.putLong("max_magicules", maxMagicules);
        tag.putLong("base_physical_power", basePhysicalPower);
        tag.putLong("existence_points", existencePoints);
        tag.putLong("essence", essence);
        tag.putLong("essence_progress", essenceProgress);
        tag.putInt("attribute_points", attributePoints);
        tag.putString("active_mimic_form", activeMimicForm);
        tag.putInt("soul_points", soulPoints);
        tag.putInt("battles_won", battlesWon);
        tag.putInt("notable_acts", notableActs);
        tag.putInt("harvest_sleep_ticks", harvestSleepTicks);
        tag.putInt("story_chapter", storyChapter);
        tag.putString("story_objective", storyObjective);
        tag.putString("nation_name", nationName);
        tag.putString("nation_alignment", nationAlignment);
        tag.putBoolean("demon_lord_seed", demonLordSeed);
        tag.putBoolean("true_demon_lord", trueDemonLord);
        tag.putBoolean("hero_egg", heroEgg);
        tag.putBoolean("true_hero", trueHero);
        tag.putBoolean("harvest_festival_active", harvestFestivalActive);
        tag.putBoolean("raphael_awakened", raphaelAwakened);
        tag.putBoolean("ciel_awakened", cielAwakened);
        tag.putBoolean("received_name", receivedName);
        tag.putBoolean("mastered_combat_skill", masteredCombatSkill);
        tag.putBoolean("elemental_blessing", elementalBlessing);
        tag.putBoolean("survived_critical_hunger", survivedCriticalHunger);
        tag.putBoolean("absorbed_hunger_factor", absorbedHungerFactor);
        tag.putBoolean("soul_contract", soulContract);
        tag.putBoolean("won_nether_duel", wonNetherDuel);
        tag.put("skills", saveStrings(skills));
        tag.put("titles", saveStrings(titles));

        CompoundTag attributeTag = new CompoundTag();
        attributes.forEach((attribute, value) -> attributeTag.putInt(attribute.id(), value));
        tag.put("attributes", attributeTag);

        ListTag stomachTags = new ListTag();
        stomach.forEach(entry -> stomachTags.add(entry.save()));
        tag.put("stomach", stomachTags);

        ListTag mimicTags = new ListTag();
        mimicForms.values().forEach(form -> mimicTags.add(form.save()));
        tag.put("mimic_forms", mimicTags);

        ListTag subordinateTags = new ListTag();
        subordinates.forEach(entry -> subordinateTags.add(entry.save()));
        tag.put("subordinates", subordinateTags);

        CompoundTag diplomacyTag = new CompoundTag();
        diplomacy.forEach(diplomacyTag::putInt);
        tag.put("diplomacy", diplomacyTag);
        return tag;
    }

    public static TensuraPlayerData load(CompoundTag tag) {
        TensuraPlayerData data = new TensuraPlayerData();
        data.raceId = stringOr(tag, "race_id", data.raceId);
        data.evolutionStage = tag.contains("evolution_stage")
                ? stringOr(tag, "evolution_stage", EvolutionRegistry.inferStage(data.raceId))
                : EvolutionRegistry.inferStage(data.raceId);
        data.pendingEvolution = tag.getString("pending_evolution");
        data.magicules = longOr(tag, "magicules", data.magicules);
        data.maxMagicules = Math.max(1L, longOr(tag, "max_magicules", data.maxMagicules));
        data.basePhysicalPower = Math.max(1L, longOr(tag, "base_physical_power", data.basePhysicalPower));
        data.existencePoints = Math.max(0L, longOr(tag, "existence_points", data.existencePoints));
        data.essence = Math.max(0L, longOr(tag, "essence", 0L));
        data.essenceProgress = Math.max(0L, longOr(tag, "essence_progress", 0L));
        data.attributePoints = Math.max(0, tag.getInt("attribute_points"));
        data.activeMimicForm = tag.getString("active_mimic_form");
        data.soulPoints = Math.max(0, tag.getInt("soul_points"));
        data.battlesWon = Math.max(0, tag.getInt("battles_won"));
        data.notableActs = Math.max(0, tag.getInt("notable_acts"));
        data.harvestSleepTicks = Math.max(0, tag.getInt("harvest_sleep_ticks"));
        data.storyChapter = Math.max(0, tag.getInt("story_chapter"));
        data.storyObjective = stringOr(tag, "story_objective", data.storyObjective);
        data.nationName = tag.getString("nation_name");
        data.nationAlignment = stringOr(tag, "nation_alignment", data.nationAlignment);
        data.demonLordSeed = tag.getBoolean("demon_lord_seed");
        data.trueDemonLord = tag.getBoolean("true_demon_lord");
        data.heroEgg = tag.getBoolean("hero_egg");
        data.trueHero = tag.getBoolean("true_hero");
        data.harvestFestivalActive = tag.getBoolean("harvest_festival_active");
        data.raphaelAwakened = tag.getBoolean("raphael_awakened");
        data.cielAwakened = tag.getBoolean("ciel_awakened");
        data.receivedName = tag.getBoolean("received_name");
        data.masteredCombatSkill = tag.getBoolean("mastered_combat_skill");
        data.elementalBlessing = tag.getBoolean("elemental_blessing");
        data.survivedCriticalHunger = tag.getBoolean("survived_critical_hunger");
        data.absorbedHungerFactor = tag.getBoolean("absorbed_hunger_factor");
        data.soulContract = tag.getBoolean("soul_contract");
        data.wonNetherDuel = tag.getBoolean("won_nether_duel");

        data.skills.clear();
        loadStrings(tag.getList("skills", Tag.TAG_STRING), data.skills);
        data.titles.clear();
        loadStrings(tag.getList("titles", Tag.TAG_STRING), data.titles);

        data.attributes.clear();
        CompoundTag attributeTag = tag.getCompound("attributes");
        for (CharacterAttribute attribute : CharacterAttribute.values()) {
            data.attributes.put(attribute, Math.max(0, Math.min(100, attributeTag.getInt(attribute.id()))));
        }

        data.stomach.clear();
        ListTag stomachTags = tag.getList("stomach", Tag.TAG_COMPOUND);
        for (int i = 0; i < stomachTags.size(); i++) {
            data.stomach.add(StomachEntry.load(stomachTags.getCompound(i)));
        }

        data.mimicForms.clear();
        ListTag mimicTags = tag.getList("mimic_forms", Tag.TAG_COMPOUND);
        for (int i = 0; i < mimicTags.size(); i++) {
            MimicForm form = MimicForm.load(mimicTags.getCompound(i));
            if (!form.entityId().isBlank()) {
                data.mimicForms.put(form.entityId(), form);
            }
        }
        if (!data.mimicForms.containsKey(data.activeMimicForm)) {
            data.activeMimicForm = "";
        }

        data.subordinates.clear();
        ListTag subordinateTags = tag.getList("subordinates", Tag.TAG_COMPOUND);
        for (int i = 0; i < subordinateTags.size(); i++) {
            data.subordinates.add(SubordinateData.load(subordinateTags.getCompound(i)));
        }

        data.diplomacy.clear();
        CompoundTag diplomacyTag = tag.getCompound("diplomacy");
        diplomacyTag.getAllKeys().forEach(key -> data.diplomacy.put(key, diplomacyTag.getInt(key)));
        data.recalculateExistencePoints();
        return data;
    }

    private static ListTag saveStrings(Set<String> values) {
        ListTag result = new ListTag();
        values.forEach(value -> result.add(StringTag.valueOf(value)));
        return result;
    }

    private static void loadStrings(ListTag source, Set<String> target) {
        for (int i = 0; i < source.size(); i++) {
            target.add(source.getString(i));
        }
    }

    private static String stringOr(CompoundTag tag, String key, String fallback) {
        String value = tag.getString(key);
        return value.isBlank() ? fallback : value;
    }

    private static long longOr(CompoundTag tag, String key, long fallback) {
        return tag.contains(key) ? tag.getLong(key) : fallback;
    }

    public void recalculateExistencePoints() {
        existencePoints = com.gabriel.tensuranewworld.gameplay.ExistencePointCalculator.calculate(this);
    }

    public void addEssence(long amount) {
        long accepted = Math.max(0L, amount);
        essence = saturatedAdd(essence, accepted);
        long powerGain = accepted / 2L;
        maxMagicules = Math.max(1L, saturatedAdd(maxMagicules, powerGain));
        magicules = Math.min(maxMagicules, saturatedAdd(magicules, powerGain));
        basePhysicalPower = Math.max(1L, saturatedAdd(basePhysicalPower, powerGain));
        long threshold = RaceRegistry.essencePerGrowthPoint(raceId);
        long progress = saturatedAdd(essenceProgress, accepted);
        long earned = progress / threshold;
        essenceProgress = progress % threshold;
        attributePoints = (int) Math.min(Integer.MAX_VALUE, (long) attributePoints + earned);
        recalculateExistencePoints();
    }

    public boolean investAttribute(CharacterAttribute attribute) {
        if (attributePoints <= 0 || getAttribute(attribute) >= 100) {
            return false;
        }
        attributes.put(attribute, getAttribute(attribute) + 1);
        attributePoints--;
        if (attribute == CharacterAttribute.STRENGTH) {
            basePhysicalPower = saturatedAdd(basePhysicalPower, 1_000L);
        } else if (attribute == CharacterAttribute.INTELLIGENCE) {
            long updatedMaximum = saturatedAdd(maxMagicules, 1_000L);
            maxMagicules = Math.max(1L, updatedMaximum);
            magicules = Math.min(maxMagicules, saturatedAdd(magicules, 1_000L));
        }
        recalculateExistencePoints();
        return true;
    }

    public void recordMimicDefeat(MimicForm defeatedForm) {
        MimicForm existing = mimicForms.get(defeatedForm.entityId());
        if (existing == null && mimicForms.size() >= 128) {
            return;
        }
        mimicForms.put(defeatedForm.entityId(), existing == null ? defeatedForm
                : existing.addDefeat(defeatedForm.maxHealth(), defeatedForm.attackDamage(),
                        defeatedForm.movementSpeed(), defeatedForm.armor(),
                        defeatedForm.fireImmune(), defeatedForm.waterBreathing()));
    }

    public boolean selectMimicForm(String entityId) {
        return selectMimicForm(entityId, com.gabriel.tensuranewworld.integration.MorphAccess.allowed(raceId, skills));
    }

    public boolean selectMimicForm(String entityId, boolean authorized) {
        if (!authorized || entityId.isBlank() || !mimicForms.containsKey(entityId)) {
            return false;
        }
        activeMimicForm = entityId;
        return true;
    }

    public void clearMimicForm() {
        activeMimicForm = "";
    }

    private static long saturatedAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    public String getRaceId() { return raceId; }
    public void setRaceId(String raceId) {
        this.raceId = raceId == null || raceId.isBlank() ? "tensura:slime" : raceId;
        if (!RaceRegistry.isSlime(this.raceId)) {
            activeMimicForm = "";
        }
        recalculateExistencePoints();
    }
    public String getEvolutionStage() { return evolutionStage; }
    public void setEvolutionStage(String value) { evolutionStage = value == null ? "" : value; }
    public String getPendingEvolution() { return pendingEvolution; }
    public void setPendingEvolution(String value) { pendingEvolution = value == null ? "" : value; }
    public long getMagicules() { return magicules; }
    public void setMagicules(long magicules) { this.magicules = Math.max(0L, Math.min(magicules, maxMagicules)); }
    public long getMaxMagicules() { return maxMagicules; }
    public void setMaxMagicules(long maxMagicules) { this.maxMagicules = Math.max(1L, maxMagicules); this.magicules = Math.min(magicules, this.maxMagicules); recalculateExistencePoints(); }
    public long getBasePhysicalPower() { return basePhysicalPower; }
    public void setBasePhysicalPower(long value) { basePhysicalPower = Math.max(1L, value); recalculateExistencePoints(); }
    public long getExistencePoints() { return existencePoints; }
    public long getEssence() { return essence; }
    public long getEssenceProgress() { return essenceProgress; }
    public long getEssenceForNextPoint() { return RaceRegistry.essencePerGrowthPoint(raceId); }
    public int getAttributePoints() { return attributePoints; }
    public int getAttribute(CharacterAttribute attribute) { return attributes.getOrDefault(attribute, 0); }
    public Map<CharacterAttribute, Integer> getAttributes() { return Map.copyOf(attributes); }
    public int getSoulPoints() { return soulPoints; }
    public void setSoulPoints(int soulPoints) { this.soulPoints = Math.max(0, soulPoints); }
    public int getBattlesWon() { return battlesWon; }
    public void addBattleWin() { battlesWon++; }
    public int getNotableActs() { return notableActs; }
    public void addNotableAct() { notableActs++; }
    public int getHarvestSleepTicks() { return harvestSleepTicks; }
    public void setHarvestSleepTicks(int value) { harvestSleepTicks = Math.max(0, value); }
    public int getStoryChapter() { return storyChapter; }
    public void setStoryChapter(int value) { storyChapter = Math.max(0, value); }
    public String getStoryObjective() { return storyObjective; }
    public void setStoryObjective(String value) { storyObjective = value; }
    public String getNationName() { return nationName; }
    public void setNationName(String value) { nationName = value; }
    public String getNationAlignment() { return nationAlignment; }
    public void setNationAlignment(String value) { nationAlignment = value; }
    public boolean isDemonLordSeed() { return demonLordSeed; }
    public void setDemonLordSeed(boolean value) { demonLordSeed = value; }
    public boolean isTrueDemonLord() { return trueDemonLord; }
    public void setTrueDemonLord(boolean value) { trueDemonLord = value; }
    public boolean hasHeroEgg() { return heroEgg; }
    public void setHeroEgg(boolean value) { heroEgg = value; }
    public boolean isTrueHero() { return trueHero; }
    public void setTrueHero(boolean value) { trueHero = value; }
    public boolean isHarvestFestivalActive() { return harvestFestivalActive; }
    public void setHarvestFestivalActive(boolean value) { harvestFestivalActive = value; }
    public boolean isRaphaelAwakened() { return raphaelAwakened; }
    public void setRaphaelAwakened(boolean value) { raphaelAwakened = value; }
    public boolean isCielAwakened() { return cielAwakened; }
    public void setCielAwakened(boolean value) { cielAwakened = value; }
    public boolean hasReceivedName() { return receivedName; }
    public void setReceivedName(boolean value) { receivedName = value; }
    public boolean hasMasteredCombatSkill() { return masteredCombatSkill; }
    public void setMasteredCombatSkill(boolean value) { masteredCombatSkill = value; }
    public boolean hasElementalBlessing() { return elementalBlessing; }
    public void setElementalBlessing(boolean value) { elementalBlessing = value; }
    public boolean hasSurvivedCriticalHunger() { return survivedCriticalHunger; }
    public void setSurvivedCriticalHunger(boolean value) { survivedCriticalHunger = value; }
    public boolean hasAbsorbedHungerFactor() { return absorbedHungerFactor; }
    public void setAbsorbedHungerFactor(boolean value) { absorbedHungerFactor = value; }
    public boolean hasSoulContract() { return soulContract; }
    public void setSoulContract(boolean value) { soulContract = value; }
    public boolean hasWonNetherDuel() { return wonNetherDuel; }
    public void setWonNetherDuel(boolean value) { wonNetherDuel = value; }
    public Set<String> getSkills() { return skills; }
    public Set<String> getTitles() { return titles; }
    public List<StomachEntry> getStomach() { return stomach; }
    public List<MimicForm> getMimicForms() { return List.copyOf(mimicForms.values()); }
    public String getActiveMimicForm() { return activeMimicForm; }
    public MimicForm getSelectedMimicForm() { return mimicForms.get(activeMimicForm); }
    public List<SubordinateData> getSubordinates() { return subordinates; }
    public Map<String, Integer> getDiplomacy() { return diplomacy; }
}
