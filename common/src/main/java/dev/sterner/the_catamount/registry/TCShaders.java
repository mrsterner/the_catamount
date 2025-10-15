package dev.sterner.the_catamount.registry;

import net.minecraft.client.renderer.ShaderInstance;

public class TCShaders {
    public static ShaderInstance rendertypeEntityDesaturated;

    public static void setRendertypeEntityDesaturated(ShaderInstance shader) {
        rendertypeEntityDesaturated = shader;
    }

    public static ShaderInstance getRendertypeEntityDesaturated() {
        return rendertypeEntityDesaturated;
    }

}