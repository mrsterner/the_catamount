package dev.sterner.the_catamount.entity.goal;


import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class CatamountVanishGoal extends Goal {
    private final CatamountEntity catamount;
    private int vanishTimer;
    private boolean isVanishing;
    private VanishType vanishType;

    private static final int VANISH_DURATION = 40;
    private static final int COOLDOWN_TICKS = 6000;
    private int cooldownRemaining;

    private enum VanishType {
        DESPAWN,
        RELOCATE
    }

    public CatamountVanishGoal(CatamountEntity catamount) {
        this.catamount = catamount;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
        this.cooldownRemaining = 0;
    }

    @Override
    public boolean canUse() {
        if (cooldownRemaining > 0) {
            cooldownRemaining--;
            return false;
        }

        if (!shouldVanish()) {
            return false;
        }

        if (catamount.getHealth() < catamount.getMaxHealth() * 0.3f) {
            vanishType = VanishType.RELOCATE;
            return true;
        } else if (catamount.getTarget() == null && catamount.getRandom().nextFloat() < 0.3f) {
            vanishType = VanishType.DESPAWN;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return isVanishing && vanishTimer < VANISH_DURATION;
    }

    @Override
    public void start() {
        isVanishing = true;
        vanishTimer = 0;
        catamount.getNavigation().stop();
        catamount.setTarget(null);
    }

    @Override
    public void tick() {
        vanishTimer++;

        catamount.setDeltaMovement(0, 0, 0);

        spawnWindParticles();

        if (vanishTimer % 10 == 0) {
            catamount.level().playSound(null, catamount.blockPosition(),
                    SoundEvents.BREEZE_IDLE_AIR, SoundSource.HOSTILE, 1.0f, 0.8f + catamount.getRandom().nextFloat() * 0.4f);
        }

        if (vanishTimer >= VANISH_DURATION) {
            completeVanish();
        }
    }

    private void completeVanish() {
        if (catamount.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    catamount.getX(), catamount.getY() + 1, catamount.getZ(),
                    50, 0.5, 0.5, 0.5, 0.1);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    catamount.getX(), catamount.getY() + 1, catamount.getZ(),
                    20, 0.5, 0.5, 0.5, 0.05);

            serverLevel.playSound(null, catamount.blockPosition(),
                    SoundEvents.BREEZE_DEATH, SoundSource.HOSTILE, 1.5f, 1.0f);
        }

        switch (vanishType) {
            case DESPAWN -> despawn();
            case RELOCATE -> relocate();
        }
    }

    private void despawn() {
        catamount.setFullyManifested(false);
        catamount.discard();
    }

    private void relocate() {
        BlockPos newPos = findRelocatePosition();

        if (newPos != null) {
            catamount.teleportTo(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);

            if (catamount.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        catamount.getX(), catamount.getY() + 1, catamount.getZ(),
                        30, 0.5, 0.5, 0.5, 0.05);

                serverLevel.playSound(null, catamount.blockPosition(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.6f);
            }

            catamount.heal(catamount.getMaxHealth() * 0.2f);
        } else {
            despawn();
        }
    }

    private BlockPos findRelocatePosition() {
        Level level = catamount.level();
        BlockPos currentPos = catamount.blockPosition();

        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = catamount.getRandom().nextDouble() * Math.PI * 2;
            double distance = 30 + catamount.getRandom().nextDouble() * 20;

            int x = currentPos.getX() + (int)(Math.cos(angle) * distance);
            int z = currentPos.getZ() + (int)(Math.sin(angle) * distance);

            BlockPos testPos = new BlockPos(x, level.getMaxBuildHeight() - 1, z);
            while (testPos.getY() > level.getMinBuildHeight() &&
                    !level.getBlockState(testPos).isSolidRender(level, testPos)) {
                testPos = testPos.below();
            }

            testPos = testPos.above();

            if (level.getBlockState(testPos).isAir() &&
                    level.getBlockState(testPos.above()).isAir() &&
                    level.getBlockState(testPos.below()).isSolidRender(level, testPos.below())) {
                return testPos;
            }
        }

        return null;
    }

    private void spawnWindParticles() {
        if (!(catamount.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ParticleTypes.SMOKE,
                catamount.getX(), catamount.getY() + 1, catamount.getZ(),
                3, 0.3, 0.5, 0.3, 0.02);

        if (vanishTimer % 5 == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    catamount.getX(), catamount.getY() + 1, catamount.getZ(),
                    2, 0.4, 0.4, 0.4, 0.03);
        }

        if (vanishTimer % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.GUST,
                    catamount.getX(), catamount.getY() + 1, catamount.getZ(),
                    1, 0.2, 0.3, 0.2, 0.1);
        }
    }

    private boolean shouldVanish() {
        long dayTime = catamount.level().getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime < 23000;

        if (catamount.isFeedingFrenzy()) {
            return false;
        }

        if (catamount.getHealth() < catamount.getMaxHealth() * 0.3f) {
            return true;
        }

        if (!isNight) {
            return false;
        }

        return catamount.getTarget() == null && catamount.getRandom().nextInt(600) == 0;
    }

    @Override
    public void stop() {
        isVanishing = false;
        vanishTimer = 0;
        cooldownRemaining = COOLDOWN_TICKS;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}