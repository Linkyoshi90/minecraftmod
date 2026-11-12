package com.linkyoshi90.harmontide.entity;

/**
 * Harpies and frogs on their wedding day and anniversaries (2026-11-10): the girls wear the veil through a flag (FLAG_VEIL), these
 * through a synced boolean of their own, which the renderer reads for the feather crown (harpies) or the lily-pad veil (frogs and toads).
 */
public interface WeddingVeil {
    boolean isVeiled();

    void setVeiled(boolean veiled);
}
