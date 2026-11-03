package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.registry.ModComponents;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;

/**
 * Butter, cheese and ice cream (entity.Dairy): named for the family whose milk made them ("Firm Harpy Cheese"), with the effects they
 * give in the tooltip. Plain ones (a cow's milk, or a cheese brought by a minotaur) are only food.
 */
public class DairyItem extends Item {
    private static final String[] HARDNESS = {"soft", "firm", "hard"};

    public DairyItem(Properties properties) {
        super(properties);
    }

    public static Component familyName(FamilyMilk family) {
        return Component.translatable("family.monstergirls." + family.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public Component getName(ItemStack stack) {
        FamilyMilk family = stack.get(ModComponents.MILK_FAMILY.get());
        boolean cheese = stack.is(ModItems.CHEESE.get());
        if (cheese && stack.has(ModComponents.CHEESE_AGE.get())) {
            String hard = HARDNESS[Dairy.hardness(stack)];
            return family == null ? Component.translatable("item.monstergirls.cheese." + hard)
                    : Component.translatable("item.monstergirls.cheese." + hard + ".family", familyName(family));
        }
        if (family != null) return Component.translatable(this.getDescriptionId() + ".family", familyName(family));
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FamilyMilk family = stack.get(ModComponents.MILK_FAMILY.get());
        List<Holder<MobEffect>> diet = stack.get(ModComponents.MILK_DIET.get());
        int seconds = Dairy.seconds(stack);
        if (family != null) for (Holder<MobEffect> e : family.effects()) tooltip.add(line(e, seconds, stack));
        if (diet != null) for (Holder<MobEffect> e : diet) tooltip.add(line(e, seconds, stack).copy().append(Component.translatable("item.monstergirls.dairy.diet")));
        if (stack.is(ModItems.CHEESE.get()) && stack.has(ModComponents.CHEESE_AGE.get()) && Dairy.hardness(stack) < 2) {
            tooltip.add(Component.translatable("item.monstergirls.cheese.aging").withStyle(ChatFormatting.GRAY));
        }
    }

    private static Component line(Holder<MobEffect> e, int seconds, ItemStack stack) {
        boolean strong = stack.is(ModItems.CHEESE.get()) && Dairy.hardness(stack) == 2;
        Component name = strong ? Component.translatable("potion.withAmplifier", e.value().getDisplayName(), Component.translatable("potion.potency.1")) : e.value().getDisplayName();
        return Component.translatable("item.monstergirls.dairy.effect", name, seconds / 60, String.format("%02d", seconds % 60)).withStyle(ChatFormatting.BLUE);
    }
}
