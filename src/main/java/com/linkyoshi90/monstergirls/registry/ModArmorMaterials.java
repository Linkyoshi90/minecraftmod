package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/** Armour made from what the girls leave behind (textures: textures/models/armor/&lt;name&gt;_layer_1/2.png, tools/gen_harmontide_items.py). */
public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, MonsterGirlsMod.MODID);

    /** Shed lamia skin: a light cloak, leather's protection. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SNAKESKIN = register("snakeskin", 1, 2, 3, 1, 15, SoundEvents.ARMOR_EQUIP_LEATHER, () -> ModItems.SHED_SKIN.get());
    /** Arachne silk: iron's protection at a fraction of the weight, but it wears out faster (see the durability in ModItems). */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SPIDER_SILK = register("spider_silk", 2, 5, 6, 2, 12, SoundEvents.ARMOR_EQUIP_LEATHER, () -> ModItems.SPIDER_SILK.get());
    /** Merfolk scales: flippers. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MERFOLK_SCALE = register("merfolk_scale", 1, 2, 3, 1, 12, SoundEvents.ARMOR_EQUIP_TURTLE, () -> ModItems.MERFOLK_SCALE.get());
    /** Leather and feathers: the winged cap. */
    /** Headgear friends give (entity/Headgear): a point of armour, drawn by GeckoLib (the vanilla layer is blank). */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HEADGEAR = register("headgear", 1, 1, 1, 1, 15, SoundEvents.ARMOR_EQUIP_LEATHER, () -> net.minecraft.world.item.Items.STRING);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WINGED_CAP = register("winged_cap", 1, 1, 1, 1, 15, SoundEvents.ARMOR_EQUIP_ELYTRA, () -> net.minecraft.world.item.Items.FEATHER);

    /** Realm silver (entity/RealmSilver): iron's protection; monster girls' blows land soft on it, and a full set keeps wild ones off. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> REALM_SILVER = register("realm_silver", 2, 5, 6, 2, 12, SoundEvents.ARMOR_EQUIP_IRON,
            () -> ModItems.REALM_SILVER_INGOT.get());

    private ModArmorMaterials() {}

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(String name, int boots, int legs, int chest, int helmet, int enchantability,
                                                                        Holder<SoundEvent> sound, Supplier<ItemLike> repair) {
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
            m.put(ArmorItem.Type.BOOTS, boots);
            m.put(ArmorItem.Type.LEGGINGS, legs);
            m.put(ArmorItem.Type.CHESTPLATE, chest);
            m.put(ArmorItem.Type.HELMET, helmet);
            m.put(ArmorItem.Type.BODY, chest);
        }), enchantability, sound, () -> Ingredient.of(repair.get()), List.of(new ArmorMaterial.Layer(MonsterGirlsMod.id(name))), 0.0F, 0.0F));
    }
}
