package net.mine_diver.amphetamine.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AmphetamineMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN = "net.mine_diver.amphetamine.mixin.MixinServerChunkCache";
    private static final String INTEGER = "java/lang/Integer";
    private static final String MAP = "java/util/Map";
    private static final String HELPERS = "net/mine_diver/amphetamine/mixin/AmphetamineMixinPlugin";

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (MIXIN.equals(mixinClassName))
            rewriteBoxedLookups(targetClass);
    }

    static int rewriteBoxedLookups(ClassNode targetClass) {
        int rewrites = 0;
        for (MethodNode method : targetClass.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode boxing)
                        || !INTEGER.equals(boxing.owner)
                        || !"valueOf".equals(boxing.name)
                        || !"(I)Ljava/lang/Integer;".equals(boxing.desc))
                    continue;

                AbstractInsnNode next = boxing.getNext();
                while (next != null && next.getOpcode() == -1)
                    next = next.getNext();
                if (!(next instanceof MethodInsnNode mapCall) || !MAP.equals(mapCall.owner))
                    continue;

                String descriptor = switch (mapCall.name) {
                    case "containsKey" -> "(Ljava/util/Map;I)Z";
                    case "get" -> "(Ljava/util/Map;I)Ljava/lang/Object;";
                    default -> null;
                };
                if (descriptor == null)
                    continue;

                boxing.owner = HELPERS;
                boxing.name = mapCall.name;
                boxing.desc = descriptor;
                boxing.itf = false;
                method.instructions.remove(mapCall);
                ++rewrites;
            }
        }
        return rewrites;
    }

    public static boolean containsKey(Map<Integer, ?> map, int key) {
        return ((Int2ObjectMap<?>) map).containsKey(key);
    }

    public static Object get(Map<Integer, ?> map, int key) {
        return ((Int2ObjectMap<?>) map).get(key);
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
