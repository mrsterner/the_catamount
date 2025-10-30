package dev.sterner.the_catamount.entity.task;

import com.google.common.collect.ImmutableMap;
import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;


public class CatamountSmoothLookAtTask extends Behavior<CatamountEntity> {

    public CatamountSmoothLookAtTask() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CatamountEntity entity) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, CatamountEntity entity, long gameTime) {
        entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
                .ifPresent(target -> entity.getLookControl().setLookAt(target, 10.0F, 10.0F));
    }
}