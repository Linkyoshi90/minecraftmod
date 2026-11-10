package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Elf;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Old customs (2026-11-10, after Monster Girl Encyclopedia): some kinds take to a stranger in their own way, once each.
 * <ul>
 * <li>DUEL: a wild fighter (lizard warriors, minotaurs of the old breeds, oni and giants, hellhounds and wolves, wyrms,
 * valkyrie and dark elf, lioness) hit in melee by a player who isn't yet her friend never falls below half health to it:
 * the blow that would take her there makes her yield instead. She stops fighting (her pack too), stops seeing the
 * winner as prey, and respects them: REWARD friendship. Arrows don't count; it has to be face to face.</li>
 * <li>SNARE: a wild spinner (every arachne) or tigress now and then snares a player she can see within SNARE_RANGE (a
 * web at their feet; the tigress just runs them down) and comes to claim them. If they don't strike her before she
 * reaches them, she claims them: REWARD friendship, and the web is cut (the tigress pins them a moment). Struck, she
 * gives up for a day.</li>
 * <li>SERVICE: a wild house spirit (kikimora, brownie, domovoi) near a hearth whose owner is home lets herself in and
 * keeps it: every VISIT ticks at the fire she tidies (Elf.tidy, and the cobwebs go) for SERVICE_STEP friendship; after
 * VISITS she has made up her mind to stay fond of that house: REWARD more.</li>
 * </ul>
 * Each only with a player she likes less than FOND and hasn't courted before; grown, wild (no father) girls only.
 * Criteria of the advancement "old_customs". Config "courtship".
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Courtship {
    public enum Custom { DUEL, SNARE, SERVICE }

    public static final int REWARD = 30, FOND = 10, SNARE_RANGE = 12, SNARE_ODDS = 30, SNARE_LIMIT = 600, COOLDOWN = 24000,
            SERVICE_RANGE = 32, VISIT = 600, VISITS = 6, SERVICE_STEP = 2, HEARTH_NEAR = 4;
    public static final String ADVANCEMENT = "old_customs";
    private static final String COURTED = "Courted", SNARING = "Snaring", SNARE_UNTIL = "SnareUntil", STRUCK = "SnareStruck",
            NEXT_SNARE = "NextSnare", SERVED = "Served", NEXT_VISIT = "NextVisit";

    private static final Set<GirlSpecies> DUELISTS = EnumSet.of(
            GirlSpecies.SALAMANDER_LIZARDFOLK, GirlSpecies.CROC_LIZARDFOLK, GirlSpecies.KOMODO_LIZARDFOLK,
            GirlSpecies.MINOAN_MINOTAUR, GirlSpecies.AUROCHS_MINOTAUR, GirlSpecies.BISON_MINOTAUR, GirlSpecies.RHINO_MINOTAUR,
            GirlSpecies.ONI_DEVIL, GirlSpecies.OGRE_DEVIL, GirlSpecies.TROLL_DEVIL, GirlSpecies.FIRE_GIANT_DEVIL, GirlSpecies.FROST_GIANT_DEVIL,
            GirlSpecies.HELLHOUND_CANID, GirlSpecies.CERBERUS_CANID, GirlSpecies.AMAROK_CANID, GirlSpecies.WOLF_CANID,
            GirlSpecies.WYRM_DRAGON, GirlSpecies.WYVERN_DRAGON, GirlSpecies.BAHAMUT_DRAGON,
            GirlSpecies.VALKYRIE_ELF, GirlSpecies.DARK_ELF, GirlSpecies.LIONESS_NEKOMATA);
    private static final Set<GirlSpecies> SERVANTS = EnumSet.of(GirlSpecies.KIKIMORA_DEVIL, GirlSpecies.BROWNIE_ELF, GirlSpecies.DOMOVOI_ELF);

    private Courtship() {}

    /** Her kind's custom, or null. */
    @Nullable
    public static Custom of(GirlSpecies species) {
        if (DUELISTS.contains(species)) return Custom.DUEL;
        if (SERVANTS.contains(species)) return Custom.SERVICE;
        if (species.family() == GirlSpecies.Family.ARACHNE || species == GirlSpecies.TIGER_NEKOMATA) return Custom.SNARE;
        return null;
    }

    /** Whether her custom could still win her over for this player: grown, wild, not yet fond of them, not courted by them before. */
    public static boolean open(AbstractMonsterGirl girl, Player player) {
        return MonsterGirlsConfig.courtship() && !girl.isBaby() && girl.getFather() == null && !player.isCreative() && !player.isSpectator()
                && girl.getAffection(player) < FOND && !courted(girl, player.getUUID());
    }

    public static boolean courted(AbstractMonsterGirl girl, UUID player) {
        ListTag list = girl.getPersistentData().getList(COURTED, Tag.TAG_INT_ARRAY);
        for (Tag t : list) if (NbtUtils.loadUUID(t).equals(player)) return true;
        return false;
    }

    // --- the duel ------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onBlow(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof AbstractMonsterGirl girl) || !(girl.level() instanceof ServerLevel level)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        CompoundTag data = girl.getPersistentData();
        if (data.hasUUID(SNARING) && data.getUUID(SNARING).equals(player.getUUID())) data.putBoolean(STRUCK, true);   // struck while she came: no claim
        if (event.getSource().getDirectEntity() != player || of(girl.getSpecies()) != Custom.DUEL || !open(girl, player)) return;
        if (girl.getHealth() - event.getAmount() > girl.getMaxHealth() / 2) return;
        event.setCanceled(true);
        yieldTo(level, girl, player);
    }

    /** Beaten fairly: she and her kin close by stand down, and she won't see the winner as prey again. */
    public static void yieldTo(ServerLevel level, AbstractMonsterGirl girl, Player player) {
        for (AbstractMonsterGirl kin : level.getEntitiesOfClass(AbstractMonsterGirl.class, girl.getBoundingBox().inflate(16),
                g -> g == girl || g.getSpecies() == girl.getSpecies() && g.getTarget() == player)) {
            kin.setFlag(AbstractMonsterGirl.FLAG_CALM, true);
            kin.setTarget(null);
            kin.setLastHurtByMob(null);
            kin.getNavigation().stop();
        }
        level.sendParticles(ParticleTypes.CLOUD, girl.getX(), girl.getY() + 0.3, girl.getZ(), 10, 0.4, 0.1, 0.4, 0.02);
        win(level, girl, player, Custom.DUEL);
    }

    // --- the snare and the service -------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 10 != 0 || !(event.getEntity() instanceof AbstractMonsterGirl girl) || !(girl.level() instanceof ServerLevel level)) return;
        Custom custom = of(girl.getSpecies());
        if (custom == null || net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()) return;   // the tests set their own going
        if (custom == Custom.SNARE) {
            if (girl.getPersistentData().hasUUID(SNARING)) updateSnare(level, girl);
            else if (girl.tickCount % 40 == 0 && girl.getRandom().nextInt(SNARE_ODDS) == 0) trySnare(level, girl);
        } else if (custom == Custom.SERVICE) {
            serve(level, girl);
        }
    }

    /** She looks for someone to snare: in sight, not too far, and someone she could still court. Whether she did. */
    public static boolean trySnare(ServerLevel level, AbstractMonsterGirl girl) {
        if (girl.isInWater() || girl.getTarget() != null || level.getGameTime() < girl.getPersistentData().getLong(NEXT_SNARE)) return false;
        Player best = null;
        for (Player p : level.getEntitiesOfClass(Player.class, girl.getBoundingBox().inflate(SNARE_RANGE))) {
            if (!open(girl, p) || girl.isHostileTo(p) || !girl.hasLineOfSight(p)) continue;
            if (best == null || p.distanceToSqr(girl) < best.distanceToSqr(girl)) best = p;
        }
        return best != null && snare(level, girl, best);
    }

    /** The snare is sprung on this player: a web at their feet (a spinner's), and she comes for them. */
    public static boolean snare(ServerLevel level, AbstractMonsterGirl girl, Player player) {
        boolean spinner = girl.getSpecies().family() == GirlSpecies.Family.ARACHNE;
        if (spinner) {
            BlockPos feet = player.blockPosition();
            if (!level.getBlockState(feet).isAir()) return false;
            level.setBlockAndUpdate(feet, Blocks.COBWEB.defaultBlockState());
        }
        CompoundTag data = girl.getPersistentData();
        data.putUUID(SNARING, player.getUUID());
        data.putLong(SNARE_UNTIL, level.getGameTime() + SNARE_LIMIT);
        data.remove(STRUCK);
        player.displayClientMessage(Component.translatable("message.monstergirls.courtship.snare_" + (spinner ? "web" : "pounce"), girl.getDisplayName()), false);
        return true;
    }

    /** Twice a second while she comes: on to her catch, and the claim when she reaches them unharmed. */
    public static void updateSnare(ServerLevel level, AbstractMonsterGirl girl) {
        CompoundTag data = girl.getPersistentData();
        Player player = level.getPlayerByUUID(data.getUUID(SNARING));
        boolean failed = player == null || !player.isAlive() || data.getBoolean(STRUCK) || level.getGameTime() > data.getLong(SNARE_UNTIL)
                || player.distanceToSqr(girl) > 24 * 24 || !open(girl, player);
        if (failed) {
            if (player != null) cutWeb(level, player);
            endSnare(girl);
            data.putLong(NEXT_SNARE, level.getGameTime() + COOLDOWN);
            return;
        }
        boolean spinner = girl.getSpecies().family() == GirlSpecies.Family.ARACHNE;
        if (girl.distanceToSqr(player) > 2.5 * 2.5) {
            girl.getNavigation().moveTo(player, spinner ? 1.2 : 1.6);
            return;
        }
        girl.getNavigation().stop();
        if (spinner) cutWeb(level, player);
        else player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));   // pinned a moment
        endSnare(girl);
        win(level, girl, player, Custom.SNARE);
    }

    public static boolean snaring(AbstractMonsterGirl girl) {
        return girl.getPersistentData().hasUUID(SNARING);
    }

    private static void endSnare(AbstractMonsterGirl girl) {
        girl.getPersistentData().remove(SNARING);
        girl.getPersistentData().remove(SNARE_UNTIL);
        girl.getPersistentData().remove(STRUCK);
    }

    private static void cutWeb(ServerLevel level, Player player) {
        for (BlockPos pos : List.of(player.blockPosition(), player.blockPosition().above())) {
            if (level.getBlockState(pos).is(Blocks.COBWEB)) level.destroyBlock(pos, false);
        }
    }

    /**
     * Every VISIT ticks a wild house spirit looks for a hearth within SERVICE_RANGE whose owner is home (by it) and she could
     * court: she walks over, and once by the fire she keeps it. True if she did a visit's work.
     */
    public static boolean serve(ServerLevel level, AbstractMonsterGirl girl) {
        if (level.getGameTime() < girl.getPersistentData().getLong(NEXT_VISIT)) return false;
        girl.getPersistentData().putLong(NEXT_VISIT, level.getGameTime() + 40);    // look again soon if there's nothing yet
        if (girl.isBaby() || girl.getFather() != null) return false;
        BlockPos hearth = null;
        Player owner = null;
        for (BlockPos pos : HearthBlockEntity.loaded(level)) {
            if (pos.distSqr(girl.blockPosition()) > SERVICE_RANGE * SERVICE_RANGE) continue;
            if (!(level.getBlockEntity(pos) instanceof HearthBlockEntity h) || h.owner() == null) continue;
            Player p = level.getPlayerByUUID(h.owner());
            if (p == null || !(open(girl, p) || visits(girl, p) > 0 && !courted(girl, p.getUUID()) && !p.isSpectator()) || p.distanceToSqr(pos.getCenter()) > SERVICE_RANGE * SERVICE_RANGE) continue;
            if (hearth == null || pos.distSqr(girl.blockPosition()) < hearth.distSqr(girl.blockPosition())) {
                hearth = pos;
                owner = p;
            }
        }
        if (hearth == null) return false;
        if (girl.distanceToSqr(hearth.getCenter()) > HEARTH_NEAR * HEARTH_NEAR) {
            girl.getNavigation().moveTo(hearth.getX() + 0.5, hearth.getY() + 1, hearth.getZ() + 0.5, 1.0);
            return false;
        }
        return keep(level, girl, hearth, owner);
    }

    /** A visit's work at this hearth: loose things into the chests, cobwebs down, and a little fondness for its owner. */
    public static boolean keep(ServerLevel level, AbstractMonsterGirl girl, BlockPos hearth, Player owner) {
        CompoundTag data = girl.getPersistentData();
        data.putLong(NEXT_VISIT, level.getGameTime() + VISIT);
        Elf.tidy(level, girl, null);
        for (BlockPos pos : BlockPos.betweenClosed(hearth.offset(-6, -2, -6), hearth.offset(6, 4, 6))) {
            if (level.getBlockState(pos).is(Blocks.COBWEB)) level.destroyBlock(pos, false);
        }
        level.sendParticles(ParticleTypes.WHITE_ASH, girl.getX(), girl.getY() + 0.2, girl.getZ(), 12, 0.6, 0.1, 0.6, 0.0);
        girl.playSound(SoundEvents.BRUSH_GENERIC, 0.6F, 1.2F);
        String key = SERVED + owner.getUUID();
        int visits = data.getInt(key) + 1;
        data.putInt(key, visits);
        if (visits == 1) owner.displayClientMessage(Component.translatable("message.monstergirls.courtship.service_begun", girl.getDisplayName()), false);
        if (visits >= VISITS) {
            data.remove(key);
            win(level, girl, owner, Custom.SERVICE);
        } else {
            girl.addAffection(owner, SERVICE_STEP);
        }
        return true;
    }

    public static int visits(AbstractMonsterGirl girl, Player owner) {
        return girl.getPersistentData().getInt(SERVED + owner.getUUID());
    }

    // --- won over ------------------------------------------------------------------------------------------------

    /** Her custom is fulfilled: she remembers this player for good. */
    public static void win(ServerLevel level, AbstractMonsterGirl girl, Player player, Custom custom) {
        ListTag list = girl.getPersistentData().getList(COURTED, Tag.TAG_INT_ARRAY);
        list.add(NbtUtils.createUUID(player.getUUID()));
        girl.getPersistentData().put(COURTED, list);
        girl.addAffection(player, REWARD);
        girl.blushFor(100);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getY() + girl.getBbHeight(), girl.getZ(), 10, 0.5, 0.3, 0.5, 0.0);
        String name = custom.name().toLowerCase(Locale.ROOT);
        player.displayClientMessage(Component.translatable("message.monstergirls.courtship." + name, girl.getDisplayName()), false);
        if (player instanceof ServerPlayer sp) {
            AdvancementHolder a = sp.server.getAdvancements().get(MonsterGirlsMod.id(ADVANCEMENT));
            if (a != null) sp.getAdvancements().award(a, name);
        }
    }
}
