package com.ghostipedia.nebulaeae2.activity;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import com.ghostipedia.nebulaeae2.locating.ProviderLocateMenu;
import appeng.api.networking.security.IActionHost;
import appeng.menu.me.crafting.CraftingCPUMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ActivityNetworking {
    private static Consumer<ActivityResponse> receiver = response -> {};
    private static final Map<ServerPlayer, Integer> REQUEST_TICKS = new WeakHashMap<>();

    private ActivityNetworking() {}

    public static void setReceiver(Consumer<ActivityResponse> value) { receiver = value; }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("nebulae_activity_2");
        registrar.playToServer(ActivityRequest.TYPE, ActivityRequest.CODEC, ActivityNetworking::request);
        registrar.playToClient(ActivityResponse.TYPE, ActivityResponse.CODEC,
                (payload, context) -> context.enqueueWork(() -> receiver.accept(payload)));
    }

    private static void request(ActivityRequest request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof CraftingCPUMenu menu)
                    || menu.containerId != request.menuId() || !menu.stillValid(player)
                    || !(menu instanceof ProviderLocateMenu locating)) return;
            var grid = locating.nebulae$locateGrid();
            if (grid == null || !(menu.getTarget() instanceof IActionHost host)
                    || host.getActionableNode() == null || host.getActionableNode().getGrid() != grid) return;
            var last = REQUEST_TICKS.get(player);
            if (last != null && player.tickCount - last < 2) return;
            REQUEST_TICKS.put(player, player.tickCount);
            var service = grid.getService(IActivityService.class);
            var data = service.archive().query(service.segments(), Math.clamp(request.tab(), 0, 2),
                    Math.clamp(request.page(), 0, Integer.MAX_VALUE / ActivityArchive.PAGE_SIZE - 1),
                    request.window(), request.search(), request.status(), request.mine() ? player.getUUID() : null,
                    request.selected(), Math.clamp(request.detailPage(), 0, Integer.MAX_VALUE / ActivityArchive.PAGE_SIZE - 1),
                    request.sort(), request.reverse(), player.registryAccess());
            PacketDistributor.sendToPlayer(player, new ActivityResponse(menu.containerId, request.serial(), data));
        });
    }
}
