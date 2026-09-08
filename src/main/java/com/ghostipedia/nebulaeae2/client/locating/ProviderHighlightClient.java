package com.ghostipedia.nebulaeae2.client.locating;

import java.util.List;
import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.ghostipedia.nebulaeae2.locating.LocateProviderRequest;
import com.ghostipedia.nebulaeae2.locating.ProviderLocations;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = NebulaeAE2.MODID, value = Dist.CLIENT)
public final class ProviderHighlightClient {
    private static final long DURATION_NANOS = 20_000_000_000L;
    private static final long BLINK_PERIOD_NANOS = 1_000_000_000L;
    private static List<BlockPos> positions = List.of();
    private static ClientLevel highlightedLevel;
    private static long startedAt;

    private ProviderHighlightClient() {}

    @SubscribeEvent
    public static void disconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        positions = List.of();
        highlightedLevel = null;
    }

    public static void receive(ProviderLocations payload) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || minecraft.player.containerMenu.containerId != payload.menuId()
                || !minecraft.level.dimension().location().equals(payload.dimension())) {
            return;
        }
        positions = payload.positions();
        highlightedLevel = minecraft.level;
        startedAt = System.nanoTime();
        if (positions.isEmpty()) {
            minecraft.player.displayClientMessage(Component.translatable("gui.nebulaeae2.locating.none"), true);
        } else {
            minecraft.player.closeContainer();
            var nearest = positions.getFirst();
            minecraft.player.displayClientMessage(Component.translatable("gui.nebulaeae2.locating.found", positions.size(),
                    nearest.getX(), nearest.getY(), nearest.getZ()), false);
        }
    }

    @SubscribeEvent
    public static void onClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 0 || !Screen.hasShiftDown()
                || !(event.getScreen() instanceof CraftConfirmScreen || event.getScreen() instanceof CraftingCPUScreen<?>)) {
            return;
        }
        var screen = (AEBaseScreen<?>) event.getScreen();
        var stack = screen.getStackUnderMouse(event.getMouseX(), event.getMouseY());
        if (stack != null) {
            PacketDistributor.sendToServer(new LocateProviderRequest(screen.getMenu().containerId, stack.stack().what()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || positions.isEmpty()) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        long elapsed = System.nanoTime() - startedAt;
        if (minecraft.level != highlightedLevel || elapsed >= DURATION_NANOS) {
            positions = List.of();
            highlightedLevel = null;
            return;
        }
        if (elapsed % BLINK_PERIOD_NANOS >= BLINK_PERIOD_NANOS / 2) {
            return;
        }
        var pose = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        var buffers = minecraft.renderBuffers().bufferSource();
        var vertices = buffers.getBuffer(ProviderHighlightRenderType.OUTLINE);
        for (var pos : positions) {
            LevelRenderer.renderLineBox(pose, vertices, new AABB(pos).inflate(0.01), 1F, 0F, 0F, 0.9F);
        }
        buffers.endBatch(ProviderHighlightRenderType.OUTLINE);
        pose.popPose();
    }
}
