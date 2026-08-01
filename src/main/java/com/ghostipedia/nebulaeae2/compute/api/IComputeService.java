package com.ghostipedia.nebulaeae2.compute.api;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;

public interface IComputeService extends IGridService {

    long acquireUpTo(IGridNode node, long maximumCwut);

    long acquireWholeUnitsUpTo(IGridNode node, long maximumUnits, long cwutPerUnit);

    default boolean tryAcquire(IGridNode node, long cwut) {
        if (cwut == 0) {
            return true;
        }
        return acquireWholeUnitsUpTo(node, 1, cwut) == 1;
    }

    void chargeSynchronousDebt(IGridNode node, long cwu);

    ComputeSnapshot snapshot();

    void invalidateReservations();

    void updateChannelOverloadReservation(long cwut);

    void clearChannelOverloadReservation();
}
