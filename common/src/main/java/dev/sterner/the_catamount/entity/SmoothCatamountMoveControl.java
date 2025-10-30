package dev.sterner.the_catamount.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.LivingEntity;

public class SmoothCatamountMoveControl extends MoveControl {
    private final int maxTurnY;
    private final float outsideWaterSpeedModifier;

    public SmoothCatamountMoveControl(Mob mob, int maxTurnY, float outsideWaterSpeedModifier) {
        super(mob);
        this.maxTurnY = maxTurnY;
        this.outsideWaterSpeedModifier = outsideWaterSpeedModifier;
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO || this.mob.getNavigation().isDone()) {
            this.mob.setSpeed(0.0f);
            this.mob.setXxa(0.0f);
            this.mob.setYya(0.0f);
            this.mob.setZza(0.0f);
            return;
        }

        double dx = this.wantedX - this.mob.getX();
        double dy = this.wantedY - this.mob.getY();
        double dz = this.wantedZ - this.mob.getZ();
        double distanceSq = dx * dx + dy * dy + dz * dz;

        if (distanceSq < 1E-7) {
            this.mob.setZza(0.0f);
            return;
        }

        float pathYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);

        if (this.mob instanceof CatamountEntity cat && cat.getTarget() != null) {
            LivingEntity target = cat.getTarget();
            if (target.isAlive()) {
                double tx = target.getX() - this.mob.getX();
                double tz = target.getZ() - this.mob.getZ();
                pathYaw = (float) (Math.toDegrees(Math.atan2(tz, tx)) - 90.0);
            }
        }

        this.mob.setYRot(approachDegrees(this.mob.getYRot(), pathYaw, maxTurnY));
        this.mob.yBodyRot = this.mob.getYRot();

        this.mob.yHeadRot = this.mob.getYRot();

        float speed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        float yawDiff = Math.abs(Mth.wrapDegrees(this.mob.getYRot() - pathYaw));
        float turnFactor = getTurningSpeedFactor(yawDiff);

        this.mob.setSpeed(speed * outsideWaterSpeedModifier * turnFactor);
    }

    private static float getTurningSpeedFactor(float degreesToTurn) {
        return 1.0f - Mth.clamp((degreesToTurn - 10.0f) / 50.0f, 0.0f, 1.0f);
    }

    private static float approachDegrees(float current, float target, float maxDelta) {
        float delta = Mth.wrapDegrees(target - current);
        if (delta > maxDelta) delta = maxDelta;
        if (delta < -maxDelta) delta = -maxDelta;
        return current + delta;
    }
}