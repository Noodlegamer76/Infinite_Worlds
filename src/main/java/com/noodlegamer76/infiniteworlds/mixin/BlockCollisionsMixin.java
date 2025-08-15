package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.mixin.accessor.BlockCollisionsAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin {

    @Shadow @Final private Cursor3D cursor;

    @Shadow @Nullable protected abstract BlockGetter getChunk(int x, int z);

    @Shadow @Final private BlockPos.MutableBlockPos pos;

    @Redirect(
            method = "computeNext",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockCollisions;getChunk(II)Lnet/minecraft/world/level/BlockGetter;")
    )
    public BlockGetter getStackedChunkForCollision(BlockCollisions instance, int x, int z) {
        Level level = ((BlockCollisionsAccessor) this).getCollisionGetter() instanceof Level collisionGetter ? collisionGetter : null;
        if (level == null) return getChunk(x, z);

        int sectionsPerLevel = Math.max(1, level.getSectionsCount());

        int absSectionY = SectionPos.blockToSectionCoord(cursor.nextY());
        int baseLayerSectionY = Math.floorDiv(absSectionY, sectionsPerLevel) * sectionsPerLevel;
        SectionPos baseLayerChunkPos;

       if (level.isClientSide) {
           baseLayerChunkPos = SectionPos.of(
                   SectionPos.blockToSectionCoord(x),
                   SectionPos.blockToSectionCoord(cursor.nextY()),
                   SectionPos.blockToSectionCoord(z)
           );
       }
       else {
           baseLayerChunkPos = SectionPos.of(
                   SectionPos.blockToSectionCoord(x),
                   baseLayerSectionY,
                   SectionPos.blockToSectionCoord(z)
           );
       }

        LevelChunk chunk = ChunkManagerStorage.getManager(level).getBaseChunk(baseLayerChunkPos);

        if (chunk != null) return chunk;

        return getChunk(x, z);
    }

}
