package com.linkyoshi90.harmontide.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;

/** Finding jukeboxes that are playing a song, and what they're playing. */
public final class JukeboxSensing {
    private JukeboxSensing() {}

    /** The closest jukebox within {@code radius} blocks that is playing a song right now, if any (loaded chunks only). */
    @Nullable
    public static BlockPos findPlaying(Level level, BlockPos center, int radius) {
        if (!(level instanceof ServerLevel server)) return null;
        BlockPos best = null;
        int chunks = (radius >> 4) + 1;
        for (int cx = -chunks; cx <= chunks; cx++) {
            for (int cz = -chunks; cz <= chunks; cz++) {
                LevelChunk chunk = server.getChunkSource().getChunkNow((center.getX() >> 4) + cx, (center.getZ() >> 4) + cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof JukeboxBlockEntity jukebox) || !jukebox.getSongPlayer().isPlaying()) continue;
                    BlockPos pos = be.getBlockPos();
                    if (pos.distSqr(center) > (double) radius * radius) continue;
                    if (best == null || pos.distSqr(center) < best.distSqr(center)) best = pos.immutable();
                }
            }
        }
        return best;
    }

    public static boolean isPlaying(Level level, @Nullable BlockPos pos) {
        return pos != null && level.isLoaded(pos) && level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox
                && jukebox.getSongPlayer().isPlaying();
    }

    /** Which song the jukebox at {@code pos} is playing (e.g. {@code JukeboxSongs.PIGSTEP}), or null. */
    @Nullable
    public static ResourceKey<JukeboxSong> songAt(Level level, @Nullable BlockPos pos) {
        if (!isPlaying(level, pos) || !(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox)) return null;
        JukeboxSong song = jukebox.getSongPlayer().getSong();
        return song == null ? null : level.registryAccess().registryOrThrow(Registries.JUKEBOX_SONG).getResourceKey(song).orElse(null);
    }

    @Nullable
    public static JukeboxBlockEntity jukeboxAt(Level level, @Nullable BlockPos pos) {
        return pos != null && level.isLoaded(pos) && level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox ? jukebox : null;
    }
}
