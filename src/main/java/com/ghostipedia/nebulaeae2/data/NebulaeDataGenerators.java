package com.ghostipedia.nebulaeae2.data;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.providers.ProviderType;

public final class NebulaeDataGenerators {
    private NebulaeDataGenerators() {}

    public static void init(GTRegistrate registrate) {
        registrate.addDataGenerator(ProviderType.LANG, NebulaeLanguageProvider::addTranslations);
    }
}
