package com.noodlegamer76.infiniteworlds.event;

import com.noodlegamer76.infiniteworlds.InfiniteWorlds;
import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = InfiniteWorlds.MODID)
public class ChunkLoadingEvent {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension().location().toString().endsWith("_layer_1")) {
            ChunkManager manager = ChunkManagerStorage.getManagerForLayer(serverLevel);
            LayerIndex index = manager.layerIndexSavedData.getIndexFromLayerWorld(event.getChunk().getPos());
            if (index == null) {
                return;
            }
            manager.addChunk(index.basePos, (LevelChunk) event.getChunk());
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension().location().toString().endsWith("_layer_1")) {
            ChunkManager manager = ((LevelWithManager) serverLevel).infiniteWorlds$getChunkManager();
            LayerIndex index = manager.layerIndexSavedData.getIndexFromLayerWorld(event.getChunk().getPos());
            if (index == null) {
                return;
            }
            manager.removeChunk(index.basePos);
        }
    }
}
