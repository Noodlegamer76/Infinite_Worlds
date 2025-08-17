package com.noodlegamer76.infiniteworlds.mixin;

import com.noodlegamer76.infiniteworlds.level.util.CreateLayer;
import com.noodlegamer76.infiniteworlds.level.util.LayerUtils;
import com.noodlegamer76.infiniteworlds.level.util.LevelCreationVariables;
import com.noodlegamer76.infiniteworlds.level.util.LevelWithManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;

    @Shadow @Final private Executor executor;

    @Shadow @Final protected LevelStorageSource.LevelStorageAccess storageSource;

    @Inject(method = "createLevels", at = @At("TAIL"))
    private void create(ChunkProgressListener listener, CallbackInfo ci) {
        LevelCreationVariables.setLevels(levels);
        LevelCreationVariables.setExecutor(executor);
        LevelCreationVariables.setStorageSource(storageSource);
        LevelCreationVariables.setListener(listener);

        ArrayList<ServerLevel> layers = new ArrayList<>();
        for (ServerLevel parent: levels.values()) {
            ServerLevel level = CreateLayer.createLayer(parent, 1);
            layers.add(level);
        }

        for (ServerLevel level: layers) {
            levels.put(level.dimension(), level);
            if (!LayerUtils.isLevelStorageLevel(level)) {
                ServerLevel serverLevel = null;
                for (ServerLevel current: layers) {
                    if (current.dimension().equals(LayerUtils.getLevelKey(level, 1))) {
                        serverLevel = current;
                        break;
                    }
                }
                if (serverLevel == null) {
                    continue;
                }
                ((LevelWithManager) serverLevel).infiniteWorlds$setChunkManager(((LevelWithManager) level).infiniteWorlds$getChunkManager());
            }
        }
    }
}
