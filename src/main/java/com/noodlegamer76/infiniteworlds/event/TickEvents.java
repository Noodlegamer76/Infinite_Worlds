package com.noodlegamer76.infiniteworlds.event;

import com.noodlegamer76.infiniteworlds.InfiniteWorlds;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.loading.LayerTicketManager;
import com.noodlegamer76.infiniteworlds.level.loading.RenderDistanceManagers;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = InfiniteWorlds.MODID)
public class TickEvents {
    private static int tick = 0;

    @SubscribeEvent
    public static void preTick(ServerTickEvent.Pre event) {
        if (++tick > Integer.MAX_VALUE - 1000) tick = 0;

        event.getServer().getAllLevels().forEach(level -> {
            if (!ChunkManagerStorage.containsManager(level)) {
                return;
            }

            if (!level.dimension().location().toString().endsWith("_layer_1")) {
                LayerTicketManager manager = ChunkManagerStorage.getManager(level).ticketManager;

                if (manager == null) {
                    return;
                }

                if (tick % 20 == 0) {
                    try {
                        manager.updatePlayerTickets();
                    } catch (Exception e) {
                        InfiniteWorlds.LOGGER.error("Error updating player tickets for level " + level.dimension().location(), e);
                    }
                }

                manager.processQueuedTickets();
            }
        });

        RenderDistanceManagers.tick(event.getServer());
    }


}