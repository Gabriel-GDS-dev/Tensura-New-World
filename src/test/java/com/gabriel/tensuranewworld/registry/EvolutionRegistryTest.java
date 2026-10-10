package com.gabriel.tensuranewworld.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

final class EvolutionRegistryTest {
    @Test
    void slimeNamingRequiresEpAndAReceivedName() {
        EvolutionPath path = EvolutionRegistry.get("named_slime").orElseThrow();
        assertEquals(2, path.conditions().size());
        assertTrue(path.conditions().stream().anyMatch(condition -> condition.type() == EvolutionCondition.Type.EP));
        assertTrue(path.conditions().stream().anyMatch(
                condition -> condition.type() == EvolutionCondition.Type.RECEIVED_NAME));
    }

    @Test
    void demonSlimeRemainsAHarvestOnlyEvolution() {
        EvolutionPath path = EvolutionRegistry.get("demon_slime").orElseThrow();
        assertTrue(path.harvestOnly());
    }

    @Test
    void goblinPathIsNotDefinedByEpAlone() {
        EvolutionPath path = EvolutionRegistry.get("hobgoblin").orElseThrow();
        assertTrue(path.conditions().stream().anyMatch(
                condition -> condition.type() == EvolutionCondition.Type.RECEIVED_NAME));
    }

