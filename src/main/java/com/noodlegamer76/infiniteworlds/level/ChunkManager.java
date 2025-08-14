package com.noodlegamer76.infiniteworlds.level;

import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
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
    private final Map<SectionPos, LevelChunk> chunks = new HashMap<>();
    public final Level baseLevel;
    public final Level layerLevel;
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

    public Map<SectionPos, LevelChunk> getChunks() {
        return chunks;
    }

    public LevelChunk getChunk(SectionPos pos) {
        return chunks.get(pos);
    }

    public LevelChunk addChunk(SectionPos pos, LevelChunk chunk) {
        return chunks.put(pos, chunk);
    }

    public LevelChunk removeChunk(SectionPos pos) {
        return chunks.remove(pos);
    }

    public void clear() {
        chunks.clear();
    }
}
