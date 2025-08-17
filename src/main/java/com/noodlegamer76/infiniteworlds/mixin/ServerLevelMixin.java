package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import com.noodlegamer76.infiniteworlds.level.util.LayerUtils;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

  //@Inject(
  //        method = "sendBlockUpdated",
  //        at = @At("HEAD"),
  //        cancellable = true
  //)
  //public void sendBlockUpdatedFix(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
  //    if (!LayerUtils.isLevelStorageLevel((ServerLevel)(Object)this)) {
  //        return;
  //    }

  //    ChunkManager manager = ((LevelWithManager) this).infiniteWorlds$getChunkManager();
  //    if (manager.layerIndexSavedData == null) return;

  //    ChunkPos chunkPos = new ChunkPos(pos);
  //    LevelChunk chunk = manager.getLayerChunk(chunkPos);
  //    LayerIndex index = manager.layerIndexSavedData.getIndexFromLayerWorld(chunkPos);
  //    if (chunk == null || index == null) return;

  //    int x = Math.floorMod(pos.getX(), 16) + (index.basePos.getX() * 16);
  //    int y = pos.getY() - index.basePos.getY() * 16;
  //    int z = Math.floorMod(pos.getZ(), 16) + (index.basePos.getZ() * 16);

  //    BlockPos updatedPos = new BlockPos(x, y, z);
  //    manager.baseLevel.sendBlockUpdated(updatedPos, oldState, newState, flags);
  //    ci.cancel();
  //}
}
