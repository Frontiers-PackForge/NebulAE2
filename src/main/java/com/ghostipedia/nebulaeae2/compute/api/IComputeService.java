package com.ghostipedia.nebulaeae2.compute.api;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;

import java.util.UUID;

public interface IComputeService extends IGridService {

    boolean tryReserveCrafting(IGridNode node, UUID reservationId, long cwut);

    void finishCraftingAdmission(UUID reservationId);

    boolean canDispatchCrafting();

    boolean canAdmitCrafting();

    ComputeSnapshot snapshot();

    void invalidateReservations();

    void updateChannelOverloadReservation(long cwut);

    void clearChannelOverloadReservation();
}
