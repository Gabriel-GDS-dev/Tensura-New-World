package com.gabriel.tensuranewworld.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.brigadier.StringReader;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import org.junit.jupiter.api.Test;

final class FormCommandArgumentTest {
    @Test
    void formArgumentAcceptsNamespacedEntityIds() throws Exception {
        assertEquals("minecraft:sheep",
                ResourceLocationArgument.id().parse(new StringReader("minecraft:sheep")).toString());
    }
}
