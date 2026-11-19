package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.CreatureVolume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * The creature sliders: the chatter slider skips a share of their idle calls, and the volume slider scales every
 * Harmontide creature sound this client plays (0 = not played).
 * Which sounds count is decided in entity/CreatureVolume. Looping sounds that tick themselves are left alone, since
 * whoever started them may look them up again by identity.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID, value = Dist.CLIENT)
public final class CreatureSoundVolume {
    private static final RandomSource RANDOM = RandomSource.create();

    private CreatureSoundVolume() {}

    @SubscribeEvent
    public static void onPlay(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || sound instanceof TickableSoundInstance || sound.isRelative()) return;
        if (CreatureVolume.isChatter(sound.getLocation())
                && !CreatureVolume.chatterPlays(HarmontideClientConfig.creatureChatter(), RANDOM.nextFloat())) {
            event.setSound(null);
            return;
        }
        float factor = HarmontideClientConfig.creatureVolume();
        if (factor >= 1.0F) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!CreatureVolume.applies(mc.level, sound.getLocation(), sound.getSource(), sound.getX(), sound.getY(), sound.getZ())) return;
        event.setSound(factor <= 0.0F ? null : new Scaled(sound, factor));
    }

    /** The same sound, quieter. */
    private record Scaled(SoundInstance inner, float factor) implements SoundInstance {
        @Override public ResourceLocation getLocation() { return inner.getLocation(); }
        @Override @Nullable public WeighedSoundEvents resolve(SoundManager manager) { return inner.resolve(manager); }
        @Override public Sound getSound() { return inner.getSound(); }
        @Override public SoundSource getSource() { return inner.getSource(); }
        @Override public boolean isLooping() { return inner.isLooping(); }
        @Override public boolean isRelative() { return inner.isRelative(); }
        @Override public int getDelay() { return inner.getDelay(); }
        @Override public float getVolume() { return inner.getVolume() * factor; }
        @Override public float getPitch() { return inner.getPitch(); }
        @Override public double getX() { return inner.getX(); }
        @Override public double getY() { return inner.getY(); }
        @Override public double getZ() { return inner.getZ(); }
        @Override public Attenuation getAttenuation() { return inner.getAttenuation(); }
        @Override public boolean canStartSilent() { return inner.canStartSilent(); }
        @Override public boolean canPlaySound() { return inner.canPlaySound(); }
        @Override public CompletableFuture<AudioStream> getStream(SoundBufferLibrary library, Sound sound, boolean looping) {
            return inner.getStream(library, sound, looping);
        }
    }
}
