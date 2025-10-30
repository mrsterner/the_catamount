package dev.sterner.the_catamount.entity.task;

import com.google.common.collect.ImmutableMap;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.entity.brain.BrainUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class CatamountPounceTask<T extends CatamountEntity> extends Behavior<T> {
    private final int interval;
    private final double animationTimeOfAttack;
    private long animationTime = 0;
    private final int animationDuration;
    private final RunTask<T> runTask;
    private final FinishRunningTask<T> finishRunningTask;

    private boolean hasLeaped = false;

    public CatamountPounceTask(RunTask<T> runTask, FinishRunningTask<T> finishRunningTask,
                               int interval, int animationDuration, double animationTimeOfAttack) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT
        ));
        this.interval = interval;
        this.animationDuration = animationDuration;
        this.animationTimeOfAttack = animationTimeOfAttack;
        this.runTask = runTask;
        this.finishRunningTask = finishRunningTask;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T entity) {
        LivingEntity target = BrainUtils.getAttackTarget(entity);

        if (target == null) {
            return false;
        }

        double distance = entity.distanceToSqr(target);
        boolean isInRange = distance >= 9.0 && distance <= 36.0; // 3-6 blocks
        boolean targetIsSmall = target.getBbHeight() < 2.0;
        boolean notCrouched = !entity.isCrouched();
        boolean canSee = BehaviorUtils.canSee(entity, target);

        return notCrouched && targetIsSmall && isInRange && canSee;
    }

    @Override
    protected void start(ServerLevel level, T entity, long gameTime) {
        entity.setAggressive(true);
        LivingEntity target = BrainUtils.getAttackTarget(entity);

        if (target != null) {
            entity.setTarget(target);
            BehaviorUtils.lookAtEntity(entity, target);

            runTask.run(level, entity, gameTime);

            entity.setCrouched(true);

            animationTime = gameTime + animationDuration;
            hasLeaped = false;
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, T entity, long gameTime) {
        return gameTime < this.animationTime &&
                entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, T entity, long gameTime) {
        LivingEntity target = BrainUtils.getAttackTarget(entity);

        if (target == null) {
            return;
        }

        if (!hasLeaped && gameTime >= animationTime - animationDuration + 2) {
            performLeap(entity, target);
            hasLeaped = true;
        }

        if (animationTime == gameTime + animationTimeOfAttack) {
            entity.swing(InteractionHand.MAIN_HAND);

            if (entity.distanceToSqr(target) < 9.0) {
                entity.doHurtTarget(target);

                if (target.isAlive()) {
                    target.makeStuckInBlock(level.getBlockState(entity.blockPosition().above()),
                            new Vec3(0.25, 0.05, 0.25));
                }
            }
        }

        BehaviorUtils.lookAtEntity(entity, target);
    }

    @Override
    protected void stop(ServerLevel level, T entity, long gameTime) {
        entity.setAggressive(false);

        finishRunningTask.run(level, entity, gameTime);

        this.animationTime = 0;
        this.hasLeaped = false;

        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN,
                true, this.interval);
    }

    private void performLeap(T entity, LivingEntity target) {
        Vec3 direction = target.position()
                .subtract(entity.position())
                .normalize();

        double horizontalSpeed = 0.8;
        double verticalSpeed = 0.4;

        Vec3 leapVector = new Vec3(
                direction.x * horizontalSpeed,
                verticalSpeed,
                direction.z * horizontalSpeed
        );

        entity.setDeltaMovement(entity.getDeltaMovement().add(leapVector));
        entity.hasImpulse = true;
    }

    public interface RunTask<T> {
        void run(ServerLevel level, T entity, long time);
    }

    public interface FinishRunningTask<T> {
        void run(ServerLevel level, T entity, long time);
    }
}