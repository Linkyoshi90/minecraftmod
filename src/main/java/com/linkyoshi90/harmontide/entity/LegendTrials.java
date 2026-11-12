package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Legendary trials (2026-11-11, after Grimoire of Gaia's boss drops). A wild legend (Rarity.LEGENDARY) tests a player who comes
 * within {@link #NOTICE} blocks and isn't yet her friend; pass and she is fond of them ({@link #REWARD}) and gives her relic
 * (item/RelicItem). Five kinds of legend, each with her trial and relic:
 * <ul>
 *   <li>SKY (the great birds): an OFFERING (a golden apple, a phantom membrane or a blaze rod), relic Sky Plume.</li>
 *   <li>SEA (the deep's giants): an OFFERING (a nautilus shell, prismarine crystals or a glow ink sac), relic Tide Pearl.</li>
 *   <li>SERPENT (the great snakes and lizards): an OFFERING (an emerald, a golden apple or an ender pearl), relic Serpent's Eye.</li>
 *   <li>DRAGON (dragons, hounds of hell, giants, the riders, the monkey king): STRENGTH, beat her fairly in melee to half health
 *   (she yields instead of falling), relic Dragon Scale.</li>
 *   <li>MOON (the night's own, the oracles, the sacred beasts, the deathless): a VIGIL, stay within {@link #WATCH} blocks of her
 *   for a whole night ({@link #VIGIL} ticks of it), relic Moon Mirror.</li>
 * </ul>
 * One trial at a time per legend, for {@link #LASTS} ticks; each player can pass each legend's once. Advancement "legend_trial"
 * (a criterion per kind), config "legendTrials".
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class LegendTrials {
    public enum Kind { OFFERING, STRENGTH, VIGIL }

    public enum Group {
        SKY(Kind.OFFERING, ModItems.SKY_PLUME, List.of(Items.GOLDEN_APPLE, Items.PHANTOM_MEMBRANE, Items.BLAZE_ROD)),
        SEA(Kind.OFFERING, ModItems.TIDE_PEARL, List.of(Items.NAUTILUS_SHELL, Items.PRISMARINE_CRYSTALS, Items.GLOW_INK_SAC)),
        SERPENT(Kind.OFFERING, ModItems.SERPENT_EYE, List.of(Items.EMERALD, Items.GOLDEN_APPLE, Items.ENDER_PEARL)),
        DRAGON(Kind.STRENGTH, ModItems.DRAGON_SCALE, List.of()),
        MOON(Kind.VIGIL, ModItems.MOON_MIRROR, List.of());

        public final Kind kind;
        public final Supplier<? extends Item> relic;
        public final List<Item> offerings;

        Group(Kind kind, Supplier<? extends Item> relic, List<Item> offerings) {
            this.kind = kind;
            this.relic = relic;
            this.offerings = offerings;
        }
    }

    public static final Map<String, Group> GROUPS = new HashMap<>();

    static {
        for (String id : List.of("phoenix_harpy", "zhuque_harpy", "yatagarasu_harpy", "roc_harpy", "ziz_harpy", "bennu_harpy", "garuda_harpy",
                "alkonost_harpy", "anzu_centaur", "ratatoskr_rodent")) GROUPS.put(id, Group.SKY);
        for (String id : List.of("leviathan_mermaid", "cetus_mermaid", "scylla_mollusk", "charybdis_mollusk", "giant_kraken_mollusk", "kun_dragon",
                "turtworld_lizardfolk", "black_tortoise_lizardfolk")) GROUPS.put(id, Group.SEA);
        for (String id : List.of("xiangliu_lamia", "echidna_lamia", "medusa_lamia", "wadjet_lamia", "basilisk_lizardfolk", "jormungandr_dragon",
                "orochi_dragon", "quetzalcoatl_dragon", "cipactli_lizardfolk")) GROUPS.put(id, Group.SERPENT);
        for (String id : List.of("long_dragon", "bahamut_dragon", "ender_dragon", "cerberus_canid", "amarok_canid", "behemoth_devil", "taotie_devil",
                "pazuzu_devil", "war_rider", "famine_rider", "pestilence_rider", "death_rider", "charro_negro_rider", "sun_wukong_rodent")) GROUPS.put(id, Group.DRAGON);
        for (String id : List.of("kaguya_elf", "norn_elf", "ophanim_elf", "bastet_nekomata", "ammit_nekomata", "nue_nekomata", "lilith_devil",
                "sphinx_centaur", "lamassu_centaur", "qilin_centaur", "urcuchillay_centaur", "ark_seraphim_centaur", "koschei_undead",
                "gashadokuro_undead")) GROUPS.put(id, Group.MOON);
    }

    public static final int NOTICE = 8, REWARD = 40, FRIEND = Orders.AFFECTION, WATCH = 24, VIGIL = 8000, VIGIL_STEP = 100;
    public static final long LASTS = 3 * 24000L;
    public static final String ADVANCEMENT = "legend_trial";
    private static final String TRIAL = "LegendTrial", PASSED = "LegendTrialPassed", PLAYER = "Player", OFFER = "Offering", UNTIL = "Until",
            WATCHED = "Watched";

    private LegendTrials() {}

    @Nullable
    public static Group group(Mob mob) {
        return GROUPS.get(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath());
    }

    /** Whether she would set this player a trial: a wild, grown legend not yet their friend, whose trial they haven't passed. */
    public static boolean open(Mob mob, Player player) {
        return HarmontideConfig.legendTrials() && mob instanceof Bonded b && group(mob) != null && !mob.isBaby() && b.getFather() == null
                && b.getAffection(player) < FRIEND && !passed(mob, player.getUUID()) && !player.isCreative() && !player.isSpectator();
    }

    public static boolean passed(Mob mob, UUID player) {
        for (Tag t : mob.getPersistentData().getList(PASSED, Tag.TAG_INT_ARRAY)) if (NbtUtils.loadUUID(t).equals(player)) return true;
        return false;
    }

    /** Her trial under way for this player, or null. */
    @Nullable
    public static CompoundTag trialFor(Mob mob, Player player) {
        CompoundTag t = mob.getPersistentData().getCompound(TRIAL);
        return t.hasUUID(PLAYER) && t.getUUID(PLAYER).equals(player.getUUID()) ? t : null;
    }

    public static boolean underWay(Mob mob) {
        return mob.getPersistentData().contains(TRIAL);
    }

    /** She sets the player her trial. Whether she did. */
    public static boolean begin(ServerLevel level, Mob mob, Player player) {
        Group g = group(mob);
        if (g == null || underWay(mob) || !open(mob, player)) return false;
        CompoundTag t = new CompoundTag();
        t.putUUID(PLAYER, player.getUUID());
        t.putLong(UNTIL, level.getGameTime() + LASTS);
        Component what;
        if (g.kind == Kind.OFFERING) {
            Item want = g.offerings.get(mob.getRandom().nextInt(g.offerings.size()));
            t.putString(OFFER, BuiltInRegistries.ITEM.getKey(want).toString());
            what = Component.translatable("message.harmontide.legend_trial.offering", mob.getDisplayName(), new ItemStack(want).getHoverName());
        } else {
            what = Component.translatable("message.harmontide.legend_trial." + g.kind.name().toLowerCase(Locale.ROOT), mob.getDisplayName());
        }
        mob.getPersistentData().put(TRIAL, t);
        player.displayClientMessage(what.copy().withStyle(ChatFormatting.GOLD), false);
        level.playSound(null, mob.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.NEUTRAL, 0.8F, 0.7F);
        mob.getLookControl().setLookAt(player);
        return true;
    }

    /** The trial is passed: she is fond of them and hands them her relic. */
    public static void pass(ServerLevel level, Mob mob, Player player) {
        Group g = group(mob);
        mob.getPersistentData().remove(TRIAL);
        ListTag list = mob.getPersistentData().getList(PASSED, Tag.TAG_INT_ARRAY);
        list.add(NbtUtils.createUUID(player.getUUID()));
        mob.getPersistentData().put(PASSED, list);
        if (mob instanceof Bonded b) {
            b.addAffection(player, REWARD);
            b.blushFor(100);
        }
        if (g != null) {
            ItemStack relic = new ItemStack(g.relic.get());
            if (!player.getInventory().add(relic)) player.drop(relic, false);
            if (player instanceof ServerPlayer sp) {
                var a = sp.server.getAdvancements().get(HarmontideMod.id(ADVANCEMENT));
                if (a != null) sp.getAdvancements().award(a, g.name().toLowerCase(Locale.ROOT));
            }
        }
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 40, 0.6, 0.6, 0.6, 0.3);
        level.playSound(null, mob.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 0.8F);
        player.displayClientMessage(Component.translatable("message.harmontide.legend_trial.passed", mob.getDisplayName(),
                g == null ? Component.empty() : new ItemStack(g.relic.get()).getHoverName()).withStyle(ChatFormatting.GOLD), false);
    }

    // --- noticing, the vigil, running out --------------------------------------------------------------------------

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 != 11 || !(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        if (group(mob) == null) return;
        if (underWay(mob)) {
            if (mob.tickCount % VIGIL_STEP == 11 || mob.tickCount % 200 == 51) update(level, mob);
            return;
        }
        if (net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()) return;   // the tests set their own going
        Player near = level.getNearestPlayer(mob, NOTICE);
        if (near != null) begin(level, mob, near);
    }

    /** Her trial goes on: run out, or (a vigil) another stretch of the night watched or broken off. */
    public static void update(ServerLevel level, Mob mob) {
        CompoundTag t = mob.getPersistentData().getCompound(TRIAL);
        Player player = t.hasUUID(PLAYER) ? level.getPlayerByUUID(t.getUUID(PLAYER)) : null;
        if (level.getGameTime() > t.getLong(UNTIL) || player != null && !HarmontideConfig.legendTrials()) {
            mob.getPersistentData().remove(TRIAL);
            if (player != null) player.displayClientMessage(Component.translatable("message.harmontide.legend_trial.lapsed", mob.getDisplayName()), false);
            return;
        }
        Group g = group(mob);
        if (g == null || g.kind != Kind.VIGIL || player == null) return;
        watch(level, mob, player, !level.isDay());
    }

    /** One stretch of the vigil: by night, near her, it adds up; dawn after a whole night passes it; leaving her starts it over. */
    public static void watch(ServerLevel level, Mob mob, Player player, boolean night) {
        CompoundTag t = trialFor(mob, player);
        if (t == null) return;
        int watched = t.getInt(WATCHED);
        if (night) {
            t.putInt(WATCHED, player.distanceToSqr(mob) <= WATCH * WATCH && player.isAlive() ? watched + VIGIL_STEP : 0);
        } else if (watched >= VIGIL) {
            pass(level, mob, player);
        } else if (watched > 0) {
            t.putInt(WATCHED, 0);
            player.displayClientMessage(Component.translatable("message.harmontide.legend_trial.vigil_broken", mob.getDisplayName()), false);
        }
    }

    public static int watched(Mob mob, Player player) {
        CompoundTag t = trialFor(mob, player);
        return t == null ? 0 : t.getInt(WATCHED);
    }

    // --- the offering ------------------------------------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        if (offer(level, mob, event.getEntity(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    /** The player hands her something: if it is what she asked for, the trial is passed. Whether it was. */
    public static boolean offer(ServerLevel level, Mob mob, Player player, ItemStack stack) {
        CompoundTag t = trialFor(mob, player);
        if (t == null || !t.contains(OFFER) || stack.isEmpty()) return false;
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ResourceLocation.tryParse(t.getString(OFFER)))) return false;
        if (!player.getAbilities().instabuild) stack.shrink(1);
        pass(level, mob, player);
        return true;
    }

    // --- strength ----------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onBlow(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        if (!(event.getSource().getEntity() instanceof Player player) || event.getSource().getDirectEntity() != player) return;
        Group g = group(mob);
        if (g == null || g.kind != Kind.STRENGTH || trialFor(mob, player) == null) return;
        if (mob.getHealth() - event.getAmount() > mob.getMaxHealth() / 2) return;
        event.setCanceled(true);
        if (mob instanceof AbstractMonsterGirl girl) girl.setFlag(AbstractMonsterGirl.FLAG_CALM, true);
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.getNavigation().stop();
        pass(level, mob, player);
    }
}
