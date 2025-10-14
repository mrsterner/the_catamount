package dev.sterner.the_catamount.neoforge;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.data_attachment.TCDataAttachmentsNeoForge;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.events.ModEventHandlers;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import dev.sterner.the_catamount.payload.SyncCatamountPlayerDataPayload;
import dev.sterner.the_catamount.registry.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(TheCatamount.MOD_ID)
public class TheCatamountNeoForge {

    private final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheCatamount.MOD_ID);
    private final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TheCatamount.MOD_ID);
    private final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TheCatamount.MOD_ID);
    private final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, TheCatamount.MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheCatamount.MOD_ID);
    private final DeferredRegister<DataComponentType<?>> DATAS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TheCatamount.MOD_ID);


    public TheCatamountNeoForge(IEventBus eventBus) {
        CREATIVE_TABS.register("main", () -> new TCCreativeTabs().createMain());
        ENTITY_TYPES.register("catamount", () -> TCEntityTypes.CATAMOUNT);

        ITEMS.register("beast_ivory", () -> TCItems.BEAST_IVORY);
        ITEMS.register("white_ash", () -> TCItems.WHITE_ASH);
        ITEMS.register("bone_heap", () -> TCItems.BONE_HEAP);
        ITEMS.register("suspicious_dirt", () -> TCItems.SUSPICIOUS_DIRT);
        ITEMS.register("petroglyph", () -> TCItems.PETROGLYPH);

        ITEMS.register("monstrous_remains", () -> TCItems.MONSTROUS_REMAINS);
        ITEMS.register("monstrous_skull", () -> TCItems.MONSTROUS_SKULL);

        BLOCKS.register("white_ash", () -> TCBlocks.WHITE_ASH);
        BLOCKS.register("bone_heap", () -> TCBlocks.BONE_HEAP);
        BLOCKS.register("suspicious_dirt", () -> TCBlocks.SUSPICIOUS_DIRT);
        BLOCKS.register("petroglyph", () -> TCBlocks.PETROGLYPH);
        BLOCKS.register("monstrous_remains", () -> TCBlocks.MONSTROUS_REMAINS);
        BLOCKS.register("monstrous_skull", () -> TCBlocks.MONSTROUS_SKULL);

        DATAS.register("white_ash_coated", () -> TCDataComponents.WHITE_ASH_COATED);
        BLOCK_ENTITY_TYPES.register("suspicious_dirt", () -> TCBlockEntityTypes.SUSPICIOUS_DIRT);
        BLOCK_ENTITY_TYPES.register("monstrous_remains", () -> TCBlockEntityTypes.MONSTROUS_REMAINS);

        TCDataAttachmentsNeoForge.ATTACHMENT_TYPES.register(eventBus);
        ENTITY_TYPES.register(eventBus);
        DATAS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        CREATIVE_TABS.register(eventBus);
        TheCatamount.init();

        eventBus.addListener(TheCatamountNeoForge::onRegisterPayloadHandlers);
        eventBus.addListener(TheCatamountNeoForge::onEntityAttribute);
    }

    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event){
        var registrar = event.registrar("1");
        registrar.playToClient(SyncCatamountPlayerDataPayload.ID, SyncCatamountPlayerDataPayload.STREAM_CODEC, (payload, ctx) -> payload.handleS2C());
    }

    public static void onEntityAttribute(EntityAttributeCreationEvent event) {
        event.put(TCEntityTypes.CATAMOUNT, CatamountEntity.createAttributes().build() );
    }

    @EventBusSubscriber(modid = TheCatamount.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {

        @SubscribeEvent
        public static void registerListeners(AddReloadListenerEvent event) {
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
    }
}
