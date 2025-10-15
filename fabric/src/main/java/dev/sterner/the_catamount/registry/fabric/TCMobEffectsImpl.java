package dev.sterner.the_catamount.registry.fabric;

import dev.sterner.the_catamount.TheCatamount;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class TCMobEffectsImpl {

    public static Holder<MobEffect> createHolder(String name, MobEffect effect){
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, TheCatamount.id(name), effect);
    }
}