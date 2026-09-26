package com.ghostipedia.nebulaeae2.stockpile;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class MEStockpileTopperPartMachine extends TieredPartMachine
        implements IGridConnectedMachine, IMuiMachine, IDataStickInteractable, IControllable, IDistinctPart {
    public static final int FILTER_SLOTS = 9;
    private static final String DATA_KEY = "NebulaeStockpileTopper";
    @SaveField
    private final GridNodeHolder nodeHolder;
    @SaveField
    private final StockpileInputs.Items items;
    @SaveField
    private final StockpileInputs.Fluids fluids;
    @SaveField
    private final ProgrammableCircuitSlotTrait circuitSlot;
    @SaveField
    @SyncToClient
    private boolean distinct;
    @SaveField
    private final ExportOnlyAEItemSlot watcher = new ExportOnlyAEItemSlot();
    @SaveField
    private long target = 1;
    @SaveField
    private long refillGap;
    @SaveField
    private boolean refilling;
    @SaveField
    private boolean workingEnabled = true;
    private final IActionSource actionSource;
    private boolean online;
    private long watchedAmount;

    public MEStockpileTopperPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        nodeHolder = attachTrait(new GridNodeHolder(this));
        actionSource = IActionSource.ofMachine(nodeHolder.getMainNode()::getNode);
        items = attachTrait(new StockpileInputs.Items(this));
        fluids = attachTrait(new StockpileInputs.Fluids(this));
        circuitSlot = attachTrait(new ProgrammableCircuitSlotTrait());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getHandlerList().setDistinct(distinct);
        getHandlerList().setColor(getPaintingColor());
        subscribeServerTick(() -> {
            if (getOffsetTimer() % 40 == 0) refreshStock();
        });
    }

    @Override
    public boolean canShared(MultiblockControllerMachine controller, String substructureName) {
        return false;
    }

    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
        syncDataHolder.markClientSyncFieldDirty("distinct");
        getHandlerList().setDistinctAndNotify(distinct);
        refreshStock();
        setChanged();
    }

    @Override
    public boolean supportsDistinct() {
        return true;
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerList().setColor(color, true);
    }

    public ProgrammableCircuitSlotTrait getCircuitSlot() {
        return circuitSlot;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        refreshStock();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.of(newFacing));
    }

    @Override
    public void saveChanges() {
        setChanged();
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean enabled) {
        workingEnabled = enabled;
        refreshStock();
        setChanged();
    }

    @Nullable
    MEStorage storage() {
        var grid = getMainNode().getGrid();
        return grid != null && getMainNode().isActive() ? grid.getStorageService().getInventory() : null;
    }

    boolean canSupply() {
        return workingEnabled && refilling && getWatcher() != null && storage() != null;
    }

    boolean canSupply(@Nullable GenericStack filter) {
        return filter != null && canSupply() && !conflictsWithStockingPart(filter);
    }

    private boolean conflictsWithStockingPart(GenericStack filter) {
        if (distinct) return false;
        for (var controller : getControllers()) {
            for (var part : controller.getParts()) {
                if (part == this || !(part instanceof IGridConnectedMachine connected)
                        || connected.getMainNode().getGrid() != getMainNode().getGrid()) continue;
                if (part instanceof IDistinctPart distinctPart && distinctPart.isDistinct()) continue;
                if (part instanceof MEStockpileTopperPartMachine
                        && part.getBlockPos().compareTo(getBlockPos()) > 0) continue;
                for (var trait : part.getAllTraits()) {
                    boolean stocking = trait instanceof ExportOnlyAEItemList itemList && itemList.isStocking()
                            || trait instanceof ExportOnlyAEFluidList fluidList && fluidList.isStocking();
                    if (stocking && trait instanceof IConfigurableSlotList slots
                            && slots.hasStackInConfig(filter, false)) return true;
                }
            }
        }
        return false;
    }

    long extract(GenericStack filter, long amount, boolean simulate) {
        var storage = storage();
        if (!canSupply(filter) || storage == null || amount <= 0) return 0;
        return storage.extract(filter.what(), amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                actionSource);
    }

    private void refreshStock() {
        if (isRemote() || items == null || fluids == null) return;
        boolean wasOnline = online;
        boolean wasRefilling = refilling;
        var storage = storage();
        online = storage != null;
        var watched = getWatcher();
        if (watched == null) {
            setRefilling(false);
            watchedAmount = 0;
        } else if (storage != null && workingEnabled) {
            watchedAmount = storage.getAvailableStacks().get(watched.what());
            setRefilling(StockpileRefillRules.shouldRefill(refilling, watchedAmount, target, refillGap));
        }
        refreshSlots(items, storage);
        refreshSlots(fluids, storage);
        if (wasOnline != online || wasRefilling != refilling) {
            items.notifyListeners();
            fluids.notifyListeners();
        }
    }

    private void refreshSlots(IConfigurableSlotList slots, @Nullable MEStorage storage) {
        for (int i = 0; i < slots.getConfigurableSlots(); i++) {
            var slot = slots.getConfigurableSlot(i);
            var filter = slot.getConfig();
            long available = canSupply(filter) && storage != null
                    ? storage.extract(filter.what(), Integer.MAX_VALUE, Actionable.SIMULATE, actionSource) : 0;
            slot.setStock(available > 0 ? new GenericStack(filter.what(), available) : null);
        }
    }

    private void setRefilling(boolean value) {
        if (refilling != value) {
            refilling = value;
            setChanged();
        }
    }

    public ExportOnlyAEItemList getItems() {
        return items;
    }

    public ExportOnlyAEFluidList getFluids() {
        return fluids;
    }

    @Nullable
    public GenericStack getWatcher() {
        return watcher.getConfig();
    }

    public void setWatcher(@Nullable GenericStack stack) {
        if (stack != null && !(stack.what() instanceof AEItemKey || stack.what() instanceof AEFluidKey)) return;
        watcher.setConfig(stack == null ? null : new GenericStack(stack.what(), 1));
        settingsChanged();
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long amount) {
        target = Math.max(1, amount);
        refillGap = Math.min(refillGap, target);
        settingsChanged();
    }

    public long getRefillGap() {
        return refillGap;
    }

    public void setRefillGap(long amount) {
        refillGap = Math.clamp(amount, 0, target);
        settingsChanged();
    }

    public boolean isRefilling() {
        return refilling;
    }

    public long getWatchedAmount() {
        return watchedAmount;
    }

    public void setItemFilter(int slot, @Nullable GenericStack stack) {
        setFilter(items, slot, stack, false);
    }

    public void setFluidFilter(int slot, @Nullable GenericStack stack) {
        setFilter(fluids, slot, stack, true);
    }

    private void setFilter(IConfigurableSlotList slots, int index, @Nullable GenericStack stack, boolean fluid) {
        if (index < 0 || index >= FILTER_SLOTS) return;
        if (stack != null && !(fluid ? stack.what() instanceof AEFluidKey : stack.what() instanceof AEItemKey)) return;
        for (int i = 0; stack != null && i < FILTER_SLOTS; i++) {
            var other = slots.getConfigurableSlot(i).getConfig();
            if (i != index && other != null && other.what().equals(stack.what())) return;
        }
        slots.getConfigurableSlot(index).setConfig(stack == null ? null : new GenericStack(stack.what(), 1));
        refreshStock();
        setChanged();
    }

    private void settingsChanged() {
        refilling = false;
        refreshStock();
        setChanged();
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData data, PanelSyncManager syncManager,
            UISettings settings) {
        StockpileTopperUI.build(this, mainWidget, data, syncManager, settings);
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            var tag = new CompoundTag();
            tag.put(DATA_KEY, writeSettings(player.registryAccess()));
            dataStick.set(GTDataComponents.DATA_COPY_TAG, CustomData.of(tag));
            dataStick.set(DataComponents.CUSTOM_NAME, Component.translatable("item.nebulaeae2.stockpile_settings"));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        var data = dataStick.get(GTDataComponents.DATA_COPY_TAG);
        if (data == null || !data.contains(DATA_KEY)) return InteractionResult.PASS;
        if (!isRemote()) {
            readSettings(player.registryAccess(), data.copyTag().getCompound(DATA_KEY));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_paste_settings"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    public CompoundTag writeSettings(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        tag.putInt("Version", 1);
        writeFilters(tag, "Items", items, registries);
        writeFilters(tag, "Fluids", fluids, registries);
        if (getWatcher() != null) tag.put("Watcher", GenericStack.writeTag(registries, getWatcher()));
        tag.putLong("Target", target);
        tag.putLong("RefillGap", refillGap);
        tag.putBoolean("WorkingEnabled", workingEnabled);
        tag.putBoolean("Distinct", distinct);
        tag.putInt("GhostCircuit", circuitSlot.storage.getStackInSlot(0).isEmpty()
                ? -1 : circuitSlot.getCurrentCircuit());
        return tag;
    }

    private void writeFilters(CompoundTag tag, String name, IConfigurableSlotList slots,
            HolderLookup.Provider registries) {
        var filters = new CompoundTag();
        for (int i = 0; i < FILTER_SLOTS; i++) {
            var config = slots.getConfigurableSlot(i).getConfig();
            if (config != null) filters.put(Integer.toString(i), GenericStack.writeTag(registries, config));
        }
        tag.put(name, filters);
    }

    public void readSettings(HolderLookup.Provider registries, CompoundTag tag) {
        if (tag.getInt("Version") != 1) return;
        for (int i = 0; i < FILTER_SLOTS; i++) {
            items.getConfigurableSlot(i).setConfig(null);
            fluids.getConfigurableSlot(i).setConfig(null);
        }
        workingEnabled = false;
        for (int i = 0; i < FILTER_SLOTS; i++) {
            String key = Integer.toString(i);
            setItemFilter(i, GenericStack.readTag(registries, tag.getCompound("Items").getCompound(key)));
            setFluidFilter(i, GenericStack.readTag(registries, tag.getCompound("Fluids").getCompound(key)));
        }
        target = Math.max(1, tag.getLong("Target"));
        refillGap = Math.clamp(tag.getLong("RefillGap"), 0, target);
        workingEnabled = tag.getBoolean("WorkingEnabled");
        if (tag.contains("Distinct")) setDistinct(tag.getBoolean("Distinct"));
        if (tag.contains("GhostCircuit")) {
            int circuit = tag.getInt("GhostCircuit");
            circuitSlot.storage.setStackInSlot(0, circuit < 0 ? ItemStack.EMPTY
                    : IntCircuitBehaviour.stack(Math.min(circuit, IntCircuitBehaviour.CIRCUIT_MAX)));
        }
        setWatcher(GenericStack.readTag(registries, tag.getCompound("Watcher")));
    }
}
