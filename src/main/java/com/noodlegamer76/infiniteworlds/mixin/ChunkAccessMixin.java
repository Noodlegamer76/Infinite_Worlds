package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.client.ClientStackedChunk;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {
    @Shadow
    @Final
    protected ChunkPos chunkPos;

    @Shadow @Nullable public abstract Level getLevel();

    @Inject(
            method = "isSectionEmpty",
            at = @At("HEAD"),
            cancellable = true
    )
    public void isSectionEmpty(int y, CallbackInfoReturnable<Boolean> cir) {
        SectionPos pos = SectionPos.of(chunkPos.x, y, chunkPos.z);
        LevelChunk chunk = ((LevelWithManager) getLevel()).infiniteWorlds$getChunkManager().getLayerChunk(pos);

        if (chunk instanceof ClientStackedChunk) {
            cir.setReturnValue(chunk.isSectionEmpty(y));
        }
    }
}
