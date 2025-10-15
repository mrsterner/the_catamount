package dev.sterner.the_catamount.registry.fabric;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.registry.TCParticles;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class TCParticlesFabric {

    public static final Registry<ParticleType<?>> PARTICLE_TYPES =
            BuiltInRegistries.PARTICLE_TYPE;

    public static void init() {
        TCParticles.SPIRIT_FACE = Registry.register(
                PARTICLE_TYPES,
                TheCatamount.id("struggling_spirit"),
                FabricParticleTypes.simple(false)
        );
    }
}