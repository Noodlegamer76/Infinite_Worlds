package com.noodlegamer76.infiniteworlds.level.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class ChunkRenderSection {
    private final SectionPos pos;
    private final SectionRenderDispatcher.RenderSection renderSection;
    private boolean removed = false;
    private double cachedDistance;
    public static RenderRegionCache cache = new RenderRegionCache();
    boolean shouldUpdateNeighborsNext = true;

    public ChunkRenderSection(SectionPos pos, SectionRenderDispatcher.RenderSection renderSection) {
        this.pos = pos;
        this.renderSection = renderSection;
    }

    public SectionPos getPos() {
        return pos;
    }

    public SectionRenderDispatcher.RenderSection getRenderSection() {
        return renderSection;
    }

    public void rebuild() {
        RenderChunkContext.set(pos.getY());
        try {
            cache = new RenderRegionCache();
            renderSection.rebuildSectionAsync(StackedChunkRenderer.getDispatcher(), cache);
        } finally {
            RenderChunkContext.clear();
        }
    }

    public void close() {
        RenderSystem.recordRenderCall(renderSection::releaseBuffers);
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public void updateDistance(Vec3 camPos) {
        cachedDistance = new BlockPos(pos.x() * 16, pos.y() * 16, pos.z() * 16)
                .getBottomCenter()
                .distanceToSqr(camPos);
    }

    public double getCachedDistance() {
        return cachedDistance;
    }

}
