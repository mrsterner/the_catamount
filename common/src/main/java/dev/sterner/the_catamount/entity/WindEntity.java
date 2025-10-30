package dev.sterner.the_catamount.entity;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WindEntity extends Mob {
    private static final EntityDataAccessor<Boolean> AGGRESSIVE = SynchedEntityData.defineId(WindEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> TARGET_PLAYER = SynchedEntityData.defineId(WindEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final int MAX_LIFETIME = 20 * 30;
    private static final int AGGRESSIVE_LIFETIME = 20 * 10;
    private int lifetimeTicks = 0;
    private Vec3 windDirection;
    private int damageTimer = 0;

    public WindEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);

        this.noPhysics = true;

        double angle = level.random.nextDouble() * Math.PI * 2;
        this.windDirection = new Vec3(Math.cos(angle), 0, Math.sin(angle)).normalize();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AGGRESSIVE, false);
        builder.define(TARGET_PLAYER, Optional.empty());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    public void setAggressive(boolean aggressive) {
        entityData.set(AGGRESSIVE, aggressive);
    }

    public boolean isAggressive() {
        return entityData.get(AGGRESSIVE);
    }

    public void setTargetPlayer(UUID uuid) {
        entityData.set(TARGET_PLAYER, Optional.of(uuid));
    }

    public Optional<UUID> getTargetPlayer() {
        return entityData.get(TARGET_PLAYER);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            lifetimeTicks++;

            int maxLife = isAggressive() ? AGGRESSIVE_LIFETIME : MAX_LIFETIME;

            if (isAggressive() && getTargetPlayer().isPresent()) {
                ServerPlayer target = (ServerPlayer) level().getPlayerByUUID(getTargetPlayer().get());
                if (target != null && target.isAlive()) {
                    Vec3 toTarget = target.position().subtract(position()).normalize();
                    windDirection = windDirection.lerp(toTarget, 0.1);
                    setDeltaMovement(windDirection.scale(0.3));
                } else {
                    setDeltaMovement(windDirection.scale(0.1));
                }
            } else {
                setDeltaMovement(windDirection.scale(0.1));
            }

            move(MoverType.SELF, getDeltaMovement());

            List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(2.0));

            for (LivingEntity entity : nearby) {
                if (isAggressive() && damageTimer <= 0) {
                    float damage = 2.0f + random.nextFloat();

                    if (entity.getHealth() > damage) {
                        entity.hurt(damageSources().magic(), damage);

                        if (entity instanceof ServerPlayer player) {
                            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);
                            CatamountPlayerDataAttachment.setData(player, data.addPoints((int) damage));
                        }
                    } else {
                        entity.hurt(damageSources().magic(), 0.0f);
                    }

                    damageTimer = 20;
                } else {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false));
                }
            }

            if (damageTimer > 0) damageTimer--;

            if (lifetimeTicks >= maxLife) {
                discard();
            }
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
    public void readAdditionalSaveData(CompoundTag tag) {
        lifetimeTicks = tag.getInt("LifetimeTicks");
        setAggressive(tag.getBoolean("Aggressive"));
        if (tag.hasUUID("TargetPlayer")) {
            setTargetPlayer(tag.getUUID("TargetPlayer"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LifetimeTicks", lifetimeTicks);
        tag.putBoolean("Aggressive", isAggressive());
        getTargetPlayer().ifPresent(uuid -> tag.putUUID("TargetPlayer", uuid));
    }
}