package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.mixin.accessor.ChunkAccessAccessor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.DebugLevelSource;
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
        if (level == null) return;

        SectionPos reqSec = SectionPos.of(pos);

        ChunkManager manager = ChunkManagerStorage.getManager(level);
        if (manager == null) return;

        final int sectionsPerLevel = Math.max(1, level.getSectionsCount());
        final int minSection = level.getMinSection();

        final int sectionY = reqSec.getY();
        final int relativeSectionY  = sectionY - minSection;
        final int baseLayerSectionY = minSection + Math.floorDiv(relativeSectionY, sectionsPerLevel) * sectionsPerLevel;
        final int localIndex  = Math.floorMod(relativeSectionY, sectionsPerLevel);

        SectionPos baseLayerSecPos = SectionPos.of(reqSec.getX(), baseLayerSectionY, reqSec.getZ());
        LevelChunk layerChunk = manager.getChunk(baseLayerSecPos);
        if (layerChunk == null) return;

        LevelChunkSection section = layerChunk.getSection(localIndex);

        int lx = pos.getX() & 15;
        int ly = pos.getY() & 15;
        int lz = pos.getZ() & 15;

        cir.setReturnValue(section.getBlockState(lx, ly, lz));
    }
}
