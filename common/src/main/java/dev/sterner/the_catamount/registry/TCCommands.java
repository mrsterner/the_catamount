package dev.sterner.the_catamount.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.sterner.the_catamount.PlatformHelper;
import dev.sterner.the_catamount.catamount_events.*;
import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.payload.FogEffectPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TCCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("catamount")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug")

                        .then(Commands.literal("trigger")
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int count = IntegerArgumentType.getInteger(context, "count");
                                            DebugUsage.debugTriggerBatch(player, count);
                                            player.sendSystemMessage(Component.literal("Triggered " + count + " events")
                                                    .withStyle(ChatFormatting.GREEN));
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("spawn")
                                .then(Commands.argument("stage", IntegerArgumentType.integer(3, 5))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int stage = IntegerArgumentType.getInteger(context, "stage");
                                            DebugUsage.debugSpawnCatamount(player, stage);
                                            player.sendSystemMessage(Component.literal("Spawned stage " + stage + " catamount")
                                                    .withStyle(ChatFormatting.RED));
                                            return 1;
                                        })
                                )
                        )

                        // Passive Events
                        .then(Commands.literal("wind")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugWind(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("soul_conversion")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugSoulFireConversion(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("spiritface")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugSpiritFace(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("lightorb")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugLightOrb(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("animalstare")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugAnimalStare(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("villagersweat")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugVillagerSweat(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("footsteps")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugFootsteps(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("pale")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int count = DebugUsage.debugPaleAnimals(player);
                                    player.sendSystemMessage(Component.literal("Made " + count + " animals pale")
                                            .withStyle(ChatFormatting.GRAY));
                                    return 1;
                                })
                        )

                        .then(Commands.literal("extradamage")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugExtraDamage(player);
                                    return 1;
                                })
                        )

                        // Damaging Events
                        .then(Commands.literal("stealfood")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugStealFood(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("dangerousleaves")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugDangerousLeaves(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("dangerouswind")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugDangerousWind(player);
                                    return 1;
                                })
                        )

                        // Dangerous Events
                        .then(Commands.literal("eyesindark")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugEyesInDark(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("feedingfrenzy")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugFeedingFrenzy(player);
                                    return 1;
                                })
                        )

                        // Deadly Events
                        .then(Commands.literal("bloodparticles")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugBloodParticles(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("boneheap")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugBoneHeap(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("multiplewinds")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugMultipleWinds(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("nightmanifestation")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugNightManifestation(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("fog")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugFog(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("freeze")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int count = DebugUsage.debugFreezeAnimals(player);
                                    player.sendSystemMessage(Component.literal("Froze " + count + " animals")
                                            .withStyle(ChatFormatting.AQUA));
                                    return 1;
                                })
                        )

                        .then(Commands.literal("setstage")
                                .then(Commands.argument("stage", IntegerArgumentType.integer(-1, 5))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int stage = IntegerArgumentType.getInteger(context, "stage");

                                            CatamountPlayerDataAttachment.Data data =
                                                    CatamountPlayerDataAttachment.getData(player);
                                            CatamountPlayerDataAttachment.setData(player,
                                                    data.withCatamountStage(stage));

                                            player.sendSystemMessage(Component.literal("Set catamount stage to " + stage)
                                                    .withStyle(ChatFormatting.GOLD));
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("setpoints")
                                .then(Commands.argument("points", IntegerArgumentType.integer(0, 1000))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int points = IntegerArgumentType.getInteger(context, "points");

                                            CatamountPlayerDataAttachment.Data data =
                                                    CatamountPlayerDataAttachment.getData(player);
                                            CatamountPlayerDataAttachment.setData(player,
                                                    data.withPoints(points));

                                            player.sendSystemMessage(Component.literal("Set points to " + points)
                                                    .withStyle(ChatFormatting.GOLD));
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("clearcooldown")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    CatamountPlayerDataAttachment.Data data =
                                            CatamountPlayerDataAttachment.getData(player);
                                    CatamountPlayerDataAttachment.setData(player,
                                            data.withEventCooldown(0));

                                    player.sendSystemMessage(Component.literal("Cleared event cooldown")
                                            .withStyle(ChatFormatting.GREEN));
                                    return 1;
                                })
                        )

                        .then(Commands.literal("resetdata")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    CatamountPlayerDataAttachment.setData(player,
                                            new CatamountPlayerDataAttachment.Data());

                                    player.sendSystemMessage(Component.literal("Reset all catamount data")
                                            .withStyle(ChatFormatting.YELLOW));
                                    return 1;
                                })
                        )

                        .then(Commands.literal("advance")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    boolean advanced = CatamountPlayerDataAttachment.tryAdvanceStage(player);

                                    if (advanced) {
                                        CatamountPlayerDataAttachment.Data data =
                                                CatamountPlayerDataAttachment.getData(player);
                                        player.sendSystemMessage(Component.literal("Advanced to stage " + data.catamountStage())
                                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                                    } else {
                                        player.sendSystemMessage(Component.literal("Not enough points to advance stage")
                                                .withStyle(ChatFormatting.RED));
                                    }

                                    return advanced ? 1 : 0;
                                })
                        )

                        .then(Commands.literal("listevents")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    CatamountPlayerDataAttachment.Data data =
                                            CatamountPlayerDataAttachment.getData(player);

                                    player.sendSystemMessage(Component.literal("=== Available Events ===")
                                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                                    player.sendSystemMessage(Component.literal("Passive Events: " +
                                                    PassiveEvents.getAll().size())
                                            .withStyle(ChatFormatting.GREEN));

                                    if (data.catamountStage() >= 1) {
                                        player.sendSystemMessage(Component.literal("Damaging Events: " +
                                                        DamagingEvents.getAll().size())
                                                .withStyle(ChatFormatting.YELLOW));
                                    }

                                    if (data.catamountStage() >= 2) {
                                        player.sendSystemMessage(Component.literal("Dangerous Events: " +
                                                        DangerousEvents.getAll().size())
                                                .withStyle(ChatFormatting.GOLD));
                                    }

                                    if (data.catamountStage() >= 3) {
                                        player.sendSystemMessage(Component.literal("Deadly Events: " +
                                                        DeadlyEvents.getAll().size())
                                                .withStyle(ChatFormatting.RED));
                                    }

                                    return 1;
                                })
                        )
                )
        );
    }

    static class DebugUsage {

        // === PASSIVE EVENTS ===

        public static void debugWind(ServerPlayer player) {
            new PassiveEvents.SummonTheWindEvent().execute(player);
            player.sendSystemMessage(Component.literal("Summoned the wind")
                    .withStyle(ChatFormatting.AQUA));
        }

        public static void debugSoulFireConversion(ServerPlayer player) {
            new PassiveEvents.SoulFireConversionEvent().execute(player);
            player.sendSystemMessage(Component.literal("Soul fire conversion triggered")
                    .withStyle(ChatFormatting.AQUA));
        }

        public static void debugSpiritFace(ServerPlayer player) {
            new PassiveEvents.FaintSpiritEvent().execute(player);
            player.sendSystemMessage(Component.literal("Spawned spirit face")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        public static void debugLightOrb(ServerPlayer player) {
            new PassiveEvents.BallOfLightEvent().execute(player);
            player.sendSystemMessage(Component.literal("Spawned ball of light")
                    .withStyle(ChatFormatting.YELLOW));
        }

        public static void debugAnimalStare(ServerPlayer player) {
            new PassiveEvents.AnimalStareEvent().execute(player);
            player.sendSystemMessage(Component.literal("Animals are staring")
                    .withStyle(ChatFormatting.WHITE));
        }

        public static void debugVillagerSweat(ServerPlayer player) {
            new PassiveEvents.VillagerSweatEvent().execute(player);
            player.sendSystemMessage(Component.literal("Villagers are sweating")
                    .withStyle(ChatFormatting.GREEN));
        }

        public static void debugFootsteps(ServerPlayer player) {
            new PassiveEvents.FootstepsEvent().execute(player);
            player.sendSystemMessage(Component.literal("Mysterious footsteps")
                    .withStyle(ChatFormatting.GRAY));
        }

        public static int debugPaleAnimals(ServerPlayer player) {
            PassiveEvents.PaleAnimalsEvent event = new PassiveEvents.PaleAnimalsEvent();
            event.execute(player);

            List<?> animals = player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.animal.Animal.class,
                    player.getBoundingBox().inflate(32.0)
            );

            return animals.size();
        }

        public static void debugExtraDamage(ServerPlayer player) {
            new PassiveEvents.ExtraDamageEvent().execute(player);
            player.sendSystemMessage(Component.literal("Extra damage active for 1 minute")
                    .withStyle(ChatFormatting.GOLD));
        }

        // === DAMAGING EVENTS ===

        public static void debugStealFood(ServerPlayer player) {
            new DamagingEvents.StealFoodEvent().execute(player);
            player.sendSystemMessage(Component.literal("Food stolen from nearby containers")
                    .withStyle(ChatFormatting.GOLD));
        }

        public static void debugDangerousLeaves(ServerPlayer player) {
            new DamagingEvents.DangerousLeavesEvent().execute(player);
            player.sendSystemMessage(Component.literal("Nearby leaves are now dangerous")
                    .withStyle(ChatFormatting.RED));
        }

        public static void debugDangerousWind(ServerPlayer player) {
            new DamagingEvents.DangerousWindEvent().execute(player);
            player.sendSystemMessage(Component.literal("Spawned dangerous wind")
                    .withStyle(ChatFormatting.RED));
        }

        // === DANGEROUS EVENTS ===

        public static void debugEyesInDark(ServerPlayer player) {
            new DangerousEvents.EyesInDarkEvent().execute(player);
            player.sendSystemMessage(Component.literal("Eyes watching from the dark")
                    .withStyle(ChatFormatting.DARK_RED));
        }

        public static void debugFeedingFrenzy(ServerPlayer player) {
            new DangerousEvents.FeedingFrenzyEvent().execute(player);
            player.sendSystemMessage(Component.literal("Feeding frenzy activated")
                    .withStyle(ChatFormatting.DARK_RED));
        }

        // === DEADLY EVENTS ===

        public static void debugBloodParticles(ServerPlayer player) {
            new DeadlyEvents.BloodParticlesEvent().execute(player);
            player.sendSystemMessage(Component.literal("Blood particles falling")
                    .withStyle(ChatFormatting.DARK_RED));
        }

        public static void debugBoneHeap(ServerPlayer player) {
            new DeadlyEvents.BoneHeapEvent().execute(player);
            player.sendSystemMessage(Component.literal("Bone heaps spawned")
                    .withStyle(ChatFormatting.WHITE));
        }

        public static void debugMultipleWinds(ServerPlayer player) {
            new DeadlyEvents.MultipleWindGustsEvent().execute(player);
            player.sendSystemMessage(Component.literal("Multiple wind gusts spawned")
                    .withStyle(ChatFormatting.AQUA));
        }

        public static void debugNightManifestation(ServerPlayer player) {
            new DeadlyEvents.NightManifestationEvent().execute(player);
            player.sendSystemMessage(Component.literal("Night manifestation summoned")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        }

        public static void debugFog(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            BlockPos center = player.blockPosition();

            List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class,
                    AABB.ofSize(Vec3.atCenterOf(center), 64, 64, 64));

            for (ServerPlayer nearbyPlayer : nearbyPlayers) {
                FogEffectPayload payload = new FogEffectPayload(
                        Vec3.atCenterOf(center),
                        64.0f,
                        600
                );
                PlatformHelper.sendPayloadToPlayer(nearbyPlayer, payload);
            }

            player.sendSystemMessage(Component.literal("Fog effect applied")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        public static int debugFreezeAnimals(ServerPlayer player) {
            PassiveEvents.AnimalStareEvent event = new PassiveEvents.AnimalStareEvent();
            event.execute(player);

            List<?> animals = player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.animal.Animal.class,
                    player.getBoundingBox().inflate(32.0)
            );

            return animals.size();
        }

        // === UTILITY ===

        public static void debugTriggerBatch(ServerPlayer player, int count) {
            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);

            List<CatamountEvent> validEvents = new ArrayList<>();
            validEvents.addAll(PassiveEvents.getAll());

            if (data.catamountStage() >= 1) {
                validEvents.addAll(DamagingEvents.getAll());
            }
            if (data.catamountStage() >= 2) {
                validEvents.addAll(DangerousEvents.getAll());
            }
            if (data.catamountStage() >= 3) {
                validEvents.addAll(DeadlyEvents.getAll());
            }

            Collections.shuffle(validEvents);

            for (int i = 0; i < Math.min(count, validEvents.size()); i++) {
                validEvents.get(i).execute(player);
            }
        }

        public static void debugSpawnCatamount(ServerPlayer player, int stage) {
            ServerLevel level = player.serverLevel();
            BlockPos spawnPos = player.blockPosition().offset(5, 0, 5);

            CatamountEntity catamount = new CatamountEntity(TCEntityTypes.CATAMOUNT, level);
            catamount.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            catamount.setStage(stage);
            catamount.setOwnerUUID(Optional.of(player.getUUID()));

            level.addFreshEntity(catamount);

            CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);
            CatamountPlayerDataAttachment.setData(player,
                    data.withCatamountUUID(Optional.of(catamount.getUUID())));
        }
    }
}