package dev.sterner.the_catamount.fabric;

import dev.sterner.the_catamount.TheCatamount;
import net.fabricmc.api.ModInitializer;

/**
 * This class is the entrypoint for the mod on the Fabric platform.
 */
public class TheCatamountFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        TheCatamount.init();
    }
}
