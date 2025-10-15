package dev.sterner.the_catamount.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.sterner.the_catamount.mob_effects.BleedingEffect;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.awt.*;

public class TCMobEffects {

    @ExpectPlatform
    public static Holder<MobEffect> createHolder(String name, MobEffect effect){
        throw new AssertionError();
    }

    public static final Holder<MobEffect> BLEEDING = createHolder("bleeding", new BleedingEffect(MobEffectCategory.HARMFUL, new Color(150,50,50).getRGB()));

    public static void init(){

    }
}