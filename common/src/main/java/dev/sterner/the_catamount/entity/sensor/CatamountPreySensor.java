package dev.sterner.the_catamount.entity.sensor;

import com.google.common.collect.ImmutableSet;
import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class CatamountPreySensor extends Sensor<CatamountEntity> {

    private static final double DETECTION_RANGE = 32.0;

    @Override
    protected void doTick(ServerLevel level, CatamountEntity catamount) {
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                LivingEntity.class,
                catamount.getBoundingBox().inflate(DETECTION_RANGE),
                entity -> entity != catamount && entity.isAlive()
        );

        List<LivingEntity> validPrey = nearbyEntities.stream()
                .filter(this::isValidPrey)
                .filter(entity -> !isOwner(catamount, entity))
                .toList();

        Optional<LivingEntity> nearestTarget = validPrey.stream()
                .filter(entity -> catamount.hasLineOfSight(entity))
                .min(Comparator.comparingDouble(catamount::distanceToSqr));

        if (nearestTarget.isPresent()) {
            catamount.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestTarget.get());

            if (!catamount.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                catamount.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, nearestTarget.get());
            }
        } else {
            catamount.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.ATTACK_TARGET
        );
    }

    private boolean isValidPrey(LivingEntity entity) {
        return entity instanceof Animal ||
                entity instanceof AbstractVillager ||
                entity instanceof Player;
    }

    private boolean isOwner(CatamountEntity catamount, LivingEntity entity) {
        if (entity instanceof Player player) {
            return catamount.getOwnerUUID()
                    .map(uuid -> uuid.equals(player.getUUID()))
                    .orElse(false);
        }
        return false;
    }
}