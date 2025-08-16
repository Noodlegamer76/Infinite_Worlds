package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.client.renderer.RenderChunkContext;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderRegionCache.class)
public class RenderRegionCacheMixin {

    @Redirect(
            method = "lambda$getChunkInfo$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getChunk(II)Lnet/minecraft/world/level/chunk/LevelChunk;")
    )
    private static LevelChunk fixGetLevelChunk(Level level, int chunkX, int chunkZ) {
        Integer y = RenderChunkContext.get();
        if (y != null) {
            SectionPos pos = SectionPos.of(chunkX, y, chunkZ);
            LevelChunk chunk = ((LevelWithManager) level).infiniteWorlds$getChunkManager().getBaseChunk(pos);
            if (chunk != null) {
                return chunk;
            }
        }

        return level.getChunk(chunkX, chunkZ);
    }
}
