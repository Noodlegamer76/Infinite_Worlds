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
        int sectionX = SectionPos.blockToSectionCoord(x);
        int sectionY = SectionPos.blockToSectionCoord(cursor.nextY());
        int sectionZ = SectionPos.blockToSectionCoord(z);
        SectionPos chunkPos = SectionPos.of(sectionX, sectionY, sectionZ);

        Level level = ((BlockCollisionsAccessor) this).getCollisionGetter() instanceof Level collisionGetter ? collisionGetter : null;

        LevelChunk chunk = null;
        if (level != null) {
            chunk = ChunkManagerStorage.getManager(level).getChunk(chunkPos);
        }

        if (chunk != null) {
            return chunk;
        }

        return getChunk(x, z);
    }
}
