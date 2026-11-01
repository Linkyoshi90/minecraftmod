package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, MonsterGirlsMod.MODID);

    /** A falling drop of milk (client/MilkDripParticle). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MILK_DRIP = PARTICLE_TYPES.register("milk_drip", () -> new SimpleParticleType(false));

    private ModParticles() {}
}
