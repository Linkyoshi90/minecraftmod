package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.Bonded;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * A bucket of a family's milk. Drunk, it gives that family's effects; fed to any of the young, it brings her a day
 * closer to grown. Recipes take it through the c:buckets/milk tag.
 */
public class FamilyMilkItem extends Item {
    public static final int EFFECT_TICKS = 3 * 60 * 20;
    public static final int GROWTH_TICKS = 24000;
    private static final int DRINK_TICKS = 32;

    private final FamilyMilk milk;

    public FamilyMilkItem(FamilyMilk milk, Properties properties) {
        super(properties);
        this.milk = milk;
    }

    public FamilyMilk milk() {
        return this.milk;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity drinker) {
        if (!level.isClientSide) {
            for (Holder<MobEffect> effect : this.milk.effects()) drinker.addEffect(new MobEffectInstance(effect, EFFECT_TICKS));
        }
        return drinker instanceof Player player ? ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)) : stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return DRINK_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Bonded young) || !target.isBaby()) return InteractionResult.PASS;
        if (target.level() instanceof ServerLevel server) {
            young.setGrowTicks(young.getGrowTicks() + GROWTH_TICKS);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            target.playSound(SoundEvents.GENERIC_DRINK, 1.0F, 1.2F);
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + target.getBbHeight() + 0.2, target.getZ(), 5, 0.2, 0.2, 0.2, 0.0);
        }
        return InteractionResult.sidedSuccess(target.level().isClientSide);
    }
}
