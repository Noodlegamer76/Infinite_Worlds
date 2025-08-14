package com.noodlegamer76.infiniteworlds.level.client;

import com.noodlegamer76.infiniteworlds.level.client.renderer.StackedChunkRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientStackedChunk extends LevelChunk {
    SectionPos pos;
    private static final int HEIGHT = 16;

    public ClientStackedChunk(Level level, SectionPos pos) {
        super(level, new ChunkPos(pos.x(), pos.z()));
        this.pos = pos;
    }

    public SectionPos getSectionPos() {
        return pos;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public int getHeight(Heightmap.Types type, int x, int z) {
        return HEIGHT;
    }

    @Override
    public boolean isSectionEmpty(int y) {
        return super.isSectionEmpty(0);
    }

    @Override
    public int getMinBuildHeight() {
        return pos.getY() * 16;
    }

    @Override
    public int getMaxBuildHeight() {
        return pos.getY() * 16 + HEIGHT;
    }

    @Override
    public LevelChunkSection getSection(int index) {
        return sections[0];
    }

    @Override
    public LevelChunkSection[] getSections() {
        return new LevelChunkSection[] { sections[0] };
    }

    @Override
    public int getMaxSection() {
        return getMinSection();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return sections[0].getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        StackedChunkRenderer.markDirty(this.pos);
        int x = pos.getX() & 15;
        int y = pos.getY() & 15;
        int z = pos.getZ() & 15;
        LevelChunkSection section = this.getSection(0);
        return section.setBlockState(x, y, z, state, isMoving);
    }
}
