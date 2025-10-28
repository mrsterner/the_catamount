package dev.sterner.the_catamount.entity.goal;


import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;


public class CatamountPounceGoal extends Goal {
    private final CatamountEntity catamount;
    private LivingEntity target;
    private int pounceChargeTime;
    private int pinDuration;
    private boolean hasPounced;
    private boolean isPinning;

    private static final double POUNCE_DISTANCE_MIN = 3.0;
    private static final double POUNCE_DISTANCE_MAX = 8.0;
    private static final int CHARGE_TIME = 15;
    private static final int PIN_TIME = 30;

    public CatamountPounceGoal(CatamountEntity catamount) {
        this.catamount = catamount;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (catamount.isCrouched()) {
            return false;
        }

        target = catamount.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (target.getBbHeight() >= 2.0) {
            return false;
        }

        double distance = catamount.distanceTo(target);

        if (distance < POUNCE_DISTANCE_MIN || distance > POUNCE_DISTANCE_MAX) {
            return false;
        }

        if (!catamount.getSensing().hasLineOfSight(target)) {
            return false;
        }

        return catamount.getRandom().nextFloat() < 0.3f;
    }

    @Override
    public boolean canContinueToUse() {
        if (isPinning) {
            return pinDuration > 0 && target != null && target.isAlive();
        }
        return !hasPounced && target != null && target.isAlive();
    }

    @Override
    public void start() {
        pounceChargeTime = CHARGE_TIME;
        pinDuration = 0;
        hasPounced = false;
        isPinning = false;
        catamount.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }

        if (isPinning) {
            pinDuration--;
            catamount.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (catamount.distanceTo(target) < 2.0) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 4, false, false));

                if (pinDuration % 10 == 0) {
                    catamount.doHurtTarget(target);
                }
            } else {
                isPinning = false;
            }
            return;
        }

        if (!hasPounced) {
            catamount.getLookControl().setLookAt(target, 30.0F, 30.0F);
            catamount.getNavigation().stop();

            pounceChargeTime--;

            if (pounceChargeTime <= 0) {
                executePounce();
            }
        }
    }

    private void executePounce() {
        hasPounced = true;

        Vec3 targetPos = target.position();
        Vec3 currentPos = catamount.position();
        Vec3 direction = targetPos.subtract(currentPos).normalize();

        double horizontalSpeed = 1.2;
        double verticalSpeed = 0.5;

        Vec3 leapVelocity = new Vec3(
                direction.x * horizontalSpeed,
                verticalSpeed,
                direction.z * horizontalSpeed
        );

        catamount.setDeltaMovement(leapVelocity);
        catamount.hasImpulse = true;

        catamount.setCrouched(true);

        catamount.level().getServer().execute(() -> {
            checkPounceHit();
        });
    }

    private void checkPounceHit() {
        int checkDelay = 10;

        scheduleCheck(checkDelay);
    }

    private void scheduleCheck(int ticksDelay) {
        if (ticksDelay <= 0) {
            if (target != null && target.isAlive() && catamount.distanceTo(target) < 2.5) {
                isPinning = true;
                pinDuration = PIN_TIME;

                catamount.doHurtTarget(target);

                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, PIN_TIME, 10, false, false));

                target.setOnGround(true);
            }
        } else {
            catamount.level().getServer().execute(() -> {
                scheduleCheck(ticksDelay - 1);
            });
        }
    }

    @Override
    public void stop() {
        target = null;
        pounceChargeTime = 0;
        pinDuration = 0;
        hasPounced = false;
        isPinning = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
