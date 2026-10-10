package com.gabriel.tensuranewworld.data;

import com.gabriel.tensuranewworld.TensuraNewWorld;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TensuraNewWorld.MOD_ID);

    public static final Supplier<AttachmentType<TensuraPlayerData>> PLAYER_DATA =
            ATTACHMENTS.register("player_data", () -> AttachmentType.builder(TensuraPlayerData::new)
                    .serialize(TensuraPlayerData.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}
