package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.client.ClientStackedChunk;
import com.noodlegamer76.infiniteworlds.level.client.renderer.ChunkRenderSection;
import com.noodlegamer76.infiniteworlds.level.client.renderer.StackedChunkRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ViewArea.class)
public class ViewAreaMixin {

    @Shadow public SectionRenderDispatcher.RenderSection[] sections;

    @Inject(
            method = "setDirty",
            at = @At("HEAD"),
            cancellable = true
    )
    public void setStackedChunkDirty(int sectionX, int sectionY, int sectionZ, boolean reRenderOnMainThread, CallbackInfo ci) {
        SectionPos chunkPos = SectionPos.of(sectionX, sectionY, sectionZ);
        ChunkRenderSection section = StackedChunkRenderer.getStackedSections().get(chunkPos);

        if (section != null && section.getRenderSection() != null) {
            section.getRenderSection().setDirty(reRenderOnMainThread);
        }
    }
}
