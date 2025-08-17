package com.noodlegamer76.infiniteworlds.level.util;

import com.noodlegamer76.infiniteworlds.InfiniteWorlds;
import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LayerUtils {
    public static final Map<Level, LayerUtils> LAYER_UTILS_MAP = new HashMap<>();
    private final Long2ObjectOpenHashMap<LevelChunk> chunkCache = new Long2ObjectOpenHashMap<>();
    private long lastTick = 0;
    private final Level level;

    public LayerUtils(Level level) {
        this.level = level;
    }

    @Nullable
    public LevelChunk getChunk(BlockPos pos, Level level) {
        return getChunk(SectionPos.of(pos), level);
    }

    @Nullable
    public LevelChunk getChunk(SectionPos pos, Level level) {
        if (level == null) return null;

        final int sectionsPerLevel = Math.max(1, level.getSectionsCount());
        final int minSection = level.getMinSection();
        final int baseLayerY = minSection + Math.floorDiv(pos.getY() - minSection, sectionsPerLevel) * sectionsPerLevel;

        long chunkKey = SectionPos.asLong(pos.getX(), baseLayerY, pos.getZ());

        LevelChunk chunk = chunkCache.get(chunkKey);
        if (chunk == null) {
            ChunkManager manager = ((LevelWithManager) level).infiniteWorlds$getChunkManager();
            if (manager == null) return null;

            chunk = manager.getLayerChunk(SectionPos.of(pos.getX(), baseLayerY, pos.getZ()));
            if (chunk != null) {
                chunkCache.put(chunkKey, chunk);
            }
        }

        return chunk;
    }

    private static long getTick(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().getTickCount();
        }
        return System.currentTimeMillis() / 50;
    }

    public void tickCache() {
        long tick = getTick(level);

        if (tick != lastTick) {
            chunkCache.clear();
            lastTick = tick;
        }
    }

    public static LevelChunkSection getWrappedSection(LevelChunk levelChunk, int sectionY) {
        int localSectionCount = levelChunk.getSectionsCount();
        int sectionIndex = Math.floorMod(sectionY - levelChunk.getMinSection(), localSectionCount);
        return levelChunk.getSection(sectionIndex);
    }

    public static boolean isLevelStorageLevel(ServerLevel level) {
        return level.dimension().location().toString().startsWith(InfiniteWorlds.MODID + ":") && level.dimension().location().toString().endsWith("_layer_1");
    }

    public static ResourceKey<Level> getLevelKey(ServerLevel baseLevel, int layer) {
        return ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(InfiniteWorlds.MODID,
                        baseLevel.dimension().location().toString().replaceFirst(":", "_") + "_layer_" + layer));
    }

    public static ResourceKey<DimensionType> getTypeKey(ServerLevel baseLevel, int layer) {
        return ResourceKey.create(Registries.DIMENSION_TYPE,
                ResourceLocation.fromNamespaceAndPath(InfiniteWorlds.MODID,
                        baseLevel.dimension().location().toString().replaceFirst(":", "_") + "_layer_" + layer));
    }

    public static ResourceLocation getDimLocation(ResourceLocation parent, int layer) {
        return ResourceLocation.fromNamespaceAndPath(InfiniteWorlds.MODID,
                parent.toString().replaceFirst(":", "_") + "_layer_" + layer);
    }
}
