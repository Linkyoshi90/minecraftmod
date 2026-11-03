package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.item.FamilyMilk;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** Data on item stacks: whose milk a dairy food was made from, what she had been eating, and how long a cheese has aged (entity.Dairy). */
public final class ModComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MonsterGirlsMod.MODID);

    /** The family a butter, cheese, ice cream or meal was made from. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FamilyMilk>> MILK_FAMILY = COMPONENTS.registerComponentType("milk_family",
            b -> b.persistent(Codec.STRING.xmap(FamilyMilk::valueOf, FamilyMilk::name)).networkSynchronized(NeoForgeStreamCodecs.enumCodec(FamilyMilk.class)));
    /** What she had eaten from the fridge or a friend's hand before she was milked: one effect each, on top of the family's. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Holder<MobEffect>>>> MILK_DIET = COMPONENTS.registerComponentType("milk_diet",
            b -> b.persistent(MobEffect.CODEC.listOf()).networkSynchronized(MobEffect.STREAM_CODEC.apply(ByteBufCodecs.list())));
    /** Ticks a cheese has aged underground on a cheese shelf. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHEESE_AGE = COMPONENTS.registerComponentType("cheese_age",
            b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    private ModComponents() {}
}
