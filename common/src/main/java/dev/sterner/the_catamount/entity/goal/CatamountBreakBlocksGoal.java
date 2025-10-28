package dev.sterner.the_catamount.entity.goal;

import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CatamountBreakBlocksGoal extends Goal {
    private final CatamountEntity catamount;
    private LivingEntity target;
    private BlockPos targetBlockPos;
    private int breakProgress;
    private int checkCooldown;

    private static final int BREAK_TIME = 30;
    private static final int CHECK_INTERVAL = 20;
    private static final double MAX_BREAK_DISTANCE = 4.0;

    public CatamountBreakBlocksGoal(CatamountEntity catamount) {
        this.catamount = catamount;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (catamount.getStage() < 4) {
            return false;
        }

        target = catamount.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        targetBlockPos = findBlockToBreak();

        return targetBlockPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (targetBlockPos != null && catamount.level().getBlockState(targetBlockPos).isAir()) {
            targetBlockPos = findBlockToBreak();
        }

        return targetBlockPos != null && breakProgress < BREAK_TIME;
    }

    @Override
    public void start() {
        breakProgress = 0;
        checkCooldown = 0;
        catamount.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetBlockPos == null || target == null) {
            return;
        }

        checkCooldown--;

        double distance = catamount.position().distanceTo(Vec3.atCenterOf(targetBlockPos));

        if (distance > MAX_BREAK_DISTANCE) {
            catamount.getNavigation().moveTo(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ(), 1.0);
            return;
        } else {
            catamount.getNavigation().stop();
        }

        catamount.getLookControl().setLookAt(
                targetBlockPos.getX() + 0.5,
                targetBlockPos.getY() + 0.5,
                targetBlockPos.getZ() + 0.5
        );

        breakProgress++;

        if (breakProgress % 5 == 0) {
            BlockState state = catamount.level().getBlockState(targetBlockPos);

            catamount.level().playSound(null, targetBlockPos,
                    SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.7f, 0.8f);

            if (catamount.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        targetBlockPos.getX() + 0.5,
                        targetBlockPos.getY() + 0.5,
                        targetBlockPos.getZ() + 0.5,
                        10, 0.3, 0.3, 0.3, 0.1
                );
            }
        }

        if (breakProgress >= BREAK_TIME) {
            breakBlock();
        }

        if (checkCooldown <= 0) {
            BlockPos newTarget = findBlockToBreak();
            if (newTarget != null && !newTarget.equals(targetBlockPos)) {
                targetBlockPos = newTarget;
                breakProgress = 0;
            }
            checkCooldown = CHECK_INTERVAL;
        }
    }

    private void breakBlock() {
        if (targetBlockPos == null) {
            return;
        }

        BlockState state = catamount.level().getBlockState(targetBlockPos);

        catamount.level().playSound(null, targetBlockPos,
                SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f);

        catamount.level().destroyBlock(targetBlockPos, true);

        if (catamount.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    targetBlockPos.getX() + 0.5,
                    targetBlockPos.getY() + 0.5,
                    targetBlockPos.getZ() + 0.5,
                    30, 0.4, 0.4, 0.4, 0.15
            );
        }

        targetBlockPos = findBlockToBreak();
        breakProgress = 0;
    }

    private BlockPos findBlockToBreak() {
        if (target == null) {
            return null;
        }

        Vec3 start = catamount.getEyePosition();
        Vec3 end = target.getEyePosition();
        Vec3 direction = end.subtract(start).normalize();

        double maxDistance = Math.min(start.distanceTo(end), MAX_BREAK_DISTANCE);

        for (double distance = 1.0; distance < maxDistance; distance += 0.5) {
            Vec3 pos = start.add(direction.scale(distance));
            BlockPos blockPos = BlockPos.containing(pos);
            BlockState state = catamount.level().getBlockState(blockPos);

            if (canBreakBlock(state, blockPos)) {
                return blockPos;
            }
        }

        return null;
    }

    private boolean canBreakBlock(BlockState state, BlockPos pos) {
        if (state.isAir()) {
            return false;
        }

        float hardness = state.getDestroySpeed(catamount.level(), pos);

        if (hardness < 0 || hardness > 2.5f) {
            return false;
        }

        Block block = state.getBlock();
        if (block == Blocks.BEDROCK ||
                block == Blocks.BARRIER ||
                block == Blocks.COMMAND_BLOCK ||
                block == Blocks.STRUCTURE_BLOCK) {
            return false;
        }

        return state.is(BlockTags.LOGS) ||
                state.is(BlockTags.PLANKS) ||
                state.is(BlockTags.LEAVES) ||
                state.is(BlockTags.WOODEN_DOORS) ||
                state.is(BlockTags.WOODEN_FENCES) ||
                hardness <= 2.5f;
    }

    @Override
    public void stop() {
        target = null;
        targetBlockPos = null;
        breakProgress = 0;
        checkCooldown = 0;
        catamount.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
