package dev.sterner.the_catamount.catamount_events;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class DamagingEvents {

    public static List<CatamountEvent> getAll() {
        return List.of(
                new StealFoodEvent(),
                new DangerousLeavesEvent(),
                new DangerousWindEvent()
        );
    }

    public static class StealFoodEvent extends CatamountEvent {
        public StealFoodEvent() {
            super(EventType.DAMAGING, true);
        }

        @Override
        public void execute(ServerPlayer player) {
            int stolenCount = 0;
            BlockPos searchCenter = getEventLocation(player);
            ServerLevel level = player.serverLevel();

            for (BlockPos pos : BlockPos.betweenClosed(
                    searchCenter.offset(-16, -8, -16),
                    searchCenter.offset(16, 8, 16)
            )) {
                if (stolenCount >= 5) break;

                BlockEntity blockEntity = level.getBlockEntity(pos);

                if (blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
                    ItemStack result = furnace.getItem(2);
                    if (result.has(DataComponents.FOOD)) {
                        int toSteal = Math.min(result.getCount(), 5 - stolenCount);
                        result.shrink(toSteal);
                        stolenCount += toSteal;
                    }
                }

                if (blockEntity instanceof CampfireBlockEntity campfire) {
                    for (int i = 0; i < 4; i++) {
                        ItemStack item = campfire.getItems().get(i);
                        if (!item.isEmpty()) {
                            campfire.getItems().set(i, ItemStack.EMPTY);
                            stolenCount++;
                            if (stolenCount >= 5) break;
                        }
                    }
                }
            }

            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
                    new AABB(searchCenter).inflate(16));

            for (ItemEntity itemEntity : items) {
                if (stolenCount >= 5) break;
                ItemStack stack = itemEntity.getItem();
                if (stack.has(DataComponents.FOOD)) {
                    int toSteal = Math.min(stack.getCount(), 5 - stolenCount);
                    stack.shrink(toSteal);
                    stolenCount += toSteal;
                    if (stack.isEmpty()) {
                        itemEntity.discard();
                    }
                }
            }

            if (stolenCount > 0) {
                addPoints(player, stolenCount);

                level.playSound(null, player.blockPosition(),
                        SoundEvents.ITEM_PICKUP, SoundSource.AMBIENT,
                        0.5f, 0.8f);
            }
        }
    }

    public static class DangerousLeavesEvent extends CatamountEvent {
        private static final int DURATION = 20 * 30;

        public DangerousLeavesEvent() {
            super(EventType.DAMAGING, true);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos center = getEventLocation(player);
            //TODO
        }

        public static void tickDangerousLeaves(ServerPlayer player) {
            BlockPos playerPos = player.blockPosition();
            ServerLevel level = player.serverLevel();

            for (BlockPos pos : BlockPos.betweenClosed(
                    playerPos.offset(-3, -3, -3),
                    playerPos.offset(3, 3, 3)
            )) {
                BlockState state = level.getBlockState(pos);
                if (state.is(BlockTags.LEAVES) && !state.hasBlockEntity()) {
                    if (level.getGameTime() % 40 == 0) {
                        float damage = 1.0f + player.getRandom().nextFloat();

                        if (player.getHealth() > damage) {
                            player.hurt(level.damageSources().magic(), damage);
                            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);
                            CatamountPlayerDataAttachment.setData(player, data.addPoints((int) damage));
                        } else {
                            player.hurt(level.damageSources().magic(), 0.0f);
                        }

                        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                                player.getX(), player.getY() + 1, player.getZ(),
                                5, 0.5, 0.5, 0.5, 0.1);
                    }
                    break;
                }
            }
        }
    }

    public static class DangerousWindEvent extends CatamountEvent {
        public DangerousWindEvent() {
            super(EventType.DAMAGING, true);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos location = getEventLocation(player);
            //TODO Spawn wind entity
        }
    }
}