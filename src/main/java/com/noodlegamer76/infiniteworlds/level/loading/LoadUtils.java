package com.noodlegamer76.infiniteworlds.level.loading;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;

public class LoadUtils {

    public static <T> void addTicketToStackedChunk(
            TicketType<T> type,
            SectionPos basePos,
            T value,
            boolean forceTicks,
            ServerLevel baseLevel) {

        ChunkManager manager = ((LevelWithManager) baseLevel).infiniteWorlds$getChunkManager();
        ServerLevel layerLevel = (ServerLevel) manager.getLayerLevel();
        ServerChunkCache cache = layerLevel.getChunkSource();

        LayerIndex layerIndex = manager.layerIndexSavedData.getIndexFromBaseWorld(basePos);

        if (layerIndex == null) {
            layerIndex = manager.layerIndexSavedData.createIndexFromBaseWorld(basePos, baseLevel);
            if (layerIndex == null) {
                return;
            }
        }

        cache.addRegionTicket(type, layerIndex.layerPos, 0, value, forceTicks);
    }

    public static <T> void removeTicketFromStackedChunk(
            TicketType<T> type,
            SectionPos basePos,
            T value,
            boolean forceTicks,
            ServerLevel baseLevel) {

        ChunkManager manager = ((LevelWithManager) baseLevel).infiniteWorlds$getChunkManager();
        ServerLevel layerLevel = (ServerLevel) manager.getLayerLevel();
        ServerChunkCache cache = layerLevel.getChunkSource();

        LayerIndex layerIndex = manager.layerIndexSavedData.getIndexFromBaseWorld(basePos);

        if (layerIndex == null) {
            return;
        }

        cache.removeRegionTicket(type, layerIndex.layerPos, 0, value, forceTicks);
    }

}
