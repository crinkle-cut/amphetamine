package net.mine_diver.amphetamine.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkCache;
import net.minecraft.world.chunk.ChunkSource;
import net.minecraft.world.chunk.storage.ChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ChunkCache.class)
abstract class MixinServerChunkCache {

    @Shadow private Map<Integer, Chunk> chunkByPos;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void getMap(World level, ChunkStorage arg1, ChunkSource arg2, CallbackInfo ci) {
        chunkByPos = new Int2ObjectOpenHashMap<>();
    }
}
