package dev.sterner.the_catamount.entity.brain;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BrainUtils {

    public static LivingEntity getAttackTarget(Mob entity) {
        return entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    public static void setTargetInvalid(Mob mobEntity, LivingEntity target) {
        mobEntity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mobEntity.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
    }
}