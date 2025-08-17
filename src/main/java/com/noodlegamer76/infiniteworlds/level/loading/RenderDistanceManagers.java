package com.noodlegamer76.infiniteworlds.level.loading;

import com.noodlegamer76.infiniteworlds.Config;
import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import com.noodlegamer76.infiniteworlds.network.stackedchunk.StackedChunkInfo;
import com.noodlegamer76.infiniteworlds.network.stackedchunk.StackedChunkPayload;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class RenderDistanceManagers {
    private static int tick = 0;
    private static final Map<UUID, LayerRenderDistanceManager> playerRenderManagers = new HashMap<>();

    public static void onPlayerJoin(ServerPlayer player) {
        playerRenderManagers.put(player.getUUID(), new LayerRenderDistanceManager((ServerLevel) player.level()));
    }

    public static void onPlayerLeave(ServerPlayer player) {
        playerRenderManagers.remove(player.getUUID());
    }

    public static void tick(MinecraftServer server) {
        //tick++;
        //final int sendInterval = 2;
        final int maxPerSend = Math.max(1, Config.MAX_CHUNKS_RENDER_PER_TICK.get());

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            LayerRenderDistanceManager manager = playerRenderManagers.get(player.getUUID());
            if (manager == null) continue;

            ServerLevel level = (ServerLevel) player.level();

            int horizontalRenderDistance = server.getPlayerList().getViewDistance();
            int verticalRenderDistance = Config.VERTICAL_RENDER_DISTANCE.get();

            manager.updateQueuedChunksForPlayer(player, horizontalRenderDistance, verticalRenderDistance);
            manager.clearCacheForChunksOutsideRange(player, horizontalRenderDistance, verticalRenderDistance);

            //if (tick % sendInterval != 0) continue;

            List<SectionPos> chunksToSend = manager.pollChunksToSend();
            if (chunksToSend == null || chunksToSend.isEmpty()) continue;

            if (chunksToSend.size() > maxPerSend) {
                chunksToSend = chunksToSend.subList(0, maxPerSend);
            }

            ChunkManager idxManager = ((LevelWithManager) level).infiniteWorlds$getChunkManager();
            List<StackedChunkInfo> chunks = new ArrayList<>(chunksToSend.size());

            int sectionsPerLevel = level.getSectionsCount();
            int minSectionY = level.getMinSection();

            for (SectionPos pos : chunksToSend) {
                int sectionY = pos.getY();

                int relativeY = sectionY - minSectionY;
                int baseLayerSectionY = minSectionY + (Math.floorDiv(relativeY, sectionsPerLevel)) * sectionsPerLevel;

                int offset = Math.floorMod(relativeY, sectionsPerLevel);

                LevelChunk layerChunk = idxManager.getLayerChunk(SectionPos.of(pos.getX(), baseLayerSectionY, pos.getZ()));

                if (layerChunk != null) {
                    chunks.add(new StackedChunkInfo(layerChunk, pos, offset));
                    manager.markSent(pos);
                } else {
                    manager.requeueIfNeeded(pos);
                }
            }

            if (chunks.isEmpty()) continue;

            StackedChunkPayload payload = new StackedChunkPayload(chunks);
            for (StackedChunkInfo chunk: chunks) {
                ((LevelWithManager) level).infiniteWorlds$getChunkManager().addChunk(chunk.pos(), chunk.chunk());
            }
            player.connection.send(payload);
        }
    }
}
