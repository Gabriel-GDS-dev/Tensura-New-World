package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.SubordinateData;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import java.util.ListIterator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class HarvestFestivalManager {
    public static final int SEED_SOULS = 1_000;
    public static final int AWAKENING_SOULS = 10_000;
    public static final int SLEEP_TICKS = 20 * 60 * 3;

    private HarvestFestivalManager() {
    }

    public static void evaluate(ServerPlayer player, TensuraPlayerData data) {
        if (!data.isDemonLordSeed() && data.getSoulPoints() >= SEED_SOULS) {
            data.setDemonLordSeed(true);
            ReincarnatedBridge.writeDemonLordState(player, true, false);
            VoiceOfTheWorld.notice(player, "The Demon Lord Seed has germinated.");
        }
        if (data.isDemonLordSeed() && !data.isTrueDemonLord()
                && !data.isHarvestFestivalActive() && data.getSoulPoints() >= AWAKENING_SOULS) {
            start(player, data);
        }
    }

    public static boolean start(ServerPlayer player, TensuraPlayerData data) {
        if (!data.isDemonLordSeed() || data.isTrueDemonLord() || data.getSoulPoints() < AWAKENING_SOULS) {
            return false;
        }
        data.setHarvestFestivalActive(true);
        data.setHarvestSleepTicks(SLEEP_TICKS);
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, SLEEP_TICKS, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SLEEP_TICKS, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLEEP_TICKS, 9, false, false));
        VoiceOfTheWorld.announce(player, "The Harvest Festival has begun for " + player.getGameProfile().getName() + ".");
        return true;
    }

    public static void tick(ServerPlayer player, TensuraPlayerData data) {
        if (!data.isHarvestFestivalActive()) {
            return;
        }
        player.setDeltaMovement(0.0D, Math.min(0.0D, player.getDeltaMovement().y), 0.0D);
        data.setHarvestSleepTicks(data.getHarvestSleepTicks() - 1);
        if (data.getHarvestSleepTicks() <= 0) {
            complete(player, data);
        }
    }

    public static void debugComplete(ServerPlayer player, TensuraPlayerData data) {
        if (!data.isHarvestFestivalActive()) {
            data.setDemonLordSeed(true);
            data.setSoulPoints(Math.max(data.getSoulPoints(), AWAKENING_SOULS));
            start(player, data);
        }
        data.setHarvestSleepTicks(1);
        tick(player, data);
    }

    private static void complete(ServerPlayer player, TensuraPlayerData data) {
        data.setHarvestFestivalActive(false);
        data.setTrueDemonLord(true);
        data.setSoulPoints(data.getSoulPoints() - AWAKENING_SOULS);
        data.setMaxMagicules(Math.multiplyExact(data.getMaxMagicules(), 5L));
        data.setMagicules(data.getMaxMagicules());
        data.getTitles().add("True Demon Lord");
        data.getSkills().add("tensuranewworld:beelzebuth");

        ListIterator<SubordinateData> iterator = data.getSubordinates().listIterator();
        while (iterator.hasNext()) {
            SubordinateData subordinate = iterator.next();
            if (!subordinate.giftGranted()) {
                iterator.set(subordinate.withGift(Math.round(subordinate.existencePoints() * 1.5D)));
            }
        }
        ReincarnatedBridge.writeDemonLordState(player, true, true);
        ReincarnatedBridge.writeSoulPoints(player, data.getSoulPoints());
        ReincarnatedBridge.writeMagicules(player, data.getMagicules(), data.getMaxMagicules());
        data.recalculateExistencePoints();
        RaceEvolutionManager.applyHarvestEvolution(player, data);
        RaceEvolutionManager.evaluate(player, data);
        com.gabriel.tensuranewworld.integration.SageEvolution.evaluate(player, data);
        VoiceOfTheWorld.announce(player, player.getGameProfile().getName() + " has awakened as a True Demon Lord.");
    }
}
