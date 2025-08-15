package com.noodlegamer76.infiniteworlds.level;

import com.noodlegamer76.infiniteworlds.level.loading.LayerTicketManager;
import com.noodlegamer76.infiniteworlds.level.storage.LayerIndexManagerSavedData;
import com.noodlegamer76.infiniteworlds.level.storage.LayerIndexSavedData;
import com.noodlegamer76.infiniteworlds.level.util.LayerUtils;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final ThreadLocal<Map<SectionPos, LevelChunk>> layerChunkFromBase = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<ChunkPos, LevelChunk>> layerChunkFromLayer = ThreadLocal.withInitial(HashMap::new);
    public final Level baseLevel;
    private final Level layerLevel;
    public final LayerIndexSavedData layerIndexSavedData;
    public final LayerIndexManagerSavedData layerIndexManagerSavedData;
    public final LayerTicketManager ticketManager;

    public ChunkManager(Level baseLevel) {
        this.baseLevel = baseLevel;
        if (baseLevel instanceof ServerLevel serverLevel) {
            ResourceKey<Level> layerLevelKey = LayerUtils.getLevelKey(serverLevel, 1);
            this.layerLevel = baseLevel.getServer().getLevel(layerLevelKey);
            layerIndexManagerSavedData = LayerIndexManagerSavedData.get(serverLevel);
            layerIndexSavedData = LayerIndexSavedData.get(serverLevel);
            this.ticketManager = new LayerTicketManager(serverLevel);
        }
        else {
            this.layerLevel = null;
            layerIndexManagerSavedData = null;
            layerIndexSavedData = null;
            this.ticketManager = null;
        }
    }

    public Map<SectionPos, LevelChunk> getLayerChunkFromBase() {
        return layerChunkFromBase.get();
    }

    public LevelChunk getBaseChunk(SectionPos pos) {
        return layerChunkFromBase.get().get(pos);
    }

    public LevelChunk getLayerChunk(ChunkPos pos) {
        return layerChunkFromLayer.get().get(pos);
    }

    public LevelChunk addBaseChunk(SectionPos pos, LevelChunk chunk) {
        return layerChunkFromBase.get().putIfAbsent(pos, chunk);
    }

    public LevelChunk removeBaseChunk(SectionPos pos) {
        return layerChunkFromBase.get().remove(pos);
    }

    public LevelChunk addLayerChunk(ChunkPos pos, LevelChunk chunk) {
        return layerChunkFromLayer.get().putIfAbsent(pos, chunk);
    }

    public LevelChunk removeLayerChunk(ChunkPos pos) {
        return layerChunkFromLayer.get().remove(pos);
    }

    public LevelChunk addChunk(SectionPos pos, LevelChunk chunk) {
        layerChunkFromLayer.get().putIfAbsent(chunk.getPos(), chunk);
        return layerChunkFromBase.get().putIfAbsent(pos, chunk);
    }

    public LevelChunk removeChunk(SectionPos pos) {
        LevelChunk chunk = layerChunkFromBase.get().remove(pos);
        if (chunk == null) return null;
        return layerChunkFromLayer.get().remove(chunk.getPos());
    }

    public void clear() {
        layerChunkFromBase.get().clear();
    }

    public ServerLevel getLayerLevel() {
        if (layerLevel == null && baseLevel instanceof ServerLevel serverLevel) {
            ResourceKey<Level> layerKey = LayerUtils.getLevelKey(serverLevel, 1);
            return serverLevel.getServer().getLevel(layerKey);
        }
        return (ServerLevel) layerLevel;
    }
}
