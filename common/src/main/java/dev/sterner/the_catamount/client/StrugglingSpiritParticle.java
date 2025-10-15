package dev.sterner.the_catamount.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

public class StrugglingSpiritParticle extends TextureSheetParticle {

    private final SpriteSet sprites;
    private int trailTimer = 0;

    protected StrugglingSpiritParticle(ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.friction = 0.96F;
        this.gravity = 0.0F;
        this.speedUpWhenYMotionIsBlocked = false;

        this.xd = (Math.random() - 0.5) * 0.02;
        this.yd = (Math.random() - 0.5) * 0.01;
        this.zd = (Math.random() - 0.5) * 0.02;

        this.quadSize = 0.5F;
        this.lifetime = 100;
        this.setSpriteFromAge(sprites);

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age < 20) {
            this.alpha = this.age / 20.0F;
        } else if (this.age > this.lifetime - 20) {
            this.alpha = (this.lifetime - this.age) / 20.0F;
        } else {
            this.alpha = 1.0F;
        }

        trailTimer++;
        if (trailTimer >= 3 && this.alpha > 0.3F) {
            spawnTrailParticles();
            trailTimer = 0;
        }

        this.setSpriteFromAge(sprites);
    }

    private void spawnTrailParticles() {
        for (int i = 0; i < level.random.nextInt(2) + 1; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.1;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.1;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.1;

            double velocityX = (level.random.nextDouble() - 0.5) * 0.03;
            double velocityY = -0.02;
            double velocityZ = (level.random.nextDouble() - 0.5) * 0.03;

            level.addParticle(
                    ParticleTypes.SOUL,
                    this.x + offsetX,
                    this.y + offsetY,
                    this.z + offsetZ,
                    velocityX,
                    velocityY,
                    velocityZ
            );
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new StrugglingSpiritParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}