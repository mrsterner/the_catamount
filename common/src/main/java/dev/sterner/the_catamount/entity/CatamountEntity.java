package dev.sterner.the_catamount.entity;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.registry.TCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class CatamountEntity extends PathfinderMob implements GeoEntity {

    private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(CatamountEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(CatamountEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> ANIMALS_CONSUMED = SynchedEntityData.defineId(CatamountEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUMANOIDS_CONSUMED = SynchedEntityData.defineId(CatamountEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> FEEDING_FRENZY = SynchedEntityData.defineId(CatamountEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FRENZY_KILLS_REMAINING = SynchedEntityData.defineId(CatamountEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FULLY_MANIFESTED = SynchedEntityData.defineId(CatamountEntity.class, EntityDataSerializers.BOOLEAN);

    public CatamountEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.63F)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STAGE, 3);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(ANIMALS_CONSUMED, 0);
        builder.define(HUMANOIDS_CONSUMED, 0);
        builder.define(FEEDING_FRENZY, false);
        builder.define(FRENZY_KILLS_REMAINING, 0);
        builder.define(FULLY_MANIFESTED, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && isFullyManifested()) {
            long dayTime = level().getDayTime() % 24000;

            if (dayTime >= 23000 || dayTime < 1000) {
                despawnIntoWind();
            }
        }
    }

    private void despawnIntoWind() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    getX(), getY() + 1, getZ(),
                    30, 0.5, 0.5, 0.5, 0.1);

            serverLevel.playSound(null, blockPosition(),
                    SoundEvents.BREEZE_DEATH, SoundSource.HOSTILE, 1.0f, 1.0f);

            ServerPlayer owner = getOwnerPlayer();
            if (owner != null) {
                CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(owner);
                if (data.catamountUUID().isPresent() && data.catamountUUID().get().equals(getUUID())) {
                    CatamountPlayerDataAttachment.setData(owner, data.withCatamountUUID(Optional.empty()));
                }
            }
        }

        discard();
    }

    public void onConsumeAnimal() {
        entityData.set(ANIMALS_CONSUMED, entityData.get(ANIMALS_CONSUMED) + 1);
        updateDynamicAttributes();
    }

    public void onConsumeHumanoid() {
        entityData.set(HUMANOIDS_CONSUMED, entityData.get(HUMANOIDS_CONSUMED) + 1);
        updateDynamicAttributes();
    }

    private void updateDynamicAttributes() {
        double baseHP = switch (getStage()) {
            case 3 -> 50;
            case 4 -> 150;
            case 5 -> 300;
            default -> 50;
        };

        double hp = baseHP
                + getAnimalsConsumed() * (getStage() >= 4 ? 5 : 3)
                + getHumanoidsConsumed() * (getStage() >= 4 ? 10 : 5);

        if (getStage() == 5) hp = Math.min(hp, 500);

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        if (getHealth() > hp) setHealth((float) hp);

        int prevStage = getStage();

        if (getStage() == 3 && hp >= 200) setStage(4);
        else if (getStage() == 4 && hp >= 400) setStage(5);

        if (getStage() != prevStage) {
            updateAttributesForStage(getStage());
        }
    }

    private void updateAttributesForStage(int stage) {
        switch (stage) {
            case 3 -> {
                getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
                getAttribute(Attributes.ARMOR).setBaseValue(0);
                getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);
                getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5);
            }
            case 4 -> {
                getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8);
                getAttribute(Attributes.ARMOR).setBaseValue(5);
                getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(5);
                getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.75);
            }
            case 5 -> {
                getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10);
                getAttribute(Attributes.ARMOR).setBaseValue(10);
                getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(10);
                getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
            }
        }
        updateDynamicAttributes();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isWhiteAshWeapon(source)) {
            amount *= 1.5f;
            return super.hurt(source, amount);
        }

        if (source.is(DamageTypes.FALL)
                || source.is(DamageTypes.CACTUS)
                || source.is(DamageTypes.SWEET_BERRY_BUSH)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.DROWN)
                || (getStage() == 5 && (source.is(DamageTypes.LAVA) || source.is(DamageTypes.ON_FIRE)))) {

            if (source.is(DamageTypes.DROWN) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.LAVA))
                teleportOut();
            return false;
        }

        float resistance = switch (getStage()) {
            case 3 -> 0.8f;
            case 4 -> 0.6f;
            case 5 -> 0.4f;
            default -> 1.0f;
        };

        if (!isWhiteAshWeapon(source)) {
            amount *= resistance;
        }

        return super.hurt(source, amount);
    }

    private void teleportOut() {
        if (level().isClientSide) return;

        RandomSource random = getRandom();
        BlockPos currentPos = this.blockPosition();
        Level level = this.level();

        final int MAX_ATTEMPTS = 16;
        final int MAX_HORIZONTAL_DISTANCE = 12;
        final int MAX_VERTICAL_DISTANCE = 8;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int dx = random.nextIntBetweenInclusive(-MAX_HORIZONTAL_DISTANCE, MAX_HORIZONTAL_DISTANCE);
            int dy = random.nextIntBetweenInclusive(-MAX_VERTICAL_DISTANCE, MAX_VERTICAL_DISTANCE);
            int dz = random.nextIntBetweenInclusive(-MAX_HORIZONTAL_DISTANCE, MAX_HORIZONTAL_DISTANCE);

            BlockPos targetPos = currentPos.offset(dx, dy, dz);

            while (targetPos.getY() > level.getMinBuildHeight() && !level.getBlockState(targetPos.below()).isSolid()) {
                targetPos = targetPos.below();
            }

            AABB checkBox = this.getBoundingBox().move(
                    targetPos.getX() + 0.5 - this.getX(),
                    targetPos.getY() - this.getY(),
                    targetPos.getZ() + 0.5 - this.getZ()
            );

            if (level.noCollision(this, checkBox) && level.getBlockState(targetPos.below()).isSolid()) {
                this.teleportTo(targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1, getZ(), 20, 0.3, 0.5, 0.3, 0.02);
                    serverLevel.playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.8F + random.nextFloat() * 0.4F);
                }

                return;
            }
        }

        Vec3 fallback = position().add((random.nextDouble() - 0.5) * 4.0, 0, (random.nextDouble() - 0.5) * 4.0);
        this.teleportTo(fallback.x, fallback.y, fallback.z);
    }

    private boolean isWhiteAshWeapon(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity living) {
            if (living.getMainHandItem().has(TCDataComponents.WHITE_ASH_COATED)) {
                return Boolean.TRUE.equals(living.getMainHandItem().get(TCDataComponents.WHITE_ASH_COATED));
            }
        }
        return false;
    }

    public int getAnimalsConsumed() {
        return entityData.get(ANIMALS_CONSUMED);
    }

    public int getHumanoidsConsumed() {
        return entityData.get(HUMANOIDS_CONSUMED);
    }

    public void setAnimalsConsumed(int consumed){
        entityData.set(ANIMALS_CONSUMED, consumed);
    }

    public void increaseAnimalsConsumed(){
        setAnimalsConsumed(getAnimalsConsumed() + 1);
    }

    public void setHumanoidsConsumed(int consumed){
        entityData.set(HUMANOIDS_CONSUMED, consumed);
    }

    public void increaseHumanoidsConsumed(){
        setHumanoidsConsumed(getHumanoidsConsumed() + 1);
    }

    public int getStage() {
        return entityData.get(STAGE);
    }

    public Optional<UUID> getOwnerUUID() {
        return entityData.get(OWNER_UUID);
    }

    public void setStage(int stage) {
        entityData.set(STAGE, stage);
    }

    public void setOwnerUUID(Optional<UUID> ownerUUID) {
        entityData.set(OWNER_UUID, ownerUUID);
    }

    public boolean isFeedingFrenzy() {
        return entityData.get(FEEDING_FRENZY);
    }

    public void setFeedingFrenzy(boolean frenzy) {
        entityData.set(FEEDING_FRENZY, frenzy);
    }

    public int getFrenzyKillsRemaining() {
        return entityData.get(FRENZY_KILLS_REMAINING);
    }

    public void setFrenzyKillsRemaining(int remaining) {
        entityData.set(FRENZY_KILLS_REMAINING, remaining);
    }

    public boolean isFullyManifested() {
        return entityData.get(FULLY_MANIFESTED);
    }

    public void setFullyManifested(boolean manifested) {
        entityData.set(FULLY_MANIFESTED, manifested);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Stage", getStage());
        compound.putInt("AnimalsConsumed", getAnimalsConsumed());
        compound.putInt("HumanoidsConsumed", getHumanoidsConsumed());
        compound.putBoolean("FeedingFrenzy", isFeedingFrenzy());
        compound.putInt("FrenzyKillsRemaining", getFrenzyKillsRemaining());
        compound.putBoolean("FullyManifested", isFullyManifested());
        if (getOwnerUUID().isPresent()) {
            compound.putUUID("OwnerUUID", getOwnerUUID().get());
        }
    }


    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setStage(compound.getInt("Stage"));
        if (compound.hasUUID("OwnerUUID")) {
            setOwnerUUID(Optional.of(compound.getUUID("OwnerUUID")));
        } else {
            setOwnerUUID(Optional.empty());
        }
        setAnimalsConsumed(compound.getInt("AnimalsConsumed"));
        setHumanoidsConsumed(compound.getInt("HumanoidsConsumed"));
        setFeedingFrenzy(compound.getBoolean("FeedingFrenzy"));
        setFrenzyKillsRemaining(compound.getInt("FrenzyKillsRemaining"));
        setFullyManifested(compound.getBoolean("FullyManifested"));

        updateAttributesForStage(getStage());
    }

    @Override
    public void die(DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            CatamountPlayerDataAttachment.tellDeath(player, this);
        }

        ServerPlayer owner = getOwnerPlayer();
        if (owner != null) {
            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(owner);
            if (data.catamountUUID().isPresent() && data.catamountUUID().get().equals(getUUID())) {
                CatamountPlayerDataAttachment.setData(owner, data.withCatamountUUID(Optional.empty()));
            }
        }

        super.die(source);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);

        if (hurt && target instanceof LivingEntity living) {
            if (isFeedingFrenzy() && living.getHealth() <= 0) {
                onFrenzyKill(living);
            }
        }

        return hurt;
    }

    private void onFrenzyKill(LivingEntity victim) {
        if (level().isClientSide) return;

        int killsLeft = getFrenzyKillsRemaining();
        if (killsLeft <= 0) return;

        ServerPlayer owner = getOwnerPlayer();
        if (owner == null) return;

        int points = (int) victim.getMaxHealth();

        if (victim instanceof Animal) {
            increaseAnimalsConsumed();
        } else if (victim instanceof Villager || victim instanceof AbstractVillager) {
            increaseHumanoidsConsumed();
        }

        CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(owner);
        CatamountPlayerDataAttachment.setData(owner, data.addPoints(points));

        setFrenzyKillsRemaining(killsLeft - 1);

        if (getFrenzyKillsRemaining() <= 0) {
            exitFrenzyMode();
        }

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @Nullable
    private ServerPlayer getOwnerPlayer() {
        if (getOwnerUUID().isEmpty()) return null;

        if (level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(getOwnerUUID().get());
            if (entity instanceof ServerPlayer player) {
                return player;
            }
        }

        return null;
    }

    private void exitFrenzyMode() {
        setFeedingFrenzy(false);
        setFrenzyKillsRemaining(0);

        level().playSound(null, blockPosition(),
                SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    //------- GeckoLib -------
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("move.walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walking", 5, this::walkingAnimController));
    }

    protected <E extends CatamountEntity> PlayState walkingAnimController(final AnimationState<E> event) {
        if (event.isMoving())
            return event.setAndContinue(WALK_ANIM);

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
