package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import com.noodlegamer76.infiniteworlds.level.util.LayerUtils;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

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

        ChunkManager manager = ((LevelWithManager) level).infiniteWorlds$getChunkManager();
        if (manager == null) return;

        LevelChunkSection section = LayerUtils.getWrappedSection((LevelChunk) (Object) this, SectionPos.blockToSectionCoord(pos.getY()));

        int lx = pos.getX() & 15;
        int ly = pos.getY() & 15;
        int lz = pos.getZ() & 15;

        cir.setReturnValue(section.getBlockState(lx, ly, lz));
    }

    @Inject(
            method = "setBlockState",
            at = @At("HEAD"),
            cancellable = true
    )
    public void setBlockStateStacked(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> cir) {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        ChunkManager manager = ((LevelWithManager) level).infiniteWorlds$getChunkManager();
        if (manager == null) {
            return;
        }

        LevelChunkSection section = LayerUtils.getWrappedSection((LevelChunk) (Object) this, SectionPos.blockToSectionCoord(pos.getY()));

        int lx = pos.getX() & 15;
        int ly = pos.getY() & 15;
        int lz = pos.getZ() & 15;

        BlockState oldState = section.setBlockState(lx, ly, lz, state);
        cir.setReturnValue(oldState);
    }

    @Inject(
            method = "getFluidState(III)Lnet/minecraft/world/level/material/FluidState;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getFluidStateFix(int x, int y, int z, CallbackInfoReturnable<FluidState> cir) {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        ChunkManager manager = ((LevelWithManager) level).infiniteWorlds$getChunkManager();
        if (manager == null) return;

        LevelChunkSection section = LayerUtils.getWrappedSection((LevelChunk) (Object) this, SectionPos.blockToSectionCoord(y));

        int lx = x & 15;
        int ly = y & 15;
        int lz = z & 15;

        cir.setReturnValue(section.getFluidState(lx, ly, lz));
    }
}
