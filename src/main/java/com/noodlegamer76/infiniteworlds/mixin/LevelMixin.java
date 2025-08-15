package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Shadow
    public abstract void markAndNotifyBlock(BlockPos p_46605_, @Nullable LevelChunk levelchunk, BlockState blockstate, BlockState p_46606_, int p_46607_, int p_46608_);

    @Shadow
    public abstract void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState1, int i);

    @Shadow
    public abstract void onBlockStateChange(BlockPos pos, BlockState blockState, BlockState newState);

    @Shadow
    @Final
    public boolean isClientSide;

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    @Shadow
    public abstract boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft);

    @Shadow
    public boolean captureBlockSnapshots;

    @Shadow
    public ArrayList<BlockSnapshot> capturedBlockSnapshots;

    @Shadow
    @Final
    private ResourceKey<Level> dimension;

    @Shadow
    public abstract LevelChunk getChunk(int chunkX, int chunkZ);

    @Shadow public abstract boolean isClientSide();

    @Inject(method = "getChunkAt", at = @At("TAIL"), cancellable = true)
    public void getChunkAtFix(BlockPos pos, CallbackInfoReturnable<LevelChunk> cir) {
        SectionPos chunkPos = SectionPos.of(pos);
        LevelChunk chunk = ChunkManagerStorage.getManager((Level) (Object) this).getBaseChunk(chunkPos);

        if (chunk != null) {
            cir.setReturnValue(chunk);
        }
    }

    @Inject(
            method = "getBlockState",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getBlockStateFix(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        Level level = (Level) (Object) this;
        if (level == null) return;

        ChunkManager manager = ChunkManagerStorage.getManager(level);
        if (manager == null) return;

        SectionPos baseLayerSectionPos;
        if (!isClientSide()) {
            final int sectionY = SectionPos.blockToSectionCoord(pos.getY());

            final int sectionsPerLevel = Math.max(1, level.getSectionsCount());
            final int minSection = level.getMinSection();
            final int baseLayerSectionY = minSection + Math.floorDiv(sectionY - minSection, sectionsPerLevel) * sectionsPerLevel;

            baseLayerSectionPos = SectionPos.of(
                    SectionPos.blockToSectionCoord(pos.getX()),
                    baseLayerSectionY,
                    SectionPos.blockToSectionCoord(pos.getZ())
            );
        }
        else {
            baseLayerSectionPos = SectionPos.of(pos);
        }

        LevelChunk layerChunk = manager.getBaseChunk(baseLayerSectionPos);
        if (layerChunk == null) return;

        cir.setReturnValue(layerChunk.getBlockState(pos));
    }


    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void setBlockStateFix(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        SectionPos chunkPos = SectionPos.of(pos);
        LevelChunk chunk = ChunkManagerStorage.getManager((Level) (Object) this).getBaseChunk(chunkPos);

        if (chunk != null) {
            Level level = (Level) (Object) this;
            BlockSnapshot blockSnapshot = null;
            if (this.captureBlockSnapshots && !this.isClientSide) {
                blockSnapshot = BlockSnapshot.create(dimension, level, pos, flags);
                this.capturedBlockSnapshots.add(blockSnapshot);
            }

            BlockState old = getBlockState(pos);
            old.getLightEmission(level, pos);
            old.getLightBlock(level, pos);
            chunk.setBlockState(pos, state, (flags & 64) != 0);
            if (state == null) {
                if (blockSnapshot != null) {
                    this.capturedBlockSnapshots.remove(blockSnapshot);
                }

                cir.setReturnValue(false);
            } else {
                this.getBlockState(pos);
                if (blockSnapshot == null) {
                    this.markAndNotifyBlock(pos, chunk, state, state, flags, recursionLeft);
                }

                cir.setReturnValue(true);
            }
        }

    }
}