    @Test
    void gddEvolutionThresholdsAndWorldConditionsAreRegistered() {
        List<ExpectedPath> expected = List.of(
                expected("named_slime", "slime", "named_slime", 100_000L, EvolutionCondition.Type.RECEIVED_NAME),
                expected("demon_slime", "named_slime", "demon_slime", 500_000L, EvolutionCondition.Type.TRUE_DEMON_LORD),
                expected("ultimate_slime", "demon_slime", "ultimate_slime", 2_500_000L, EvolutionCondition.Type.TRUE_DEMON_LORD),
                expected("hobgoblin", "goblin", "hobgoblin", 5_000L, EvolutionCondition.Type.RECEIVED_NAME),
                expected("ogre_like_goblin", "hobgoblin", "ogre_like_goblin", 40_000L, EvolutionCondition.Type.BATTLES),
                expected("high_goblin", "ogre_like_goblin", "high_goblin", 200_000L, EvolutionCondition.Type.TITLE),
                expected("kijin", "ogre", "kijin", 25_000L, EvolutionCondition.Type.RECEIVED_NAME),
                expected("wicked_oni", "kijin", "wicked_oni", 150_000L, EvolutionCondition.Type.COMBAT_MASTERY),
                expected("divine_oni", "wicked_oni", "divine_oni", 1_000_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("high_orc", "orc", "high_orc", 10_000L, EvolutionCondition.Type.HUNGER_CRITICAL),
                expected("orc_lord", "high_orc", "orc_lord", 80_000L, EvolutionCondition.Type.HUNGER_FACTOR),
                expected("orc_disaster", "orc_lord", "orc_disaster", 400_000L, EvolutionCondition.Type.TITLE),
                expected("greater_daemon", "lesser_daemon", "greater_daemon", 50_000L, EvolutionCondition.Type.SOUL_CONTRACT),
                expected("arch_daemon", "greater_daemon", "arch_daemon", 300_000L, EvolutionCondition.Type.NETHER_DUEL),
                expected("demon_peer", "arch_daemon", "demon_peer", 1_500_000L, null),
                expected("enlightened_dwarf", "dwarf", "enlightened_dwarf", 25_000L, EvolutionCondition.Type.RECEIVED_NAME),
                expected("dwarf_saint", "enlightened_dwarf", "dwarf_saint", 150_000L, EvolutionCondition.Type.COMBAT_MASTERY),
                expected("divine_dwarf", "dwarf_saint", "divine_dwarf", 750_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("enlightened_elf", "elf", "enlightened_elf", 25_000L, EvolutionCondition.Type.RECEIVED_NAME),
                expected("elf_saint", "enlightened_elf", "elf_saint", 150_000L, EvolutionCondition.Type.COMBAT_MASTERY),
                expected("divine_elf", "elf_saint", "divine_elf", 750_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("ancient_giant", "giant", "ancient_giant", 50_000L, EvolutionCondition.Type.BATTLES),
                expected("divine_giant", "ancient_giant", "divine_giant", 750_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("dragonewt", "lizardman", "dragonewt", 30_000L, EvolutionCondition.Type.RECEIVED_NAME),
                expected("true_dragonewt", "dragonewt", "true_dragonewt", 250_000L, EvolutionCondition.Type.COMBAT_MASTERY),
                expected("divine_dragon", "true_dragonewt", "divine_dragon", 1_200_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("enlightened_merfolk", "merfolk", "enlightened_merfolk", 25_000L, EvolutionCondition.Type.RECEIVED_NAME),
                expected("merfolk_saint", "enlightened_merfolk", "merfolk_saint", 175_000L, EvolutionCondition.Type.COMBAT_MASTERY),
                expected("divine_fish", "merfolk_saint", "divine_fish", 800_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("beast_lord", "beastfolk", "beast_lord", 30_000L, EvolutionCondition.Type.BATTLES),
                expected("spirit_beast", "beast_lord", "spirit_beast", 250_000L, EvolutionCondition.Type.SOUL_CONTRACT),
                expected("divine_beast", "spirit_beast", "divine_beast", 1_000_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("harpy_queen", "harpy", "harpy_queen", 30_000L, EvolutionCondition.Type.BATTLES),
                expected("spirit_bird", "harpy_queen", "spirit_bird", 250_000L, EvolutionCondition.Type.COMBAT_MASTERY),
                expected("divine_bird", "spirit_bird", "divine_bird", 1_000_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("vampire", "ghoul", "vampire", 20_000L, EvolutionCondition.Type.RECEIVED_NAME),
                expected("vampire_overcomer", "vampire", "vampire_overcomer", 150_000L, EvolutionCondition.Type.COMBAT_MASTERY),
                expected("vampire_lord", "vampire_overcomer", "vampire_lord", 500_000L, EvolutionCondition.Type.SOUL_CONTRACT),
                expected("divine_vampire", "vampire_lord", "divine_vampire", 1_500_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("wight_king", "wight", "wight_king", 30_000L, EvolutionCondition.Type.BATTLES),
                expected("spirit_skeleton", "wight_king", "spirit_skeleton", 200_000L, EvolutionCondition.Type.SOUL_CONTRACT),
                expected("divine_skeleton", "spirit_skeleton", "divine_skeleton", 1_000_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("spirit_boar", "orc_disaster", "spirit_boar", 900_000L, EvolutionCondition.Type.SOUL_CONTRACT),
                expected("divine_boar", "spirit_boar", "divine_boar", 2_000_000L, EvolutionCondition.Type.ELEMENTAL_BLESSING),
                expected("devil_lord", "demon_peer", "devil_lord", 2_500_000L, EvolutionCondition.Type.NETHER_DUEL));

        for (ExpectedPath item : expected) {
            EvolutionPath path = EvolutionRegistry.get(item.id()).orElseThrow();
            assertEquals(item.fromStage(), path.fromStage(), item.id());
            assertEquals(item.toStage(), path.toStage(), item.id());
            assertTrue(RaceRegistry.all().containsKey(path.toRaceId()), item.id());
            assertTrue(path.conditions().stream().anyMatch(condition ->
                    condition.type() == EvolutionCondition.Type.EP && condition.value() == item.ep()), item.id());
            if (item.worldCondition() == null) {
                assertEquals(1, path.conditions().size(), item.id());
            } else {
                assertTrue(path.conditions().stream().anyMatch(condition ->
                        condition.type() == item.worldCondition()), item.id());
            }
        }
    }

    @Test
    void aliasesFromTheBaseModResolveToNewWorldStages() {
        assertEquals("ultimate_slime", EvolutionRegistry.inferStage("tensura:god_slime"));
        assertEquals("ogre_like_goblin", EvolutionRegistry.inferStage("tensura:enlightened_hobgoblin"));
        assertEquals("high_goblin", EvolutionRegistry.inferStage("tensura:hobgoblin_saint"));
        assertEquals("demon_peer", EvolutionRegistry.inferStage("tensura:daemon_lord"));
        assertEquals("orc", EvolutionRegistry.inferStage("tensura:orc"));
    }

    @Test
    void everyAddedEvolutionTargetHasARacialProfile() {
        for (String race : List.of(
                "enlightened_dwarf", "dwarf_saint", "divine_dwarf",
                "enlightened_elf", "elf_saint", "divine_elf",
                "ancient_giant", "divine_giant",
                "dragonewt", "true_dragonewt", "divine_dragon",
                "enlightened_merfolk", "merfolk_saint", "divine_fish",
                "beast_lord", "spirit_beast", "divine_beast",
                "harpy_queen", "spirit_bird", "divine_bird",
                "vampire", "vampire_overcomer", "vampire_lord", "divine_vampire",
                "wight_king", "spirit_skeleton", "divine_skeleton",
                "spirit_boar", "divine_boar", "devil_lord")) {
            assertTrue(RaceRegistry.all().containsKey("tensura:" + race), race);
        }
    }

    @Test
    void onlyTheDemonSlimeTransitionIsHarvestLocked() {
        assertTrue(EvolutionRegistry.get("demon_slime").orElseThrow().harvestOnly());
        for (String id : List.of("named_slime", "ultimate_slime", "hobgoblin", "kijin", "high_orc",
                "greater_daemon", "demon_peer")) {
            assertFalse(EvolutionRegistry.get(id).orElseThrow().harvestOnly(), id);
        }
    }

    private static ExpectedPath expected(String id, String fromStage, String toStage, long ep,
            EvolutionCondition.Type worldCondition) {
        return new ExpectedPath(id, fromStage, toStage, ep, worldCondition);
    }

    private record ExpectedPath(String id, String fromStage, String toStage, long ep,
            EvolutionCondition.Type worldCondition) {
    }
}
