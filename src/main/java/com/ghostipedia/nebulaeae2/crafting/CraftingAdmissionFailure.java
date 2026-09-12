package com.ghostipedia.nebulaeae2.crafting;

import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingSubmitResult;

public enum CraftingAdmissionFailure implements ICraftingSubmitResult {
    INSUFFICIENT_CWU;

    @Override
    public CraftingSubmitErrorCode errorCode() {
        return CraftingSubmitErrorCode.CPU_OFFLINE;
    }

    @Override
    public Object errorDetail() {
        return null;
    }

    @Override
    public ICraftingLink link() {
        return null;
    }
}
