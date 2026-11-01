package com.linkyoshi90.monstergirls.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** A drop of milk: falls straight down and is gone when it lands. Uses vanilla's falling-drip sprite, tinted cream. */
public class MilkDripParticle extends TextureSheetParticle {
    private MilkDripParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.pickSprite(sprites);
        this.setColor(1.0F, 0.98F, 0.92F);
        this.setSize(0.02F, 0.02F);
        this.quadSize = 0.06F;
        this.gravity = 0.6F;
        this.lifetime = 40;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.onGround) this.remove();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new MilkDripParticle(level, x, y, z, this.sprites);
        }
    }
}
