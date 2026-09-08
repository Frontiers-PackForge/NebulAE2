package com.ghostipedia.nebulaeae2.locating;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;
import java.util.WeakHashMap;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.menu.AEBaseMenu;
import appeng.parts.AEBasePart;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ProviderLocator {
    private static final Map<ServerPlayer, Integer> LAST_REQUEST = new WeakHashMap<>();

    private ProviderLocator() {}

    public static void handle(LocateProviderRequest request, IPayloadContext context) {
        context.enqueueWork(() -> locate(request, (ServerPlayer) context.player()));
    }

    private static void locate(LocateProviderRequest request, ServerPlayer player) {
        if (!(player.containerMenu instanceof AEBaseMenu menu) || menu.containerId != request.menuId()
                || !(menu instanceof ProviderLocateMenu locating) || !menu.stillValid(player)) {
            return;
        }
        var last = LAST_REQUEST.get(player);
        if (last != null && player.tickCount - last < 10) {
            return;
        }
        LAST_REQUEST.put(player, player.tickCount);
        var grid = locating.nebulae$locateGrid();
        if (grid == null || !(menu.getTarget() instanceof IActionHost host) || host.getActionableNode() == null
                || host.getActionableNode().getGrid() != grid || !locating.nebulae$canLocate(request.key())) {
            return;
        }
        var nearest = new TreeSet<BlockPos>(Comparator.comparingDouble((BlockPos pos) -> pos.distSqr(player.blockPosition()))
                .thenComparingLong(BlockPos::asLong));
        for (var node : grid.getNodes()) {
            if (node.getLevel() != player.serverLevel() || !node.isActive()) {
                continue;
            }
            var provider = node.getService(ICraftingProvider.class);
            if (provider == null || provider.getAvailablePatterns().stream().noneMatch(pattern -> {
                for (var output : pattern.getOutputs()) {
                    if (output.what().equals(request.key())) {
                        return true;
                    }
                }
                return false;
            })) {
                continue;
            }
            var owner = node.getOwner();
            BlockEntity block = owner instanceof BlockEntity entity ? entity : owner instanceof AEBasePart part ? part.getBlockEntity() : null;
            if (block != null && !block.isRemoved() && block.getLevel() == player.serverLevel()) {
                nearest.add(block.getBlockPos().immutable());
                if (nearest.size() > ProviderLocations.MAX_POSITIONS) {
                    nearest.pollLast();
                }
            }
        }
        PacketDistributor.sendToPlayer(player, new ProviderLocations(menu.containerId, player.level().dimension().location(), nearest.stream().toList()));
    }
}
