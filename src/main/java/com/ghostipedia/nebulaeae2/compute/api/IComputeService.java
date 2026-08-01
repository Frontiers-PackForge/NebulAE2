package com.ghostipedia.nebulaeae2.compute.api;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;

public interface IComputeService extends IGridService {

    long acquireUpTo(IGridNode node, long maximumCwut);

    default boolean tryAcquire(IGridNode node, long cwut) {
        return acquireUpTo(node, cwut) == cwut;
    }

    void chargeSynchronousDebt(IGridNode node, long cwu);

    ComputeSnapshot snapshot();

    void invalidateReservations();
}
