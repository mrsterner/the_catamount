package dev.sterner.the_catamount.registry.neoforge;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.registry.TCParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TCParticlesNeoForge {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, TheCatamount.MOD_ID);

    private static final Supplier<SimpleParticleType> SPIRIT_FACE_SUPPLIER =
            PARTICLE_TYPES.register("struggling_spirit", () -> new SimpleParticleType(false));

    public static void init(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }

    public static void assignParticles() {
        TCParticles.SPIRIT_FACE = SPIRIT_FACE_SUPPLIER.get();
    }
}