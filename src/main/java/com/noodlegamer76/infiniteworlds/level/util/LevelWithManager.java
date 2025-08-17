package com.noodlegamer76.infiniteworlds.level.util;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;

public interface LevelWithManager {
    ChunkManager infiniteWorlds$getChunkManager();

    ChunkManager infiniteWorlds$setChunkManager(ChunkManager chunkManager);

    LayerUtils infiniteWorlds$getLayerUtils();
}