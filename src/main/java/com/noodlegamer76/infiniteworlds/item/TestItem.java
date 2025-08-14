package com.noodlegamer76.infiniteworlds.item;

import com.noodlegamer76.infiniteworlds.level.client.ClientStackedChunk;
import com.noodlegamer76.infiniteworlds.level.util.FillChunk;
import com.noodlegamer76.infiniteworlds.network.stackedchunk.StackedChunkInfo;
import com.noodlegamer76.infiniteworlds.network.stackedchunk.StackedChunkPayload;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

public class TestItem extends Item {
    public TestItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide) {
        }
        if (level.isClientSide && !player.isShiftKeyDown()) {
        }
        if (!level.isClientSide() && !player.isShiftKeyDown()) {
            ServerLevel parent = (ServerLevel) level;
            MinecraftServer server = parent.getServer();
           // CreateLayer.createLayer(parent, 1);
          //StackedChunkStorage.clear();
          List<StackedChunkInfo> chunks = new ArrayList<>();
         for (int x = 0; x < 5; x++) {
             for (int y = 0; y < 5; y++) {
                 for (int z = 0; z < 5; z++) {
                     SectionPos pos = SectionPos.of(x - 1, y + 20, z - 1);
                     LevelChunk chunk = new ClientStackedChunk(parent, pos);
                     FillChunk.fillChunkWithDirt(chunk);
                     chunks.add(new StackedChunkInfo(chunk, pos, 0));
                 }
             }
         }
         StackedChunkPayload payload = new StackedChunkPayload(chunks);
         ((ServerPlayer) player).connection.send(payload);
        }
        if (level instanceof ServerLevel serverLevel && player.isShiftKeyDown()) {
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }
}
