package com.noodlegamer76.infiniteworlds.network.stackedchunk;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LevelChunk;

public record StackedChunkInfo(LevelChunk chunk, SectionPos pos, int offset) {
}
