package com.gabriel.tensuranewworld.registry;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Validate the reflection boundary against the actual shipped dependency, without initializing mods. */
class ReincarnatedApiContractTest {
    private Set<String> methods(byte[] bytes) {
        Set<String> result = new HashSet<>();
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                result.add(name + descriptor);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return result;
    }

    private byte[] tensura(String name) throws Exception {
        try (ZipFile jar = new ZipFile(Path.of("libs/tensura-neoforge-2.0.1.3.jar").toFile());
                var stream = jar.getInputStream(jar.getEntry(name.replace('.', '/') + ".class"))) {
            return stream.readAllBytes();
        }
    }

    private byte[] manas(String name) throws Exception {
        try (ZipFile jar = new ZipFile(Path.of("libs/manascore-neoforge-4.0.0.2.jar").toFile());
                var stream = jar.getInputStream(jar.getEntry("META-INF/jars/manascore-skill-neoforge-4.0.0.2.jar"));
                var nested = new ZipInputStream(new ByteArrayInputStream(stream.readAllBytes()))) {
            for (var entry = nested.getNextEntry(); entry != null; entry = nested.getNextEntry()) {
                if (entry.getName().equals(name.replace('.', '/') + ".class")) return nested.readAllBytes();
            }
        }
        throw new AssertionError("Missing API class: " + name);
    }

    @Test void equipsSelectedModeThroughOfficialServerValidatedPacket() throws Exception {
        Set<String> api = methods(tensura("io.github.manasmods.tensura.network.c2s.RequestAbilityModeChangePacket"));
        String packet = "Lio/github/manasmods/tensura/network/c2s/RequestAbilityModeChangePacket;";
        assertTrue(api.contains("changeAbilityPacket(IILnet/minecraft/resources/ResourceLocation;I)" + packet));
        assertTrue(api.contains("toggleAbilityPacket(Lnet/minecraft/resources/ResourceLocation;)" + packet));
    }

    @Test void runtimeCostsAndLearningMetadataExistInInstalledVersion() throws Exception {
        String instance = "Lio/github/manasmods/manascore/skill/api/ManasSkillInstance;";
        String living = "Lnet/minecraft/world/entity/LivingEntity;";
        Set<String> skill = methods(tensura("io.github.manasmods.tensura.ability.TensuraSkill"));
        assertTrue(skill.contains("getModeLearningList(" + instance + ")Ljava/util/List;"));
        assertTrue(skill.contains("getLearningPointRequirement(" + instance + living + "I)D"));
        assertTrue(skill.contains("getModeName(" + instance + "I)Lnet/minecraft/network/chat/Component;"));
        assertTrue(skill.contains("getLearningCostMultiplier(I)F"));
        assertTrue(skill.contains("isAffectedByStatus(" + instance + living + "I)Z"));
        Set<String> costs = methods(tensura("io.github.manasmods.tensura.ability.TensuraSkillInstance"));
        assertTrue(costs.contains("getMagiculeCost(" + living + "I)D"));
        assertTrue(costs.contains("getAuraCost(" + living + "I)D"));
    }

    @Test void nativeStorageProvidesLearnedInstancesAndPerModeCooldowns() throws Exception {
        Set<String> storage = methods(manas("io.github.manasmods.manascore.skill.impl.SkillStorage"));
        assertTrue(storage.contains("getLearnedSkills()Ljava/util/Collection;"));
        Set<String> instance = methods(manas("io.github.manasmods.manascore.skill.api.ManasSkillInstance"));
        assertTrue(instance.contains("getCoolDown(I)I"));
        assertTrue(instance.contains("getMastery()D"));
        assertTrue(instance.contains("getModes()I"));
        Set<String> sleep = methods(tensura("io.github.manasmods.tensura.effect.debuff.SleepEffect"));
        assertTrue(sleep.contains("isForcedSleeping(Lnet/minecraft/world/entity/LivingEntity;)Z"));
    }
}
