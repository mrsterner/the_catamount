package dev.sterner.the_catamount.data_attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.sterner.the_catamount.TheCatamount;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CatamountPlayerDataAttachment {

    @ExpectPlatform
    public static Data getData(Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setData(Player player, Data data) {
        throw new AssertionError();
    }


    public static void sync(Player player, Data data) {

    }

    public record Data(int points){

        public Data() {
            this(0);
        }

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("duration").forGetter(d -> d.points)
        ).apply(instance, Data::new));

        public static final ResourceLocation ID = TheCatamount.id("catamount_player_data");
    }
}
