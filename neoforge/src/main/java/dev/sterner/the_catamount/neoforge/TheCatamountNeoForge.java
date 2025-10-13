package com.example.examplemod.neoforge;

import dev.sterner.the_catamount.TheCatamount;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Main class for the mod on the NeoForge platform.
 */
@Mod(TheCatamount.MOD_ID)
public class TheCatamountNeoForge {
    public ExampleModNeoForge(IEventBus eventBus) {
        TheCatamount.init();
    }
}
