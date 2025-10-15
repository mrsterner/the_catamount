package dev.sterner.the_catamount.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LightOrbEntity extends Entity {
    private static final int MAX_LIFETIME = 20 * 60;
    private int lifetimeTicks = 0;
    private Vec3 glideDirection;
    private float height;

    public LightOrbEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;

        double angle = level.random.nextDouble() * Math.PI * 2;
        this.glideDirection = new Vec3(Math.cos(angle), 0, Math.sin(angle)).normalize();
        this.height = (float) getY();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            lifetimeTicks++;

            float bobbing = (float) Math.sin(lifetimeTicks * 0.05) * 0.05f;
            Vec3 movement = glideDirection.scale(0.05);
            movement = movement.add(0, bobbing, 0);

            setDeltaMovement(movement);
            move(MoverType.SELF, getDeltaMovement());

            if (lifetimeTicks % 100 == 0) {
                height -= 0.5f;
                setPos(getX(), height, getZ());
            }

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        getX(), getY(), getZ(),
                        2, 0.1, 0.1, 0.1, 0.01);
            }

            if (lifetimeTicks >= MAX_LIFETIME || getY() < level().getMinBuildHeight() + 10) {
                discard();
            }
        } else {
            level().addParticle(ParticleTypes.GLOW,
                    getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifetimeTicks = tag.getInt("LifetimeTicks");
        height = tag.getFloat("Height");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LifetimeTicks", lifetimeTicks);
        tag.putFloat("Height", height);
    }
}