package com.gabriel.tensuranewworld.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class RaphaelRenderingTest {
    @Test
    void customPanelOverridesVanillaBackgroundWithoutApplyingBlur() throws Exception {
        AtomicBoolean overrideFound = new AtomicBoolean();
        AtomicBoolean delegatesBackground = new AtomicBoolean();
        // Inspect compiled code without booting Minecraft or initializing its registries.
        try (InputStream code = getClass().getResourceAsStream("RaphaelScreen.class")) {
            new ClassReader(code).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    if (!name.equals("renderBackground") || !descriptor.equals(
                            "(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")) return null;
                    overrideFound.set((access & Opcodes.ACC_PUBLIC) != 0);
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String method,
                                String desc, boolean isInterface) {
                            delegatesBackground.set(true);
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        assertTrue(overrideFound.get(), "Screen.render must dispatch to the custom background override");
        assertFalse(delegatesBackground.get(), "Background must not blur or cover already rendered text");
    }
}
