package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Which sounds the creature volume slider (client config creatureVolume, client/CreatureSoundVolume) turns down.
 * Kept free of client classes so the GameTests can check the rules.
 *
 * A sound counts when it is one of the mod's own creature events (harmontide:entity.*), or when a mob or
 * hostile sound plays right on a Harmontide creature: most of their sounds are vanilla ones (parrot, frog,
 * player hurt...) played at their position, and the client is never told who made a positioned sound.
 */
public final class CreatureVolume {
    /** How far from a creature's hitbox a sound may start and still be hers. */
    private static final double REACH = 0.5;

    private CreatureVolume() {}

    /** Slider percent (0 to 100) as a volume factor. */
    public static float factor(int percent) {
        return Math.max(0, Math.min(100, percent)) / 100.0F;
    }

    /** One of the mod's own creature sound events. */
    public static boolean isCreatureEvent(ResourceLocation id) {
        return HarmontideMod.MODID.equals(id.getNamespace()) && id.getPath().startsWith("entity.");
    }

    /** The sound slots that are idle chatter (the chatter slider skips some of these; hurt, death, eat... always play). */
    public static final java.util.Set<String> CHATTER_SLOTS =
            java.util.Set.of("ambient", "sing", "caw", "hoot", "giggle", "laugh", "bellow", "cry", "bass", "hat");

    /** One of the mod's creature events that is idle chatter, e.g. harmontide:entity.sparrow_harpy.ambient. */
    public static boolean isChatter(ResourceLocation id) {
        if (!isCreatureEvent(id)) return false;
        String path = id.getPath();
        return CHATTER_SLOTS.contains(path.substring(path.lastIndexOf('.') + 1));
    }

    /** Whether one chatter sound plays, given the slider's share (0 to 1) and a roll in [0, 1). */
    public static boolean chatterPlays(float share, float roll) {
        return roll < share;
    }

    /** A living Harmontide mob (not a player, not a projectile or other mod entity). */
    public static boolean isCreature(Entity entity) {
        return entity instanceof LivingEntity && !(entity instanceof Player)
                && HarmontideMod.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace());
    }

    /** Sound categories a creature's borrowed vanilla sounds come in (never music, weather or the player's own). */
    public static boolean creatureSource(SoundSource source) {
        return source == SoundSource.NEUTRAL || source == SoundSource.HOSTILE || source == SoundSource.VOICE;
    }

    /** Whether a Harmontide creature stands where this sound starts. */
    public static boolean onCreature(Level level, double x, double y, double z) {
        AABB around = new AABB(x - 3, y - 3, z - 3, x + 3, y + 3, z + 3);
        for (Entity e : level.getEntities((Entity) null, around, CreatureVolume::isCreature)) {
            if (e.getBoundingBox().inflate(REACH).contains(x, y, z)) return true;
        }
        return false;
    }

    /** Whether the slider applies to this sound. */
    public static boolean applies(Level level, ResourceLocation event, SoundSource source, double x, double y, double z) {
        if (isCreatureEvent(event)) return true;
        return creatureSource(source) && onCreature(level, x, y, z);
    }
}
