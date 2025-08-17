package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    @Shadow @Final public ServerLevel level;

   //@Inject(
   //        method = "blockChanged",
   //        at = @At("HEAD"),
   //        cancellable = true
   //)
   //public void setBlockStateFix(BlockPos pos, CallbackInfo ci) {
   //    final int sectionsPerLevel = Math.max(1, level.getSectionsCount());
   //    final int minSection = level.getMinSection();
   //    final int baseLayerY = minSection + Math.floorDiv(pos.getY() - minSection, sectionsPerLevel) * sectionsPerLevel;

   //    ChunkManager manager = ((LevelWithManager) level).infiniteWorlds$getChunkManager();
   //    SectionPos sectionPos = SectionPos.of(pos.getX(), baseLayerY, pos.getZ());
   //    LayerIndex index = manager.layerIndexSavedData.getIndexFromBaseWorld(sectionPos);
   //    if (index == null) return;
   //    ChunkHolder holder = manager.getLayerLevel().getChunkSource().chunkMap.getVisibleChunkIfPresent(index.layerPos.toLong());
   //    if (holder != null && holder.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING)) {

   //        int x = Math.floorMod(pos.getX(), 16) + (index.layerPos.x * 16);
   //        int y = pos.getY() - index.basePos.getY() * 16;
   //        int z = Math.floorMod(pos.getZ(), 16) + (index.layerPos.z * 16);

   //        BlockPos updatedPos = new BlockPos(x, y, z);
   //        holder.blockChanged(updatedPos);
   //        ci.cancel();
   //    }
   //}
}
