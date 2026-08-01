package com.ghostipedia.nebulaeae2.compute;

import appeng.api.networking.IGridNode;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.helpers.InterfaceLogicHost;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.automation.IOBusPart;
import net.minecraft.resources.ResourceLocation;

public final class NodeWorkloadClassifier {

    private static final ResourceLocation ACTIVE_FORMATION_PLANE = ResourceLocation.fromNamespaceAndPath(
            "extendedae", "active_formation_plane");

    private NodeWorkloadClassifier() {}

    public static boolean requiresScheduledWorkGrant(IGridNode node) {
        var owner = node.getOwner();
        return owner instanceof IOBusPart
                || owner instanceof AnnihilationPlanePart
                || owner instanceof IOPortBlockEntity
                || owner instanceof InterfaceLogicHost
                || isActiveFormationPlane(node);
    }

    private static boolean isActiveFormationPlane(IGridNode node) {
        var representation = node.getVisualRepresentation();
        return representation != null && representation.getId().equals(ACTIVE_FORMATION_PLANE);
    }
}
