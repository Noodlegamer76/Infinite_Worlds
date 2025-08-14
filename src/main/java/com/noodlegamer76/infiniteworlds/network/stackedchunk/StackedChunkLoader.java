package com.noodlegamer76.infiniteworlds.network.stackedchunk;

import com.noodlegamer76.infiniteworlds.level.client.ClientStackedChunk;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class StackedChunkLoader {

    public static List<ClientStackedChunk> loadChunksFromPayload(ClientLevel clientLevel, StackedChunkPayload payload) {
        FriendlyByteBuf buffer = payload.getChunkDataBuffer();
        int count = buffer.readVarInt();

        List<ClientStackedChunk> chunks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int x = buffer.readInt();
            int y = buffer.readInt();
            int z = buffer.readInt();

            int length = buffer.readVarInt();
            byte[] chunkData = new byte[length];
            buffer.readBytes(chunkData);

            SectionPos pos = SectionPos.of(x, y, z);
            ClientStackedChunk chunk = new ClientStackedChunk(clientLevel, pos);

            FriendlyByteBuf chunkBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(chunkData));
            chunk.getSection(0).read(chunkBuffer);
            chunkBuffer.release();

            chunks.add(chunk);
        }

        return chunks;
    }

}
