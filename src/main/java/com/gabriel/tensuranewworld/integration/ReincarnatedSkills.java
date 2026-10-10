package com.gabriel.tensuranewworld.integration;

import com.gabriel.tensuranewworld.data.SkillView;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.registry.SkillAvailability;
import com.gabriel.tensuranewworld.registry.SkillCategory;
import com.gabriel.tensuranewworld.registry.SkillRegistry;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.LivingEntity;

/** Reads the installed API rather than maintaining a second, divergent skill catalog. */
public final class ReincarnatedSkills {
    public static final String SNAPSHOT_KEY = "RaphaelSkills";
    public static final String API = "io.github.manasmods.manascore.skill.api.SkillAPI";
    private static final String STORAGES = "io.github.manasmods.tensura.storage.TensuraStorages";

    private ReincarnatedSkills() { }

    public static List<SkillView> snapshot(LivingEntity entity, TensuraPlayerData data) {
        List<SkillView> result = new ArrayList<>();
        Set<String> nativeIds = new HashSet<>();
        Object storage = callStatic(API, "getSkillsFrom", entity);
        Object existence = callStatic(STORAGES, "getExistenceFrom", entity);
        Object ability = callStatic(STORAGES, "getAbilityFrom", entity);
        Object learned = call(storage, "getLearnedSkills");
        if (learned instanceof Collection<?> instances) {
            for (Object instance : instances) {
                try {
                    SkillView view = describe(entity, instance, existence, ability);
                    if (view.id().equals("greatsage:raphael")) continue;
                    result.add(view.withUnlockGuide(SkillUnlockGuide.text(view.id(), call(instance, "getSkill"), entity)));
                    nativeIds.add(view.id());
                } catch (RuntimeException ignored) {
                    // A changed third-party skill must not prevent the other rows from appearing.
                }
            }
        }
        // Include locked native abilities so their acquisition instructions can be
        // consulted before learning them. Never persist them as learned player skills.
        Object registry = callStatic(API, "getSkillRegistry");
        if (registry instanceof Iterable<?> registered) {
            for (Object skill : registered) {
                Object identifier = call(skill, "getRegistryName");
                if (identifier == null || nativeIds.contains(identifier.toString())) continue;
                String id = identifier.toString();
                if (id.equals("greatsage:raphael")) continue;
                Object name = call(skill, "getName");
                Object description = call(skill, "getSkillDescription");
                Object icon = call(skill, "getSkillIcon");
                SkillCategory category = SkillCategory.from(String.valueOf(call(skill, "getType")), skill.getClass().getName());
                String hint = "Nao adquirida. Leia COMO DESBLOQUEAR nesta ficha. Este registro do catalogo nao concede a skill nem permite equipar.";
                result.add(new SkillView(id, key(name), text(name, id), key(description), text(description, ""),
                        icon == null ? "" : icon.toString(), category, 0, 0, false, false, false, false, -1,
                        List.of(new SkillView.Mode(0, "", "Como desbloquear", -1, -1, -1, -1, 0, false, -1,
                                SkillAvailability.UNAVAILABLE, hint)))
                        .withUnlockGuide(SkillUnlockGuide.text(id, skill, entity)));
                nativeIds.add(id);
            }
        }
        for (String id : data.getSkills().stream().sorted().toList()) {
            if (nativeIds.contains(id)) continue;
            String path = id.substring(id.indexOf(':') + 1);
            if (id.startsWith("tensuranewworld:") && nativeIds.contains("tensura:" + path)) continue;
            if (id.equals("tensuranewworld:raphael") && nativeIds.contains("greatsage:raphael")) continue;
            var definition = SkillRegistry.get(id).orElse(null);
            String name = definition == null ? path.replace('_', ' ') : definition.displayName();
            SkillCategory category = definition == null ? SkillCategory.OTHER
                    : SkillCategory.from(definition.tier().name(), "");
            String hint = id.startsWith("tensuranewworld:")
                    ? "Registro de progressao New World. Ainda nao possui uma acao propria. Para usar, adquira a habilidade equivalente no Reincarnated; este registro nao libera uma skill do mod-base."
                    : "Esta habilidade nao consta no armazenamento atual do Reincarnated. Adquira-a novamente pelas regras do mod-base para ativar.";
            result.add(new SkillView(id, "", name, "", definition == null ? "Registro aprendido." : definition.description(),
                    "", category, 0, 0, false, false, false, false, -1,
                    List.of(new SkillView.Mode(0, "", "Sem acao integrada", -1, -1, -1, -1, 0, false, -1,
                            SkillAvailability.UNAVAILABLE, hint)))
                    .withUnlockGuide(SkillUnlockGuide.text(id, null, entity)));
        }
        result.sort(java.util.Comparator.comparing(SkillView::category).thenComparing(SkillView::fallbackName));
        return List.copyOf(result);
    }

