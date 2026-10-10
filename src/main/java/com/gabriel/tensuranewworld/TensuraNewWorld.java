package com.gabriel.tensuranewworld;

import com.gabriel.tensuranewworld.data.ModAttachments;
import com.gabriel.tensuranewworld.network.NetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(TensuraNewWorld.MOD_ID)
public final class TensuraNewWorld {
    public static final String MOD_ID = "tensuranewworld";

    public TensuraNewWorld(IEventBus modBus) {
        com.gabriel.tensuranewworld.integration.skill.SageSkills.register();
        ModAttachments.ATTACHMENTS.register(modBus);
        modBus.addListener(NetworkHandler::registerPayloads);
    }
}
