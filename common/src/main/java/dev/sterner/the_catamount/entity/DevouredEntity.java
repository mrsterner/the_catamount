package dev.sterner.the_catamount.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;

public class DevouredEntity extends Vex {
    private static final int MAX_LIFETIME = 20 * 60 * 5;
    private int lifetimeTicks = 0;

    public DevouredEntity(EntityType<? extends Vex> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vex.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            lifetimeTicks++;

            if (level().isDay() && level().canSeeSky(blockPosition())) {
                despawnIntoSmoke();
                return;
            }

            if (lifetimeTicks >= MAX_LIFETIME) {
                despawnIntoSmoke();
            }
        }
    }

    private void despawnIntoSmoke() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    getX(), getY() + 0.5, getZ(),
                    10, 0.3, 0.3, 0.3, 0.02);
        }
        discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN) ||
                source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }
}