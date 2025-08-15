package com.noodlegamer76.infiniteworlds.level;

import com.noodlegamer76.infiniteworlds.level.util.LayerUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChunkManagerStorage {
    private static final ThreadLocal<Map<ResourceKey<Level>, ChunkManager>> MANAGERS = ThreadLocal.withInitial(HashMap::new);

    public static ChunkManager getManager(Level level) {
        ResourceKey<Level> key = level.dimension();

        if (level instanceof ServerLevel serverLevel) {
            ChunkManager manager = MANAGERS.get().computeIfAbsent(key, k -> new ChunkManager(serverLevel));

            ResourceKey<Level> layerKey = LayerUtils.getLevelKey(serverLevel, 1);
            ServerLevel layerLevel = serverLevel.getServer().getLevel(layerKey);
            if (layerLevel != null) {
                MANAGERS.get().putIfAbsent(layerKey, manager);
            }

            return manager;
        }

        return MANAGERS.get().computeIfAbsent(key, k -> new ChunkManager(level));
    }

    public static ChunkManager getManagerForLayer(ServerLevel layerLevel) {
        if (layerLevel == null) return null;
        ResourceKey<Level> layerKey = layerLevel.dimension();
        ChunkManager m = MANAGERS.get().get(layerKey);
        if (m != null) return m;

        for (ChunkManager candidate : MANAGERS.get().values()) {
            if (candidate.getLayerLevel() == layerLevel) return candidate;
        }
        return null;
    }

    public static void removeManager(Level level) {
        MANAGERS.get().remove(level.dimension());
    }

    public static boolean containsManager(Level level) {
        return MANAGERS.get().containsKey(level.dimension());
    }

    public static Collection<ChunkManager> getAllManagers() {
        return MANAGERS.get().values();
    }

    public static void clear() {
        MANAGERS.get().clear();
    }
}
