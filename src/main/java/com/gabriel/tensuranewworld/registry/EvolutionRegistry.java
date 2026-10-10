package com.gabriel.tensuranewworld.registry;

import static com.gabriel.tensuranewworld.registry.EvolutionCondition.Type.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class EvolutionRegistry {
    private static final Map<String, EvolutionPath> PATHS = new LinkedHashMap<>();

    static {
        path("named_slime", "slime", "named_slime", "tensura:slime", "Named Slime",
                "A true name stabilizes the soul and opens the slime bloodline.", false,
                EvolutionCondition.ep(100_000L), EvolutionCondition.flag(RECEIVED_NAME));
        path("demon_slime", "named_slime", "demon_slime", "tensura:demon_slime", "Demon Slime",
                "The Harvest Festival rebuilds the body around a Demon Lord soul.", true,
                EvolutionCondition.ep(500_000L), EvolutionCondition.flag(TRUE_DEMON_LORD));
        path("ultimate_slime", "demon_slime", "ultimate_slime", "tensura:god_slime", "Ultimate Slime",
                "A perfected spiritual body approaching the divine.", false,
                EvolutionCondition.ep(2_500_000L), EvolutionCondition.flag(TRUE_DEMON_LORD));

        path("hobgoblin", "goblin", "hobgoblin", "tensura:hobgoblin", "Hobgoblin",
                "Naming expands the goblin body and intelligence.", false,
                EvolutionCondition.ep(5_000L), EvolutionCondition.flag(RECEIVED_NAME));
        path("ogre_like_goblin", "hobgoblin", "ogre_like_goblin", "tensura:enlightened_hobgoblin", "Ogre-like Goblin",
                "A veteran body shaped by repeated survival.", false,
                EvolutionCondition.ep(40_000L), EvolutionCondition.battles(50));
        path("high_goblin", "ogre_like_goblin", "high_goblin", "tensura:hobgoblin_saint", "High Goblin / Enki",
                "Leadership turns individual strength into a people.", false,
                EvolutionCondition.ep(200_000L), EvolutionCondition.title("Leader"));

        path("kijin", "ogre", "kijin", "tensura:kijin", "Kijin",
                "A named ogre whose magic and will have condensed.", false,
                EvolutionCondition.ep(25_000L), EvolutionCondition.flag(RECEIVED_NAME));
        path("wicked_oni", "kijin", "wicked_oni", "tensura:wicked_oni", "Fair / Wicked Oni",
                "Combat mastery determines the oni nature that emerges.", false,
                EvolutionCondition.ep(150_000L), EvolutionCondition.flag(COMBAT_MASTERY));
        path("divine_oni", "wicked_oni", "divine_oni", "tensura:divine_oni", "Divine Oni",
                "An elemental altar blessing completes the oni vessel.", false,
                EvolutionCondition.ep(1_000_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("high_orc", "orc", "high_orc", "tensura:high_orc", "High Orc",
                "The body adapts after surviving true starvation.", false,
                EvolutionCondition.ep(10_000L), EvolutionCondition.flag(HUNGER_CRITICAL));
        path("orc_lord", "high_orc", "orc_lord", "tensura:orc_lord", "Orc Lord",
                "The Hunger factor becomes authority over the tribe.", false,
                EvolutionCondition.ep(80_000L), EvolutionCondition.flag(HUNGER_FACTOR));
        path("orc_disaster", "orc_lord", "orc_disaster", "tensura:orc_disaster", "Orc Disaster",
                "The crowned appetite becomes a calamity.", false,
                EvolutionCondition.ep(400_000L), EvolutionCondition.title("King of Orcs"));

        path("greater_daemon", "lesser_daemon", "greater_daemon", "tensura:greater_daemon", "Greater Daemon",
                "A human soul contract anchors a stronger material form.", false,
                EvolutionCondition.ep(50_000L), EvolutionCondition.flag(SOUL_CONTRACT));
        path("arch_daemon", "greater_daemon", "arch_daemon", "tensura:arch_daemon", "Arch Daemon",
                "Victory in the Nether proves dominance among daemons.", false,
                EvolutionCondition.ep(300_000L), EvolutionCondition.flag(NETHER_DUEL));
        path("demon_peer", "arch_daemon", "demon_peer", "tensura:daemon_lord", "Demon Peer",
                "A daemon whose existence rivals the rulers of the world.", false,
                EvolutionCondition.ep(1_500_000L));

        path("enlightened_human", "human", "enlightened_human", "tensura:enlightened_human", "Enlightened Human",
                "The Hero Egg answers a human soul with sufficient existence.", false,
                EvolutionCondition.ep(100_000L), EvolutionCondition.flag(HERO_EGG));
        path("human_saint", "enlightened_human", "human_saint", "tensura:human_saint", "Human Saint",
                "The awakened hero transcends ordinary human limits.", false,
                EvolutionCondition.ep(500_000L), EvolutionCondition.flag(TRUE_HERO));
        path("divine_human", "human_saint", "divine_human", "tensura:divine_human", "Divine Human",
                "A perfected heroic vessel bearing the Visage of God.", false,
                EvolutionCondition.ep(2_500_000L), EvolutionCondition.title("Visage of God"));

        path("enlightened_dwarf", "dwarf", "enlightened_dwarf", "tensura:enlightened_dwarf", "Enlightened Dwarf",
                "A true name awakens the craft and strength of the dwarf lineage.", false,
                EvolutionCondition.ep(25_000L), EvolutionCondition.flag(RECEIVED_NAME));
        path("dwarf_saint", "enlightened_dwarf", "dwarf_saint", "tensura:dwarf_saint", "Dwarf Saint",
                "Combat mastery tempers the dwarf into a spiritual warrior.", false,
                EvolutionCondition.ep(150_000L), EvolutionCondition.flag(COMBAT_MASTERY));
        path("divine_dwarf", "dwarf_saint", "divine_dwarf", "tensura:divine_dwarf", "Divine Dwarf",
                "An elemental blessing completes the dwarf's divine vessel.", false,
                EvolutionCondition.ep(750_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("enlightened_elf", "elf", "enlightened_elf", "tensura:enlightened_elf", "Enlightened Elf",
                "A true name awakens the elf's ancient magical potential.", false,
                EvolutionCondition.ep(25_000L), EvolutionCondition.flag(RECEIVED_NAME));
        path("elf_saint", "enlightened_elf", "elf_saint", "tensura:elf_saint", "Elf Saint",
                "Combat mastery strengthens the elf's spiritual nature.", false,
                EvolutionCondition.ep(150_000L), EvolutionCondition.flag(COMBAT_MASTERY));
        path("divine_elf", "elf_saint", "divine_elf", "tensura:divine_elf", "Divine Elf",
                "An elemental blessing elevates the elf into a divine being.", false,
                EvolutionCondition.ep(750_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("ancient_giant", "giant", "ancient_giant", "tensura:ancient_giant", "Ancient Giant",
                "Survival through repeated battles awakens the giant's ancient strength.", false,
                EvolutionCondition.ep(50_000L), EvolutionCondition.battles(25));
        path("divine_giant", "ancient_giant", "divine_giant", "tensura:divine_giant", "Divine Giant",
                "An elemental blessing perfects the giant's colossal body.", false,
                EvolutionCondition.ep(750_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("dragonewt", "lizardman", "dragonewt", "tensura:dragonewt", "Dragonewt",
                "A true name awakens draconic power within the lizardman.", false,
                EvolutionCondition.ep(30_000L), EvolutionCondition.flag(RECEIVED_NAME));
        path("true_dragonewt", "dragonewt", "true_dragonewt", "tensura:true_dragonewt", "True Dragonewt",
                "Combat mastery brings the draconic bloodline to maturity.", false,
                EvolutionCondition.ep(250_000L), EvolutionCondition.flag(COMBAT_MASTERY));
        path("divine_dragon", "true_dragonewt", "divine_dragon", "tensura:divine_dragon", "Divine Dragon",
                "An elemental blessing awakens the divine dragon nature.", false,
                EvolutionCondition.ep(1_200_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("enlightened_merfolk", "merfolk", "enlightened_merfolk", "tensura:enlightened_merfolk", "Enlightened Merfolk",
                "A true name deepens the merfolk's connection to the sea.", false,
                EvolutionCondition.ep(25_000L), EvolutionCondition.flag(RECEIVED_NAME));
        path("merfolk_saint", "enlightened_merfolk", "merfolk_saint", "tensura:merfolk_saint", "Merfolk Saint",
                "Combat mastery awakens the spiritual currents of the sea.", false,
                EvolutionCondition.ep(175_000L), EvolutionCondition.flag(COMBAT_MASTERY));
        path("divine_fish", "merfolk_saint", "divine_fish", "tensura:divine_fish", "Divine Fish",
                "An elemental blessing completes the merfolk's divine form.", false,
                EvolutionCondition.ep(800_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("beast_lord", "beastfolk", "beast_lord", "tensura:beast_lord", "Beast Lord",
                "A name and proven survival awaken the beastfolk's leadership.", false,
                EvolutionCondition.ep(30_000L), EvolutionCondition.flag(RECEIVED_NAME),
                EvolutionCondition.battles(25));
        path("spirit_beast", "beast_lord", "spirit_beast", "tensura:spirit_beast", "Spirit Beast",
                "A soul contract anchors the beast lord's spiritual body.", false,
                EvolutionCondition.ep(250_000L), EvolutionCondition.flag(SOUL_CONTRACT));
        path("divine_beast", "spirit_beast", "divine_beast", "tensura:divine_beast", "Divine Beast",
                "An elemental blessing perfects the beast's divine nature.", false,
                EvolutionCondition.ep(1_000_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("harpy_queen", "harpy", "harpy_queen", "tensura:harpy_queen", "Harpy Queen",
                "A true name and a record of survival establish the harpy's authority.", false,
                EvolutionCondition.ep(30_000L), EvolutionCondition.flag(RECEIVED_NAME),
                EvolutionCondition.battles(25));
        path("spirit_bird", "harpy_queen", "spirit_bird", "tensura:spirit_bird", "Spirit Bird",
                "Combat mastery awakens the harpy's spiritual flight.", false,
                EvolutionCondition.ep(250_000L), EvolutionCondition.flag(COMBAT_MASTERY));
        path("divine_bird", "spirit_bird", "divine_bird", "tensura:divine_bird", "Divine Bird",
                "An elemental blessing perfects the bird's divine form.", false,
                EvolutionCondition.ep(1_000_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("vampire", "ghoul", "vampire", "tensura:vampire", "Vampire",
                "A true name awakens the vampire lineage within the ghoul.", false,
                EvolutionCondition.ep(20_000L), EvolutionCondition.flag(RECEIVED_NAME));
        path("vampire_overcomer", "vampire", "vampire_overcomer", "tensura:vampire_overcomer", "Vampire Overcomer",
                "Combat mastery proves the vampire can overcome its limitations.", false,
                EvolutionCondition.ep(150_000L), EvolutionCondition.flag(COMBAT_MASTERY));
        path("vampire_lord", "vampire_overcomer", "vampire_lord", "tensura:vampire_lord", "Vampire Lord",
                "A soul contract anchors the vampire's growing authority.", false,
                EvolutionCondition.ep(500_000L), EvolutionCondition.flag(SOUL_CONTRACT));
        path("divine_vampire", "vampire_lord", "divine_vampire", "tensura:divine_vampire", "Divine Vampire",
                "An elemental blessing elevates the vampire beyond mortal limits.", false,
                EvolutionCondition.ep(1_500_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("wight_king", "wight", "wight_king", "tensura:wight_king", "Wight King",
                "Surviving repeated battles awakens the wight's authority over the dead.", false,
                EvolutionCondition.ep(30_000L), EvolutionCondition.battles(25));
        path("spirit_skeleton", "wight_king", "spirit_skeleton", "tensura:spirit_skeleton", "Spirit Skeleton",
                "A soul contract strengthens the wight king's spiritual existence.", false,
                EvolutionCondition.ep(200_000L), EvolutionCondition.flag(SOUL_CONTRACT));
        path("divine_skeleton", "spirit_skeleton", "divine_skeleton", "tensura:divine_skeleton", "Divine Skeleton",
                "An elemental blessing perfects the spirit skeleton's divine body.", false,
                EvolutionCondition.ep(1_000_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("spirit_boar", "orc_disaster", "spirit_boar", "tensura:spirit_boar", "Spirit Boar",
                "A soul contract transforms the Orc Disaster into a spiritual boar.", false,
                EvolutionCondition.ep(900_000L), EvolutionCondition.flag(SOUL_CONTRACT));
        path("divine_boar", "spirit_boar", "divine_boar", "tensura:divine_boar", "Divine Boar",
                "An elemental blessing completes the boar's divine awakening.", false,
                EvolutionCondition.ep(2_000_000L), EvolutionCondition.flag(ELEMENTAL_BLESSING));

        path("devil_lord", "demon_peer", "devil_lord", "tensura:devil_lord", "Devil Lord",
                "A Nether victory proves the Demon Peer can command demonic authority.", false,
                EvolutionCondition.ep(2_500_000L), EvolutionCondition.flag(NETHER_DUEL));
    }

    private EvolutionRegistry() {
    }

    private static void path(String id, String from, String to, String race, String name,
            String description, boolean harvestOnly, EvolutionCondition... conditions) {
        PATHS.put(id, new EvolutionPath(id, from, to, race, name, description, harvestOnly, List.of(conditions)));
    }

    public static Optional<EvolutionPath> get(String id) {
        return Optional.ofNullable(PATHS.get(id));
    }

    public static List<EvolutionPath> next(String stage) {
        List<EvolutionPath> result = new ArrayList<>();
        PATHS.values().stream().filter(path -> path.fromStage().equals(stage)).forEach(result::add);
        return List.copyOf(result);
    }

    public static String inferStage(String raceId) {
        String path = raceId.contains(":") ? raceId.substring(raceId.indexOf(':') + 1) : raceId;
        return switch (path) {
            case "god_slime" -> "ultimate_slime";
            case "enlightened_hobgoblin" -> "ogre_like_goblin";
            case "hobgoblin_saint" -> "high_goblin";
            case "daemon_lord" -> "demon_peer";
            default -> path;
        };
    }
}
