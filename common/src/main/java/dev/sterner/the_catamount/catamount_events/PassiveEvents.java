package dev.sterner.the_catamount.catamount_events;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.data_attachment.FrozenAnimalDataAttachment;
import dev.sterner.the_catamount.data_attachment.PaleAnimalDataAttachment;
import dev.sterner.the_catamount.data_attachment.SoulConversionDataAttachment;
import dev.sterner.the_catamount.entity.LightOrbEntity;
import dev.sterner.the_catamount.entity.WindEntity;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
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
                    SoulConversionDataAttachment.ConversionRecord record =
                            new SoulConversionDataAttachment.ConversionRecord(state, endTime);

                    data = data.withConversion(immutablePos, record);
                    SoulConversionListener.convertBlock(level, immutablePos, block);
                    foundAny = true;
                }
            }

            if (foundAny) {
                SoulConversionDataAttachment.setData(level, data);
                level.playSound(null, center, SoundEvents.SOUL_ESCAPE.value(),
                        SoundSource.AMBIENT, 0.8f, 0.8f);
            }
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
                    level.setBlock(pos, record.originalState(), 3);

                    RandomSource random = level.getRandom();
                    for (int i = 0; i < 3; i++) {
                        level.sendParticles(ParticleTypes.SMOKE,
                                pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                                pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                                1, 0, 0, 0, 0);
                    }

                    data = data.withoutConversion(pos);
                    changed = true;
                }
            }

            if (changed) {
                SoulConversionDataAttachment.setData(level, data);
            }
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

            BlockPos particlePos = findValidSurface(level, location);
            if (particlePos == null) return;

            createFaceParticles(level, particlePos);

            level.playSound(null, particlePos, SoundEvents.SOUL_ESCAPE.value(),
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

        //TODO Particles is not ideal, texture is better
        private void createFaceParticles(ServerLevel level, BlockPos pos) {
            level.sendParticles(ParticleTypes.SOUL,
                    pos.getX() + 0.3, pos.getY() + 0.7, pos.getZ() + 0.5,
                    3, 0.05, 0.05, 0.05, 0.0);

            level.sendParticles(ParticleTypes.SOUL,
                    pos.getX() + 0.7, pos.getY() + 0.7, pos.getZ() + 0.5,
                    3, 0.05, 0.05, 0.05, 0.0);

            for (double x = 0.3; x <= 0.7; x += 0.1) {
                double y = 0.4 - Math.abs(x - 0.5) * 0.2;
                level.sendParticles(ParticleTypes.SOUL,
                        pos.getX() + x, pos.getY() + y, pos.getZ() + 0.5,
                        1, 0.02, 0.02, 0.02, 0.0);
            }
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