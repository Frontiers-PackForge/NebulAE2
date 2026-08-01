package com.ghostipedia.nebulaeae2.compute.network;

import com.ghostipedia.nebulaeae2.compute.api.ComputeSnapshot;

public interface NetworkStatusComputeExtension {

    ComputeSnapshot EMPTY_SNAPSHOT = new ComputeSnapshot(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    ComputeSnapshot nebulae$getComputeSnapshot();

    void nebulae$setComputeSnapshot(ComputeSnapshot snapshot);
}
