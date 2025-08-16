package com.noodlegamer76.infiniteworlds.event;

import com.noodlegamer76.infiniteworlds.InfiniteWorlds;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.client.renderer.StackedChunkRenderer;
import com.noodlegamer76.infiniteworlds.level.util.LayerUtils;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = InfiniteWorlds.MODID)
public class ChangeClientLevelEvent {

    @SubscribeEvent
    public static void unloadClientLevelEvent(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            StackedChunkRenderer.clear();
            ChunkManagerStorage.clear();
            LayerUtils.LAYER_UTILS_MAP.clear();
        }
    }

    @SubscribeEvent
    public static void loadClientLevelEvent(LevelEvent.Load event) {
    }
}
