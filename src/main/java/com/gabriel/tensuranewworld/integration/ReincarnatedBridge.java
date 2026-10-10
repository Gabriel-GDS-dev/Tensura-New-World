package com.gabriel.tensuranewworld.integration;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.registry.EvolutionRegistry;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class ReincarnatedBridge {
    private static final List<String> STORAGE_CLASSES = List.of(
            "io.github.manasmods.tensura.storage.TensuraStorages",
            "io.github.manasmods.tensura.api.TensuraStorages");
    private static final List<String> RACE_API_CLASSES = List.of(
            "io.github.manasmods.manascore.api.race.RaceAPI",
            "io.github.manasmods.manascore.race.api.RaceAPI");
    private static final List<String> SKILL_API_CLASSES = List.of(
            "io.github.manasmods.manascore.api.skill.SkillAPI",
            "io.github.manasmods.manascore.skill.api.SkillAPI");

    private ReincarnatedBridge() {
    }

    public static boolean syncFromBase(LivingEntity entity, TensuraPlayerData data) {
        Object existence = existence(entity).orElse(null);
        if (existence == null) {
            return false;
        }

        readNumber(existence, "getMagicule").ifPresent(value -> {
            // Tensura exposes current magicules and EP, but no maximum magicule value.
            // Preserve New World's own maximum while ensuring imported energy is never clipped.
            if (value > data.getMaxMagicules()) {
                data.setMaxMagicules(value);
            }
            data.setMagicules(value);
        });
        readNumber(existence, "getSoulPoints").ifPresent(value -> data.setSoulPoints(value.intValue()));
        readBoolean(existence, "isDemonLordSeed").ifPresent(data::setDemonLordSeed);
        readBoolean(existence, "isTrueDemonLord").ifPresent(data::setTrueDemonLord);
        readBoolean(existence, "isHeroEgg").ifPresent(data::setHeroEgg);
        readBoolean(existence, "isTrueHero").ifPresent(data::setTrueHero);
        Object name = invokeNoArgs(existence, "getName");
        if (name instanceof String value && !value.isBlank()) {
            data.setReceivedName(true);
        }
        raceId(entity).ifPresent(baseRace -> {
            if (data.getEvolutionStage().isBlank()) {
                data.setRaceId(baseRace);
                data.setEvolutionStage(EvolutionRegistry.inferStage(baseRace));
            } else if (baseRace.equals("tensura:god_slime") && data.isTrueDemonLord()) {
                // Accept the native forward milestone instead of reverting it to Demon Slime.
                data.setRaceId(baseRace);
                data.setEvolutionStage("ultimate_slime");
                data.setPendingEvolution("");
            } else if (!baseRace.equals(data.getRaceId())) {
                // New World owns evolution after the initial lineage is selected.
                writeRace(entity, data.getRaceId());
            }
        });
        skillIds(entity).forEach(data.getSkills()::add);
        data.recalculateExistencePoints();
        return true;
    }

    public static void writeMagicules(LivingEntity entity, long current, long maximum) {
        existence(entity).ifPresent(existence -> {
            invokeSetter(existence, "setMagicule", Math.min(current, maximum));
            invokeNoArgs(existence, "markDirty");
        });
    }

    public static void writeSoulPoints(LivingEntity entity, int soulPoints) {
        existence(entity).ifPresent(existence -> {
            invokeSetter(existence, "setSoulPoints", soulPoints);
            invokeNoArgs(existence, "markDirty");
        });
    }

    public static boolean writeRace(LivingEntity entity, String raceId) {
        ResourceLocation location = ResourceLocation.tryParse(raceId);
        if (location == null) {
            return false;
        }
        for (String className : RACE_API_CLASSES) {
            try {
                Class<?> api = Class.forName(className);
                Method getter = findStatic(api, "getRaceFrom", entity.getClass());
                if (getter == null) {
                    continue;
                }
                Object storage = getter.invoke(null, entity);
                for (Method method : storage.getClass().getMethods()) {
                    if (method.getName().equals("setRace")
                            && method.getParameterCount() == 2
                            && method.getParameterTypes()[0] == ResourceLocation.class
                            && (method.getParameterTypes()[1] == boolean.class
                                    || method.getParameterTypes()[1] == Boolean.class)) {
                        Object result = method.invoke(storage, location, false);
                        invokeNoArgs(storage, "markDirty");
                        return !(result instanceof Boolean success) || success;
                    }
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Keep the core usable if an upstream API package changes.
            }
        }
        return false;
    }

    public static void writeDemonLordState(LivingEntity entity, boolean seed, boolean trueDemonLord) {
        existence(entity).ifPresent(existence -> {
            invokeSetter(existence, "setDemonLordSeed", seed);
            invokeSetter(existence, "setTrueDemonLord", trueDemonLord);
            invokeNoArgs(existence, "markDirty");
        });
    }

    public static long baseExistencePoints(LivingEntity entity) {
        return existence(entity)
                .flatMap(value -> readNumber(value, "getEP"))
                .orElseGet(() -> Math.max(1L, Math.round(entity.getMaxHealth() * 100.0D)));
    }

    public static Optional<String> raceId(LivingEntity entity) {
        for (String className : RACE_API_CLASSES) {
            try {
                Class<?> api = Class.forName(className);
                Method getter = findStatic(api, "getRaceFrom", entity.getClass());
                if (getter == null) {
                    continue;
                }
                Object storage = getter.invoke(null, entity);
                Object optionalRace = invokeNoArgs(storage, "getRace");
                if (optionalRace instanceof Optional<?> race && race.isPresent()) {
                    Object id = invokeNoArgs(race.get(), "getRaceId");
                    if (id instanceof ResourceLocation location) {
                        return Optional.of(location.toString());
                    }
                    if (id != null) {
                        return Optional.of(id.toString());
                    }
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // The base mod API is optional at compile time and versioned independently.
            }
        }
        return Optional.empty();
    }

    private static Collection<String> skillIds(LivingEntity entity) {
        java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>();
        for (String className : SKILL_API_CLASSES) {
            try {
                Class<?> api = Class.forName(className);
                Method getter = findStatic(api, "getSkillsFrom", entity.getClass());
                if (getter == null) {
                    continue;
                }
                Object storage = getter.invoke(null, entity);
                Object learned = invokeNoArgs(storage, "getLearnedSkills");
                if (learned instanceof Iterable<?> values) {
                    for (Object skill : values) {
                        Object id = invokeNoArgs(skill, "getSkillId");
                        if (id != null) {
                            result.add(id.toString());
                        }
                    }
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Keep the core usable if an upstream API package changes.
            }
        }
        return result;
    }

    private static Optional<Object> existence(LivingEntity entity) {
        for (String className : STORAGE_CLASSES) {
            try {
                Class<?> storage = Class.forName(className);
                Method getter = findStatic(storage, "getExistenceFrom", entity.getClass());
                if (getter != null) {
                    return Optional.ofNullable(getter.invoke(null, entity));
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Running the unit tests does not require the upstream mod.
            }
        }
        return Optional.empty();
    }

    private static Method findStatic(Class<?> type, String name, Class<?> argumentType) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name)
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(argumentType)) {
                return method;
            }
        }
        return null;
    }

    private static Optional<Long> readNumber(Object target, String methodName) {
        Object value = invokeNoArgs(target, methodName);
        return value instanceof Number number ? Optional.of(number.longValue()) : Optional.empty();
    }

    private static Optional<Boolean> readBoolean(Object target, String methodName) {
        Object value = invokeNoArgs(target, methodName);
        return value instanceof Boolean flag ? Optional.of(flag) : Optional.empty();
    }

    private static Object invokeNoArgs(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void invokeSetter(Object target, String methodName, Object value) {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                continue;
            }
            try {
                Class<?> type = method.getParameterTypes()[0];
                Object adapted = value;
                if (value instanceof Number number) {
                    if (type == int.class || type == Integer.class) adapted = number.intValue();
                    else if (type == long.class || type == Long.class) adapted = number.longValue();
                    else if (type == float.class || type == Float.class) adapted = number.floatValue();
                    else if (type == double.class || type == Double.class) adapted = number.doubleValue();
                }
                method.invoke(target, adapted);
                return;
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                // Try another overload if present.
            }
        }
    }
}
