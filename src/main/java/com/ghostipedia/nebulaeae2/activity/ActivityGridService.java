package com.ghostipedia.nebulaeae2.activity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.parts.AEBasePart;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class ActivityGridService implements IActivityService, IGridServiceProvider {
    private final IGrid grid;
    private UUID history;
    private ActivityArchive archive;
    private UUID writable;
    private boolean dirtyNodes;

    public ActivityGridService(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public void addNode(IGridNode node, CompoundTag savedData) {
        if (archive == null) archive = ActivityArchive.get(node.getLevel().getServer());
        restoreHistory(savedData);
        ((ActivityStorageBinding) grid.getStorageService()).nebulae$bindActivity(this);
    }

    void restoreHistory(CompoundTag savedData) {
        if (savedData != null) {
            var legacy = new HashSet<UUID>();
            var list = savedData.getList("nebulaeActivitySegments", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                try {
                    legacy.add(UUID.fromString(list.getString(i)));
                } catch (IllegalArgumentException ignored) {
                }
            }
            UUID savedReference = savedData.hasUUID("nebulaeActivityHistory")
                    ? savedData.getUUID("nebulaeActivityHistory") : null;
            UUID incoming = archive.mergeHistory(savedReference, archive.importHistory(legacy));
            var merged = archive.mergeHistory(history, incoming);
            if (!Objects.equals(history, merged)) {
                history = merged;
                writable = null;
                dirtyNodes = true;
            }
            if (!Objects.equals(savedReference, history) || savedData.contains("nebulaeActivitySegments")) {
                dirtyNodes = true;
            }
        } else if (history != null) {
            dirtyNodes = true;
        }
    }

    @Override
    public void removeNode(IGridNode node) {
        writable = null;
    }

    @Override
    public void onServerEndTick() {
        if (archive == null || !grid.getNodes().iterator().hasNext()) return;
        if (history == null) writableSegment();
        markNodesChanged();
    }

    @Override
    public void saveNodeData(IGridNode node, CompoundTag savedData) {
        if (history == null) return;
        savedData.remove("nebulaeActivitySegments");
        savedData.putUUID("nebulaeActivityHistory", history);
    }

    @Override
    public ActivityArchive archive() {
        return archive;
    }

    @Override
    public Set<UUID> segments() {
        return archive == null ? Set.of() : archive.historySegments(history);
    }

    @Override
    public UUID writableSegment() {
        UUID segment = writable;
        if (segment == null) {
            segment = archive.branchHistory(history);
            history = segment;
            writable = segment;
            dirtyNodes = true;
            markNodesChanged();
        }
        return segment;
    }

    private void markNodesChanged() {
        if (!dirtyNodes) return;
        dirtyNodes = false;
        for (var node : ImmutableList.copyOf(grid.getNodes())) {
            var owner = node.getOwner();
            if (owner instanceof BlockEntity block) {
                block.setChanged();
            } else if (owner instanceof AEBasePart part) {
                part.getBlockEntity().setChanged();
            } else if (owner instanceof IGridConnectedMachine machine) {
                machine.self().setChanged();
            }
        }
    }
}
