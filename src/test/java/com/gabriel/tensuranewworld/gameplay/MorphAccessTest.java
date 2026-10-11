package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.integration.MorphAccess;
import com.gabriel.tensuranewworld.registry.RaceRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MorphAccessTest {
    @Test void everyRaceRequiresSlimeOrHigherPredation() {
        RaceRegistry.all().forEach((id, race) -> {
            boolean slime = race.lineage().equals("slime");
            assertEquals(slime, MorphAccess.allowed(id, List.of()), id);
            assertEquals(slime, MorphAccess.allowed(id, List.of("tensura:predator", "tensura:great_sage")), id);
            for (String skill : List.of("tensura:gluttony", "tensura:beelzebuth", "tensura:azathoth")) assertTrue(MorphAccess.allowed(id, List.of(skill)), id + skill);
        });
        assertFalse(MorphAccess.allowed("", List.of("other:gluttony")));
    }
}
