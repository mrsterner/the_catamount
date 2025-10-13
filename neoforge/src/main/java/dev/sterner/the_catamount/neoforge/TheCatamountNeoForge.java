package dev.sterner.the_catamount.neoforge;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.listener.SoulConversionListener;
import dev.sterner.the_catamount.registry.TCBlocks;
import dev.sterner.the_catamount.registry.TCCreativeTabs;
import dev.sterner.the_catamount.registry.TCEntityTypes;
import dev.sterner.the_catamount.registry.TCItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(TheCatamount.MOD_ID)
public class TheCatamountNeoForge {

    private final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheCatamount.MOD_ID);
    private final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TheCatamount.MOD_ID);
    private final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TheCatamount.MOD_ID);
    private final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, TheCatamount.MOD_ID);


    public TheCatamountNeoForge(IEventBus eventBus) {
        CREATIVE_TABS.register("main", () -> new TCCreativeTabs().createMain());
        ENTITY_TYPES.register("catamount", () -> TCEntityTypes.CATAMOUNT);

        ITEMS.register("beast_ivory", () -> TCItems.BEAST_IVORY);
        ITEMS.register("white_ash", () -> TCItems.WHITE_ASH);
        ITEMS.register("bone_heap", () -> TCItems.BONE_HEAP);

        BLOCKS.register("white_ash", () -> TCBlocks.WHITE_ASH);
        BLOCKS.register("bone_heap", () -> TCBlocks.BONE_HEAP);


        ENTITY_TYPES.register(eventBus);
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        CREATIVE_TABS.register(eventBus);
        TheCatamount.init();
    }

    @SubscribeEvent
    public static void registerListeners(AddReloadListenerEvent event) {
        event.addListener(SoulConversionListener.LOADER);
    }
}
