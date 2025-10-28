package dev.sterner.the_catamount.entity.goal;

import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CatamountHuntGoal extends Goal {
    private final CatamountEntity catamount;
    private LivingEntity target;
    private int attackTime;
    private int crouchTransitionTime;
    private static final int CROUCH_TRANSITION_DELAY = 30;

    public CatamountHuntGoal(CatamountEntity catamount) {
        this.catamount = catamount;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = catamount.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && !catamount.getNavigation().isDone();
    }

    @Override
    public void start() {
        attackTime = 0;
        crouchTransitionTime = 0;
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }

        double distanceToTarget = catamount.distanceToSqr(target);
        catamount.getLookControl().setLookAt(target, 30.0F, 30.0F);

        boolean shouldCrouch = target.getBbHeight() < 2.0;

        if (shouldCrouch && !catamount.isCrouched()) {
            if (distanceToTarget < 64.0) {
                crouchTransitionTime++;
                if (crouchTransitionTime >= CROUCH_TRANSITION_DELAY) {
                    catamount.setCrouched(true);
                    crouchTransitionTime = 0;
                }
            } else {
                crouchTransitionTime = 0;
            }
        } else if (!shouldCrouch && catamount.isCrouched()) {
            catamount.setCrouched(false);
            crouchTransitionTime = 0;
        }

        if (distanceToTarget > getAttackReachSqr(target)) {
            catamount.getNavigation().moveTo(target, getSpeedModifier());
        } else {
            catamount.getNavigation().stop();
        }

        attackTime = Math.max(attackTime - 1, 0);
        if (attackTime <= 0 && distanceToSqr(catamount, target) <= getAttackReachSqr(target)) {
            attackTime = 20;
            catamount.doHurtTarget(target);
        }
    }

    @Override
    public void stop() {
        target = null;
        attackTime = 0;
        crouchTransitionTime = 0;
        catamount.getNavigation().stop();
    }

    private double getSpeedModifier() {
        return catamount.isCrouched() ? 1.3 : 1.0;
    }

    protected double getAttackReachSqr(LivingEntity target) {
        double reach = catamount.isCrouched() ? 2.5 : 3.5;
        return (reach + target.getBbWidth()) * (reach + target.getBbWidth());
    }

    protected static double distanceToSqr(CatamountEntity entity, LivingEntity target) {
        Vec3 vec3 = new Vec3(target.getX() - entity.getX(),
                target.getY() - entity.getY(),
                target.getZ() - entity.getZ());
        return vec3.lengthSqr();
    }
}