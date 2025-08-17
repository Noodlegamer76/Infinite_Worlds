package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import com.noodlegamer76.infiniteworlds.level.util.LayerUtils;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelWithManager {

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

    @Unique
    private ChunkManager infiniteWorlds$cachedManager;

    @Unique
    private LayerUtils infiniteWorlds$cachedUtils;

    @Unique
    public ChunkManager infiniteWorlds$getChunkManager() {
        return infiniteWorlds$cachedManager == null ? infiniteWorlds$cachedManager = ChunkManagerStorage.getManager((Level) (Object) this) : infiniteWorlds$cachedManager;
    }

    @Unique
    public ChunkManager infiniteWorlds$setChunkManager(ChunkManager manager) {
        return infiniteWorlds$cachedManager = manager;
    }

    @Unique
    public LayerUtils infiniteWorlds$getLayerUtils() {
        return infiniteWorlds$cachedUtils == null ? infiniteWorlds$cachedUtils = LayerUtils.LAYER_UTILS_MAP.computeIfAbsent((Level) (Object) this, LayerUtils::new) : infiniteWorlds$cachedUtils;
    }

    @Inject(method = "getChunkAt", at = @At("TAIL"), cancellable = true)
    public void getChunkAtFix(BlockPos pos, CallbackInfoReturnable<LevelChunk> cir) {
        if (infiniteWorlds$cachedManager == null) return;
        SectionPos chunkPos = SectionPos.of(pos);

        LevelChunk chunk = infiniteWorlds$cachedManager.getLayerChunk(chunkPos);

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
        if (infiniteWorlds$cachedManager == null) return;
        Level level = (Level) (Object) this;
        if (level == null) return;

        SectionPos baseLayerSectionPos;
        if (!isClientSide()) {
            LevelChunk layerChunk = ((LevelWithManager) level).infiniteWorlds$getLayerUtils().getChunk(pos, level);

            if (layerChunk == null) return;

            cir.setReturnValue(layerChunk.getBlockState(pos));
        }
        else {
            baseLayerSectionPos = SectionPos.of(pos);

            LevelChunk layerChunk = infiniteWorlds$cachedManager.getLayerChunk(baseLayerSectionPos);
            if (layerChunk == null) return;

            cir.setReturnValue(layerChunk.getBlockState(pos));
        }
    }

    @Inject(
            method = "getFluidState",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getFluidStateFix(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        if (infiniteWorlds$cachedManager == null) return;
        Level level = (Level) (Object) this;
        if (level == null) return;

        SectionPos baseLayerSectionPos;
        if (!isClientSide()) {
            LevelChunk layerChunk = ((LevelWithManager) level).infiniteWorlds$getLayerUtils().getChunk(pos, level);

            if (layerChunk == null) return;

            cir.setReturnValue(layerChunk.getFluidState(pos));
        }
        else {
            baseLayerSectionPos = SectionPos.of(pos);

            LevelChunk layerChunk = infiniteWorlds$cachedManager.getLayerChunk(baseLayerSectionPos);
            if (layerChunk == null) return;

            cir.setReturnValue(layerChunk.getFluidState(pos));
        }
    }

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void setBlockStateFix(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (infiniteWorlds$cachedManager == null) return;
        SectionPos sectionPos = SectionPos.of(pos);

        LevelChunk chunk = isClientSide ? infiniteWorlds$getChunkManager().getLayerChunk(sectionPos) : infiniteWorlds$getLayerUtils().getChunk(pos, (Level) (Object) this);

        if (chunk != null) {
            Level level = (Level) (Object) this;
            BlockSnapshot blockSnapshot = null;
            if (this.captureBlockSnapshots && !this.isClientSide) {
                blockSnapshot = BlockSnapshot.create(dimension, level, pos, flags);
                this.capturedBlockSnapshots.add(blockSnapshot);
            }

            BlockState old = chunk.getBlockState(pos);
            old.getLightEmission(level, pos);
            old.getLightBlock(level, pos);
            chunk.setBlockState(pos, state, (flags & 64) != 0);
            if (state == null) {
                if (blockSnapshot != null) {
                    this.capturedBlockSnapshots.remove(blockSnapshot);
                }

                cir.setReturnValue(false);
            } else {
                if (blockSnapshot == null) {
                    markAndNotifyBlock(pos, chunk, old, state, flags, recursionLeft);
                }
                cir.setReturnValue(true);
            }
        }
    }
}