    private static SkillView describe(LivingEntity entity, Object instance, Object existence, Object ability) {
        Object skill = call(instance, "getSkill");
        Object identifier = call(instance, "getSkillId");
        if (identifier == null || skill == null) throw new IllegalStateException("Missing skill");
        String id = identifier.toString();
        Object name = call(skill, "getName");
        Object description = call(skill, "getSkillDescription");
        Object icon = call(skill, "getSkillIcon");
        SkillCategory category = SkillCategory.from(String.valueOf(call(skill, "getType")), skill.getClass().getName());
        double mastery = number(call(instance, "getMastery"), 0);
        // Native canInteractSkill/canActivateSkill and isOutOfEnergy may send chat,
        // switch passives off or consume staff charge. A read-only snapshot must
        // inspect their conditions without invoking these action validators.
        boolean physicalInteraction = !entity.isSpectator()
                && !flag(callStatic("io.github.manasmods.tensura.effect.debuff.SleepEffect", "isForcedSleeping", entity), false);
        if (category == SkillCategory.RESISTANCE && flag(callStatic(
                "io.github.manasmods.tensura.storage.ep.ExistenceStorage", "isInSleepMode", existence), false)) {
            physicalInteraction = false;
        }
        boolean toggleable = flag(call(instance, "canBeToggled", entity), false);
        int count = Math.max(1, Math.min(128, (int) number(call(instance, "getModes"), 1)));
        Object learning = call(skill, "getModeLearningList", instance);
        CompoundTag tag = call(instance, "getTag") instanceof CompoundTag value ? value : new CompoundTag();
        double mp = number(call(existence, "getMagicule"), 0);
        double ap = number(call(existence, "getAura"), 0);
        List<SkillView.Mode> modes = new ArrayList<>();
        for (int mode = 0; mode < count; mode++) {
            boolean areaAllowed = areaAllowed(entity, skill, mode);
            boolean interacts = physicalInteraction && areaAllowed
                    && !flag(callProtected(skill, "isAffectedByStatus", instance, entity, mode), false);
            boolean silenced = entity.getAirSupply() < entity.getMaxAirSupply()
                    || entity.getActiveEffects().stream().anyMatch(effect -> {
                        var effectId = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
                        return effectId != null && effectId.toString().equals("tensura:silence");
                    });
            if (silenced && (id.equals("tensura:voice_cannon") || category == SkillCategory.MAGIC
                    && !flag(call(skill, "isInstantCast", instance, entity), false))) interacts = false;
            Object modeName = call(skill, "getModeName", instance, mode);
            String modeId = String.valueOf(call(skill, "getModeId", instance, mode));
            double requirement = number(call(skill, "getLearningPointRequirement", instance, entity, mode), 0);
            double points = tag.getDouble(modeId);
            boolean modeLearning = learning instanceof List<?> list && list.contains(mode) && points < requirement;
            double cost = number(call(instance, "getMagiculeCost", entity, mode), -1);
            double auraCost = number(call(instance, "getAuraCost", entity, mode), -1);
            int cooldown = (int) number(call(instance, "getCoolDown", mode), 0);
            boolean slottable = flag(call(skill, "canBeSlotted", instance, entity, mode), false);
            boolean passive = !slottable;
            double multiplier = number(call(skill, "getLearningCostMultiplier", mode), 1);
            SkillAvailability state = SkillAvailability.evaluate(cost >= 0 && auraCost >= 0, mastery,
                    modeLearning, passive, interacts, cooldown,
                    flag(call(instance, "canIgnoreCoolDown", entity, mode), false),
                    entity.hasInfiniteMaterials(), mp, ap, Math.max(0, cost), Math.max(0, auraCost));
            String hint = switch (state) {
                case LEARNING -> category == SkillCategory.RESISTANCE
                        ? "Aprendizado pendente: faltam " + amount(-mastery) + " pontos. Esta resistencia progride com exposicao ao dano/efeito correspondente, conforme as regras do Reincarnated."
                        : "Faltam " + amount(-mastery) + " pontos de aprendizado. Equipe em um slot e tente usar no mundo para treinar; cada tentativa segue os custos, recarga e penalidades do Reincarnated.";
                case MODE_LEARNING -> "Treine este modo no mundo: " + amount(points) + " / " + amount(requirement)
                        + " pontos. As tentativas de uso desbloqueiam o modo pelas regras do Reincarnated.";
                case COOLDOWN -> "Aguarde " + amount(Math.ceil(cooldown / 20.0)) + " s para usar este modo novamente.";
                case ENERGY -> "Recupere energia. Necessario: " + amount(cost) + " magicules e " + amount(auraCost)
                        + " aura. Atual: " + amount(mp) + " magicules e " + amount(ap) + " aura.";
                case SEALED -> !areaAllowed ? "Esta habilidade esta proibida nesta area ou batalha. Saia da area restrita ou conclua a batalha para liberar o uso."
                        : entity.isSpectator() ? "Saia do modo espectador para usar habilidades."
                        : silenced ? "Recupere o ar e remova Silencio para usar este modo. Magias com conjuracao instantanea podem ignorar essa restricao."
                        : "Uso impedido pelo Reincarnated. Remova estados de repouso, aprisionamento ou supressao e confira as condicoes na descricao da habilidade.";
                case PASSIVE -> toggleable ? "Efeito passivo: use o botao de alternancia. Nao ocupa slot de combate."
                        : "Efeito passivo ou contextual: funciona pelas condicoes da descricao; nao possui ativacao por slot.";
                case READY -> "Equipe em um slot e use a tecla indicada no mundo. Mire no alvo quando necessario. O servidor verifica alvo, area e demais condicoes ao usar.";
                case UNAVAILABLE -> "A API desta habilidade nao informa os custos de uso. Consulte a interface do mod responsavel; nao e possivel equipar por este painel.";
            };
            int slot = -1;
            for (int index = 0; index < 3; index++) {
                Object equipped = call(ability, "getAbilitySlot", index);
                Object equippedSkill = call(equipped, "getSkill");
                if (id.equals(String.valueOf(call(equippedSkill, "getRegistryName")))
                        && number(call(equipped, "getMode"), -1) == mode) { slot = index; break; }
            }
            modes.add(new SkillView.Mode(mode, key(modeName), text(modeName, "Modo " + (mode + 1)), cost, auraCost,
                    cost < 0 ? -1 : cost * multiplier, auraCost < 0 ? -1 : auraCost * multiplier,
                    cooldown, slottable && state != SkillAvailability.UNAVAILABLE, slot, state, hint));
        }
        return new SkillView(id, key(name), text(name, id), key(description), text(description, ""),
                icon == null ? "" : icon.toString(), category, mastery,
                (int) number(call(instance, "getMaxMastery"), 0), toggleable,
                flag(call(instance, "isToggled"), false), toggleable && physicalInteraction && mastery >= 0
                        && areaAllowed(entity, skill, -1)
                        && !flag(callProtected(skill, "isAffectedByStatus", instance, entity, 0), false),
                flag(call(instance, "isTemporarySkill"), false), (int) number(call(instance, "getRemoveTime"), -1), modes);
    }

