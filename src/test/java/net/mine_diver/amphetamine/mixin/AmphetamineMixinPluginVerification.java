package net.mine_diver.amphetamine.mixin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;

public final class AmphetamineMixinPluginVerification {
    public static void main(String[] args) throws IOException {
        ClassNode target = new ClassNode();
        try (InputStream input = ClassLoader.getSystemResourceAsStream("net/minecraft/world/chunk/ChunkCache.class")) {
            if (input == null)
                throw new AssertionError("ChunkCache.class is missing from the test classpath");
            new ClassReader(input).accept(target, 0);
        }

        int rewrites = AmphetamineMixinPlugin.rewriteBoxedLookups(target);
        int helperCalls = 0;
        for (MethodNode method : target.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call
                        && "net/mine_diver/amphetamine/mixin/AmphetamineMixinPlugin".equals(call.owner))
                    ++helperCalls;
            }
        }
        if (rewrites != 3 || helperCalls != 3)
            throw new AssertionError("Expected three primitive lookup rewrites, got " + rewrites);
    }
}
