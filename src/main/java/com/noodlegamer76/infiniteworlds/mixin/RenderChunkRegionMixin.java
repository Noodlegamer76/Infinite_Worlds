package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderChunkRegion.class)
public class RenderChunkRegionMixin {

    @Shadow @Final protected Level level;

    @Inject(
            method = "getBlockState",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getBlockStateRedirect(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        SectionPos chunkPos = SectionPos.of(pos);
        LevelChunk chunk = ChunkManagerStorage.getManager(level).getBaseChunk(chunkPos);

        if (chunk != null) {
            cir.setReturnValue(chunk.getBlockState(pos));
        }
    }
}
