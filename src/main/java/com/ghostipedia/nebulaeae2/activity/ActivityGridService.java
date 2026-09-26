package com.ghostipedia.nebulaeae2.activity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.google.common.collect.ImmutableList;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.parts.AEBasePart;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class ActivityGridService implements IActivityService, IGridServiceProvider {
    private final IGrid grid;
    private final Set<UUID> segments = new HashSet<>();
    private ActivityArchive archive;
    private UUID writable;

    public ActivityGridService(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public void addNode(IGridNode node, CompoundTag savedData) {
        writable = null;
        archive = ActivityArchive.get(node.getLevel().getServer());
        if (savedData != null) {
            var list = savedData.getList("nebulaeActivitySegments", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                try {
                    segments.add(UUID.fromString(list.getString(i)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        ((ActivityStorageBinding) grid.getStorageService()).nebulae$bindActivity(this);
    }

    @Override
    public void removeNode(IGridNode node) {
        writable = null;
    }

    @Override
    public void onServerEndTick() {
        if (archive != null && writable == null && grid.getNodes().iterator().hasNext()) writableSegment();
    }

    @Override
    public void saveNodeData(IGridNode node, CompoundTag savedData) {
        var list = new ListTag();
        for (var id : segments) {
            list.add(StringTag.valueOf(id.toString()));
        }
        savedData.put("nebulaeActivitySegments", list);
    }

    @Override
    public ActivityArchive archive() {
        return archive;
    }

    @Override
    public Set<UUID> segments() {
        return Set.copyOf(segments);
    }

    @Override
    public UUID writableSegment() {
        UUID segment = writable;
        if (segment == null) {
            segment = UUID.randomUUID();
            writable = segment;
            segments.add(segment);
            archive.ensureSegment(segment);
            for (var node : ImmutableList.copyOf(grid.getNodes())) {
                var owner = node.getOwner();
                if (owner instanceof BlockEntity block) {
                    block.setChanged();
                } else if (owner instanceof AEBasePart part) {
                    part.getBlockEntity().setChanged();
                }
            }
        }
        return segment;
    }
}
