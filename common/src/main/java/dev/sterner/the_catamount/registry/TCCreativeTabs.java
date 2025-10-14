package dev.sterner.the_catamount.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TCCreativeTabs {

    public CreativeModeTab createMain(){
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4)
                .title(Component.translatable("itemGroup.the_catamount"))
                .icon(() -> new ItemStack(TCItems.BEAST_IVORY))
                .displayItems((itemDisplayParameters, output) -> {
                    output.accept(TCItems.BEAST_IVORY);
                    output.accept(TCItems.SUSPICIOUS_DIRT);
                    output.accept(TCItems.BONE_HEAP);
                    output.accept(TCItems.WHITE_ASH);

                }).build();
    }
}
