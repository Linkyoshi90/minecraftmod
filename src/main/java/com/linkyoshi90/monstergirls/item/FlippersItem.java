package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.NeoForgeMod;

/** Merfolk-scale flippers: boots that make you swim much faster. */
public class FlippersItem extends ArmorItem {
    public FlippersItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.BOOTS, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return super.getDefaultAttributeModifiers().withModifierAdded(NeoForgeMod.SWIM_SPEED,
                new AttributeModifier(MonsterGirlsMod.id("flippers_swim_speed"), 0.6, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.FEET);
    }
}
