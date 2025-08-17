package com.noodlegamer76.infiniteworlds.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Inject(
            method = "handleBlockBreakAction",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onHandleBlockBreakAction(
            BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci
    ) {
        if (maxBuildHeight == 30000000) {
            return;
        }

        int newMaxBuildHeight = 30000000;

        ((ServerPlayerGameMode)(Object)this)
                .handleBlockBreakAction(pos, action, face, newMaxBuildHeight, sequence);

        ci.cancel();
    }
}
