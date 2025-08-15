package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Shadow
    public abstract Level getLevel();

    @Inject(
            method = "getBlockState",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getBlockStateStacked(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        ChunkManager manager = ChunkManagerStorage.getManager(level);
        if (manager == null) return;

        final int sectionsPerLevel = Math.max(1, level.getSectionsCount());
        final int minSection = level.getMinSection();
        final int sectionY = SectionPos.blockToSectionCoord(pos.getY());

        final int baseLayerSectionY = minSection + Math.floorDiv(sectionY - minSection, sectionsPerLevel) * sectionsPerLevel;
        SectionPos baseLayerSectionPos = SectionPos.of(
                SectionPos.blockToSectionCoord(pos.getX()),
                baseLayerSectionY,
                SectionPos.blockToSectionCoord(pos.getZ())
        );

        LevelChunk layerChunk = manager.getBaseChunk(baseLayerSectionPos);
        if (layerChunk == null) return;

        int localSectionCount = layerChunk.getSectionsCount();
        int sectionIndexInChunk = Math.floorMod(sectionY - layerChunk.getMinSection(), localSectionCount);

        LevelChunkSection section = layerChunk.getSection(sectionIndexInChunk);
        if (section == null) return;

        int lx = pos.getX() & 15;
        int ly = pos.getY() & 15;
        int lz = pos.getZ() & 15;

        cir.setReturnValue(section.getBlockState(lx, ly, lz));
    }



}
