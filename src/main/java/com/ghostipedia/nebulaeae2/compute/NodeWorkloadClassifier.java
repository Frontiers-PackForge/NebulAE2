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
        var representation = node.getVisualRepresentation();
        return requiresScheduledWorkGrant(
                owner.getClass(),
                representation == null ? null : representation.getId());
    }

    public static boolean requiresScheduledWorkGrant(Class<?> ownerType, ResourceLocation representation) {
        return IOBusPart.class.isAssignableFrom(ownerType)
                || AnnihilationPlanePart.class.isAssignableFrom(ownerType)
                || IOPortBlockEntity.class.isAssignableFrom(ownerType)
                || InterfaceLogicHost.class.isAssignableFrom(ownerType)
                || ACTIVE_FORMATION_PLANE.equals(representation);
    }
}
