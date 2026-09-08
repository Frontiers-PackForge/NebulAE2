package com.ghostipedia.nebulaeae2.blocking;

public enum ProviderBlockingMode {
    OFF,
    ANY_CONTENTS,
    PATTERN_INPUTS,
    NON_INPUT_CONTENTS;

    public boolean blocks(boolean recognizedInput, boolean exempt) {
        return !exempt && switch (this) {
            case OFF -> false;
            case ANY_CONTENTS -> true;
            case PATTERN_INPUTS -> recognizedInput;
            case NON_INPUT_CONTENTS -> !recognizedInput;
        };
    }

    public ProviderBlockingMode next(boolean backwards) {
        return values()[Math.floorMod(ordinal() + (backwards ? -1 : 1), values().length)];
    }
}
