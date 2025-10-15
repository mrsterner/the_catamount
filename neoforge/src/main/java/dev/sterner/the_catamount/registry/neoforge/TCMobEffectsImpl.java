package dev.sterner.the_catamount.registry.neoforge;

import dev.sterner.the_catamount.neoforge.TheCatamountNeoForge;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public class TCMobEffectsImpl {
    
    public static Holder<MobEffect> createHolder(String name, MobEffect effect){
        return TheCatamountNeoForge.MOB_EFFECTS.register(name, () -> effect);
    }
}