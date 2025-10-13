package dev.sterner.the_catamount.listener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SoulConversionListener {

    public static final SoulfireConversionResourceListener LOADER = new SoulfireConversionResourceListener(new Gson(), "soul_conversion");
    public static final Map<Block, Block> CONVERSION_PAIR = new HashMap<>();

    public static class SoulfireConversionResourceListener extends SimpleJsonResourceReloadListener {

        public SoulfireConversionResourceListener(Gson gson, String directory) {
            super(gson, directory);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
            for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
                ResourceLocation file = entry.getKey();
                JsonElement element = entry.getValue();

                try {
                    if (element.isJsonArray()) {
                        for (JsonElement e : element.getAsJsonArray()) {
                            parseJson(e.getAsJsonObject(), file);
                        }
                    } else if (element.isJsonObject()) {
                        parseJson(element.getAsJsonObject(), file);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(e.fillInStackTrace());
                }
            }
        }

        private void parseJson(JsonObject json, ResourceLocation file) {
            String blockJson = json.has("fromBlock") ? json.get("fromBlock").getAsString() : null;
            String targetJson = json.has("toBlock") ? json.get("toBlock").getAsString() : null;

            if (blockJson != null && targetJson != null) {
                ResourceLocation fromId = ResourceLocation.tryParse(blockJson);
                ResourceLocation toId = ResourceLocation.tryParse(targetJson);
                if (fromId == null || toId == null) return;

                ConversionData data = ConversionData.CODEC.decode(JsonOps.INSTANCE, json)
                        .getOrThrow(IllegalArgumentException::new)
                        .getFirst();

                Optional<Block> fromBlock = BuiltInRegistries.BLOCK.getOptional(data.fromBlock);
                Optional<Block> toBlock = BuiltInRegistries.BLOCK.getOptional(data.toBlock);

                if (fromBlock.isPresent() && toBlock.isPresent()) {
                    CONVERSION_PAIR.put(fromBlock.get(), toBlock.get());
                }
            }
        }
    }

    public record ConversionData(ResourceLocation fromBlock, ResourceLocation toBlock) {

        public static final Codec<ConversionData> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC.fieldOf("fromBlock").forGetter(d -> d.fromBlock),
                            ResourceLocation.CODEC.fieldOf("toBlock").forGetter(d -> d.toBlock)
                    ).apply(instance, ConversionData::new)
            );
        }

    private static BlockState copySharedProperties(BlockState fromState, BlockState toState) {
        for (Property<?> property : fromState.getProperties()) {
            Property<?> targetProperty = toState.getBlock().getStateDefinition().getProperty(property.getName());
            if (targetProperty != null) {
                toState = setPropertySafe(toState, targetProperty, fromState.getValue(property));
            }
        }
        return toState;
    }

    private static <T extends Comparable<T>> BlockState setPropertySafe(BlockState state, Property<T> property, Comparable<?> value) {
        try {
            @SuppressWarnings("unchecked")
            T castedValue = (T) value;
            return state.setValue(property, castedValue);
        } catch (ClassCastException e) {
            return state;
        }
    }

    public static void convertBlock(Level level, BlockPos pos, Block fromBlock) {
        Map<Block, Block> conversionMap = SoulConversionListener.CONVERSION_PAIR;

        if (!conversionMap.containsKey(fromBlock)) return;

        Block toBlock = conversionMap.get(fromBlock);
        BlockState fromState = level.getBlockState(pos);
        BlockState newState = copySharedProperties(fromState, toBlock.defaultBlockState());

        level.setBlock(pos, newState, 3);

        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = serverLevel.getRandom();
            for (int i = 0; i < 5; i++) {
                double offsetX = 0.5 + (random.nextDouble() - 0.5);
                double offsetY = 0.5 + (random.nextDouble() - 0.5);
                double offsetZ = 0.5 + (random.nextDouble() - 0.5);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ,
                        1, 0, 0, 0, 0);
            }
        }
    }
}
