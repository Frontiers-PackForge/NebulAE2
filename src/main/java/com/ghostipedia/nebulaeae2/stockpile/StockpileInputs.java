package com.ghostipedia.nebulaeae2.stockpile;

import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

final class StockpileInputs {
    private StockpileInputs() {}

    static final class Items extends ExportOnlyAEItemList {
        Items(MEStockpileTopperPartMachine machine) {
            super(MEStockpileTopperPartMachine.FILTER_SLOTS, () -> new ItemSlot(machine));
            shouldDropInventoryInWorld(false);
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            for (var slot : inventory) {
                if (!slot.getStackInSlot(0).isEmpty()) return false;
            }
            return true;
        }
    }

    static final class Fluids extends ExportOnlyAEFluidList {
        Fluids(MEStockpileTopperPartMachine machine) {
            super(machine, MEStockpileTopperPartMachine.FILTER_SLOTS, () -> new FluidSlot(machine));
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            for (var slot : inventory) {
                if (!slot.getFluid().isEmpty()) return false;
            }
            return true;
        }
    }

    private static final class ItemSlot extends ExportOnlyAEItemSlot {
        private final MEStockpileTopperPartMachine machine;

        ItemSlot(MEStockpileTopperPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            if (index != 0 || config == null || !(config.what() instanceof AEItemKey key)) return ItemStack.EMPTY;
            int available = (int) machine.extract(config, Integer.MAX_VALUE, true);
            return available > 0 ? key.toStack(available) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int index, int amount, boolean simulate) {
            if (index != 0 || config == null || !(config.what() instanceof AEItemKey key)) return ItemStack.EMPTY;
            long extracted = machine.extract(config, amount, simulate);
            if (extracted <= 0) return ItemStack.EMPTY;
            if (!simulate) setStock(remaining(stock, extracted));
            return key.toStack((int) extracted);
        }

        @Override
        public ExportOnlyAEItemSlot copy() {
            return new ExportOnlyAEItemSlot(config, stock);
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries) {
            return filterTag(config, registries);
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
            config = GenericStack.readTag(registries, tag.getCompound(CONFIG_TAG));
            if (config != null && !(config.what() instanceof AEItemKey)) config = null;
            stock = null;
        }
    }

    private static final class FluidSlot extends ExportOnlyAEFluidSlot {
        private final MEStockpileTopperPartMachine machine;

        FluidSlot(MEStockpileTopperPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public FluidStack getFluid() {
            if (config == null || !(config.what() instanceof AEFluidKey key)) return FluidStack.EMPTY;
            int available = (int) machine.extract(config, Integer.MAX_VALUE, true);
            return available > 0 ? key.toStack(available) : FluidStack.EMPTY;
        }

        @Override
        public int getFluidAmount() {
            return getFluid().getAmount();
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (config == null || !config.what().equals(AEFluidKey.of(resource))) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int amount, FluidAction action) {
            if (config == null || !(config.what() instanceof AEFluidKey key)) return FluidStack.EMPTY;
            long extracted = machine.extract(config, amount, action.simulate());
            if (extracted <= 0) return FluidStack.EMPTY;
            if (action.execute()) setStock(remaining(stock, extracted));
            return key.toStack((int) extracted);
        }

        @Override
        public ExportOnlyAEFluidSlot copy() {
            return new ExportOnlyAEFluidSlot(config, stock);
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries) {
            return filterTag(config, registries);
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
            config = GenericStack.readTag(registries, tag.getCompound(CONFIG_TAG));
            if (config != null && !(config.what() instanceof AEFluidKey)) config = null;
            stock = null;
        }
    }

    private static CompoundTag filterTag(GenericStack config, HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        if (config != null) tag.put("config", GenericStack.writeTag(registries, config));
        return tag;
    }

    private static GenericStack remaining(GenericStack stock, long extracted) {
        return stock == null || stock.amount() <= extracted ? null
                : new GenericStack(stock.what(), stock.amount() - extracted);
    }
}
