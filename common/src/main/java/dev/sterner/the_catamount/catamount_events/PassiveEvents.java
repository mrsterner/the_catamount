package dev.sterner.the_catamount.catamount_events;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.data_attachment.FrozenAnimalDataAttachment;
import dev.sterner.the_catamount.data_attachment.PaleAnimalDataAttachment;
import dev.sterner.the_catamount.data_attachment.SoulConversionDataAttachment;
import dev.sterner.the_catamount.entity.LightOrbEntity;
import dev.sterner.the_catamount.entity.WindEntity;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import dev.sterner.the_catamount.registry.TCParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class PassiveEvents {

    public static List<CatamountEvent> getAll() {
        return List.of(
                new SummonTheWindEvent(),
                new SoulFireConversionEvent(),
                new FaintSpiritEvent(),
                new BallOfLightEvent(),
                new AnimalStareEvent(),
                new VillagerSweatEvent(),
                new FootstepsEvent(),
                new PaleAnimalsEvent(),
                new ExtraDamageEvent()
        );
    }

    public static class SummonTheWindEvent extends CatamountEvent {
        public SummonTheWindEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos location = getEventLocation(player);
            ServerLevel level = player.serverLevel();

            WindEntity wind = new WindEntity(TCEntityTypes.WIND, level);
            wind.setPos(location.getX() + 0.5, location.getY(), location.getZ() + 0.5);
            wind.setAggressive(false);

            level.addFreshEntity(wind);

            level.playSound(null, location, SoundEvents.BREEZE_IDLE_AIR,
                    SoundSource.AMBIENT, 0.6f, 1.0f);
        }
    }

    public static class SoulFireConversionEvent extends CatamountEvent {

        public SoulFireConversionEvent() {
            super(EventType.PASSIVE, false);
        }

        public static void tickConversions(ServerLevel level) {
            if (level.dimension() != Level.OVERWORLD) {
                return;
            }

            SoulConversionDataAttachment.Data data = SoulConversionDataAttachment.getData(level);

            if (data.activeConversions().isEmpty()) return;

            long currentTime = level.getGameTime();
            boolean changed = false;

            for (Map.Entry<BlockPos, SoulConversionDataAttachment.ConversionRecord> entry :
                    new ArrayList<>(data.activeConversions().entrySet())) {

                BlockPos pos = entry.getKey();
                SoulConversionDataAttachment.ConversionRecord record = entry.getValue();

                if (currentTime >= record.endTime()) {
                    BlockState currentState = level.getBlockState(pos);
                    Block currentBlock = currentState.getBlock();

                    Block expectedSoulBlock = SoulConversionListener.CONVERSION_PAIR.get(record.originalState().getBlock());

                    if (currentBlock == expectedSoulBlock) {
                        if (record.originalState().getBlock() instanceof CampfireBlock && record.extraData() != null) {
                            restoreCampfire(level, pos, record);
                        } else {
                            level.setBlock(pos, record.originalState(), 3);
                        }

                        RandomSource random = level.getRandom();
                        for (int i = 0; i < 3; i++) {
                            level.sendParticles(ParticleTypes.SMOKE,
                                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                                    pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                                    1, 0, 0, 0, 0);
                        }
                    }

                    data = data.withoutConversion(pos);
                    changed = true;
                }
            }

            if (changed) {
                SoulConversionDataAttachment.setData(level, data);
            }
        }

        private static void restoreCampfire(ServerLevel level, BlockPos pos, SoulConversionDataAttachment.ConversionRecord record) {
            BlockEntity soulCampfireEntity = level.getBlockEntity(pos);
            NonNullList<ItemStack> currentItems = NonNullList.create();

            if (soulCampfireEntity instanceof CampfireBlockEntity soulCampfire) {
                for (int i = 0; i < soulCampfire.getItems().size(); i++) {
                    ItemStack item = soulCampfire.getItems().get(i);
                    if (!item.isEmpty()) {
                        currentItems.add(item.copy());
                    }
                }
            }

            level.setBlock(pos, record.originalState(), 3);

            BlockEntity newBlockEntity = level.getBlockEntity(pos);
            if (newBlockEntity instanceof CampfireBlockEntity campfire) {
                if (record.extraData() != null) {
                    campfire.loadAdditional(record.extraData(), level.registryAccess());
                }

                for (int i = 0; i < Math.min(currentItems.size(), campfire.getItems().size()); i++) {
                    ItemStack item = currentItems.get(i);
                    if (!item.isEmpty()) {
                        if (!campfire.placeFood(null, item, 1)) {
                            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), item);
                        }
                    }
                }

                campfire.setChanged();
            }
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos center = getEventLocation(player);
            ServerLevel level = player.serverLevel();
            RandomSource random = player.getRandom();

            int duration = (10 + random.nextInt(41)) * 20;
            long endTime = level.getGameTime() + duration;

            boolean foundAny = false;
            SoulConversionDataAttachment.Data data = SoulConversionDataAttachment.getData(level);

            for (BlockPos pos : BlockPos.betweenClosed(
                    center.offset(-10, -10, -10),
                    center.offset(10, 10, 10)
            )) {
                BlockState state = level.getBlockState(pos);
                Block block = state.getBlock();

                if (SoulConversionListener.CONVERSION_PAIR.containsKey(block)) {
                    BlockPos immutablePos = pos.immutable();

                    if (block instanceof CampfireBlock) {
                        if (!state.getValue(CampfireBlock.LIT)) {
                            continue;
                        }

                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        if (blockEntity instanceof CampfireBlockEntity campfire) {
                            data = handleCampfireConversion(level, immutablePos, state, campfire, endTime, data);
                            foundAny = true;
                            continue;
                        }
                    }

                    SoulConversionDataAttachment.ConversionRecord record =
                            new SoulConversionDataAttachment.ConversionRecord(state, endTime);

                    data = data.withConversion(immutablePos, record);
                    SoulConversionDataAttachment.setData(level, data);

                    SoulConversionListener.convertBlock(level, immutablePos, block);
                }
            }

            if (foundAny) {
                SoulConversionDataAttachment.setData(level, data);
                level.playSound(null, center, SoundEvents.SOUL_ESCAPE.value(),
                        SoundSource.AMBIENT, 0.8f, 0.8f);
            }
        }

        private SoulConversionDataAttachment.Data handleCampfireConversion(ServerLevel level, BlockPos pos, BlockState state,
                                                                           CampfireBlockEntity campfire, long endTime,
                                                                           SoulConversionDataAttachment.Data data) {

            NonNullList<ItemStack> items = NonNullList.create();
            for (int i = 0; i < campfire.getItems().size(); i++) {
                ItemStack item = campfire.getItems().get(i);
                if (!item.isEmpty()) {
                    items.add(item.copy());
                }
            }

            CompoundTag campfireData = new CompoundTag();
            campfire.saveAdditional(campfireData, level.registryAccess());

            SoulConversionDataAttachment.ConversionRecord record =
                    new SoulConversionDataAttachment.ConversionRecord(state, endTime, campfireData);
            data = data.withConversion(pos, record);

            Block targetBlock = SoulConversionListener.CONVERSION_PAIR.get(state.getBlock());
            if (targetBlock != null) {
                BlockState newState = SoulConversionListener.copySharedProperties(state, targetBlock.defaultBlockState());
                level.setBlock(pos, newState, 3);

                BlockEntity newBlockEntity = level.getBlockEntity(pos);
                if (newBlockEntity instanceof CampfireBlockEntity soulCampfire) {

                    for (int i = 0; i < Math.min(items.size(), soulCampfire.getItems().size()); i++) {
                        ItemStack item = items.get(i);
                        if (!item.isEmpty()) {
                            if (!soulCampfire.placeFood(null, item, 1)) {
                                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), item);
                            }
                        }
                    }
                }

                for (int i = 0; i < 5; i++) {
                    double offsetX = 0.5 + (level.random.nextDouble() - 0.5);
                    double offsetY = 0.5 + (level.random.nextDouble() - 0.5);
                    double offsetZ = 0.5 + (level.random.nextDouble() - 0.5);
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ,
                            1, 0, 0, 0, 0);
                }
            }

            return data;
        }
    }

    public static class FaintSpiritEvent extends CatamountEvent {
        public FaintSpiritEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos location = getEventLocation(player);
            ServerLevel level = player.serverLevel();

            BlockPos surfacePos = findValidSurface(level, location);
            if (surfacePos == null) {
                surfacePos = location;
            }

            Direction facing = findFacingDirection(level, surfacePos);

            Vec3 particlePos = Vec3.atCenterOf(surfacePos).add(
                    facing.getStepX() * 0.6,
                    0,
                    facing.getStepZ() * 0.6
            );

            level.sendParticles(
                    TCParticles.SPIRIT_FACE,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0, 0, 0, 0.0
            );

            level.playSound(null, surfacePos, SoundEvents.SOUL_ESCAPE.value(),
                    SoundSource.AMBIENT, 0.3f, 0.5f);
        }

        private BlockPos findValidSurface(ServerLevel level, BlockPos center) {
            RandomSource random = level.getRandom();

            for (int i = 0; i < 10; i++) {
                BlockPos pos = center.offset(
                        random.nextInt(20) - 10,
                        random.nextInt(6) - 3,
                        random.nextInt(20) - 10
                );

                if (level.getBlockState(pos).isSolidRender(level, pos)) {
                    return pos;
                }
            }
            return null;
        }

        private Direction findFacingDirection(ServerLevel level, BlockPos pos) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(pos.relative(dir)).isAir()) {
                    return dir;
                }
            }
            return Direction.NORTH;
        }
    }

    public static class BallOfLightEvent extends CatamountEvent {
        public BallOfLightEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos location = getEventLocation(player).above(20);
            ServerLevel level = player.serverLevel();

            LightOrbEntity orb = new LightOrbEntity(TCEntityTypes.LIGHT_ORB, level);
            orb.setPos(location.getX() + 0.5, location.getY(), location.getZ() + 0.5);

            level.addFreshEntity(orb);
        }
    }

    public static class AnimalStareEvent extends CatamountEvent {
        private static final int FREEZE_DURATION = 20 * 5;

        public AnimalStareEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            List<Animal> animals = findNearbyEntities(player, Animal.class, 32.0);

            if (animals.isEmpty()) return;

            ServerLevel level = player.serverLevel();
            float yaw = player.getRandom().nextFloat() * 360f;
            long endTime = level.getGameTime() + FREEZE_DURATION;

            FrozenAnimalDataAttachment.Data data = FrozenAnimalDataAttachment.getData(level);

            for (Animal animal : animals) {
                animal.setYRot(yaw);
                animal.setYHeadRot(yaw);
                animal.setYBodyRot(yaw);
                animal.setXRot(0);
                animal.yRotO = yaw;
                animal.yHeadRotO = yaw;
                animal.yBodyRotO = yaw;
                animal.xRotO = 0;

                animal.getNavigation().stop();
                animal.setDeltaMovement(Vec3.ZERO);

                data = data.withFrozenAnimal(animal.getUUID(), endTime, yaw, 0);

                level.sendParticles(ParticleTypes.SNOWFLAKE,
                        animal.getX(), animal.getY() + animal.getBbHeight() / 2, animal.getZ(),
                        10, 0.3, 0.3, 0.3, 0.01);
            }

            FrozenAnimalDataAttachment.setData(level, data);

            level.playSound(null, player.blockPosition(),
                    SoundEvents.GLASS_BREAK, SoundSource.AMBIENT, 0.5f, 1.5f);
        }

        public static void tickFrozenAnimals(ServerLevel level) {
            FrozenAnimalDataAttachment.Data data = FrozenAnimalDataAttachment.getData(level);
            long currentTime = level.getGameTime();
            boolean changed = false;

            for (UUID uuid : new ArrayList<>(data.frozenAnimals().keySet())) {
                FrozenAnimalDataAttachment.FrozenRecord record = data.frozenAnimals().get(uuid);

                if (currentTime >= record.endTime()) {
                    data = data.withoutFrozenAnimal(uuid);
                    changed = true;

                    Entity entity = level.getEntity(uuid);
                    if (entity instanceof Animal animal) {
                        level.sendParticles(ParticleTypes.POOF,
                                animal.getX(), animal.getY() + animal.getBbHeight() / 2, animal.getZ(),
                                5, 0.2, 0.2, 0.2, 0.02);
                    }
                } else {
                    Entity entity = level.getEntity(uuid);
                    if (entity instanceof Animal animal) {
                        animal.getNavigation().stop();
                        animal.setDeltaMovement(Vec3.ZERO);

                        animal.setYRot(record.yaw());
                        animal.setYHeadRot(record.yaw());
                        animal.setYBodyRot(record.yaw());
                        animal.setXRot(record.pitch());
                        animal.yRotO = record.yaw();
                        animal.yHeadRotO = record.yaw();
                        animal.yBodyRotO = record.yaw();
                        animal.xRotO = record.pitch();

                        if (currentTime % 20 == 0) {
                            level.sendParticles(ParticleTypes.SNOWFLAKE,
                                    animal.getX(), animal.getY() + animal.getBbHeight() / 2, animal.getZ(),
                                    2, 0.2, 0.2, 0.2, 0.01);
                        }
                    }
                }
            }

            if (changed) {
                FrozenAnimalDataAttachment.setData(level, data);
            }
        }
    }

    public static class VillagerSweatEvent extends CatamountEvent {
        public VillagerSweatEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            List<Villager> villagers = findNearbyEntities(player, Villager.class, 32.0);

            for (Villager villager : villagers) {
                player.serverLevel().sendParticles(ParticleTypes.SPLASH,
                        villager.getX(), villager.getY() + 2, villager.getZ(),
                        3, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

    public static class FootstepsEvent extends CatamountEvent {
        public FootstepsEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            BlockPos nearbyPos = player.blockPosition().offset(
                    player.getRandom().nextInt(10) - 5,
                    0,
                    player.getRandom().nextInt(10) - 5
            );

            BlockState groundBlock = player.serverLevel().getBlockState(nearbyPos.below());
            SoundType soundType = groundBlock.getSoundType();

            player.serverLevel().playSound(null, nearbyPos,
                    soundType.getStepSound(),
                    SoundSource.AMBIENT,
                    0.5f, 1.0f);
        }
    }

    public static class PaleAnimalsEvent extends CatamountEvent {
        private static final int DURATION = 20 * 10;

        public PaleAnimalsEvent() {
            super(EventType.PASSIVE, false);
        }

        @Override
        public void execute(ServerPlayer player) {
            List<Animal> animals = findNearbyEntities(player, Animal.class, 32.0);
            ServerLevel level = player.serverLevel();

            if (animals.isEmpty()) return;

            long endTime = level.getGameTime() + DURATION;
            PaleAnimalDataAttachment.Data data = PaleAnimalDataAttachment.getData(level);

            for (Animal animal : animals) {
                data = data.withPaleAnimal(animal.getUUID(), endTime);

                animal.hurt(player.damageSources().magic(), 0.0f);

                level.sendParticles(ParticleTypes.WHITE_SMOKE,
                        animal.getX(), animal.getY() + animal.getBbHeight() / 2, animal.getZ(),
                        5, 0.3, 0.3, 0.3, 0.01);
            }

            PaleAnimalDataAttachment.setData(level, data);

            PaleAnimalDataAttachment.syncToTrackingPlayers(level, data, player);
        }

        public static void tickPaleAnimals(ServerLevel level) {
            PaleAnimalDataAttachment.Data data = PaleAnimalDataAttachment.getData(level);
            long currentTime = level.getGameTime();
            boolean changed = false;

            for (UUID uuid : new ArrayList<>(data.paleAnimals().keySet())) {
                long endTime = data.paleAnimals().get(uuid);

                if (currentTime >= endTime) {
                    data = data.withoutPaleAnimal(uuid);
                    changed = true;
                }
            }

            if (changed) {
                PaleAnimalDataAttachment.setData(level, data);
                PaleAnimalDataAttachment.syncToAll(level, data);
            }
        }
    }

    public static class ExtraDamageEvent extends CatamountEvent {
        public ExtraDamageEvent() {
            super(EventType.PASSIVE, true, 2);
        }

        @Override
        public void execute(ServerPlayer player) {
            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);
            CatamountPlayerDataAttachment.setData(player, data.withExtraDamageTimer(1200));
        }
    }
}