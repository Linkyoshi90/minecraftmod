package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The Demon Realm's air, on the client: the more realm grass and realm leaves round the player ({@link #density}), the thicker a dusky
 * rose-violet fog closes in, haze and violet motes drift past, and now and then something sighs in the distance. It eases in and out
 * ({@link #strength} follows the density slowly), so walking into the realm feels like crossing into somewhere else.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID, value = Dist.CLIENT)
public final class RealmAtmosphere {
    /** The fog's colour deep in the realm. */
    private static final float R = 0.42F, G = 0.15F, B = 0.40F;
    /** How close the fog comes at full strength, in blocks. */
    private static final float FAR = 36.0F;
    /** Realm blocks round the player for the full effect. */
    private static final int FULL = 40, RADIUS = 6;
    private static float target, strength, last;

    private RealmAtmosphere() {
    }

    /** The share of the realm round a spot, 0 to 1: realm grass, leaves and weepcaps within RADIUS, counted every half second. */
    public static float density(Level level, BlockPos at) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-RADIUS, -4, -RADIUS), at.offset(RADIUS, 6, RADIUS))) {
            BlockState s = level.getBlockState(p);
            if (s.is(ModBlocks.REALM_GRASS.get()) || s.is(ModBlocks.REALM_LEAVES.get()) || s.is(ModBlocks.WEEPCAP.get())) n++;
        }
        return Math.min(1.0F, n / (float) FULL);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null || mc.level == null || mc.isPaused()) return;
        if (p.tickCount % 10 == 0) target = com.linkyoshi90.monstergirls.MonsterGirlsConfig.realmFog() ? density(mc.level, p.blockPosition()) : 0.0F;
        last = strength;
        strength += (target - strength) * 0.04F;
        if (strength < 0.02F) return;
        RandomSource r = mc.level.random;
        for (int i = 0; i < 3; i++) {
            if (r.nextFloat() > strength) continue;
            double x = p.getX() + r.nextGaussian() * 6, y = p.getY() + r.nextDouble() * 4 - 0.5, z = p.getZ() + r.nextGaussian() * 6;
            mc.level.addParticle(r.nextInt(4) == 0 ? ParticleTypes.PORTAL : RealmGrassBlock.HAZE, x, y, z, 0.0, 0.01, 0.0);
        }
        if (strength > 0.5F && r.nextInt(500) == 0) {
            mc.getSoundManager().play(SimpleSoundInstance.forAmbientAddition(r.nextBoolean()
                    ? SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value() : SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS.value()));
        }
    }

    private static float now(float partialTick) {
        return Mth.lerp(partialTick, last, strength);
    }

    @SubscribeEvent
    public static void onFogColour(ViewportEvent.ComputeFogColor event) {
        float s = now((float) event.getPartialTick()) * 0.75F;
        if (s <= 0.01F) return;
        event.setRed(Mth.lerp(s, event.getRed(), R));
        event.setGreen(Mth.lerp(s, event.getGreen(), G));
        event.setBlue(Mth.lerp(s, event.getBlue(), B));
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        float s = now((float) event.getPartialTick());
        if (s <= 0.02F || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
        float far = Mth.lerp(s, event.getFarPlaneDistance(), Math.min(event.getFarPlaneDistance(), FAR));
        event.setFarPlaneDistance(far);
        event.setNearPlaneDistance(Mth.lerp(s, event.getNearPlaneDistance(), far * 0.1F));
        event.setCanceled(true);                                            // NeoForge applies the changed fog only when cancelled
    }
}
