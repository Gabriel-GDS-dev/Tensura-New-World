package com.gabriel.tensuranewworld.registry;

import static org.junit.jupiter.api.Assertions.*;
import com.gabriel.tensuranewworld.data.SkillView;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkillViewTest {
    @Test void snapshotRetainsFractionalCostsModesHintsAndSlotState() {
        var mode = new SkillView.Mode(2, "tensura.skill.mode.predator.stomach", "Stomach", 125.5, 22.75,
                251, 45.5, 80, true, 1, SkillAvailability.MODE_LEARNING, "Treine este modo.");
        var view = new SkillView("tensura:predator", "tensura.skill.predator", "Predator", "description.key",
                "Description", "tensura:textures/skill/unique/predator.png", SkillCategory.UNIQUE,
                250.5, 1000, true, false, true, true, 200, List.of(mode));
        assertEquals(view, SkillView.load(view.save()));
    }
    @Test void unknownFutureStateFailsClosedWithoutBreakingOtherSkillRows() {
        var mode = new SkillView.Mode(0, "", "Unknown", -1, -1, -1, -1, 0, false, -1,
                SkillAvailability.UNAVAILABLE, "Unsupported");
        var view = new SkillView("addon:unknown", "", "Unknown", "", "", "", SkillCategory.OTHER,
                0, 0, false, false, false, false, -1, List.of(mode));
        var tag = view.save();
        tag.getList("Modes", net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).putString("State", "FUTURE_STATE");
        assertEquals(SkillAvailability.UNAVAILABLE, SkillView.load(tag).modes().getFirst().availability());
    }
}
