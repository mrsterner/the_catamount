package dev.sterner.the_catamount.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.sterner.the_catamount.catamount_events.*;
import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TCCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("catamount")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug")

                        .then(Commands.literal("status")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    CatamountPlayerDataAttachment.Data data =
                                            CatamountPlayerDataAttachment.getData(player);

                                    player.sendSystemMessage(Component.literal("=== Catamount Status ===")
                                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                                    player.sendSystemMessage(Component.literal("Stage: " + data.catamountStage())
                                            .withStyle(ChatFormatting.WHITE));
                                    player.sendSystemMessage(Component.literal("Points: " + data.points())
                                            .withStyle(ChatFormatting.WHITE));
                                    player.sendSystemMessage(Component.literal("Event Cooldown: " + data.getEventCooldown() + " ticks")
                                            .withStyle(ChatFormatting.WHITE));
                                    player.sendSystemMessage(Component.literal("Extra Damage Timer: " + data.getExtraDamageTimer() + " ticks")
                                            .withStyle(ChatFormatting.WHITE));
                                    player.sendSystemMessage(Component.literal("Death Cooldown: " + data.deathCooldownTimer() + " ticks")
                                            .withStyle(ChatFormatting.WHITE));
                                    player.sendSystemMessage(Component.literal("Feeding Frenzy Cooldown: " + data.getFeedingFrenzyCooldown() + " ticks")
                                            .withStyle(ChatFormatting.WHITE));

                                    if (data.catamountUUID().isPresent()) {
                                        player.sendSystemMessage(Component.literal("Catamount Manifested: YES")
                                                .withStyle(ChatFormatting.RED));
                                    } else {
                                        player.sendSystemMessage(Component.literal("Catamount Manifested: NO")
                                                .withStyle(ChatFormatting.GREEN));
                                    }
                                    return 1;
                                })
                        )

                        .then(Commands.literal("trigger")
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int count = IntegerArgumentType.getInteger(context, "count");
                                            DebugUsage.debugTriggerBatch(player, count);
                                            player.sendSystemMessage(Component.literal("Triggered " + count + " events!")
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
                                            player.sendSystemMessage(Component.literal("Spawned stage " + stage + " catamount!")
                                                    .withStyle(ChatFormatting.RED));
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("extradamage")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugExtraDamage(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("soulfire")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    DebugUsage.debugSoulFireConversion(player);
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

                                    player.sendSystemMessage(Component.literal("Cleared event cooldown!")
                                            .withStyle(ChatFormatting.GREEN));
                                    return 1;
                                })
                        )

                        .then(Commands.literal("resetdata")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    CatamountPlayerDataAttachment.setData(player,
                                            new CatamountPlayerDataAttachment.Data());

                                    player.sendSystemMessage(Component.literal("Reset all catamount data!")
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
                                        player.sendSystemMessage(Component.literal("Advanced to stage " + data.catamountStage() + "!")
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

    static class DebugUsage{
        public static void triggerSpecificEvent(ServerPlayer player) {
            CatamountEvent event = new PassiveEvents.ExtraDamageEvent();
            if (event.canExecute(player)) {
                event.execute(player);
            }
        }

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

        public static void debugExtraDamage(ServerPlayer player) {
            new PassiveEvents.ExtraDamageEvent().execute(player);
            player.sendSystemMessage(Component.literal("Extra damage active for 1 minute!")
                    .withStyle(ChatFormatting.GOLD));
        }

        public static void debugSoulFireConversion(ServerPlayer player) {
            new PassiveEvents.SoulFireConversionEvent().execute(player);
            player.sendSystemMessage(Component.literal("Soul fire conversion triggered!")
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
