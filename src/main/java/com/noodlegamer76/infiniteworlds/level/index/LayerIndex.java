package com.noodlegamer76.infiniteworlds.level.index;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public class LayerIndex {
    public final ChunkPos layerPos;
    public final int layerPosSection;
    public final SectionPos basePos;

    public LayerIndex(ChunkPos layerPos, int layerPosSection, SectionPos baseLevelPos) {
        this.layerPos = layerPos;
        this.layerPosSection = layerPosSection;
        this.basePos = baseLevelPos;
    }
}
