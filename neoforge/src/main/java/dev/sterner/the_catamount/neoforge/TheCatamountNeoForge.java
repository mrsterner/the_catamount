package dev.sterner.the_catamount.neoforge;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.data_attachment.PaleAnimalDataAttachment;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.entity.DevouredEntity;
import dev.sterner.the_catamount.entity.WindEntity;
import dev.sterner.the_catamount.events.ModEventHandlers;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import dev.sterner.the_catamount.payload.*;
import dev.sterner.the_catamount.registry.*;
import dev.sterner.the_catamount.registry.neoforge.TCParticlesNeoForge;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

@Mod(TheCatamount.MOD_ID)
public class TheCatamountNeoForge {

    private final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheCatamount.MOD_ID);
    private final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TheCatamount.MOD_ID);
    private final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TheCatamount.MOD_ID);
    private final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, TheCatamount.MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheCatamount.MOD_ID);
    private final DeferredRegister<DataComponentType<?>> DATAS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TheCatamount.MOD_ID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, TheCatamount.MOD_ID);
    public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(Registries.SENSOR_TYPE, TheCatamount.MOD_ID);

    public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, TheCatamount.MOD_ID);


    public TheCatamountNeoForge(IEventBus eventBus) {
        CREATIVE_TABS.register("main", () -> new TCCreativeTabs().createMain());
        ENTITY_TYPES.register("catamount", () -> TCEntityTypes.CATAMOUNT);
        ENTITY_TYPES.register("devoured", () -> TCEntityTypes.DEVOURED);
        ENTITY_TYPES.register("wind", () -> TCEntityTypes.WIND);
        ENTITY_TYPES.register("light_orb", () -> TCEntityTypes.LIGHT_ORB);

        ITEMS.register("beast_ivory", () -> TCItems.BEAST_IVORY);
        ITEMS.register("white_ash", () -> TCItems.WHITE_ASH);
        ITEMS.register("bone_heap", () -> TCItems.BONE_HEAP);
        ITEMS.register("suspicious_dirt", () -> TCItems.SUSPICIOUS_DIRT);
        ITEMS.register("devour_petroglyph", () -> TCItems.DEVOUR_PETROGLYPH);
        ITEMS.register("awaken_petroglyph", () -> TCItems.AWAKEN_PETROGLYPH);
        ITEMS.register("lightening_petroglyph", () -> TCItems.LIGHTENING_PETROGLYPH);
        ITEMS.register("slain_petroglyph", () -> TCItems.SLAIN_PETROGLYPH);

        ITEMS.register("monstrous_remains", () -> TCItems.MONSTROUS_REMAINS);
        ITEMS.register("monstrous_skull", () -> TCItems.MONSTROUS_SKULL);

        BLOCKS.register("white_ash", () -> TCBlocks.WHITE_ASH);
        BLOCKS.register("bone_heap", () -> TCBlocks.BONE_HEAP);
        BLOCKS.register("suspicious_dirt", () -> TCBlocks.SUSPICIOUS_DIRT);
        BLOCKS.register("devour_petroglyph", () -> TCBlocks.DEVOUR_PETROGLYPH);
        BLOCKS.register("awaken_petroglyph", () -> TCBlocks.AWAKEN_PETROGLYPH);
        BLOCKS.register("lightening_petroglyph", () -> TCBlocks.LIGHTENING_PETROGLYPH);
        BLOCKS.register("slain_petroglyph", () -> TCBlocks.SLAIN_PETROGLYPH);
        BLOCKS.register("monstrous_remains", () -> TCBlocks.MONSTROUS_REMAINS);
        BLOCKS.register("monstrous_skull", () -> TCBlocks.MONSTROUS_SKULL);

        DATAS.register("white_ash_coated", () -> TCDataComponents.WHITE_ASH_COATED);
        BLOCK_ENTITY_TYPES.register("suspicious_dirt", () -> TCBlockEntityTypes.SUSPICIOUS_DIRT);
        BLOCK_ENTITY_TYPES.register("monstrous_remains", () -> TCBlockEntityTypes.MONSTROUS_REMAINS);

        SENSORS.register("catamount_sensor", () -> TCSensorTypes.CATAMOUNT_SENSOR);
        SERIALIZERS.register("attack_type",
                () -> EntityDataSerializer.forValueType(CatamountEntity.AttackType.STREAM_CODEC)
        );

        TCDataAttachmentsNeoForge.ATTACHMENT_TYPES.register(eventBus);
        ENTITY_TYPES.register(eventBus);
        DATAS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        CREATIVE_TABS.register(eventBus);
        MOB_EFFECTS.register(eventBus);
        SENSORS.register(eventBus);
        SERIALIZERS.register(eventBus);
        TheCatamount.init();
        TCParticlesNeoForge.init(eventBus);

        eventBus.addListener(this::commonSetup);
        eventBus.addListener(TheCatamountNeoForge::onRegisterPayloadHandlers);
        eventBus.addListener(TheCatamountNeoForge::onEntityAttribute);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(TCParticlesNeoForge::assignParticles);
    }

    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event){
        var registrar = event.registrar("1");
        registrar.playToClient(SyncCatamountPlayerDataPayload.ID, SyncCatamountPlayerDataPayload.STREAM_CODEC, (payload, ctx) -> payload.handleS2C());
        registrar.playToClient(PaleAnimalSyncPayload.ID, PaleAnimalSyncPayload.STREAM_CODEC, (payload, ctx) -> payload.handleS2C());
        registrar.playToClient(SyncPaleAnimalDataPayload.ID, SyncPaleAnimalDataPayload.STREAM_CODEC, (payload, ctx) -> payload.handleS2C());
        registrar.playToClient(EventTriggeredPayload.ID, EventTriggeredPayload.STREAM_CODEC, (payload, ctx) -> payload.handleS2C());
        registrar.playToClient(FogEffectPayload.ID, FogEffectPayload.STREAM_CODEC, (payload, ctx) -> payload.handleS2C());
    }

    public static void onEntityAttribute(EntityAttributeCreationEvent event) {
        event.put(TCEntityTypes.CATAMOUNT, CatamountEntity.createAttributes().build() );
        event.put(TCEntityTypes.DEVOURED, DevouredEntity.createAttributes().build() );
        event.put(TCEntityTypes.WIND, WindEntity.createAttributes().build() );
    }

    @EventBusSubscriber(modid = TheCatamount.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {

        @SubscribeEvent
        public static void registerListeners(AddReloadListenerEvent event) {
            TheCatamount.LOGGER.info("Registering SoulConversionListener");
            event.addListener(SoulConversionListener.LOADER);
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingDamageEvent.Pre event) {
            float newAmount = ModEventHandlers.onLivingDamage(
                    event.getEntity(),
                    event.getSource(),
                    event.getOriginalDamage()
            );
            event.setNewDamage(newAmount);
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            ModEventHandlers.onLivingDeath(event.getEntity(), event.getSource());
        }

        @SubscribeEvent
        public static void onCommandsRegister(RegisterCommandsEvent event) {
            TCCommands.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onServerLevelTick(LevelTickEvent.Post event) {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                ModEventHandlers.onServerLevelTick(serverLevel);
            }
        }

        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            Level level = event.getEntity().level();
            if (level instanceof ServerLevel serverLevel && event.getEntity() instanceof ServerPlayer player) {
                PaleAnimalDataAttachment.Data paleData = PaleAnimalDataAttachment.getData(serverLevel);
                PaleAnimalDataAttachment.sync(serverLevel, paleData, List.of(player));
            }
        }
    }
}