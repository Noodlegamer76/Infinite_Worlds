package com.noodlegamer76.infiniteworlds.level.loading;

import com.noodlegamer76.infiniteworlds.Config;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class LayerRenderDistanceManager {
    private final ServerLevel baseLevel;

    private final Deque<SectionPos> chunkSendQueue = new ArrayDeque<>();
    private final Set<SectionPos> queuedSet = new HashSet<>();

    private final Set<SectionPos> sentChunksCache = new HashSet<>();

    private static final int SECTIONS_PER_TOLL = Math.max(1, Config.MAX_CHUNKS_RENDER_PER_TICK.get());

    public LayerRenderDistanceManager(ServerLevel baseLevel) {
        this.baseLevel = baseLevel;
    }

    public void updateQueuedChunksForPlayer(ServerPlayer player, int horizontalRenderDistanceChunks, int verticalRenderDistanceChunks) {
        LayerTicketManager ticketManager = ChunkManagerStorage.getManager(baseLevel).ticketManager;
        Set<LayerIndex> loadedLayers = ticketManager.getActiveTickets();

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        double horizontalDistanceBlocks = horizontalRenderDistanceChunks * 16.0;
        double verticalDistanceBlocks = verticalRenderDistanceChunks * 16.0;
        double horizontalDistanceSq = horizontalDistanceBlocks * horizontalDistanceBlocks;

        List<SectionPos> filteredChunks = new ArrayList<>();
        for (LayerIndex layerIndex : loadedLayers) {
            SectionPos basePos = layerIndex.basePos;
            int sectionsCount = Math.max(1, baseLevel.getSectionsCount());

            for (int i = 0; i < sectionsCount; i++) {
                SectionPos pos = SectionPos.of(basePos.getX(), basePos.getY() + i, basePos.getZ());

                if (sentChunksCache.contains(pos)) continue;

                double dx = px - pos.getX() * 16.0;
                double dy = py - pos.getY() * 16.0;
                double dz = pz - pos.getZ() * 16.0;

                if (Math.abs(dy) > verticalDistanceBlocks) continue;
                double hSq = dx * dx + dz * dz;
                if (hSq > horizontalDistanceSq) continue;

                if (queuedSet.add(pos)) {
                    filteredChunks.add(pos);
                }
            }
        }

        chunkSendQueue.addAll(filteredChunks);

        List<SectionPos> sortedQueue = new ArrayList<>(chunkSendQueue);
        sortedQueue.sort(Comparator.comparingDouble(pos -> {
            double dx = px - pos.getX() * 16.0;
            double dy = py - pos.getY() * 16.0;
            double dz = pz - pos.getZ() * 16.0;
            return dx * dx + dy * dy + dz * dz;
        }));

        chunkSendQueue.clear();
        chunkSendQueue.addAll(sortedQueue);
    }


    public List<SectionPos> pollChunksToSend() {
        List<SectionPos> toSend = new ArrayList<>(SECTIONS_PER_TOLL);
        int taken = 0;
        while (taken < SECTIONS_PER_TOLL && !chunkSendQueue.isEmpty()) {
            SectionPos pos = chunkSendQueue.pollFirst();
            queuedSet.remove(pos);
            toSend.add(pos);
            taken++;
        }
        return toSend;
    }

    public void markSent(SectionPos pos) {
        sentChunksCache.add(pos);
    }

    public void requeueIfNeeded(SectionPos pos) {
        if (!sentChunksCache.contains(pos) && queuedSet.add(pos)) {
            chunkSendQueue.addLast(pos);
        }
    }

    public void clearCacheForChunksOutsideRange(ServerPlayer player, int horizontalRenderDistanceChunks, int verticalRenderDistanceChunks) {
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        double horizontalDistanceBlocks = horizontalRenderDistanceChunks * 16.0;
        double verticalDistanceBlocks = verticalRenderDistanceChunks * 16.0;
        double horizontalDistanceSq = horizontalDistanceBlocks * horizontalDistanceBlocks;

        sentChunksCache.removeIf(pos -> {
            double dx = px - pos.getX() * 16.0;
            double dy = py - pos.getY() * 16.0;
            double dz = pz - pos.getZ() * 16.0;
            if (Math.abs(dy) > verticalDistanceBlocks) return true;
            double hSq = dx * dx + dz * dz;
            return hSq > horizontalDistanceSq;
        });

        Iterator<SectionPos> it = chunkSendQueue.iterator();
        while (it.hasNext()) {
            SectionPos pos = it.next();
            double dx = px - pos.getX() * 16.0;
            double dy = py - pos.getY() * 16.0;
            double dz = pz - pos.getZ() * 16.0;
            if (Math.abs(dy) > verticalDistanceBlocks || (dx * dx + dz * dz) > horizontalDistanceSq) {
                it.remove();
                queuedSet.remove(pos);
            }
        }
    }
}
