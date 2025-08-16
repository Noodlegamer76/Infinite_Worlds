package com.noodlegamer76.infiniteworlds.network.stackedchunk;

import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.client.ClientStackedChunk;
import com.noodlegamer76.infiniteworlds.level.client.renderer.StackedChunkRenderer;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;


public class StackedChunkHandler {

    public static void handle(StackedChunkPayload payload, IPayloadContext context) {
        Level level = context.player().level();

        if (!(level instanceof ServerLevel)) {
            Minecraft.getInstance().execute(() -> {
                List<ClientStackedChunk> chunks = StackedChunkLoader.loadChunksFromPayload((ClientLevel) level, payload);
                for (ClientStackedChunk chunk : chunks) {
                    ((LevelWithManager) level).infiniteWorlds$getChunkManager().addChunk(chunk.getSectionPos(), chunk);
                    StackedChunkRenderer.addStackedChunk(chunk.getSectionPos());
                }
            });
        }
    }
}
