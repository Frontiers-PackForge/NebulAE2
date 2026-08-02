package com.ghostipedia.nebulaeae2.compute.api;

import appeng.api.networking.IGridNodeService;

import java.util.UUID;

public interface IComputeSource extends IGridNodeService {

    UUID sourceId();

    long installedCwut();

    long commitCwut(UUID gridLeaseId, long serverTick, long targetTotalCwut);
}
