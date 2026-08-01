package com.ghostipedia.nebulaeae2.client;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.ghostipedia.nebulaeae2.compute.ComputeTuning;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.parts.IPartItem;
import appeng.api.storage.IStorageProvider;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.automation.IOBusPart;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = NebulaeAE2.MODID, value = Dist.CLIENT)
public final class ComputeReservationTooltipHandler {

    private static final List<TagKey<Item>> INTERFACE_TAGS = List.of(
            itemTag("ae2", "interface"),
            itemTag("extendedae", "extended_interface"),
            itemTag("extendedae", "oversize_interface"),
            itemTag("extendedae", "extended_emc_interface"),
            itemTag("megacells", "mega_interface"));
    private static final List<TagKey<Item>> CRAFTING_PROVIDER_TAGS = List.of(
            itemTag("ae2", "pattern_provider"),
            itemTag("extendedae", "extended_pattern_provider"),
            itemTag("megacells", "mega_pattern_provider"));
    private static final Set<ResourceLocation> INTERFACE_ITEMS = Set.of(
            id("megacells", "mega_emc_interface"),
            id("megacells", "cable_mega_emc_interface"));
    private static final Set<ResourceLocation> STORAGE_PROVIDER_BLOCKS = Set.of(
            id("ae2", "drive"),
            id("ae2", "me_chest"),
            id("extendedae", "ex_drive"),
            id("megacells", "cell_dock"));
    private static final Set<ResourceLocation> SCHEDULED_WORK_BLOCKS = Set.of(
            id("ae2", "io_port"),
            id("extendedae", "ex_io_port"),
            id("extendedae", "active_formation_plane"));
    private static final Set<ResourceLocation> CHANNEL_ONLY_DEVICES = Set.of(
            id("ae2", "import_bus"),
            id("ae2", "export_bus"),
            id("ae2", "annihilation_plane"),
            id("ae2", "io_port"),
            id("ae2", "spatial_io_port"),
            id("ae2", "wireless_access_point"),
            id("ae2", "terminal"),
            id("ae2", "crafting_terminal"),
            id("ae2", "pattern_encoding_terminal"),
            id("ae2", "pattern_access_terminal"),
            id("ae2", "conversion_monitor"),
            id("ae2", "me_p2p_tunnel"),
            id("ae2", "redstone_p2p_tunnel"),
            id("ae2", "item_p2p_tunnel"),
            id("ae2", "fluid_p2p_tunnel"),
            id("ae2", "fe_p2p_tunnel"),
            id("ae2", "light_p2p_tunnel"),
            id("extendedae", "ex_export_bus_part"),
            id("extendedae", "ex_import_bus_part"),
            id("extendedae", "tag_export_bus"),
            id("extendedae", "mod_export_bus"),
            id("extendedae", "active_formation_plane"),
            id("extendedae", "precise_export_bus"),
            id("extendedae", "threshold_export_bus"),
            id("extendedae", "ex_io_port"),
            id("extendedae", "ex_pattern_access_part"),
            id("extendedae", "smart_annihilation_plane"),
            id("extendedae", "ex_emc_export_bus_part"),
            id("extendedae", "ex_emc_import_bus_part"));

    private ComputeReservationTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        boolean storageProvider = isStorageProvider(stack, itemId);
        boolean craftingProvider = matchesAnyTag(stack, CRAFTING_PROVIDER_TAGS);
        boolean interfaceDevice = INTERFACE_ITEMS.contains(itemId) || matchesAnyTag(stack, INTERFACE_TAGS);
        boolean channelOnly = CHANNEL_ONLY_DEVICES.contains(itemId);
        long fixedReservation = fixedReservation(
                storageProvider,
                craftingProvider,
                interfaceDevice,
                channelOnly);

        if (fixedReservation > 0) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.nebulaeae2.compute.passive",
                    fixedReservation).withStyle(ChatFormatting.AQUA));
        }
        if (requiresScheduledWork(stack, itemId, interfaceDevice)) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.nebulaeae2.compute.active_work",
                    ComputeTuning.SCHEDULED_WORK_CWU).withStyle(ChatFormatting.GRAY));
        }
        if (interfaceDevice) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.nebulaeae2.compute.interface_stocking",
                    ComputeTuning.INTERFACE_STOCKING_GROUP_RESERVATION,
                    ComputeTuning.INTERFACE_STOCKING_SLOTS_PER_GROUP).withStyle(ChatFormatting.GRAY));
        }
        if (storageProvider) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.nebulaeae2.compute.storage_index",
                    ComputeTuning.INDEX_KEY_GROUP_RESERVATION,
                    ComputeTuning.INDEX_KEYS_PER_GROUP).withStyle(ChatFormatting.GRAY));
        }
        if (isCable(stack)) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.nebulaeae2.compute.physical_links",
                    ComputeTuning.PHYSICAL_LINK_GROUP_RESERVATION,
                    ComputeTuning.PHYSICAL_LINKS_PER_GROUP).withStyle(ChatFormatting.GRAY));
        }
    }

    private static long fixedReservation(boolean storageProvider, boolean craftingProvider, boolean interfaceDevice,
                                         boolean channelOnly) {
        boolean channelDevice = storageProvider || craftingProvider || interfaceDevice || channelOnly;
        if (!channelDevice) {
            return 0;
        }
        long reservation = ComputeTuning.CHANNEL_DEVICE_RESERVATION;
        if (storageProvider) {
            reservation += ComputeTuning.STORAGE_PROVIDER_RESERVATION;
        }
        if (craftingProvider) {
            reservation += ComputeTuning.CRAFTING_PROVIDER_RESERVATION;
        }
        return reservation;
    }

    private static boolean isStorageProvider(ItemStack stack, ResourceLocation itemId) {
        if (STORAGE_PROVIDER_BLOCKS.contains(itemId)) {
            return true;
        }
        return stack.getItem() instanceof IPartItem<?> partItem
                && IStorageProvider.class.isAssignableFrom(partItem.getPartClass());
    }

    private static boolean isCable(ItemStack stack) {
        return stack.getItem() instanceof IPartItem<?> partItem
                && ICablePart.class.isAssignableFrom(partItem.getPartClass());
    }

    private static boolean requiresScheduledWork(ItemStack stack, ResourceLocation itemId, boolean interfaceDevice) {
        if (interfaceDevice || SCHEDULED_WORK_BLOCKS.contains(itemId)) {
            return true;
        }
        if (!(stack.getItem() instanceof IPartItem<?> partItem)) {
            return false;
        }
        var partClass = partItem.getPartClass();
        return IOBusPart.class.isAssignableFrom(partClass)
                || AnnihilationPlanePart.class.isAssignableFrom(partClass);
    }

    private static boolean matchesAnyTag(ItemStack stack, List<TagKey<Item>> tags) {
        for (var tag : tags) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, id(namespace, path));
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
