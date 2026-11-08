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

    /** The game time she can next be milked by a machine (minotaur cows keep their own clock). */
    public static final Supplier<AttachmentType<Long>> NEXT_MILK = ATTACHMENT_TYPES.register("next_milk",
            () -> AttachmentType.builder(() -> 0L).serialize(com.mojang.serialization.Codec.LONG).build());

    /** How much the Order has heard of a player's ties to the Demon Realm (entity/order/Order); kept through death. */
    public static final Supplier<AttachmentType<Integer>> ORDER_NOTORIETY = ATTACHMENT_TYPES.register("order_notoriety",
            () -> AttachmentType.builder(() -> 0).serialize(com.mojang.serialization.Codec.INT).copyOnDeath().build());

    /** How far the Demon Realm has touched a player (entity/RealmTouched); kept through death. */
    public static final Supplier<AttachmentType<Integer>> REALM_TOUCH = ATTACHMENT_TYPES.register("realm_touch",
            () -> AttachmentType.builder(() -> 0).serialize(com.mojang.serialization.Codec.INT).copyOnDeath().build());

    private ModAttachments() {
    }
}
