package com.gabriel.tensuranewworld.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gabriel.tensuranewworld.data.CharacterAttribute;
import com.gabriel.tensuranewworld.data.MimicForm;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.registry.RaceRegistry;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

final class CharacterProgressionTest {
    @Test
    void essenceGrantsGrowthPointsAtRaceSpecificMilestones() {
        TensuraPlayerData data = new TensuraPlayerData();

        data.addEssence(799L);
        assertEquals(0, data.getAttributePoints());
        assertEquals(799L, data.getEssenceProgress());

        data.addEssence(1L);
        assertEquals(1, data.getAttributePoints());
        assertEquals(0L, data.getEssenceProgress());
        assertEquals(800L, data.getEssence());

        data.setRaceId("tensura:human");
        assertEquals(750L, data.getEssenceForNextPoint());
    }

    @Test
    void investingAttributesChangesCharacterPowerAndPersists() {
        TensuraPlayerData data = new TensuraPlayerData();
        data.addEssence(1_600L);
        long originalPower = data.getBasePhysicalPower();

        assertTrue(data.investAttribute(CharacterAttribute.STRENGTH));
        assertEquals(originalPower + 1_000L, data.getBasePhysicalPower());
        assertEquals(1, data.getAttribute(CharacterAttribute.STRENGTH));
        assertEquals(1, data.getAttributePoints());
        assertTrue(data.investAttribute(CharacterAttribute.INTELLIGENCE));
        assertEquals(2_800L, data.getMaxMagicules());

        TensuraPlayerData restored = TensuraPlayerData.load(data.save());
        assertEquals(2, restored.getAttribute(CharacterAttribute.STRENGTH)
                + restored.getAttribute(CharacterAttribute.INTELLIGENCE));
        assertEquals(0, restored.getAttributePoints());
        assertEquals(data.getEssence(), restored.getEssence());
        assertEquals(data.getExistencePoints(), restored.getExistencePoints());
    }

    @Test
    void absorbedFormsAndActiveSelectionSurviveSaveLoad() {
        TensuraPlayerData data = new TensuraPlayerData();
        MimicForm form = new MimicForm("minecraft:spider", "Spider", 1, 16.0F, 4.0F,
                0.3F, 2.0F, false, false);
        data.recordMimicDefeat(form);
        assertTrue(data.selectMimicForm("minecraft:spider"));

        TensuraPlayerData restored = TensuraPlayerData.load(data.save());
        assertEquals("minecraft:spider", restored.getActiveMimicForm());
        assertEquals(1, restored.getMimicForms().size());
        assertEquals(16.0F, restored.getSelectedMimicForm().maxHealth());

        restored.clearMimicForm();
        assertFalse(restored.selectMimicForm("minecraft:zombie"));
    }

    @Test
    void oldPlayerTagsLoadWithDefaultProgressionValues() {
        CompoundTag oldSave = new CompoundTag();
        oldSave.putString("race_id", "tensura:human");

        TensuraPlayerData restored = TensuraPlayerData.load(oldSave);

        assertEquals(0L, restored.getEssence());
        assertEquals(0, restored.getAttributePoints());
        assertEquals(0, restored.getAttribute(CharacterAttribute.STRENGTH));
        assertTrue(restored.getMimicForms().isEmpty());
        assertEquals(1_000L, restored.getMaxMagicules());
    }

    @Test
    void racialCatalogIncludesEveryRequestedLineage() {
        for (String race : new String[] { "slime", "human", "goblin", "ogre", "orc", "lesser_daemon",
                "dwarf", "enlightened_dwarf", "dwarf_saint", "divine_dwarf",
                "elf", "enlightened_elf", "elf_saint", "divine_elf",
                "lizardman", "dragonewt", "true_dragonewt", "divine_dragon",
                "merfolk", "enlightened_merfolk", "merfolk_saint", "divine_fish",
                "beastfolk", "beast_lord", "spirit_beast", "divine_beast",
                "harpy", "harpy_queen", "spirit_bird", "divine_bird",
                "giant", "ancient_giant", "divine_giant",
                "ghoul", "vampire", "vampire_overcomer", "vampire_lord", "divine_vampire",
                "wight", "wight_king", "spirit_skeleton", "divine_skeleton",
                "enlightened_ogre", "mystic_oni", "wicked_oni", "spirit_oni", "death_oni",
                "divine_oni", "divine_fighter", "spirit_boar", "divine_boar", "devil_lord" }) {
            assertTrue(RaceRegistry.all().containsKey("tensura:" + race), race);
        }
    }
}
