package com.noodlegamer76.infiniteworlds.level.loading;

import com.noodlegamer76.infiniteworlds.Config;
import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class LayerTicketManager {
    private final ServerLevel baseLevel;
    private final Set<LayerIndex> activeTickets = new HashSet<>();
    private final Map<LayerIndex, Boolean> keepChunks = new HashMap<>();

    private final Map<LayerIndex, TicketEntry> ticketsToAdd = new HashMap<>();
    private final Map<LayerIndex, TicketEntry> ticketsToRemove = new HashMap<>();

    private final Deque<SectionPos> indicesToCreate = new ArrayDeque<>();
    private final Set<SectionPos> indicesToCreateSet = new HashSet<>();

    private static final int MAX_TICKETS_PER_TICK = Math.max(1, Config.MAX_CHUNKS_PER_TICK.get());
    private static final int CREATE_INDICES_PER_TICK = 100;

    public LayerTicketManager(ServerLevel baseLevel) {
        this.baseLevel = baseLevel;
    }

    public void updatePlayerTickets() {
        for (LayerIndex index : activeTickets) {
            keepChunks.put(index, false);
        }

        for (ServerPlayer player : baseLevel.getServer().getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(baseLevel.dimension())) {
                continue;
            }

            ChunkManager manager = ChunkManagerStorage.getManager(baseLevel);
            AABB playerTicketArea = getPlayerTicketArea(player);

            int worldHeightInSections = Math.max(1, baseLevel.dimensionType().height() / 16);
            List<SectionPos> chunks = getStackedChunksInAABB(playerTicketArea);

            for (SectionPos chunk : chunks) {
                LayerIndex index = manager.layerIndexSavedData.getIndexFromBaseWorld(chunk);
                if (index == null) {
                    if (indicesToCreateSet.add(chunk)) {
                        indicesToCreate.add(chunk);
                    }
                    continue;
                }
                keepChunks.put(index, true);
            }
        }

        for (Map.Entry<LayerIndex, Boolean> entry : keepChunks.entrySet()) {
            LayerIndex index = entry.getKey();
            boolean keep = entry.getValue();
            double distance = getDistanceToClosestPlayer(index);

            if (keep) {
                ticketsToRemove.remove(index);
                if (!activeTickets.contains(index)) {
                    ticketsToAdd.put(index, new TicketEntry(index, distance));
                } else {
                    ticketsToAdd.remove(index);
                }
            } else {
                ticketsToAdd.remove(index);
                if (activeTickets.contains(index)) {
                    ticketsToRemove.put(index, new TicketEntry(index, distance));
                } else {
                    ticketsToRemove.remove(index);
                }
            }
        }

        keepChunks.clear();
    }

    public void processQueuedTickets() {
        int created = 0;
        ChunkManager manager = ChunkManagerStorage.getManager(baseLevel);
        while (created < CREATE_INDICES_PER_TICK && !indicesToCreate.isEmpty()) {
            SectionPos pos = indicesToCreate.poll();
            indicesToCreateSet.remove(pos);
            if (manager.layerIndexSavedData.getIndexFromBaseWorld(pos) == null) {
                manager.layerIndexSavedData.createIndexFromBaseWorld(pos, baseLevel);
            }
            created++;
        }

        List<TicketEntry> addEntries = new ArrayList<>(ticketsToAdd.values());
        addEntries.sort(Comparator.comparingDouble(e -> e.distance));

        int processed = 0;
        for (TicketEntry entry : addEntries) {
            if (processed >= MAX_TICKETS_PER_TICK) break;
            if (!activeTickets.contains(entry.index)) {
                addPlayerRegionTicket(entry.index, baseLevel);
                activeTickets.add(entry.index);
                ticketsToAdd.remove(entry.index);
                processed++;
            } else {
                ticketsToAdd.remove(entry.index);
            }
        }

        List<TicketEntry> removeEntries = new ArrayList<>(ticketsToRemove.values());
        removeEntries.sort(Comparator.comparingDouble(e -> e.distance));

        processed = 0;
        for (TicketEntry entry : removeEntries) {
            if (processed >= MAX_TICKETS_PER_TICK) break;
            if (activeTickets.contains(entry.index)) {
                removePlayerRegionTicket(entry.index, baseLevel);
                activeTickets.remove(entry.index);
                ticketsToRemove.remove(entry.index);
                processed++;
            } else {
                ticketsToRemove.remove(entry.index);
            }
        }
    }

    public Set<LayerIndex> getActiveTickets() {
        return Collections.unmodifiableSet(activeTickets);
    }

    private double getDistanceToClosestPlayer(LayerIndex index) {
        double minDistSq = Double.MAX_VALUE;
        for (ServerPlayer player : baseLevel.getServer().getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(baseLevel.dimension())) continue;
            double dx = player.getX() - index.basePos.getX() * 16;
            double dy = player.getY() - index.basePos.getY() * 16;
            double dz = player.getZ() - index.basePos.getZ() * 16;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < minDistSq) minDistSq = distSq;
        }
        return Math.sqrt(minDistSq);
    }

    public synchronized void addPlayerRegionTicket(LayerIndex index, ServerLevel baseLevel) {
        LoadUtils.addTicketToStackedChunk(TicketType.FORCED, index.basePos, index.layerPos, true, baseLevel);
    }

    public synchronized void removePlayerRegionTicket(LayerIndex index, ServerLevel baseLevel) {
        LoadUtils.removeTicketFromStackedChunk(TicketType.FORCED, index.basePos, index.layerPos, true, baseLevel);
    }

    public List<SectionPos> getStackedChunksInAABB(AABB box) {
        Set<SectionPos> uniqueBaseChunks = new HashSet<>();

        int minSectionX = SectionPos.blockToSectionCoord((int) Math.floor(box.minX));
        int maxSectionX = SectionPos.blockToSectionCoord((int) Math.floor(box.maxX));
        int minSectionY = SectionPos.blockToSectionCoord((int) Math.floor(box.minY));
        int maxSectionY = SectionPos.blockToSectionCoord((int) Math.floor(box.maxY));
        int minSectionZ = SectionPos.blockToSectionCoord((int) Math.floor(box.minZ));
        int maxSectionZ = SectionPos.blockToSectionCoord((int) Math.floor(box.maxZ));

        int worldHeightInSections = Math.max(1, baseLevel.dimensionType().height() / 16);
        int vanillaMinSection = baseLevel.getMinSection();
        int vanillaMaxSection = baseLevel.getMaxSection();

        for (int x = minSectionX; x <= maxSectionX; x++) {
            for (int y = minSectionY; y <= maxSectionY; y++) {
                for (int z = minSectionZ; z <= maxSectionZ; z++) {
                    int relativeY = y - vanillaMinSection;
                    int baseY = vanillaMinSection + Math.floorDiv(relativeY, worldHeightInSections) * worldHeightInSections;

                    if (!isVanillaLayer(baseY, vanillaMinSection, vanillaMaxSection)) {
                        uniqueBaseChunks.add(SectionPos.of(x, baseY, z));
                    }
                }
            }
        }

        return new ArrayList<>(uniqueBaseChunks);
    }

    /**
     * Checks if a given base layer's starting Y position falls within the vanilla world's vertical bounds.
     *
     * @param baseYInSections The starting Y position of the base layer.
     * @param vanillaMinSection The minimum section Y of the vanilla world.
     * @param vanillaMaxSection The maximum section Y of the vanilla world.
     * @return True if the base layer is the vanilla layer, false otherwise.
     */
    private boolean isVanillaLayer(int baseYInSections, int vanillaMinSection, int vanillaMaxSection) {
        return baseYInSections >= vanillaMinSection && baseYInSections < vanillaMaxSection;
    }

    public static AABB getPlayerTicketArea(ServerPlayer player) {
        int verticalRenderDistance = Config.VERTICAL_SIMULATION_DISTANCE.get() * 16;
        int horizontalRenderDistance = player.server.getPlayerList().getViewDistance() * 16;
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();
        return new AABB(
                px - horizontalRenderDistance,
                py - verticalRenderDistance,
                pz - horizontalRenderDistance,
                px + horizontalRenderDistance,
                py + verticalRenderDistance,
                pz + horizontalRenderDistance
        );
    }

    private record TicketEntry(LayerIndex index, double distance) {}
}
