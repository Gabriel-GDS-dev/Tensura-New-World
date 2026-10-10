package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

/** Render-time measurements: never retain dimensions from a previous form/world. */
public final class FormCamera {
    public record View(float eyeHeight, float distance) { }
    private FormCamera() { }

    public static View measure(Player player, float partialTick) {
        EntityDimensions body = player.getDimensions(player.getPose());
        float eye = body.eyeHeight();
        if (ModList.get().isLoaded("morph")) {
            Object api = ReincarnatedSkills.callStatic("me.ichun.mods.morph.api.MorphApi", "getApiImpl");
            Object info = ReincarnatedSkills.call(api, "getMorphInfo", player);
            if (Boolean.TRUE.equals(ReincarnatedSkills.call(info, "isMorphed"))) {
                Object size = ReincarnatedSkills.call(info, "getMorphSize", partialTick);
                Object height = ReincarnatedSkills.call(info, "getMorphEyeHeight", partialTick);
                if (size instanceof EntityDimensions dimensions && height instanceof Number value) {
                    // Morph owns the rendered body. Player race multipliers describe the
                    // original slime and must not shrink the camera of an acquired mob.
                    body = dimensions;
                    eye = value.floatValue();
                }
            }
        }
        return new View(eyeHeight(eye, body.height()), distance(body.width(), body.height()));
    }

    public static float eyeHeight(float reported, float height) {
        // Some morph instances retain their constructor's eye height after loading NBT.
        // Keep valid low-set eyes, but reject zero, non-finite and outside-body values.
        float body = Float.isFinite(height) && height > 0 ? height : 1.8F;
        return Float.isFinite(reported) && reported >= body * 0.25F && reported <= body
                ? reported : body * 0.85F;
    }

    public static float distance(float width, float height) {
        return Math.max(2.0F, 4.0F * Math.max(width, height) / 1.8F);
    }
}
