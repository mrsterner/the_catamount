package dev.sterner.the_catamount.entity.goal;


import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;


public class CatamountStalkGoal extends Goal {

    private final CatamountEntity catamount;
    private LivingEntity target;
    private Path path;
    private int stalkingTime;
    private int repositionTime;

    private static final double MIN_STALK_DISTANCE = 10.0;
    private static final double MAX_STALK_DISTANCE = 20.0;
    private static final double TOO_CLOSE_DISTANCE = 7.0;

    public CatamountStalkGoal(CatamountEntity catamount) {
        this.catamount = catamount;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (catamount.getStage() >= 5) {
            return false;
        }

        target = catamount.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (catamount.getRandom().nextFloat() > 0.5f) {
            return false;
        }

        double distance = catamount.distanceToSqr(target);
        return distance > MIN_STALK_DISTANCE * MIN_STALK_DISTANCE &&
                distance < MAX_STALK_DISTANCE * MAX_STALK_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) {
            return false;
        }

        double distance = catamount.distanceToSqr(target);

        if (distance < TOO_CLOSE_DISTANCE * TOO_CLOSE_DISTANCE) {
            return false;
        }

        if (distance > MAX_STALK_DISTANCE * MAX_STALK_DISTANCE) {
            return false;
        }

        return stalkingTime < 600;
    }

    @Override
    public void start() {
        stalkingTime = 0;
        repositionTime = 0;
        catamount.setCrouched(false);
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }

        stalkingTime++;
        repositionTime--;

        double distance = catamount.distanceTo(target);

        if (catamount.getRandom().nextInt(10) == 0) {
            catamount.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (distance < MIN_STALK_DISTANCE) {
            Vec3 awayVector = catamount.position().subtract(target.position()).normalize();
            BlockPos retreatPos = BlockPos.containing(
                    catamount.getX() + awayVector.x * 5,
                    catamount.getY(),
                    catamount.getZ() + awayVector.z * 5
            );
            catamount.getNavigation().moveTo(retreatPos.getX(), retreatPos.getY(), retreatPos.getZ(), 0.8);
        } else if (distance > MAX_STALK_DISTANCE * 0.8 && repositionTime <= 0) {
            Vec3 towardsVector = target.position().subtract(catamount.position()).normalize();
            double moveDistance = MIN_STALK_DISTANCE + 2.0;
            BlockPos approachPos = BlockPos.containing(
                    catamount.getX() + towardsVector.x * moveDistance,
                    target.getY(),
                    catamount.getZ() + towardsVector.z * moveDistance
            );
            catamount.getNavigation().moveTo(approachPos.getX(), approachPos.getY(), approachPos.getZ(), 0.6);
            repositionTime = 100;
        } else {

            if (repositionTime <= 0 && catamount.getRandom().nextInt(40) == 0) {
                double angle = catamount.getRandom().nextDouble() * Math.PI * 2;
                double radius = MIN_STALK_DISTANCE + 3.0;
                BlockPos circlePos = BlockPos.containing(
                        target.getX() + Math.cos(angle) * radius,
                        target.getY(),
                        target.getZ() + Math.sin(angle) * radius
                );
                catamount.getNavigation().moveTo(circlePos.getX(), circlePos.getY(), circlePos.getZ(), 0.6);
                repositionTime = 80;
            }
        }

        if (catamount.getRandom().nextInt(100) == 0) {
            catamount.getNavigation().stop();
            catamount.getLookControl().setLookAt(target, 30.0F, 30.0F);
            repositionTime = 60;
        }
    }

    @Override
    public void stop() {
        target = null;
        path = null;
        stalkingTime = 0;
        repositionTime = 0;
        catamount.getNavigation().stop();
    }
}