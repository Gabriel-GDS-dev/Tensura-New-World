package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import com.gabriel.tensuranewworld.network.TargetVitalsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TargetInspection {
    private TargetInspection() { }
    public static LivingEntity target(Player player) {
        var start = player.getEyePosition();
        var end = start.add(player.getLookAngle().scale(24));
        var wall = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        var hit = ProjectileUtil.getEntityHitResult(player, start, wall.getLocation(), player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1),
                e -> e instanceof LivingEntity && e.isAlive() && e.isPickable() && !e.isSpectator(), start.distanceToSqr(wall.getLocation()));
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }
    public static TargetVitalsPayload inspect(LivingEntity mob) {
        Object existence = ReincarnatedSkills.callStatic("io.github.manasmods.tensura.storage.TensuraStorages", "getExistenceFrom", mob);
        Object mp = ReincarnatedSkills.call(existence, "getMagicule");
        String name = mob.getDisplayName().getString();
        if (name.length() > 256) name = name.substring(0, 256);
        return new TargetVitalsPayload(mob.getId(), name, mob.getHealth(), mob.getMaxHealth(), mp instanceof Number n ? Math.max(0, n.doubleValue()) : -1,
                ReincarnatedBridge.baseExistencePoints(mob));
    }
    public static void sync(ServerPlayer player) {
        LivingEntity mob = target(player);
        PacketDistributor.sendToPlayer(player, mob == null ? new TargetVitalsPayload(-1, "", 0, 0, -1, 0) : inspect(mob));
    }
}
