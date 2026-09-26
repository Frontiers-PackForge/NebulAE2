package com.ghostipedia.nebulaeae2.activity;

import java.util.Set;
import java.util.UUID;
import appeng.api.networking.IGridService;

public interface IActivityService extends IGridService {
    ActivityArchive archive();
    Set<UUID> segments();
    UUID writableSegment();
}
