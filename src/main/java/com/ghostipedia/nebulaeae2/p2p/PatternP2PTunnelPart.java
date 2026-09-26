package com.ghostipedia.nebulaeae2.p2p;

import java.util.List;
import java.util.UUID;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.items.tools.MemoryCardItem;
import appeng.me.service.P2PService;
import appeng.parts.p2p.P2PModels;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.SettingsFrom;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class PatternP2PTunnelPart extends P2PTunnelPart<PatternP2PTunnelPart> {
    private static final P2PModels MODELS = new P2PModels(ResourceLocation.fromNamespaceAndPath("nebulaeae2", "part/p2p/p2p_tunnel_pattern"));
    private UUID identity = UUID.randomUUID();
    private boolean output;

    public PatternP2PTunnelPart(IPartItem<?> item) {
        super(item);
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(isPowered(), isActive());
    }

    public UUID identity() {
        return identity;
    }

    public PatternTunnelAddress address() {
        return new PatternTunnelAddress(getLevel().dimension(), getBlockEntity().getBlockPos(), getSide());
    }

    public PatternTunnelAddress destination() {
        return new PatternTunnelAddress(getLevel().dimension(), getBlockEntity().getBlockPos().relative(getSide()), getSide().getOpposite());
    }

    @Override
    public boolean isOutput() {
        return output;
    }

    @Override
    public List<PatternP2PTunnelPart> getOutputs() {
        return PatternTunnelRouting.outputs(this);
    }

    @Override
    public void setFrequency(short frequency) {
        if (getFrequency() != frequency) {
            identity = UUID.randomUUID();
        }
        super.setFrequency(frequency);
    }

    private void configure(short frequency, boolean output) {
        if (this.output != output) {
            identity = UUID.randomUUID();
        }
        this.output = output;
        var grid = getMainNode().getGrid();
        if (grid != null) {
            P2PService.get(grid).updateFreq(this, frequency);
        } else {
            setFrequency(frequency);
        }
        getHost().markForSave();
        getHost().markForUpdate();
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        tag.putUUID("nebulaePatternTunnelIdentity", identity);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        output = tag.getBoolean("output");
        identity = tag.hasUUID("nebulaePatternTunnelIdentity") ? tag.getUUID("nebulaePatternTunnelIdentity") : UUID.randomUUID();
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        return player.getMainHandItem().isEmpty() && joinInput(player.getOffhandItem(), player);
    }

    @Override
    public boolean onUseItemOn(ItemStack held, Player player, InteractionHand hand, Vec3 pos) {
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().isEmpty()) {
            return joinInput(held, player);
        }
        if (!(held.getItem() instanceof IMemoryCard card) || hand != InteractionHand.MAIN_HAND) {
            return super.onUseItemOn(held, player, hand, pos);
        }
        if (isClientSide()) {
            return true;
        }
        if (player.isShiftKeyDown()) {
            var grid = getMainNode().getGrid();
            if (grid == null) {
                return true;
            }
            short frequency = getFrequency();
            if (frequency == 0 || isOutput()) {
                do {
                    frequency = P2PService.get(grid).newFrequency();
                } while (PatternTunnelRouting.frequencyInUse(grid, frequency));
            }
            configure(frequency, false);
            MemoryCardItem.clearCard(held);
            held.set(AEComponents.EXPORTED_SETTINGS_SOURCE, getPartItem().asItem().getDescription());
            held.applyComponents(exportSettings(SettingsFrom.MEMORY_CARD));
            card.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
        } else if (validCard(held)) {
            configure(held.get(AEComponents.EXPORTED_P2P_FREQUENCY), true);
            card.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
        } else {
            card.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
        }
        return true;
    }

    private boolean joinInput(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof MemoryCardItem card)) {
            return false;
        }
        if (!isClientSide()) {
            if (validCard(stack)) {
                configure(stack.get(AEComponents.EXPORTED_P2P_FREQUENCY), false);
                card.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
            } else {
                card.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
            }
        }
        return true;
    }

    private boolean validCard(ItemStack stack) {
        var frequency = stack.get(AEComponents.EXPORTED_P2P_FREQUENCY);
        return frequency != null && frequency != 0 && stack.get(AEComponents.EXPORTED_P2P_TYPE) == getPartItem().asItem();
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, Player player) {
        var frequency = input.get(AEComponents.EXPORTED_P2P_FREQUENCY);
        if (frequency != null && frequency != 0 && frequency != getFrequency()) {
            configure(frequency, true);
        }
    }
}
