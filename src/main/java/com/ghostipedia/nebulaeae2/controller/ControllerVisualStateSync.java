package com.ghostipedia.nebulaeae2.controller;

import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.hooks.ticking.TickHandler;

import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

public final class ControllerVisualStateSync {

    private ControllerVisualStateSync() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ControllerVisualStateSync::onChunkSent);
    }

    public static void scheduleInitialReconciliation(ControllerBlockEntity controller) {
        if (!(controller.getLevel() instanceof ServerLevel level)) {
            return;
        }
        TickHandler.instance().addCallable(level, ignored -> reconcileForTrackingPlayers(controller, level));
    }

    private static void onChunkSent(ChunkWatchEvent.Sent event) {
        for (var blockEntity : event.getChunk().getBlockEntities().values()) {
            if (blockEntity instanceof ControllerBlockEntity controller && isLive(controller, event.getLevel())) {
                controller.updateState();
                event.getPlayer().connection.send(
                        new ClientboundBlockUpdatePacket(event.getLevel(), controller.getBlockPos()));
            }
        }
    }

    private static void reconcileForTrackingPlayers(ControllerBlockEntity controller, ServerLevel level) {
        if (!isLive(controller, level)) {
            return;
        }
        controller.updateState();
        var state = level.getBlockState(controller.getBlockPos());
        level.sendBlockUpdated(controller.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
    }

    private static boolean isLive(ControllerBlockEntity controller, ServerLevel level) {
        return !controller.isRemoved()
                && controller.getLevel() == level
                && level.getBlockEntity(controller.getBlockPos()) == controller;
    }
}
