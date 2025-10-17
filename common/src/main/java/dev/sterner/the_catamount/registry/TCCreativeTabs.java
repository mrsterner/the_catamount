package dev.sterner.the_catamount.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TCCreativeTabs {

    public CreativeModeTab createMain(){
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4)
                .title(Component.translatable("itemGroup.the_catamount"))
                .icon(() -> new ItemStack(TCItems.MONSTROUS_SKULL))
                .displayItems((itemDisplayParameters, output) -> {
                    output.accept(TCItems.BEAST_IVORY);
                    output.accept(TCItems.SUSPICIOUS_DIRT);
                    output.accept(TCItems.DEVOUR_PETROGLYPH);
                    output.accept(TCItems.AWAKEN_PETROGLYPH);
                    output.accept(TCItems.LIGHTENING_PETROGLYPH);
                    output.accept(TCItems.SLAIN_PETROGLYPH);
                    output.accept(TCItems.BONE_HEAP);
                    output.accept(TCItems.WHITE_ASH);
                    output.accept(TCItems.MONSTROUS_REMAINS);
                    output.accept(TCItems.MONSTROUS_SKULL);

                }).build();
    }
}
