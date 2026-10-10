package com.gabriel.tensuranewworld.client;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ArchitecturySkillTransportTest {
    private record TestPayload(String action) implements CustomPacketPayload {
        @Override public Type<TestPayload> type() {
            return new Type<>(ResourceLocation.fromNamespaceAndPath("tensura", "request_ability_mode_change"));
        }
    }

    public static class RecordingManager {
        static final List<CustomPacketPayload> sent = new ArrayList<>();
        public static void sendToServer(CustomPacketPayload payload) { sent.add(payload); }
        public static void sendToServer(ResourceLocation id, Object buffer) { fail("Wrong overload"); }
    }

    public static class RejectingManager {
        public static void sendToServer(CustomPacketPayload payload) { throw new IllegalStateException("Unavailable codec"); }
    }

    @Test void equipmentAndPassiveActionsUseTheTypedArchitecturyPipeline() throws Exception {
        RecordingManager.sent.clear();
        var equip = new TestPayload("equip");
        var toggle = new TestPayload("toggle");
        ArchitecturySkillTransport.send(equip, RecordingManager.class);
        ArchitecturySkillTransport.send(toggle, RecordingManager.class);
        assertEquals(List.of(equip, toggle), RecordingManager.sent);
    }

    @Test void senderErrorsArePropagatedInsteadOfReportedAsSuccess() {
        var error = assertThrows(InvocationTargetException.class,
                () -> ArchitecturySkillTransport.send(new TestPayload("equip"), RejectingManager.class));
        assertInstanceOf(IllegalStateException.class, error.getCause());
    }

    @Test void missingArchitecturyApiFailsWithoutRawPayloadFallback() {
        assertThrows(NoSuchMethodException.class,
                () -> ArchitecturySkillTransport.send(new TestPayload("equip"), Object.class));
    }

    @Test void skillControlsNeverSendUnwrappedNativePayloadsThroughNeoForge() throws Exception {
        List<String> calls = new ArrayList<>();
        try (var stream = getClass().getResourceAsStream("ReincarnatedSkillControls.class")) {
            inspect(new ClassReader(stream), "send", calls);
        }
        assertTrue(calls.stream().anyMatch(call -> call.equals(
                "com/gabriel/tensuranewworld/client/ArchitecturySkillTransport.send")));
        assertFalse(calls.stream().anyMatch(call -> call.startsWith("net/neoforged/neoforge/network/PacketDistributor.")));
    }

    @Test void installedArchitecturyTypedSenderTransformsPayloadBeforeSending() throws Exception {
        List<String> calls = new ArrayList<>();
        try (ZipFile jar = new ZipFile("libs/architectury-13.0.11-neoforge.jar");
                var stream = jar.getInputStream(jar.getEntry("dev/architectury/networking/NetworkManager.class"))) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    if (!name.equals("sendToServer") || !descriptor.equals(
                            "(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V")) return null;
                    return collector(calls);
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        assertTrue(calls.contains("dev/architectury/networking/NetworkManager.collectPackets"));
    }

    private static void inspect(ClassReader reader, String method, List<String> calls) {
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                return name.equals(method) ? collector(calls) : null;
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    private static MethodVisitor collector(List<String> calls) {
        return new MethodVisitor(Opcodes.ASM9) {
            @Override public void visitMethodInsn(int opcode, String owner, String name,
                    String descriptor, boolean isInterface) { calls.add(owner + "." + name); }
        };
    }
}
