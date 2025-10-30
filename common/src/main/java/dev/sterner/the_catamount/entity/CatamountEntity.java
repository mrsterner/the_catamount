package dev.sterner.the_catamount.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.entity.brain.CatamountBrain;
import dev.sterner.the_catamount.registry.TCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Locale;
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
    private static final EntityDataAccessor<Boolean> IS_CROUCHED = SynchedEntityData.defineId(CatamountEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(CatamountEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<AttackType> ATTACK_TYPE = SynchedEntityData.defineId(CatamountEntity.class, TheCatamount.ATTACK_TYPE);

    public CatamountEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothCatamountMoveControl(this,10, 1f);

    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ARMOR_TOUGHNESS, 0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
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
        builder.define(IS_CROUCHED, false);
        builder.define(IS_ATTACKING, false);
        builder.define(ATTACK_TYPE, AttackType.NONE);
    }

    public boolean isAttacking() {
        return entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        entityData.set(IS_ATTACKING, attacking);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("catamountBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().popPush("catamountActivityUpdate");
        CatamountBrain.updateActivity(this);
        this.level().getProfiler().pop();
    }

    @Override
    protected @NotNull Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CatamountBrain.makeBrain(this, dynamic);
    }

    protected Brain.@NotNull Provider<CatamountEntity> brainProvider() {
        return Brain.provider(CatamountBrain.MEMORIES, CatamountBrain.SENSORS);
    }


    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Brain<CatamountEntity> getBrain() {
        return (Brain<CatamountEntity>) super.getBrain();
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

    public boolean isActuallyMoving(CatamountEntity entity) {
        Vec3 m = entity.getDeltaMovement();
        return m.horizontalDistanceSqr() > 0.0001;
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

    public void updateDynamicAttributes() {
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

        //TODO reenable when more stages exist
        /*
        if (getStage() == 3 && hp >= 200) setStage(4);
        else if (getStage() == 4 && hp >= 400) setStage(5);

        if (getStage() != prevStage) {
            updateAttributesForStage(getStage());
        }

         */
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

    public boolean isCrouched() {
        return entityData.get(IS_CROUCHED);
    }

    public void setCrouched(boolean crouched) {
        entityData.set(IS_CROUCHED, crouched);
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
        compound.putBoolean("IsCrouched", isCrouched());
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
        setCrouched(compound.getBoolean("IsCrouched"));

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
            boolean isPounce = entityData.get(ATTACK_TYPE) == AttackType.POUNCE;
            triggerAttackAnimation(living, isPounce);

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
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation IDLE_CROUCHED = RawAnimation.begin().thenLoop("idle_crouched");
    protected static final RawAnimation MOVEMENT = RawAnimation.begin().thenLoop("movement");
    protected static final RawAnimation MOVEMENT_QUAD = RawAnimation.begin().thenLoop("movement_quad");
    protected static final RawAnimation RUNNING = RawAnimation.begin().thenLoop("running");
    protected static final RawAnimation POUNCE = RawAnimation.begin().thenPlay("pounce");
    protected static final RawAnimation ATTACK_CLAW_LEFT = RawAnimation.begin().thenPlay("attack_claw_left");
    protected static final RawAnimation ATTACK_CLAW_LEFT_CROUCHED = RawAnimation.begin().thenPlay("attack_claw_left_crouched");
    protected static final RawAnimation ATTACK_CLAW_RIGHT_CROUCHED = RawAnimation.begin().thenPlay("attack_claw_right_crouched");



    public enum AttackType implements StringRepresentable {
        NONE,
        POUNCE,
        CLAW_LEFT,
        CLAW_RIGHT;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Codec<AttackType> CODEC = StringRepresentable.fromEnum(AttackType::values);

        public static final StreamCodec<RegistryFriendlyByteBuf, AttackType> STREAM_CODEC =
                StreamCodec.of(
                        (buf, value) -> buf.writeUtf(value.getSerializedName()),
                        buf -> AttackType.valueOf(buf.readUtf().toUpperCase(Locale.ROOT))
                );

    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementController));
        controllers.add(new AnimationController<>(this, "attack", 0, this::attackController));
    }

    protected <E extends CatamountEntity> PlayState movementController(final AnimationState<E> event) {
        if (entityData.get(IS_ATTACKING)) {
            return PlayState.CONTINUE;
        }

        if (isActuallyMoving(event.getAnimatable())) {
            double speed = this.getDeltaMovement().horizontalDistance();

            if (isCrouched()) {
                if (speed > 0.12) {
                    return event.setAndContinue(RUNNING);
                } else {
                    return event.setAndContinue(MOVEMENT_QUAD);
                }
            } else {
                return event.setAndContinue(MOVEMENT);
            }
        } else {
            if (isCrouched()) {
                return event.setAndContinue(IDLE_CROUCHED);
            } else {
                return event.setAndContinue(IDLE);
            }
        }
    }

    protected <E extends CatamountEntity> PlayState attackController(final AnimationState<E> event) {
        if (!isAttacking()) {
            entityData.set(ATTACK_TYPE, AttackType.NONE);
            return PlayState.STOP;
        }

        if (entityData.get(ATTACK_TYPE) == AttackType.NONE) {
            return PlayState.STOP;
        }

        return switch (entityData.get(ATTACK_TYPE)) {
            case POUNCE -> event.setAndContinue(POUNCE);
            case CLAW_LEFT -> isCrouched() ?
                    event.setAndContinue(ATTACK_CLAW_LEFT_CROUCHED) :
                    event.setAndContinue(ATTACK_CLAW_LEFT);
            case CLAW_RIGHT -> event.setAndContinue(ATTACK_CLAW_RIGHT_CROUCHED);
            default -> PlayState.STOP;
        };
    }


    public void triggerAttackAnimation(LivingEntity target, boolean isPounce) {
        setAttacking(true);

        if (isPounce) {
            entityData.set(ATTACK_TYPE, AttackType.POUNCE);
        } else if (isCrouched()) {
            entityData.set(ATTACK_TYPE, entityData.get(ATTACK_TYPE) == AttackType.CLAW_LEFT ? AttackType.CLAW_RIGHT : AttackType.CLAW_LEFT);
        } else {
            entityData.set(ATTACK_TYPE, AttackType.CLAW_LEFT);
        }
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}