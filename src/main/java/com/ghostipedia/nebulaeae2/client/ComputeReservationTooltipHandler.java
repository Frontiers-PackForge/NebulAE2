package com.ghostipedia.nebulaeae2.client;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.ghostipedia.nebulaeae2.channel.ChannelOverloadPolicy;
import com.ghostipedia.nebulaeae2.compute.ComputeTuning;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.parts.IPartItem;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageCells;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.blockentity.spatial.SpatialAnchorBlockEntity;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.parts.AEBasePart;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.automation.FormationPlanePart;
import appeng.parts.automation.UpgradeablePart;
import appeng.parts.networking.DenseCablePart;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.parts.reporting.AbstractReportingPart;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = NebulaeAE2.MODID, value = Dist.CLIENT)
public final class ComputeReservationTooltipHandler {

    private static final Set<String> SUPPORTED_NAMESPACES = Set.of("ae2", "extendedae", "megacells");
    private static final ResourceLocation WIRELESS_ACCESS_POINT = id("ae2", "wireless_access_point");
    private static final ResourceLocation WIRELESS_BOOSTER = id("ae2", "wireless_booster");
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
    private static final Set<ResourceLocation> INTERFACE_FALLBACKS = Set.of(
            id("extendedae", "ex_interface"),
            id("extendedae", "ex_interface_part"),
            id("extendedae", "oversize_interface"),
            id("extendedae", "oversize_interface_part"),
            id("extendedae", "ex_emc_interface"),
            id("extendedae", "ex_emc_interface_part"),
            id("megacells", "mega_interface"),
            id("megacells", "cable_mega_interface"),
            id("megacells", "mega_emc_interface"),
            id("megacells", "cable_mega_emc_interface"));
    private static final Set<ResourceLocation> CRAFTING_PROVIDER_FALLBACKS = Set.of(
            id("extendedae", "ex_pattern_provider"),
            id("extendedae", "ex_pattern_provider_part"),
            id("megacells", "mega_pattern_provider"),
            id("megacells", "cable_mega_pattern_provider"));
    private static final Set<ResourceLocation> STORAGE_PROVIDER_FALLBACKS = Set.of(
            id("ae2", "drive"),
            id("ae2", "chest"),
            id("extendedae", "ex_drive"),
            id("megacells", "cell_dock"));
    private static final Set<ResourceLocation> CHANNEL_DEVICE_FALLBACKS = Set.of(
            id("megacells", "cell_dock"),
            id("megacells", "decompression_module"),
            id("megacells", "mega_crafting_unit"),
            id("megacells", "mega_crafting_accelerator"),
            id("megacells", "mega_crafting_monitor"),
            id("megacells", "1m_crafting_storage"),
            id("megacells", "4m_crafting_storage"),
            id("megacells", "16m_crafting_storage"),
            id("megacells", "64m_crafting_storage"),
            id("megacells", "256m_crafting_storage"),
            id("extendedae", "assembler_matrix_crafter"),
            id("extendedae", "assembler_matrix_frame"),
            id("extendedae", "assembler_matrix_glass"),
            id("extendedae", "assembler_matrix_pattern"),
            id("extendedae", "assembler_matrix_speed"),
            id("extendedae", "assembler_matrix_wall"));
    private static final Set<ResourceLocation> NETWORK_NODE_FALLBACKS = Set.of(
            id("extendedae", "crystal_assembler"),
            id("extendedae", "ex_drive"),
            id("extendedae", "ex_interface"),
            id("extendedae", "oversize_interface"),
            id("extendedae", "ex_io_port"),
            id("extendedae", "ex_molecular_assembler"),
            id("extendedae", "ex_pattern_provider"),
            id("extendedae", "ex_emc_interface"),
            id("extendedae", "assembler_matrix_crafter"),
            id("extendedae", "assembler_matrix_frame"),
            id("extendedae", "assembler_matrix_glass"),
            id("extendedae", "assembler_matrix_pattern"),
            id("extendedae", "assembler_matrix_speed"),
            id("extendedae", "assembler_matrix_wall"),
            id("megacells", "mega_interface"),
            id("megacells", "mega_pattern_provider"),
            id("megacells", "mega_emc_interface"),
            id("megacells", "mega_energy_cell"),
            id("megacells", "cell_dock"),
            id("megacells", "decompression_module"),
            id("megacells", "mega_crafting_unit"),
            id("megacells", "mega_crafting_accelerator"),
            id("megacells", "mega_crafting_monitor"),
            id("megacells", "1m_crafting_storage"),
            id("megacells", "4m_crafting_storage"),
            id("megacells", "16m_crafting_storage"),
            id("megacells", "64m_crafting_storage"),
            id("megacells", "256m_crafting_storage"));
    private static final Map<Item, ComputeCostProfile> PROFILE_CACHE = new IdentityHashMap<>();

