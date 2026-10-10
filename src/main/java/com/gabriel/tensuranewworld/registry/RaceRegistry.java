package com.gabriel.tensuranewworld.registry;

import com.gabriel.tensuranewworld.data.CharacterAttribute;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class RaceRegistry {
    private static final Map<String, RaceDefinition> RACES = new LinkedHashMap<>();

    static {
        register("slime", "slime", "Slime", 1.00, 1.10, 800, CharacterAttribute.INTELLIGENCE,
                "Predator: registra formas e absorve caracteristicas.");
        register("metal_slime", "slime", "Metal Slime", 1.35, 1.15, 800, CharacterAttribute.VITALITY,
                "Corpo resistente a dano fisico.");
        register("demon_slime", "slime", "Demon Slime", 3.00, 1.25, 800, CharacterAttribute.WILL,
                "Afinidade com magia e resistencia espiritual.");
        register("god_slime", "slime", "Ultimate Slime", 5.00, 1.35, 800, CharacterAttribute.INTELLIGENCE,
                "Grande reserva de magicules e alta adaptabilidade.");
        register("goblin", "goblin", "Goblin", 0.85, 1.05, 900, CharacterAttribute.AGILITY,
                "Crescimento rapido por sobrevivencia e cooperacao.");
        register("hobgoblin", "goblin", "Hobgoblin", 1.20, 1.10, 900, CharacterAttribute.STRENGTH,
                "Maior rendimento de Essencia em combate.");
        register("enlightened_hobgoblin", "goblin", "Ogre-like Goblin", 1.65, 1.15, 900, CharacterAttribute.WILL,
                "Afinidade espiritual despertada.");
        register("hobgoblin_saint", "goblin", "High Goblin", 2.20, 1.20, 900, CharacterAttribute.INTELLIGENCE,
                "Capacidade magica ampliada.");
        register("ogre", "ogre", "Ogre", 1.35, 0.95, 1_000, CharacterAttribute.STRENGTH,
                "Forca fisica superior.");
        register("kijin", "ogre", "Kijin", 1.75, 1.00, 1_000, CharacterAttribute.STRENGTH,
                "Forca e velocidade elevadas.");
        register("enlightened_ogre", "ogre", "Enlightened Ogre", 2.00, 1.02, 1_000, CharacterAttribute.INTELLIGENCE,
                "Afinidade elemental ampliada.");
        register("mystic_oni", "ogre", "Mystic Oni", 2.20, 1.04, 1_000, CharacterAttribute.WILL,
                "Resistencia espiritual e magia oni.");
        register("wicked_oni", "ogre", "Wicked Oni", 2.40, 1.05, 1_000, CharacterAttribute.WILL,
                "Resistencia a efeitos espirituais.");
        register("spirit_oni", "ogre", "Spirit Oni", 2.80, 1.07, 1_000, CharacterAttribute.INTELLIGENCE,
                "Corpo espiritual e afinidade elemental.");
        register("death_oni", "ogre", "Death Oni", 3.30, 1.09, 1_000, CharacterAttribute.STRENGTH,
                "Poder de combate e resistencia elevados.");
        register("divine_oni", "ogre", "Divine Oni", 4.00, 1.10, 1_000, CharacterAttribute.INTELLIGENCE,
                "Natureza divina e controle magico.");
        register("divine_fighter", "ogre", "Divine Fighter", 4.20, 1.10, 1_000, CharacterAttribute.STRENGTH,
                "Forma divina especializada em combate.");
        register("orc", "orc", "Orc", 1.10, 1.00, 900, CharacterAttribute.VITALITY,
                "Resistencia e adaptacao por batalha.");
        register("high_orc", "orc", "High Orc", 1.55, 1.05, 900, CharacterAttribute.VITALITY,
                "Maior vigor e capacidade de recuperacao.");
        register("orc_lord", "orc", "Orc Lord", 2.30, 1.10, 900, CharacterAttribute.STRENGTH,
                "Poder cresce com cada vitoria.");
        register("orc_disaster", "orc", "Orc Disaster", 3.50, 1.15, 900, CharacterAttribute.STRENGTH,
                "Predacao converte combate em poder.");
        register("spirit_boar", "orc", "Spirit Boar", 3.80, 1.18, 900, CharacterAttribute.WILL,
                "Forma espiritual da linhagem Orc.");
        register("divine_boar", "orc", "Divine Boar", 4.50, 1.20, 900, CharacterAttribute.VITALITY,
                "Forma divina com vigor excepcional.");
        register("lesser_daemon", "daemon", "Lesser Daemon", 1.40, 1.10, 1_100, CharacterAttribute.WILL,
                "Afinidade com o mundo espiritual.");
        register("greater_daemon", "daemon", "Greater Daemon", 2.00, 1.15, 1_100, CharacterAttribute.INTELLIGENCE,
                "Magia e Essencia de abates aprimoradas.");
        register("arch_daemon", "daemon", "Arch Daemon", 3.20, 1.20, 1_100, CharacterAttribute.INTELLIGENCE,
                "Alta afinidade magica e resistencia ao fogo.");
        register("daemon_lord", "daemon", "Demon Peer", 4.60, 1.25, 1_100, CharacterAttribute.WILL,
                "Presenca espiritual excepcional.");
        register("devil_lord", "daemon", "Devil Lord", 5.00, 1.30, 1_100, CharacterAttribute.INTELLIGENCE,
                "Autoridade demoniaca e grande poder magico.");
        register("human", "human", "Human", 1.00, 1.15, 750, CharacterAttribute.AGILITY,
                "Aprendizado adaptavel: recebe Essencia adicional.");
        register("enlightened_human", "human", "Enlightened Human", 1.60, 1.20, 750, CharacterAttribute.INTELLIGENCE,
                "Afinidade elevada com skills e magicules.");
        register("human_saint", "human", "Human Saint", 2.60, 1.25, 750, CharacterAttribute.WILL,
                "Resistencia espiritual e magia sagrada.");
        register("divine_human", "human", "Divine Human", 4.20, 1.30, 750, CharacterAttribute.WILL,
                "Natureza divina e grande potencial de crescimento.");
        register("dwarf", "dwarf", "Dwarf", 1.20, 1.00, 950, CharacterAttribute.VITALITY,
                "Resistencia e afinidade com equipamentos.");
        register("enlightened_dwarf", "dwarf", "Enlightened Dwarf", 1.55, 1.05, 950, CharacterAttribute.STRENGTH,
                "Forja e vigor aprimorados.");
        register("dwarf_saint", "dwarf", "Dwarf Saint", 2.20, 1.10, 950, CharacterAttribute.WILL,
                "Resistencia espiritual e fisica.");
        register("divine_dwarf", "dwarf", "Divine Dwarf", 3.50, 1.15, 950, CharacterAttribute.VITALITY,
                "Corpo divino e grande capacidade defensiva.");
        register("elf", "elf", "Elf", 1.05, 1.15, 850, CharacterAttribute.INTELLIGENCE,
                "Afinidade natural com magia.");
        register("enlightened_elf", "elf", "Enlightened Elf", 1.45, 1.20, 850, CharacterAttribute.INTELLIGENCE,
                "Afinidade magica e elemental superior.");
        register("elf_saint", "elf", "Elf Saint", 2.10, 1.25, 850, CharacterAttribute.WILL,
                "Vontade e magia espiritual elevadas.");
        register("divine_elf", "elf", "Divine Elf", 3.30, 1.30, 850, CharacterAttribute.INTELLIGENCE,
                "Natureza divina com alto controle magico.");
        register("giant", "giant", "Giant", 2.00, 0.95, 1_100, CharacterAttribute.STRENGTH,
                "Grande forca e vigor.");
        register("ancient_giant", "giant", "Ancient Giant", 3.00, 1.00, 1_100, CharacterAttribute.VITALITY,
                "Corpo antigo de resistencia excepcional.");
        register("divine_giant", "giant", "Divine Giant", 4.50, 1.05, 1_100, CharacterAttribute.STRENGTH,
                "Forca colossal e natureza divina.");
        register("lizardman", "lizardman", "Lizardman", 1.20, 1.00, 900, CharacterAttribute.VITALITY,
                "Resistencia natural e adaptacao aquatica.");
        register("dragonewt", "lizardman", "Dragonewt", 2.30, 1.10, 900, CharacterAttribute.STRENGTH,
                "Escamas resistentes e sopro draconico.");
        register("true_dragonewt", "lizardman", "True Dragonewt", 3.40, 1.15, 900, CharacterAttribute.STRENGTH,
                "Natureza draconica desperta.");
        register("divine_dragon", "lizardman", "Divine Dragon", 5.00, 1.20, 900, CharacterAttribute.WILL,
                "Forma draconica divina.");
        register("merfolk", "merfolk", "Merfolk", 1.00, 1.10, 900, CharacterAttribute.AGILITY,
                "Respiracao subaquatica e mobilidade na agua.");
        register("enlightened_merfolk", "merfolk", "Enlightened Merfolk", 1.45, 1.15, 900, CharacterAttribute.INTELLIGENCE,
                "Controle magico e adaptacao aquaticos.");
        register("merfolk_saint", "merfolk", "Merfolk Saint", 2.20, 1.20, 900, CharacterAttribute.WILL,
                "Afinidade espiritual dos mares.");
        register("divine_fish", "merfolk", "Divine Fish", 3.50, 1.25, 900, CharacterAttribute.AGILITY,
                "Forma divina adaptada ao oceano.");
        register("beastfolk", "beastfolk", "Beastfolk", 1.30, 1.05, 850, CharacterAttribute.AGILITY,
                "Sentidos aguçados e velocidade.");
        register("beast_lord", "beastfolk", "Beast Lord", 2.10, 1.10, 850, CharacterAttribute.STRENGTH,
                "Forca e instintos de lideranca.");
        register("spirit_beast", "beastfolk", "Spirit Beast", 3.00, 1.15, 850, CharacterAttribute.WILL,
                "Corpo espiritual com sentidos aguçados.");
        register("divine_beast", "beastfolk", "Divine Beast", 4.20, 1.20, 850, CharacterAttribute.AGILITY,
                "Forma divina de alta mobilidade.");
        register("harpy", "harpy", "Harpy", 1.05, 1.10, 850, CharacterAttribute.AGILITY,
                "Mobilidade e agilidade aereas.");
        register("harpy_queen", "harpy", "Harpy Queen", 1.60, 1.15, 850, CharacterAttribute.AGILITY,
                "Dominio aereo e autoridade sobre harpy.");
        register("spirit_bird", "harpy", "Spirit Bird", 2.50, 1.20, 850, CharacterAttribute.WILL,
                "Forma espiritual de voo e magia.");
        register("divine_bird", "harpy", "Divine Bird", 3.80, 1.25, 850, CharacterAttribute.INTELLIGENCE,
                "Forma divina com controle aereo.");
        register("ghoul", "vampire", "Ghoul", 1.10, 1.05, 1_000, CharacterAttribute.VITALITY,
                "Resistencia e recuperacao noturna.");
        register("vampire", "vampire", "Vampire", 1.50, 1.10, 1_000, CharacterAttribute.WILL,
                "Recuperacao noturna e resistencia espiritual.");
        register("vampire_overcomer", "vampire", "Vampire Overcomer", 2.20, 1.15, 1_000, CharacterAttribute.STRENGTH,
                "Adaptacao superior a combate e luz.");
        register("vampire_lord", "vampire", "Vampire Lord", 3.20, 1.20, 1_000, CharacterAttribute.WILL,
                "Autoridade vampirica e poder espiritual.");
        register("divine_vampire", "vampire", "Divine Vampire", 4.50, 1.25, 1_000, CharacterAttribute.INTELLIGENCE,
                "Forma vampirica divina.");
        register("wight", "wight", "Wight", 1.20, 1.10, 1_000, CharacterAttribute.WILL,
                "Resistencia a veneno e efeitos fisicos.");
        register("wight_king", "wight", "Wight King", 1.80, 1.15, 1_000, CharacterAttribute.WILL,
                "Autoridade espiritual sobre os mortos.");
        register("spirit_skeleton", "wight", "Spirit Skeleton", 2.80, 1.20, 1_000, CharacterAttribute.INTELLIGENCE,
                "Corpo espiritual e resistencia a dano.");
        register("divine_skeleton", "wight", "Divine Skeleton", 4.00, 1.25, 1_000, CharacterAttribute.WILL,
                "Forma divina imune a necessidades mortais.");
    }

    private RaceRegistry() {
    }

    private static void register(String path, String lineage, String name, double physicalMultiplier,
            double essenceMultiplier, long essencePerGrowthPoint, CharacterAttribute favoredAttribute,
            String passive) {
        String id = "tensura:" + path;
        RACES.put(id, new RaceDefinition(id, lineage, name, physicalMultiplier, essenceMultiplier,
                essencePerGrowthPoint, favoredAttribute, passive));
    }

    public static Optional<RaceDefinition> get(String id) {
        RaceDefinition exact = RACES.get(id);
        if (exact != null) {
            return Optional.of(exact);
        }
        String normalized = id == null ? "" : id.toLowerCase(java.util.Locale.ROOT);
        return RACES.values().stream()
                .filter(race -> normalized.contains(race.lineage()))
                .findFirst();
    }

    public static Map<String, RaceDefinition> all() {
        return Map.copyOf(RACES);
    }

    public static double physicalMultiplier(String raceId) {
        return get(raceId).map(RaceDefinition::physicalMultiplier).orElse(1.0D);
    }

    public static double essenceMultiplier(String raceId) {
        return get(raceId).map(RaceDefinition::essenceMultiplier).orElse(1.0D);
    }

    public static long essencePerGrowthPoint(String raceId) {
        return get(raceId).map(RaceDefinition::essencePerGrowthPoint).orElse(1_000L);
    }

    public static boolean isSlime(String raceId) {
        return get(raceId).map(race -> race.lineage().equals("slime")).orElse(false);
    }
}
