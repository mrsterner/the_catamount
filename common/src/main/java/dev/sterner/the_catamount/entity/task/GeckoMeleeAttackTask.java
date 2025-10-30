package dev.sterner.the_catamount.entity.task;


import com.google.common.collect.ImmutableMap;
import dev.sterner.the_catamount.entity.brain.BrainUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GeckoMeleeAttackTask<T extends Mob> extends Behavior<T> {
    private final int interval;
    private final double animationTimeOfAttack;
    private long animationTime = 0;
    private final int animationDuration;
    private final RunTask<T> runTask;
    private final FinishRunningTask<T> finishRunningTask;

    public GeckoMeleeAttackTask(RunTask<T> runTask, FinishRunningTask<T> finishRunningTask, int interval, double animationTime, double animationTimeOfAttack) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT
        ));
        this.interval = interval;
        this.animationDuration = (int) animationTime;
        this.animationTimeOfAttack = animationTimeOfAttack;
        this.runTask = runTask;
        this.finishRunningTask = finishRunningTask;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T entity) {
        LivingEntity livingEntity = BrainUtils.getAttackTarget(entity);
        return (BehaviorUtils.canSee(entity, livingEntity) && entity.isWithinMeleeAttackRange(livingEntity));
    }

    @Override
    protected void start(ServerLevel level, T mobEntity, long gameTime) {
        mobEntity.setAggressive(true);
        LivingEntity livingEntity = BrainUtils.getAttackTarget(mobEntity);
        mobEntity.setTarget(livingEntity);

        BehaviorUtils.lookAtEntity(mobEntity, livingEntity);
        runTask.run(level, mobEntity, gameTime);

        animationTime = gameTime + animationDuration;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, T entity, long gameTime) {
        return gameTime < this.animationTime;
    }

    @Override
    protected void tick(ServerLevel level, T entity, long gameTime) {
        if(entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            if (animationTime == gameTime + animationTimeOfAttack) {
                LivingEntity livingEntity = BrainUtils.getAttackTarget(entity);
                entity.swing(InteractionHand.MAIN_HAND);
                entity.doHurtTarget(livingEntity);
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, T entity, long gameTime) {
        entity.setAggressive(false);

        finishRunningTask.run(level, entity, gameTime);

        this.animationTime = 0;

        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, this.interval);
    }

    public interface RunTask<T> {
        void run(ServerLevel serverLevel, T mobEntity, long time);
    }

    public interface FinishRunningTask<T> {
        void run(ServerLevel serverLevel, T mobEntity, long time);
    }
}