package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModGirls;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Cursed gear (2026-11-13), after Monster Girl Encyclopedia's living armour and cursed swords: gear a wandering spirit made its own.
 * A cursed blade (item/CursedBladeItem, an iron-grade sword) and living armour (item/LivingArmorItem, an iron-grade chestplate) turn up
 * now and then in the chests of old places ({@link #CHESTS}). They work as gear, and they grow fond of whoever uses them
 * ({@link #fondness}, 0 to {@link #FOND}): the blade with every kill ({@link #KILL}), the armour with every blow it takes for its
 * wearer ({@link #BLOW}) and every minute it is worn ({@link #WORN}). Now and then they whisper to you, more sweetly the fonder they are.
 * At full fondness the spirit steps out as a girl beside you (the cursed blade or the living armour), devoted to you from the first.
 * <p>
 * As girls: the living armour takes blows lightly once befriended ({@link #ARMOURED}) and goes for whatever hurts her friend; the cursed
 * blade's strikes heal her ({@link #HUNGER} of what she deals). Off with the config option {@code cursedGear}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class CursedGear {
    public static final int FOND = 100, KILL = 4, BLOW = 1, WORN = 1, WHISPER = 1800, GUARD = 16;
    public static final float ARMOURED = 0.6F, HUNGER = 0.25F, CHANCE = 0.06F;
    public static final Set<String> CHESTS = Set.of("chests/simple_dungeon", "chests/abandoned_mineshaft", "chests/stronghold_corridor",
            "chests/stronghold_crossing", "chests/ancient_city", "chests/bastion_other", "chests/desert_pyramid", "chests/jungle_temple", "chests/woodland_mansion");
    private static final String FONDNESS = "Fondness";

    private CursedGear() {}

    public static boolean blade(ItemStack s) {
        return s.is(ModItems.CURSED_BLADE.get());
    }

    public static boolean armour(ItemStack s) {
        return s.is(ModItems.LIVING_ARMOR.get());
    }

    public static int fondness(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? 0 : d.copyTag().getInt(FONDNESS);
    }

    public static void setFondness(ItemStack s, int value) {
        CompoundTag t = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        t.putInt(FONDNESS, Math.max(0, Math.min(FOND, value)));
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    /** It grows fonder of its holder; at full fondness she steps out. The girl, or null. */
    @Nullable
    public static AbstractMonsterGirl fonder(ServerLevel level, Player holder, ItemStack s, int amount) {
        if (!HarmontideConfig.cursedGear() || amount <= 0) return null;
        setFondness(s, fondness(s) + amount);
        return fondness(s) >= FOND ? stepOut(level, holder, s) : null;
    }

    /** The spirit steps out beside its holder as a girl, devoted; the gear is gone. */
    @Nullable
    public static AbstractMonsterGirl stepOut(ServerLevel level, Player holder, ItemStack s) {
        GirlSpecies kind = blade(s) ? GirlSpecies.CURSED_BLADE_UNDEAD : armour(s) ? GirlSpecies.LIVING_ARMOR_UNDEAD : null;
        if (kind == null || !(ModGirls.type(kind).create(level) instanceof AbstractMonsterGirl girl)) return null;
        girl.moveTo(holder.getX() + 1.0, holder.getY(), holder.getZ() + 0.5, holder.getYRot() + 180.0F, 0.0F);
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(holder.blockPosition()), MobSpawnType.EVENT, null);
        girl.setPersistenceRequired();
        level.addFreshEntity(girl);
        girl.addAffection(holder, FOND);
        s.setCount(0);
        level.sendParticles(ParticleTypes.SOUL, girl.getX(), girl.getY() + 1.0, girl.getZ(), 30, 0.4, 0.8, 0.4, 0.02);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.4, girl.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
        level.playSound(null, girl.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.NEUTRAL, 1.2F, 0.9F);
        holder.displayClientMessage(Component.translatable("message.harmontide.cursed.step_out." + (kind == GirlSpecies.CURSED_BLADE_UNDEAD ? "blade" : "armor"), girl.getDisplayName()), false);
        Dairy.award(holder, "cursed_gear");
        return girl;
    }

    /** A whisper from the gear, sweeter the fonder it is. */
    public static void whisper(Player p, ItemStack s) {
        int stage = Math.min(3, fondness(s) * 4 / FOND);
        p.displayClientMessage(Component.translatable("message.harmontide.cursed.whisper." + (blade(s) ? "blade" : "armor") + "." + stage)
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
    }

    /** Every tick it is carried (item inventoryTick): the armour's minutes worn and the whispers. */
    public static void carried(ServerLevel level, Player p, ItemStack s, boolean selected) {
        boolean worn = armour(s) && p.getItemBySlot(EquipmentSlot.CHEST) == s;
        boolean held = blade(s) && selected;
        if (!worn && !held) return;
        if (worn && level.getGameTime() % 1200 == 0) fonder(level, p, s, WORN);
        if (level.getGameTime() % WHISPER == 0 && p.getRandom().nextBoolean()) whisper(p, s);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player p && p.level() instanceof ServerLevel level && blade(p.getMainHandItem())
                && !(event.getEntity() instanceof Player)) fonder(level, p, p.getMainHandItem(), KILL);
    }

    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.getSpecies() == GirlSpecies.LIVING_ARMOR_UNDEAD && g.hasAnyAffection()) {
            event.setAmount(event.getAmount() * ARMOURED);
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity hurt = event.getEntity();
        if (!(hurt.level() instanceof ServerLevel level)) return;
        if (hurt instanceof Player p) {
            ItemStack chest = p.getItemBySlot(EquipmentSlot.CHEST);
            if (armour(chest)) fonder(level, p, chest, BLOW);                     // it took the blow for them
            if (event.getSource().getEntity() instanceof Mob attacker && attacker instanceof Enemy) {
                for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(GUARD),
                        g -> g.getSpecies() == GirlSpecies.LIVING_ARMOR_UNDEAD && g.getAffection(p) >= Orders.AFFECTION)) g.setTarget(attacker);
            }
        }
        if (event.getSource().getEntity() instanceof AbstractMonsterGirl g && g.getSpecies() == GirlSpecies.CURSED_BLADE_UNDEAD && g.hasAnyAffection()) {
            g.heal(event.getNewDamage() * HUNGER);                                 // her edge drinks
        }
    }

    @SubscribeEvent
    public static void onLoot(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();
        if (!"minecraft".equals(id.getNamespace()) || !CHESTS.contains(id.getPath())) return;
        event.getTable().addPool(LootPool.lootPool().name("harmontide_cursed_gear").setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(CHANCE))
                .add(LootItem.lootTableItem(ModItems.CURSED_BLADE.get()))
                .add(LootItem.lootTableItem(ModItems.LIVING_ARMOR.get()))
                .build());
    }
}
