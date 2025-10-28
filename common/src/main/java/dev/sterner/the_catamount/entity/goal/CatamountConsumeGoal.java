package dev.sterner.the_catamount.entity.goal;


import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class CatamountConsumeGoal extends Goal {
    private final CatamountEntity catamount;
    private LivingEntity target;
    private int consumeTicks;
    private int biteTicks;
    private boolean isConsuming;

    private static final int CONSUME_DURATION = 200;
    private static final int BITE_INTERVAL = 20;
    private static final float BITE_DAMAGE = 5.0f;
    private static final float HEALTH_THRESHOLD = 0.3f;

    public CatamountConsumeGoal(CatamountEntity catamount) {
        this.catamount = catamount;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (catamount.getStage() < 5) {
            return false;
        }

        target = catamount.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (target.getHealth() / target.getMaxHealth() > HEALTH_THRESHOLD) {
            return false;
        }

        if (catamount.distanceToSqr(target) > 4.0) {
            return false;
        }

        return catamount.getRandom().nextFloat() < 0.4f;
    }

    @Override
    public boolean canContinueToUse() {
        if (!isConsuming) {
            return false;
        }

        if (target == null || !target.isAlive()) {
            return false;
        }

        return consumeTicks < CONSUME_DURATION;
    }

    @Override
    public void start() {
        isConsuming = true;
        consumeTicks = 0;
        biteTicks = 0;

        catamount.getNavigation().stop();
        catamount.setCrouched(true);

        if (catamount.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, catamount.blockPosition(),
                    SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.5f, 0.8f);
        }
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            stop();
            return;
        }

        consumeTicks++;
        biteTicks++;

        catamount.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (catamount.distanceTo(target) > 1.5) {
            double dx = catamount.getX() - target.getX();
            double dy = catamount.getY() - target.getY();
            double dz = catamount.getZ() - target.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > 0.1) {
                target.setDeltaMovement(
                        dx / distance * 0.15,
                        dy / distance * 0.05,
                        dz / distance * 0.15
                );
            }
        }

        if (biteTicks >= BITE_INTERVAL) {
            biteTicks = 0;
            performBite();
        }

        if (consumeTicks % 5 == 0) {
            spawnConsumeParticles();
        }
    }

    private void performBite() {
        target.hurt(catamount.damageSources().mobAttack(catamount), BITE_DAMAGE);

        catamount.level().playSound(null, catamount.blockPosition(),
                SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.0f, 0.7f);

        if (!target.isAlive()) {
            onTargetConsumed();
        }
    }

    private void onTargetConsumed() {
        if (target instanceof Animal) {
            catamount.increaseAnimalsConsumed();
        } else if (target instanceof AbstractVillager || target instanceof Player) {
            catamount.increaseHumanoidsConsumed();
        }

        if (catamount.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    40, 0.5, 0.5, 0.5, 0.1);

            serverLevel.playSound(null, catamount.blockPosition(),
                    SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.HOSTILE, 1.0f, 0.6f);
        }

        catamount.heal(10.0f);
    }

    private void spawnConsumeParticles() {
        if (catamount.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    3, 0.3, 0.3, 0.3, 0.01);

            if (consumeTicks % 10 == 0) {
                serverLevel.sendParticles(ParticleTypes.SOUL,
                        target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                        2, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    @Override
    public void stop() {
        isConsuming = false;
        consumeTicks = 0;
        biteTicks = 0;
        target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
