package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.item.FamilyMilk;
import com.linkyoshi90.monstergirls.item.FamilyMilkItem;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import com.linkyoshi90.monstergirls.registry.ModComponents;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Milk after it leaves her. Her diet: certain foods she ate from the fridge or a friend's hand flavour her milk for a day with one
 * effect each (two at most), carried on the bucket (ModComponents.MILK_DIET) and into anything made from it. The dairy foods (butter
 * from the churn, cheese from the cheese shelf, ice cream, a meal cooked with them) carry the family too, and give its effects, weaker
 * than a bucket: a cheese grows stronger the longer it ages. The journal counts every bucket a girl gives, and the dairy advancements.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Dairy {
    /** How long a meal flavours her milk, and how many flavours it holds. */
    public static final int DIET_TICKS = 24000, DIET_KINDS = 2;
    /** Seconds of effect from each dairy food (a bucket gives FamilyMilkItem.EFFECT_TICKS). */
    public static final int BUTTER = 45, ICE_CREAM = 90, MEAL = 60, SOFT = 60, FIRM = 180, HARD = 360;
    /** Ticks a cheese must age underground to turn firm, then hard. */
    public static final int FIRM_AGE = 24000, HARD_AGE = 72000;
    private static final String DIET = "Diet";

    /** The foods that flavour milk, and how. */
    public static final Map<Item, Holder<MobEffect>> FLAVOURS = Map.ofEntries(
            Map.entry(Items.SWEET_BERRIES, MobEffects.REGENERATION), Map.entry(Items.GLOW_BERRIES, MobEffects.NIGHT_VISION),
            Map.entry(Items.GOLDEN_CARROT, MobEffects.NIGHT_VISION), Map.entry(Items.HONEY_BOTTLE, MobEffects.ABSORPTION),
            Map.entry(Items.GOLDEN_APPLE, MobEffects.DAMAGE_RESISTANCE), Map.entry(Items.ENCHANTED_GOLDEN_APPLE, MobEffects.DAMAGE_RESISTANCE),
            Map.entry(Items.CHORUS_FRUIT, MobEffects.SLOW_FALLING), Map.entry(Items.COOKIE, MobEffects.DIG_SPEED),
            Map.entry(Items.PUMPKIN_PIE, MobEffects.LUCK), Map.entry(Items.MELON_SLICE, MobEffects.FIRE_RESISTANCE),
            Map.entry(Items.GLISTERING_MELON_SLICE, MobEffects.FIRE_RESISTANCE), Map.entry(Items.TROPICAL_FISH, MobEffects.WATER_BREATHING),
            Map.entry(Items.COOKED_COD, MobEffects.WATER_BREATHING), Map.entry(Items.COOKED_SALMON, MobEffects.WATER_BREATHING),
            Map.entry(Items.DRIED_KELP, MobEffects.DOLPHINS_GRACE), Map.entry(Items.RABBIT_STEW, MobEffects.JUMP),
            Map.entry(Items.CARROT, MobEffects.NIGHT_VISION), Map.entry(Items.BEETROOT_SOUP, MobEffects.HEALTH_BOOST));

    private Dairy() {}

    // --- her diet ------------------------------------------------------------------------------------------------------

    /** She ate this from the fridge or a friend's hand. Whether it flavours her milk. */
    public static boolean ate(LivingEntity girl, ItemStack food) {
        Holder<MobEffect> flavour = food.is(ModItems.HOMEMADE_MEAL.get()) ? MobEffects.HEALTH_BOOST : FLAVOURS.get(food.getItem());
        if (flavour == null) return false;
        long now = girl.level().getGameTime();
        ListTag list = girl.getPersistentData().getList(DIET, Tag.TAG_COMPOUND);
        String id = flavour.unwrapKey().orElseThrow().location().toString();
        for (int i = list.size() - 1; i >= 0; i--) {
            CompoundTag t = list.getCompound(i);
            if (t.getString("Effect").equals(id) || t.getLong("Until") < now) list.remove(i);
        }
        while (list.size() >= DIET_KINDS) list.remove(0);
        CompoundTag entry = new CompoundTag();
        entry.putString("Effect", id);
        entry.putLong("Until", now + DIET_TICKS);
        list.add(entry);
        girl.getPersistentData().put(DIET, list);
        return true;
    }

    /** The flavours in her milk right now. */
    public static List<Holder<MobEffect>> diet(LivingEntity girl) {
        List<Holder<MobEffect>> out = new ArrayList<>();
        long now = girl.level().getGameTime();
        for (Tag t : girl.getPersistentData().getList(DIET, Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            if (c.getLong("Until") < now) continue;
            ResourceLocation id = ResourceLocation.tryParse(c.getString("Effect"));
            if (id != null) BuiltInRegistries.MOB_EFFECT.getHolder(id).ifPresent(out::add);
        }
        return out;
    }

    /** A bucket of her milk as it comes from her, her diet's flavours on it. */
    public static ItemStack flavoured(LivingEntity girl, ItemStack milk) {
        List<Holder<MobEffect>> diet = diet(girl);
        if (!diet.isEmpty()) milk.set(ModComponents.MILK_DIET.get(), diet);
        return milk;
    }

    // --- milk and the foods made from it ---------------------------------------------------------------------------------

    /** Whether this is a bucket of milk the dairy blocks take: any family's, or a plain cow's. */
    public static boolean isMilk(ItemStack stack) {
        return stack.getItem() instanceof FamilyMilkItem || stack.is(Items.MILK_BUCKET);
    }

    @Nullable
    public static FamilyMilk familyOf(ItemStack stack) {
        if (stack.getItem() instanceof FamilyMilkItem m) return m.milk();
        return stack.get(ModComponents.MILK_FAMILY.get());
    }

    /** A dairy food made from this milk (or this dairy food): the same family and flavours. */
    public static ItemStack madeFrom(ItemStack source, Item food, int count) {
        ItemStack out = new ItemStack(food, count);
        FamilyMilk family = familyOf(source);
        if (family != null) out.set(ModComponents.MILK_FAMILY.get(), family);
        List<Holder<MobEffect>> diet = source.get(ModComponents.MILK_DIET.get());
        if (diet != null && !diet.isEmpty()) out.set(ModComponents.MILK_DIET.get(), diet);
        return out;
    }

    public static int cheeseAge(ItemStack cheese) {
        return cheese.getOrDefault(ModComponents.CHEESE_AGE.get(), 0);
    }

    /** 0 soft, 1 firm, 2 hard. */
    public static int hardness(ItemStack cheese) {
        int age = cheeseAge(cheese);
        return age >= HARD_AGE ? 2 : age >= FIRM_AGE ? 1 : 0;
    }

    /** Seconds of effect this dairy food gives, or 0 if it gives none of the family's. */
    public static int seconds(ItemStack stack) {
        if (stack.is(ModItems.BUTTER.get())) return BUTTER;
        if (stack.is(ModItems.ICE_CREAM.get())) return ICE_CREAM;
        if (stack.is(ModItems.HOMEMADE_MEAL.get())) return MEAL;
        if (stack.is(ModItems.CHEESE.get())) return switch (hardness(stack)) { case 2 -> HARD; case 1 -> FIRM; default -> SOFT; };
        return 0;
    }

    /** What eating or drinking it does beyond its food: the family's effects (dairy only) and the diet's. Hard cheese gives level II. */
    public static int apply(LivingEntity eater, ItemStack stack) {
        int given = 0;
        int ticks = stack.getItem() instanceof FamilyMilkItem || stack.is(Items.MILK_BUCKET) ? FamilyMilkItem.EFFECT_TICKS : seconds(stack) * 20;
        if (ticks <= 0) return 0;
        int amplifier = stack.is(ModItems.CHEESE.get()) && hardness(stack) == 2 ? 1 : 0;
        FamilyMilk family = stack.get(ModComponents.MILK_FAMILY.get());
        if (family != null) for (Holder<MobEffect> e : family.effects()) {
            eater.addEffect(new MobEffectInstance(e, ticks, amplifier));
            given++;
        }
        List<Holder<MobEffect>> diet = stack.get(ModComponents.MILK_DIET.get());
        if (diet != null) for (Holder<MobEffect> e : diet) {
            eater.addEffect(new MobEffectInstance(e, ticks, amplifier));
            given++;
        }
        return given;
    }

    /** After the food itself (and after a plain milk bucket has cleared every effect). */
    @SubscribeEvent
    public static void onFinish(LivingEntityUseItemEvent.Finish event) {
        if (!event.getEntity().level().isClientSide) apply(event.getEntity(), event.getItem());
    }

    /** A cow's plain milk shows its flavours too (the family milks list their own). */
    @SubscribeEvent
    public static void onTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        List<Holder<MobEffect>> diet = event.getItemStack().is(Items.MILK_BUCKET) ? event.getItemStack().get(ModComponents.MILK_DIET.get()) : null;
        if (diet != null) for (Holder<MobEffect> e : diet) {
            event.getToolTip().add(net.minecraft.network.chat.Component.translatable("item.monstergirls.milk.flavour", e.value().getDisplayName()).withStyle(net.minecraft.ChatFormatting.BLUE));
        }
    }

    // --- the journal and advancements ----------------------------------------------------------------------------------

    /** A bucket from her for this player: counted in their journal. */
    public static void recordMilk(Player player, LivingEntity girl) {
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        CompoundTag milk = data.getCompound("Milk");
        String name = girl.getName().getString();
        milk.putInt(name, milk.getInt(name) + 1);
        data.put("Milk", milk);
        data.putInt("MilkTotal", data.getInt("MilkTotal") + 1);
        player.setData(ModAttachments.JOURNAL, data);
        award(player, "first_milk");
    }

    /** Awards one of the dairy advancements (data/monstergirls/advancement/<id>.json, a single "done" criterion). */
    public static void award(@Nullable Player player, String id) {
        if (!(player instanceof ServerPlayer sp)) return;
        AdvancementHolder a = sp.server.getAdvancements().get(MonsterGirlsMod.id(id));
        if (a != null) sp.getAdvancements().award(a, "done");
    }
}
