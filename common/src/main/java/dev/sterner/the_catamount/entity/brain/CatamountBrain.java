package dev.sterner.the_catamount.entity.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.entity.task.CatamountPounceTask;
import dev.sterner.the_catamount.entity.task.CatamountSmoothLookAtTask;
import dev.sterner.the_catamount.entity.task.GeckoMeleeAttackTask;
import dev.sterner.the_catamount.registry.TCSensorTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Optional;

public class CatamountBrain {

    public static final List<SensorType<? extends Sensor<? super CatamountEntity>>> SENSORS = List.of(
            SensorType.NEAREST_PLAYERS,
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY,
            TCSensorTypes.CATAMOUNT_SENSOR
    );

    public static final List<MemoryModuleType<?>> MEMORIES = List.of(
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.AVOID_TARGET
    );

    public static Brain<?> makeBrain(CatamountEntity catamount, Dynamic<?> dynamic) {
        Brain.Provider<CatamountEntity> profile = Brain.provider(MEMORIES, SENSORS);
        Brain<CatamountEntity> brain = profile.makeBrain(dynamic);
        addCoreActivities(brain);
        addIdleActivities(brain);
        addFightActivities(catamount, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void addFightActivities(CatamountEntity catamount, Brain<CatamountEntity> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10,
                ImmutableList.of(
                        StopAttackingIfTargetInvalid.create(
                                entity -> !isPreferredAttackTarget(catamount, entity),
                                BrainUtils::setTargetInvalid,
                                false
                        ),

                        new CatamountSmoothLookAtTask(),

                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2F),

                        new CatamountPounceTask<>(
                                (level, entity, time) -> {
                                    entity.setAttacking(true);
                                },
                                (level, entity, time) -> {
                                    entity.setAttacking(false);
                                },
                                40,
                                20,
                                10
                        ),

                        new GeckoMeleeAttackTask<>(
                                (level, entity, time) -> {
                                    entity.setAttacking(true);
                                },
                                (level, entity, time) -> {
                                    entity.setAttacking(false);
                                },
                                20,
                                14,
                                7
                        )
                ), MemoryModuleType.ATTACK_TARGET);
    }

    private static void addIdleActivities(Brain<CatamountEntity> brain) {
        brain.addActivity(
                Activity.IDLE,
                10,
                ImmutableList.of(
                        StartAttacking.create(CatamountBrain::findNearestValidTarget),
                        new RunOne<>(
                                ImmutableList.of(
                                        Pair.of(SetWalkTargetFromLookTarget.create(0.6F, 3), 2),
                                        Pair.of(new DoNothing(30, 60), 1),
                                        Pair.of(RandomStroll.stroll(0.6F), 2)
                                ))
                        )

                );
    }

    private static void addCoreActivities(Brain<CatamountEntity> brain) {
        brain.addActivity(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new Swim(0.6f),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink()
                )
        );
    }

    public static void updateActivity(CatamountEntity catamount) {
        Brain<CatamountEntity> brain = catamount.getBrain();

        updateCrouchState(catamount);

        Activity currentActivity = brain.getActiveNonCoreActivity().orElse(null);
        Activity desiredActivity = getDesiredActivity(catamount);

        if (currentActivity != desiredActivity) {
            brain.setActiveActivityIfPossible(desiredActivity);
        }
    }

    private static void updateCrouchState(CatamountEntity catamount) {
        Optional<? extends LivingEntity> target = getAttackTarget(catamount);

        if (target.isPresent()) {
            LivingEntity entity = target.get();
            double distance = catamount.distanceToSqr(entity);

            boolean shouldCrouch = (entity instanceof Player && distance < 64.0) ||
                    (entity.getBbHeight() < 2.0 && distance < 36.0);

            catamount.setCrouched(shouldCrouch);
        } else {
            double speed = catamount.getDeltaMovement().horizontalDistance();
            if (speed < 0.1) {
                catamount.setCrouched(false);
            }
        }
    }

    private static Activity getDesiredActivity(CatamountEntity catamount) {
        if (catamount.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return Activity.FIGHT;
        }

        return Activity.IDLE;
    }

    private static boolean isPreferredAttackTarget(CatamountEntity catamount, LivingEntity target) {
        return getAttackTarget(catamount)
                .filter(preferredTarget -> preferredTarget == target)
                .isPresent();
    }

    private static Optional<? extends LivingEntity> getAttackTarget(CatamountEntity catamount) {
        Optional<LivingEntity> attackTarget = catamount.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (attackTarget.isPresent()) {
            return attackTarget;
        }

        return catamount.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }

    private static Optional<? extends LivingEntity> findNearestValidTarget(CatamountEntity catamount) {
        Brain<CatamountEntity> brain = catamount.getBrain();

        Optional<LivingEntity> currentTarget = brain.getMemory(MemoryModuleType.ATTACK_TARGET);
        if (currentTarget.isPresent() && isTargetStillValid(catamount, currentTarget.get())) {
            return currentTarget;
        }

        Optional<LivingEntity> nearestAttackable = brain.getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        if (nearestAttackable.isPresent() && isTargetStillValid(catamount, nearestAttackable.get())) {
            return nearestAttackable;
        }

        return brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .flatMap(entities -> entities.findClosest(entity ->
                        entity instanceof LivingEntity living &&
                                isValidPrey(living) &&
                                !isOwner(catamount, living) &&
                                catamount.hasLineOfSight(living)
                ));
    }

    private static boolean isTargetStillValid(CatamountEntity catamount, LivingEntity target) {
        return target.isAlive() &&
                catamount.distanceToSqr(target) < 1024.0 &&
                isValidPrey(target) &&
                !isOwner(catamount, target);
    }

    public static boolean isValidPrey(LivingEntity entity) {
        return entity instanceof Animal ||
                entity instanceof AbstractVillager ||
                entity instanceof Player;
    }

    private static boolean isOwner(CatamountEntity catamount, LivingEntity entity) {
        if (catamount.isFeedingFrenzy()) {
            return false;
        }

        if (entity instanceof Player player) {
            return catamount.getOwnerUUID()
                    .map(uuid -> uuid.equals(player.getUUID()))
                    .orElse(false);
        }
        return false;
    }
}