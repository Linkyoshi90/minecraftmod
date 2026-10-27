package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Data kept on players and entities. */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MonsterGirlsMod.MODID);

    /** The Harmontide journal (entity.Journal): the creatures a player has met ("Met") and befriended ("Friends"); kept through death. */
    public static final Supplier<AttachmentType<CompoundTag>> JOURNAL = ATTACHMENT_TYPES.register("journal",
            () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).copyOnDeath().build());

    /** The girls away on expeditions (entity.Expeditions): each one's saved self, when she is due back and where. */
    public static final Supplier<AttachmentType<CompoundTag>> EXPEDITIONS = ATTACHMENT_TYPES.register("expeditions",
            () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).copyOnDeath().build());

    private ModAttachments() {
    }
}
