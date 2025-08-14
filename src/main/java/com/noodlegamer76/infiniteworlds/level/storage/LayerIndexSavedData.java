package com.noodlegamer76.infiniteworlds.level.storage;

import com.noodlegamer76.infiniteworlds.level.ChunkManager;
import com.noodlegamer76.infiniteworlds.level.ChunkManagerStorage;
import com.noodlegamer76.infiniteworlds.level.index.LayerIndex;
import com.noodlegamer76.infiniteworlds.level.util.ChunkPosIterator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LayerIndexSavedData extends SavedData {
    private static final String DATA_NAME = "infiniteworlds_layer_index";

    private final Map<SectionPos, LayerIndex> indicesFromMain = new HashMap<>();
    private final Map<ChunkPos, LayerIndex> indicesFromLayer = new HashMap<>();

    public LayerIndexSavedData() {
        super();
    }

    public LayerIndexSavedData(CompoundTag compoundTag, HolderLookup.Provider provider) {
        loadFromTag(compoundTag);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        saveToTag(compoundTag);
        return compoundTag;
    }

    public static final Factory<LayerIndexSavedData> FACTORY = new Factory<>(
            LayerIndexSavedData::new,
            LayerIndexSavedData::new
    );

    public static LayerIndexSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private void loadFromTag(CompoundTag tag) {
        indicesFromMain.clear();
        indicesFromLayer.clear();
        if (tag.contains("layers")) {
            CompoundTag layersTag = tag.getCompound("layers");
            for (String key : layersTag.getAllKeys()) {
                CompoundTag layerTag = layersTag.getCompound(key);
                SectionPos mainPos = SectionPos.of(layerTag.getInt("x1"), layerTag.getInt("y1"), layerTag.getInt("z1"));
                ChunkPos layerPos = new ChunkPos(layerTag.getInt("x2"), layerTag.getInt("z2"));
                int height = layerTag.getInt("sectionHeight");
                LayerIndex layerIndex = new LayerIndex(layerPos, height, mainPos);
                indicesFromMain.put(mainPos, layerIndex);
                indicesFromLayer.put(layerPos, layerIndex);
            }
        }
    }

    private void saveToTag(CompoundTag tag) {
        CompoundTag layersTag = new CompoundTag();
        for (Map.Entry<SectionPos, LayerIndex> entry : indicesFromMain.entrySet()) {
            LayerIndex layerIndex = entry.getValue();
            CompoundTag layerTag = new CompoundTag();
            layerTag.putInt("x1", layerIndex.basePos.getX());
            layerTag.putInt("y1", layerIndex.basePos.getY());
            layerTag.putInt("z1", layerIndex.basePos.getZ());
            layerTag.putInt("x2", layerIndex.layerPos.x);
            layerTag.putInt("z2", layerIndex.layerPos.z);
            layerTag.putInt("sectionHeight", layerIndex.layerPosSection);

            layersTag.put(entry.getKey().toString(), layerTag);
        }
        tag.put("layers", layersTag);
    }

    public void putLayer(SectionPos key, LayerIndex index) {
        indicesFromMain.put(key, index);
        indicesFromLayer.put(index.layerPos, index);
        setDirty();
    }

    public void removeLayer(SectionPos key) {
        LayerIndex index = indicesFromMain.remove(key);
        if (index != null) {
            indicesFromLayer.remove(index.layerPos);
        }
        setDirty();
    }

    @Nullable
    public LayerIndex getIndexFromBaseWorld(SectionPos key) {
        return indicesFromMain.get(key);
    }

    public LayerIndex getIndexFromLayerWorld(ChunkPos key) {
        return indicesFromLayer.get(key);
    }

    public LayerIndex createIndexFromBaseWorld(SectionPos pos, ServerLevel baseLevel) {
        ChunkManager manager = ChunkManagerStorage.getManager(baseLevel);
        ChunkPosIterator iterator = manager.layerIndexManagerSavedData.getIterator();
        if (iterator == null) {
            return null;
        }

        int step = baseLevel.getMaxBuildHeight() + baseLevel.getMinBuildHeight();
        int offset = Math.floorDiv(pos.y(), step) * step;

        LayerIndex index = new LayerIndex(iterator.next(), offset, pos);
        putLayer(pos, index);
        return index;
    }
}
