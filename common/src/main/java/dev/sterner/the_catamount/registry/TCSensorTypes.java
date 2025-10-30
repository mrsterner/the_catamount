package dev.sterner.the_catamount.registry;

import dev.sterner.the_catamount.entity.sensor.CatamountPreySensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

public class TCSensorTypes {

    public static final SensorType<CatamountPreySensor> CATAMOUNT_SENSOR = new SensorType(CatamountPreySensor::new);
}