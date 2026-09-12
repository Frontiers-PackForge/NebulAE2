package com.ghostipedia.nebulaeae2.compute;

import com.ghostipedia.nebulaeae2.compute.api.IComputeSource;

import java.util.UUID;

public final class CreativeComputeSource implements IComputeSource {
    private final UUID sourceId = UUID.randomUUID();

    @Override
    public UUID sourceId() {
        return sourceId;
    }

    @Override
    public long installedCwut() {
        return 1_000_000;
    }

    @Override
    public long commitCwut(UUID gridLeaseId, long serverTick, long targetTotalCwut) {
        return Math.clamp(targetTotalCwut, 0, installedCwut());
    }
}
