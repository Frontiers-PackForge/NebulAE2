package com.ghostipedia.nebulaeae2.sticky;

import com.ghostipedia.nebulaeae2.NebulaeAE2;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.api.ids.AEComponents;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEParts;

public final class StickyCard {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NebulaeAE2.MODID);
    public static final DeferredItem<Item> ITEM = ITEMS.register("sticky_card",
            () -> Upgrades.createUpgradeCardItem(new Item.Properties()));

    private StickyCard() {}

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(StickyCard::setup);
        bus.addListener(StickyCard::creativeTab);
        NeoForge.EVENT_BUS.addListener(StickyCard::tooltip);
    }

    private static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof IBasicCellItem) {
                    Upgrades.add(ITEM, item, 1, "tooltip.nebulaeae2.sticky.cells");
                }
            }
            Upgrades.add(ITEM, AEParts.STORAGE_BUS, 1);
        });
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(ITEM);
        }
    }

    public static boolean isCard(ItemStack stack) {
        return stack.is(ITEM.get());
    }

    public static boolean installed(ItemStack stack) {
        return stack.getOrDefault(AEComponents.UPGRADES, ItemContainerContents.EMPTY).stream()
                .anyMatch(StickyCard::isCard);
    }

    public static boolean partitioned(ItemStack stack) {
        if (stack.getItem() instanceof IBasicCellItem cell) {
            return !cell.getConfigInventory(stack).keySet().isEmpty();
        }
        return false;
    }

    public static Component invalidMessage() {
        return Component.translatable("tooltip.nebulaeae2.sticky.invalid").withStyle(ChatFormatting.RED);
    }

    private static void tooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        if (isCard(stack)) {
            event.getToolTip().add(Component.translatable("tooltip.nebulaeae2.sticky.description"));
            event.getToolTip().add(Component.translatable("tooltip.nebulaeae2.sticky.partition_required"));
        } else if (stack.getItem() instanceof IBasicCellItem && installed(stack)) {
            event.getToolTip().add(partitioned(stack)
                    ? Component.translatable("tooltip.nebulaeae2.sticky.enabled")
                    : invalidMessage());
        }
    }
}