    private ComputeReservationTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!SUPPORTED_NAMESPACES.contains(itemId.getNamespace())) {
            return;
        }

        var profile = PROFILE_CACHE.computeIfAbsent(stack.getItem(), item -> classify(stack, itemId));
        if (!profile.indexSource() && StorageCells.isCellHandled(stack)) {
            profile = profile.withIndexSource();
        }
        long baseReservation = profile.baseReservation();
        var tooltip = event.getToolTip();
        if (baseReservation > 0) {
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.base_reservation",
                    baseReservation).withStyle(ChatFormatting.AQUA));
        }
        if (!Screen.hasShiftDown()) {
            if (profile.networkNode() || profile.channelDevice() || profile.storageProvider()
                    || profile.craftingProvider() || profile.wirelessAccessPoint() || profile.wirelessBooster()
                    || profile.interfaceStocking() || profile.indexSource() || profile.cableRating() > 0) {
                tooltip.add(Component.translatable("tooltip.nebulaeae2.compute.hold_shift")
                        .withStyle(ChatFormatting.GRAY));
            }
            return;
        }
        appendPassiveReservation(event, profile);
        appendDynamicCosts(event, profile);
    }

    private static void appendPassiveReservation(ItemTooltipEvent event, ComputeCostProfile profile) {
        var tooltip = event.getToolTip();
        if (profile.storageProvider()) {
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.storage_provider",
                    ComputeTuning.STORAGE_PROVIDER_RESERVATION).withStyle(ChatFormatting.GRAY));
        }
        if (profile.craftingProvider()) {
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.crafting_provider",
                    ComputeTuning.CRAFTING_PROVIDER_RESERVATION).withStyle(ChatFormatting.GRAY));
        }
        if (profile.channelDevice()) {
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.device_scaling_base",
                    ComputeTuning.BASE_CWU_COST,
                    ComputeTuning.CHANNEL_DEVICE_SCALE_GROUP).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.device_scaling_bands",
                    ComputeTuning.BASE_CWU_COST,
                    ComputeTuning.CHANNEL_DEVICE_SCALE_GROUP).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void appendDynamicCosts(ItemTooltipEvent event, ComputeCostProfile profile) {
        var tooltip = event.getToolTip();
        if (profile.wirelessAccessPoint() || profile.wirelessBooster()) {
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.wireless_booster",
                    ComputeTuning.WIRELESS_BOOSTER_RESERVATION).withStyle(ChatFormatting.GRAY));
        }
        if (profile.interfaceStocking()) {
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.interface_stocking",
                    ComputeTuning.INTERFACE_STOCKING_GROUP_RESERVATION,
                    ComputeTuning.INTERFACE_STOCKING_SLOTS_PER_GROUP).withStyle(ChatFormatting.GRAY));
        }
        if (profile.indexSource()) {
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.storage_index",
                    ComputeTuning.INDEX_KEY_GROUP_RESERVATION,
                    ComputeTuning.INDEX_KEYS_PER_GROUP).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.storage_index_detail").withStyle(ChatFormatting.GRAY));
        }
        if (profile.cableRating() > 0) {
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.channel_rating",
                    profile.cableRating()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.channel_tax",
                    ComputeTuning.BASE_CWU_COST,
                    profile.overloadStep()).withStyle(ChatFormatting.GRAY));
        }
        if (profile.networkNode()) {
            tooltip.add(Component.translatable(
                    "tooltip.nebulaeae2.compute.physical_links",
                    ComputeTuning.PHYSICAL_LINK_GROUP_RESERVATION,
                    ComputeTuning.PHYSICAL_LINKS_PER_GROUP).withStyle(ChatFormatting.GRAY));
        }
    }

    private static ComputeCostProfile classify(ItemStack stack, ResourceLocation itemId) {
        Class<?> ownerType = null;
        boolean part = stack.getItem() instanceof IPartItem<?>;
        if (stack.getItem() instanceof IPartItem<?> partItem) {
            ownerType = partItem.getPartClass();
        } else if (stack.getItem() instanceof BlockItem blockItem) {
            ownerType = resolveBlockEntityType(blockItem);
        }

        boolean interfaceStocking = isType(ownerType, InterfaceLogicHost.class)
                || INTERFACE_FALLBACKS.contains(itemId)
                || matchesAnyTag(stack, INTERFACE_TAGS);
        boolean craftingProvider = isType(ownerType, ICraftingProvider.class)
                || isType(ownerType, PatternProviderLogicHost.class)
                || CRAFTING_PROVIDER_FALLBACKS.contains(itemId)
                || matchesAnyTag(stack, CRAFTING_PROVIDER_TAGS);
        boolean storageProvider = isType(ownerType, IStorageProvider.class)
                || isType(ownerType, DriveBlockEntity.class)
                || isType(ownerType, MEChestBlockEntity.class)
                || STORAGE_PROVIDER_FALLBACKS.contains(itemId);
        boolean channelDevice = isChannelledPart(ownerType)
                || isChannelledBlock(ownerType)
                || interfaceStocking
                || craftingProvider
                || STORAGE_PROVIDER_FALLBACKS.contains(itemId)
                || CHANNEL_DEVICE_FALLBACKS.contains(itemId);
        boolean cable = part && isType(ownerType, ICablePart.class);
        int cableRating = cable ?
                (isType(ownerType, DenseCablePart.class) ?
                        ChannelOverloadPolicy.DENSE_RATING : ChannelOverloadPolicy.STANDARD_RATING) : 0;
        boolean formationPlane = isType(ownerType, FormationPlanePart.class);
        boolean indexSource = storageProvider && !formationPlane;
        boolean networkNode = isNetworkNode(ownerType)
                || NETWORK_NODE_FALLBACKS.contains(itemId);
        boolean wirelessAccessPoint = itemId.equals(WIRELESS_ACCESS_POINT)
                || isType(ownerType, WirelessAccessPointBlockEntity.class);
        return new ComputeCostProfile(
                networkNode,
                channelDevice,
                storageProvider,
                craftingProvider,
                interfaceStocking,
                indexSource,
                wirelessAccessPoint,
                itemId.equals(WIRELESS_BOOSTER),
                cableRating,
                cableRating == 0 ? 0 : ChannelOverloadPolicy.overloadStep(cableRating));
    }

    private static Class<?> resolveBlockEntityType(BlockItem blockItem) {
        if (!(blockItem.getBlock() instanceof AEBaseEntityBlock<?> entityBlock)) {
            return null;
        }
        try {
            var blockEntityType = entityBlock.getBlockEntityType();
            if (blockEntityType == null) {
                return null;
            }
            BlockEntity blockEntity = blockEntityType.create(BlockPos.ZERO, blockItem.getBlock().defaultBlockState());
            return blockEntity == null ? null : blockEntity.getClass();
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static boolean isChannelledPart(Class<?> ownerType) {
        return isType(ownerType, UpgradeablePart.class)
                || isType(ownerType, AnnihilationPlanePart.class)
                || isType(ownerType, P2PTunnelPart.class)
                || isType(ownerType, AbstractReportingPart.class)
                || isType(ownerType, InterfaceLogicHost.class)
                || isType(ownerType, PatternProviderLogicHost.class);
    }

    private static boolean isChannelledBlock(Class<?> ownerType) {
        return isType(ownerType, DriveBlockEntity.class)
                || isType(ownerType, MEChestBlockEntity.class)
                || isType(ownerType, IOPortBlockEntity.class)
                || isType(ownerType, WirelessAccessPointBlockEntity.class)
                || isType(ownerType, InterfaceLogicHost.class)
                || isType(ownerType, PatternProviderLogicHost.class)
                || isType(ownerType, CraftingBlockEntity.class)
                || isType(ownerType, SpatialAnchorBlockEntity.class)
                || isType(ownerType, SpatialIOPortBlockEntity.class)
                || isType(ownerType, SpatialPylonBlockEntity.class);
    }

    private static boolean isNetworkNode(Class<?> ownerType) {
        return isType(ownerType, AEBasePart.class)
                || isType(ownerType, IInWorldGridNodeHost.class);
    }

    private static boolean isType(Class<?> ownerType, Class<?> expectedType) {
        return ownerType != null && expectedType.isAssignableFrom(ownerType);
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

    private record ComputeCostProfile(
            boolean networkNode,
            boolean channelDevice,
            boolean storageProvider,
            boolean craftingProvider,
            boolean interfaceStocking,
            boolean indexSource,
            boolean wirelessAccessPoint,
            boolean wirelessBooster,
            int cableRating,
            int overloadStep) {

        private long baseReservation() {
            long reservation = channelDevice ? ComputeTuning.CHANNEL_DEVICE_RESERVATION : 0;
            if (storageProvider) {
                reservation += ComputeTuning.STORAGE_PROVIDER_RESERVATION;
            }
            if (craftingProvider) {
                reservation += ComputeTuning.CRAFTING_PROVIDER_RESERVATION;
            }
            return reservation;
        }

        private ComputeCostProfile withIndexSource() {
            return new ComputeCostProfile(
                    networkNode,
                    channelDevice,
                    storageProvider,
                    craftingProvider,
                    interfaceStocking,
                    true,
                    wirelessAccessPoint,
                    wirelessBooster,
                    cableRating,
                    overloadStep);
        }
    }
}
