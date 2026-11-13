package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Who can't cook (2026-11-13), after a certain lamia's famous kitchen disasters. Every girl has a cooking skill of her own, always the
 * same for her (from her UUID, {@link #skill}). Crouch and hand a friend ({@link #FRIEND}) something raw (meat, fish, a potato) and she
 * cooks it for you on the spot, once every {@link #WAIT} ticks:
 * <ul>
 * <li>{@link Skill#MASTERFUL}: a feast of a dish, more filling, with Regeneration and Strength.</li>
 * <li>{@link Skill#GOOD}: a little more filling, with Regeneration.</li>
 * <li>{@link Skill#PLAIN}: just cooked.</li>
 * <li>{@link Skill#CLUMSY} and {@link Skill#HOPELESS}: burnt love food. Barely food, it makes you queasy (Nausea, and Hunger from a
 * hopeless cook), but eat it anyway and she is overjoyed: {@link #ATE_BURNT} friendship.</li>
 * </ul>
 * Every dish carries her name. Off with the config option {@code cooking}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Cooking {
    public static final int FRIEND = Orders.AFFECTION, WAIT = 1200, ATE_BURNT = 4, ATE_GOOD = 1, FIND = 24;
    private static final String NEXT = "NextCook", BY = "CookedBy", SKILL_TAG = "CookSkill";

    public static final Map<Item, Item> COOKS = Map.of(Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP,
            Items.CHICKEN, Items.COOKED_CHICKEN, Items.MUTTON, Items.COOKED_MUTTON, Items.RABBIT, Items.COOKED_RABBIT,
            Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.POTATO, Items.BAKED_POTATO);

    private Cooking() {}

    public enum Skill {
        HOPELESS, CLUMSY, PLAIN, GOOD, MASTERFUL;

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        public boolean burns() {
            return this.ordinal() <= CLUMSY.ordinal();
        }
    }

    /** Her skill: always the same for her. A fifth hopeless, a fifth clumsy, and so on. */
    public static Skill skill(AbstractMonsterGirl girl) {
        CompoundTag pd = girl.getPersistentData();
        if (pd.contains(SKILL_TAG)) return Skill.values()[Math.floorMod(pd.getInt(SKILL_TAG), Skill.values().length)];
        UUID id = girl.getUUID();
        return Skill.values()[Math.floorMod((int) (id.getLeastSignificantBits() >>> 7) ^ 0x5bd1e995, Skill.values().length)];
    }

    public static void setSkill(AbstractMonsterGirl girl, Skill skill) {
        girl.getPersistentData().putInt(SKILL_TAG, skill.ordinal());
    }

    /** What she makes of a raw ingredient, or null if it isn't one she cooks. */
    @Nullable
    public static ItemStack dish(AbstractMonsterGirl girl, ItemStack raw) {
        Item cooked = COOKS.get(raw.getItem());
        if (cooked == null) return null;
        Skill skill = skill(girl);
        ItemStack out = new ItemStack(cooked);
        FoodProperties base = new ItemStack(cooked).get(DataComponents.FOOD);
        int nutrition = base == null ? 4 : base.nutrition();
        float saturation = base == null ? 0.5F : base.saturation();
        FoodProperties.Builder food = new FoodProperties.Builder();
        switch (skill) {
            case MASTERFUL -> food.nutrition(nutrition + 3).saturationModifier(saturation / (2.0F * Math.max(1, nutrition)) + 0.4F)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0F)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 0), 1.0F);
            case GOOD -> food.nutrition(nutrition + 1).saturationModifier(saturation / (2.0F * Math.max(1, nutrition)) + 0.2F)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 120, 0), 1.0F);
            case PLAIN -> food.nutrition(nutrition).saturationModifier(saturation / (2.0F * Math.max(1, nutrition)));
            case CLUMSY -> food.nutrition(2).saturationModifier(0.1F).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 160, 0), 1.0F);
            case HOPELESS -> food.nutrition(1).saturationModifier(0.0F).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 240, 0), 1.0F)
                    .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 300, 0), 1.0F);
        }
        out.set(DataComponents.FOOD, food.build());
        out.set(DataComponents.CUSTOM_NAME, Component.translatable("item.harmontide.dish." + skill.key(), girl.getName(), cooked.getDescription())
                .withStyle(s -> s.withItalic(false)));
        CompoundTag tag = new CompoundTag();
        tag.putUUID(BY, girl.getUUID());
        tag.putInt(SKILL_TAG, skill.ordinal());
        out.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return out;
    }

    /** She cooks it for the player. The dish, or null (not a friend, not ready, not something she cooks). */
    @Nullable
    public static ItemStack cookFor(ServerLevel level, AbstractMonsterGirl girl, Player player, ItemStack raw) {
        if (!HarmontideConfig.cooking() || girl.isBaby() || girl.getAffection(player) < FRIEND) return null;
        CompoundTag pd = girl.getPersistentData();
        if (level.getGameTime() < pd.getLong(NEXT)) {
            player.displayClientMessage(Component.translatable("message.harmontide.cooking.busy", girl.getDisplayName()), true);
            return null;
        }
        ItemStack dish = dish(girl, raw);
        if (dish == null) return null;
        pd.putLong(NEXT, level.getGameTime() + WAIT);
        raw.shrink(1);
        if (!player.getInventory().add(dish.copy())) player.drop(dish.copy(), false);
        Skill skill = skill(girl);
        level.sendParticles(skill.burns() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE, girl.getX(), girl.getEyeY(), girl.getZ(), skill.burns() ? 14 : 6, 0.3, 0.2, 0.3, 0.01);
        girl.playSound(skill.burns() ? SoundEvents.FIRE_EXTINGUISH : SoundEvents.SMOKER_SMOKE, 0.8F, 1.0F);
        player.displayClientMessage(Component.translatable("message.harmontide.cooking." + skill.key(), girl.getDisplayName()), true);
        return dish;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player p = event.getEntity();
        if (!(event.getTarget() instanceof AbstractMonsterGirl girl) || event.getHand() != InteractionHand.MAIN_HAND || !p.isShiftKeyDown()
                || !COOKS.containsKey(event.getItemStack().getItem()) || girl.getAffection(p) < FRIEND || !HarmontideConfig.cooking()) return;
        if (girl.level() instanceof ServerLevel level) cookFor(level, girl, p, event.getItemStack());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(girl.level().isClientSide));
    }

    /** Eaten: a burnt dish delights its cook (if she is near). The cook, or null. */
    @Nullable
    public static AbstractMonsterGirl eaten(ServerLevel level, Player player, ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        if (!tag.hasUUID(BY)) return null;
        if (!(level.getEntity(tag.getUUID(BY)) instanceof AbstractMonsterGirl cook) || cook.distanceToSqr(player) > FIND * FIND) return null;
        boolean burnt = Skill.values()[Math.floorMod(tag.getInt(SKILL_TAG), Skill.values().length)].burns();
        cook.addAffection(player, burnt ? ATE_BURNT : ATE_GOOD);
        level.sendParticles(ParticleTypes.HEART, cook.getX(), cook.getEyeY() + 0.4, cook.getZ(), burnt ? 6 : 2, 0.3, 0.2, 0.3, 0.0);
        if (burnt) player.displayClientMessage(Component.translatable("message.harmontide.cooking.ate_burnt", cook.getDisplayName()), false);
        return cook;
    }

    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player p && p.level() instanceof ServerLevel level) eaten(level, p, event.getItem());
    }
}
