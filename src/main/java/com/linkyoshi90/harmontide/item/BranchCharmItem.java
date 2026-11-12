package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.Clade;
import com.linkyoshi90.harmontide.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A branch charm: given once for meeting every kind on one branch of the tree of life (entity.Journal.branch). Carried in
 * the offhand it lends its bearer the first effect of that branch's milk, quietly, for as long as it is held. The plants
 * and the constructs give no milk, so theirs is a keepsake for that. Wild ones of its branch who see it in the offhand warm to its
 * bearer (Journal.charmed).
 */
public class BranchCharmItem extends Item {
    public static final int REFRESH = 40, LASTS = 300;

    public BranchCharmItem(Properties properties) {
        super(properties);
    }

    public static ItemStack of(Clade clade) {
        ItemStack stack = new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.BRANCH_CHARM.get());
        stack.set(ModComponents.CLADE.get(), clade);
        return stack;
    }

    /** What it lends, or null (no branch, or a branch without milk). */
    @Nullable
    public static Holder<MobEffect> effect(ItemStack stack) {
        Clade clade = stack.get(ModComponents.CLADE.get());
        FamilyMilk milk = clade == null ? null : FamilyMilk.of(clade);
        return milk == null || milk.effects().isEmpty() ? null : milk.effects().get(0);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player) || player.tickCount % REFRESH != 0 || player.getOffhandItem() != stack) return;
        Holder<MobEffect> effect = effect(stack);
        if (effect != null) player.addEffect(new MobEffectInstance(effect, LASTS, 0, true, false, true));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModComponents.CLADE.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Clade clade = stack.get(ModComponents.CLADE.get());
        if (clade == null) {
            tooltip.add(Component.translatable("item.harmontide.branch_charm.blank").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("clade.harmontide." + clade.key()).withStyle(clade.mythic() ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_AQUA));
        Holder<MobEffect> effect = effect(stack);
        tooltip.add(effect == null ? Component.translatable("item.harmontide.branch_charm.keepsake").withStyle(ChatFormatting.GRAY)
                : Component.translatable("item.harmontide.branch_charm.effect", effect.value().getDisplayName()).withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.harmontide.branch_charm.kin").withStyle(ChatFormatting.GREEN));
    }
}