    private static boolean areaAllowed(LivingEntity entity, Object skill, int mode) {
        if (entity.level().getServer() == null) return true;
        var overworld = entity.level().getServer().overworld();
        Object restrictions = call(callStatic(STORAGES, "getWorldRestrictionFrom", overworld), "getRestrictions");
        if (restrictions instanceof java.util.Map<?, ?> values) {
            for (Object restriction : values.values()) {
                if (entity.level().dimension().equals(call(restriction, "getDimension"))
                        && flag(call(restriction, "contains", entity.blockPosition()), false)
                        && flag(call(restriction, "isAbilityBanned", skill, mode), false)) return false;
            }
        }
        Object fights = call(callStatic(STORAGES, "getBossFightHolder", overworld), "getBossFights");
        if (fights instanceof java.util.Map<?, ?> values) {
            for (Object fight : values.values()) {
                Object joined = call(fight, "getJoinedPlayers");
                Object banned = call(fight, "getBannedAbilities");
                if (joined instanceof Collection<?> players && players.contains(entity.getUUID())
                        && banned instanceof Collection<?> skills && skills.contains(skill)) return false;
            }
        }
        return true;
    }

    private static Object callProtected(Object target, String name, Object... arguments) {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != arguments.length) continue;
                try { method.setAccessible(true); return method.invoke(target, arguments); }
                catch (ReflectiveOperationException | RuntimeException ignored) { return null; }
            }
        }
        return null;
    }

    private static String key(Object value) {
        return value instanceof Component component && component.getContents() instanceof TranslatableContents translated
                ? translated.getKey() : "";
    }

    private static String text(Object value, String fallback) {
        return value instanceof Component component ? component.getString() : fallback;
    }

    private static String amount(double value) {
        return String.format(java.util.Locale.ROOT, value == Math.floor(value) ? "%.0f" : "%.1f", value);
    }

    private static double number(Object value, double fallback) {
        return value instanceof Number number && Double.isFinite(number.doubleValue()) ? number.doubleValue() : fallback;
    }

    private static boolean flag(Object value, boolean fallback) {
        return value instanceof Boolean bool ? bool : fallback;
    }

    public static Object callStatic(String className, String method, Object... arguments) {
        try { return invoke(Class.forName(className), null, method, arguments); }
        catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) { return null; }
    }

    public static Object call(Object target, String method, Object... arguments) {
        if (target == null) return null;
        try { return invoke(target.getClass(), target, method, arguments); }
        catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) { return null; }
    }

    private static Object invoke(Class<?> type, Object target, String name, Object[] arguments)
            throws ReflectiveOperationException {
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != arguments.length) continue;
            Class<?>[] parameters = method.getParameterTypes();
            boolean matches = true;
            for (int i = 0; i < arguments.length; i++) {
                Class<?> parameter = parameters[i];
                if (parameter == int.class) parameter = Integer.class;
                else if (parameter == boolean.class) parameter = Boolean.class;
                else if (parameter == float.class) parameter = Float.class;
                else if (parameter == double.class) parameter = Double.class;
                if (arguments[i] != null && !parameter.isInstance(arguments[i])) { matches = false; break; }
            }
            if (matches) return method.invoke(target, arguments);
        }
        return null;
    }
}
