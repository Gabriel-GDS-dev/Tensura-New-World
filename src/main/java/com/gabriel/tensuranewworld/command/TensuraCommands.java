package com.gabriel.tensuranewworld.command;

import com.gabriel.tensuranewworld.TensuraNewWorld;
import com.gabriel.tensuranewworld.data.CharacterAttribute;
import com.gabriel.tensuranewworld.data.ModAttachments;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.gameplay.HarvestFestivalManager;
import com.gabriel.tensuranewworld.gameplay.NamingManager;
import com.gabriel.tensuranewworld.gameplay.RaceEvolutionManager;
import com.gabriel.tensuranewworld.gameplay.StoryManager;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import com.gabriel.tensuranewworld.network.NetworkHandler;
import com.gabriel.tensuranewworld.registry.EvolutionPath;
import com.gabriel.tensuranewworld.registry.EvolutionRegistry;
import com.gabriel.tensuranewworld.registry.SkillRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TensuraNewWorld.MOD_ID)
public final class TensuraCommands {
    private TensuraCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tnw")
                .then(Commands.literal("status").executes(TensuraCommands::status))
                .then(Commands.literal("story").executes(TensuraCommands::story))
                .then(Commands.literal("evolution").executes(TensuraCommands::evolution))
                .then(Commands.literal("evolve").executes(TensuraCommands::evolve))
                .then(Commands.literal("attribute")
                        .then(Commands.argument("name", StringArgumentType.word()).executes(TensuraCommands::investAttribute)))
                .then(Commands.literal("form")
                        .then(Commands.literal("clear").executes(TensuraCommands::clearForm))
                        .then(Commands.argument("entity_id", ResourceLocationArgument.id()).executes(TensuraCommands::selectForm)))
                .then(Commands.literal("ritual")
                        .then(Commands.literal("altar").executes(TensuraCommands::altarRitual))
                        .then(Commands.literal("soul_contract").executes(TensuraCommands::soulContract)))
                .then(Commands.literal("nation_found")
                        .then(Commands.argument("name", StringArgumentType.greedyString()).executes(TensuraCommands::foundNation)))
                .then(Commands.literal("naming")
                        .then(Commands.argument("entity_id", IntegerArgumentType.integer(0))
                                .then(Commands.argument("name", StringArgumentType.word()).executes(TensuraCommands::nameEntity))))
                .then(Commands.literal("naming_nearest")
                        .then(Commands.argument("name", StringArgumentType.word()).executes(TensuraCommands::nameNearest)))
                .then(Commands.literal("debug").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("add_souls")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(TensuraCommands::addSouls)))
                        .then(Commands.literal("add_essence")
                                .then(Commands.argument("amount", LongArgumentType.longArg(1L)).executes(TensuraCommands::addEssence)))
                        .then(Commands.literal("set_magicules")
                                .then(Commands.argument("amount", LongArgumentType.longArg(1L)).executes(TensuraCommands::setMagicules)))
                        .then(Commands.literal("grant_skill")
                                .then(Commands.argument("skill", StringArgumentType.word()).executes(TensuraCommands::grantSkill)))
                        .then(Commands.literal("set_race")
                                .then(Commands.argument("race", StringArgumentType.word()).executes(TensuraCommands::setRace)))
                        .then(Commands.literal("harvest").executes(TensuraCommands::harvest))
                        .then(Commands.literal("complete_harvest").executes(TensuraCommands::completeHarvest))));
    }

    private static int status(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        context.getSource().sendSuccess(() -> Component.literal(
                "Race: " + data.getRaceId() + " | EP: " + data.getExistencePoints()
                        + " | Magicules: " + data.getMagicules() + "/" + data.getMaxMagicules()
                        + " | Essence: " + data.getEssence() + " (" + data.getAttributePoints() + " points)"
                        + " | Souls: " + data.getSoulPoints() + " | Stage: " + data.getEvolutionStage()
                        + (data.getPendingEvolution().isBlank() ? "" : " | Ready: " + data.getPendingEvolution())
                        + " | Chapter: " + data.getStoryChapter()), false);
        return 1;
    }

    private static int story(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        TensuraPlayerData data = data(context.getSource().getPlayerOrException());
        context.getSource().sendSuccess(() -> Component.literal("Objective: " + data.getStoryObjective()), false);
        return 1;
    }

    private static int evolution(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        RaceEvolutionManager.evaluate(player, data);
        context.getSource().sendSuccess(() -> Component.literal("Evolution stage: " + data.getEvolutionStage()), false);
        java.util.List<EvolutionPath> paths = EvolutionRegistry.next(data.getEvolutionStage());
        if (paths.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("This bloodline has reached its known apex."), false);
            return 1;
        }
        for (EvolutionPath path : paths) {
            context.getSource().sendSuccess(() -> Component.literal(
                    path.displayName() + (path.harvestOnly() ? " [Harvest Festival]" : "")), false);
            path.conditions().forEach(condition -> context.getSource().sendSuccess(() -> Component.literal(
                    (condition.isMet(data) ? "[complete] " : "[pending] ") + condition.label(data)), false));
        }
        return 1;
    }

    private static int evolve(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        RaceEvolutionManager.Result result = RaceEvolutionManager.requestEvolution(player, data);
        if (result.success()) context.getSource().sendSuccess(() -> Component.literal(result.message()), true);
        else context.getSource().sendFailure(Component.literal(result.message()));
        NetworkHandler.sync(player, data);
        return result.success() ? 1 : 0;
    }

    private static int investAttribute(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        String requested = StringArgumentType.getString(context, "name");
        CharacterAttribute attribute = CharacterAttribute.byId(requested).orElse(null);
        if (attribute == null) {
            context.getSource().sendFailure(Component.literal("Unknown attribute: " + requested));
            return 0;
        }
        if (!data.investAttribute(attribute)) {
            context.getSource().sendFailure(Component.literal(
                    data.getAttributePoints() <= 0 ? "You have no growth points to spend."
                            : "This attribute has reached its limit."));
            return 0;
        }
        ReincarnatedBridge.writeMagicules(player, data.getMagicules(), data.getMaxMagicules());
        NetworkHandler.sync(player, data);
        context.getSource().sendSuccess(() -> Component.literal(
                attribute.displayName() + " increased to " + data.getAttribute(attribute) + "."), false);
        return 1;
    }

    private static int selectForm(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        String entityId = ResourceLocationArgument.getId(context, "entity_id").toString();
        if (!data.selectMimicForm(entityId)) {
            context.getSource().sendFailure(Component.literal("That form has not been absorbed."));
            return 0;
        }
        NetworkHandler.sync(player, data);
        NetworkHandler.syncMimicForm(player, data);
        context.getSource().sendSuccess(() -> Component.literal("Mimicking " + entityId + "."), false);
        return 1;
    }

    private static int clearForm(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        data.clearMimicForm();
        NetworkHandler.sync(player, data);
        NetworkHandler.syncMimicForm(player, data);
        context.getSource().sendSuccess(() -> Component.literal("Mimic form cleared."), false);
        return 1;
    }

    private static int altarRitual(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        BlockPos center = player.blockPosition();
        boolean beaconNearby = hasNearbyBeacon(player, center);
        if (!beaconNearby) {
            context.getSource().sendFailure(Component.literal("The elemental rite requires an active altar beacon within two blocks."));
            return 0;
        }
        data.setElementalBlessing(true);
        RaceEvolutionManager.evaluate(player, data);
        NetworkHandler.sync(player, data);
        context.getSource().sendSuccess(() -> Component.literal("The elemental altar has accepted your existence."), true);
        return 1;
    }

    private static boolean hasNearbyBeacon(ServerPlayer player, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 2, 2))) {
            if (player.serverLevel().getBlockState(pos).is(Blocks.BEACON)) {
                return true;
            }
        }
        return false;
    }

    private static int soulContract(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        boolean humanNearby = player.serverLevel()
                .getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.0D),
                        entity -> entity != player && (entity instanceof net.minecraft.world.entity.npc.Villager
                                || entity instanceof ServerPlayer))
                .stream().findAny().isPresent();
        if (!humanNearby) {
            context.getSource().sendFailure(Component.literal("A willing human or villager must stand within six blocks."));
            return 0;
        }
        long cost = 10_000L;
        if (data.getMagicules() < cost) {
            context.getSource().sendFailure(Component.literal("The soul contract requires 10,000 magicules."));
            return 0;
        }
        data.setMagicules(data.getMagicules() - cost);
        data.setSoulContract(true);
        ReincarnatedBridge.writeMagicules(player, data.getMagicules(), data.getMaxMagicules());
        RaceEvolutionManager.evaluate(player, data);
        NetworkHandler.sync(player, data);
        context.getSource().sendSuccess(() -> Component.literal("The human soul contract is sealed."), true);
        return 1;
    }

    private static int foundNation(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        String name = StringArgumentType.getString(context, "name").trim();
        if (name.isBlank() || name.length() > 40 || data.getSubordinates().size() < 5) {
            context.getSource().sendFailure(Component.literal("A nation needs a valid name and at least five named subordinates."));
            return 0;
        }
        data.setNationName(name);
        data.getDiplomacy().put(name, 100);
        data.getTitles().add("Leader");
        if (data.getEvolutionStage().contains("orc")) {
            data.getTitles().add("King of Orcs");
        }
        RaceEvolutionManager.evaluate(player, data);
        StoryManager.evaluate(player, data);
        NetworkHandler.sync(player, data);
        context.getSource().sendSuccess(() -> Component.literal("Nation founded: " + name), true);
        return 1;
    }

    private static int nameEntity(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Entity target = player.serverLevel().getEntity(IntegerArgumentType.getInteger(context, "entity_id"));
        NamingManager.Result result = NamingManager.name(player, target, StringArgumentType.getString(context, "name"), 1);
        if (result.success()) context.getSource().sendSuccess(() -> Component.literal(result.message()), true);
        else context.getSource().sendFailure(Component.literal(result.message()));
        NetworkHandler.sync(player, data(player));
        return result.success() ? 1 : 0;
    }

    private static int nameNearest(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Entity target = player.serverLevel()
                .getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0D), entity -> entity != player)
                .stream()
                .min(java.util.Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
        NamingManager.Result result = NamingManager.name(
                player, target, StringArgumentType.getString(context, "name"), 1);
        if (result.success()) context.getSource().sendSuccess(() -> Component.literal(result.message()), true);
        else context.getSource().sendFailure(Component.literal(result.message()));
        NetworkHandler.sync(player, data(player));
        return result.success() ? 1 : 0;
    }

    private static int addSouls(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        data.setSoulPoints(data.getSoulPoints() + IntegerArgumentType.getInteger(context, "amount"));
        ReincarnatedBridge.writeSoulPoints(player, data.getSoulPoints());
        HarvestFestivalManager.evaluate(player, data);
        NetworkHandler.sync(player, data);
        return data.getSoulPoints();
    }

    private static int addEssence(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        data.addEssence(LongArgumentType.getLong(context, "amount"));
        data.recalculateExistencePoints();
        NetworkHandler.sync(player, data);
        return 1;
    }

    private static int setMagicules(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        long amount = LongArgumentType.getLong(context, "amount");
        data.setMaxMagicules(amount);
        data.setMagicules(amount);
        ReincarnatedBridge.writeMagicules(player, amount, amount);
        RaceEvolutionManager.evaluate(player, data);
        NetworkHandler.sync(player, data);
        return 1;
    }

    private static int grantSkill(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String skill = SkillRegistry.normalize(StringArgumentType.getString(context, "skill"));
        if (SkillRegistry.get(skill).isEmpty()) {
            context.getSource().sendFailure(Component.literal("Unknown skill: " + skill));
            return 0;
        }
        data(player).getSkills().add(skill);
        data(player).recalculateExistencePoints();
        NetworkHandler.sync(player, data(player));
        return 1;
    }

    private static int setRace(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String race = StringArgumentType.getString(context, "race");
        String raceId = race.contains(":") ? race : "tensura:" + race;
        data(player).setRaceId(raceId);
        data(player).setEvolutionStage(EvolutionRegistry.inferStage(raceId));
        data(player).setPendingEvolution("");
        ReincarnatedBridge.writeRace(player, raceId);
        NetworkHandler.sync(player, data(player));
        return 1;
    }

    private static int harvest(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        return HarvestFestivalManager.start(player, data) ? 1 : 0;
    }

    private static int completeHarvest(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TensuraPlayerData data = data(player);
        HarvestFestivalManager.debugComplete(player, data);
        NetworkHandler.sync(player, data);
        return 1;
    }

    private static TensuraPlayerData data(ServerPlayer player) {
        return player.getData(ModAttachments.PLAYER_DATA);
    }
}
