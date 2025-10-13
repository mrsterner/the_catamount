package dev.sterner.the_catamount;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class TheCatamount {

    public static final String MOD_ID = "the_catamount";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {

    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
