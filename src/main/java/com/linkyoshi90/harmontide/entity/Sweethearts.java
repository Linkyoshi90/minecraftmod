package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Villager sweethearts and grandchildren (2026-11-08). A player's grown daughter (full size, never one under
 * CHILDLIKE_HEIGHT) can take a shine to a villager: on her own now and then of a morning, or introduced by her father with
 * a flower given to the villager. By day she goes to see her sweetheart, and she blushes. After {@link #COURT_DAYS} mornings
 * near each other they are wed (the villager speaks well of her father: kinder prices), and every {@link #EVERY} days a
 * little one may come along, up to {@link #LITTLE_ONES}: the player's grandchild, born devoted to her grandparent (who, like a
 * father, is never anything more to her). Wholesome only: hearts and blushes. Should the sweetheart die she grieves, and may
 * one day find another. Off with the option sweethearts.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Sweethearts {
    public static final int COURT_DAYS = 3, LITTLE_ONES = 2, EVERY = 4, SHINE_ODDS = 4, NEAR = 32, VISIT = 24, INTRODUCE = 16, GOSSIP = 20, MORNING = 2000;
    private static final String SWEETHEART = "Sweetheart", COURTED = "SweetCourted", DAY = "SweetDay", WED = "SweetWed",
            LITTLE = "SweetLittleOnes", CARRYING = "SweetCarrying";

    private Sweethearts() {}

    /** A daughter who could court: grown, full size, born to a player, not a construct. */
    public static boolean eligible(Mob mob) {
        if (!HarmontideConfig.sweethearts() || !(mob instanceof Bonded b) || b.getFather() == null || !Missing.canLong(mob)) return false;
        UUID f = b.getFather();                                               // a player's: not a frog's, not a bull's
        if (f.equals(AbstractAnuran.FROG_FATHER) || mob.level() instanceof ServerLevel l && l.getEntity(f) instanceof Mob) return false;
        return !(mob instanceof AbstractMonsterGirl g && g.getSpecies().family() == GirlSpecies.Family.CONSTRUCT);
    }

    @Nullable
    public static UUID sweetheart(Mob girl) {
        CompoundTag t = girl.getPersistentData();
        return t.hasUUID(SWEETHEART) ? t.getUUID(SWEETHEART) : null;
    }

    public static boolean wed(Mob girl) {
        return girl.getPersistentData().getBoolean(WED);
    }

    public static int courted(Mob girl) {
        return girl.getPersistentData().getInt(COURTED);
    }

    public static int littleOnes(Mob girl) {
        return girl.getPersistentData().getInt(LITTLE);
    }

    /** The girl who has this villager for her sweetheart, among those loaded within reach of him, or null. */
    @Nullable
    public static Mob sweetheartOf(ServerLevel level, Villager villager) {
        for (Mob m : level.getEntitiesOfClass(Mob.class, villager.getBoundingBox().inflate(64), m -> m instanceof Bonded)) {
            if (villager.getUUID().equals(sweetheart(m))) return m;
        }
        return null;
    }

    /** A grown villager nobody has for a sweetheart yet. */
    public static boolean free(ServerLevel level, Villager v) {
        return v.isAlive() && !v.isBaby() && sweetheartOf(level, v) == null;
    }

    @Nullable
    private static Villager villager(ServerLevel level, Mob girl) {
        UUID id = sweetheart(girl);
        return id != null && level.getEntity(id) instanceof Villager v && v.isAlive() ? v : null;
    }

    @Nullable
    private static Player father(ServerLevel level, Mob girl) {
        UUID f = ((Bonded) girl).getFather();
        return f == null ? null : level.getPlayerByUUID(f);
    }

    private static void tell(ServerLevel level, Mob girl, String key, ChatFormatting colour) {
        Player f = father(level, girl);
        if (f != null) f.displayClientMessage(Component.translatable("message.harmontide.sweetheart." + key, girl.getDisplayName()).withStyle(colour), false);
    }

    private static void hearts(ServerLevel level, Entity e, int n) {
        level.sendParticles(ParticleTypes.HEART, e.getX(), e.getY() + e.getBbHeight() + 0.3, e.getZ(), n, 0.4, 0.2, 0.4, 0.0);
    }

    /** She takes a shine to him. False if she can't (not eligible, already has someone, or he's taken). */
    public static boolean court(ServerLevel level, Mob girl, Villager v) {
        if (!eligible(girl) || sweetheart(girl) != null || !free(level, v)) return false;
        CompoundTag t = girl.getPersistentData();
        t.putUUID(SWEETHEART, v.getUUID());
        t.putInt(COURTED, 0);
        t.putBoolean(WED, false);
        hearts(level, girl, 5);
        hearts(level, v, 3);
        ((Bonded) girl).blushFor(200);
        tell(level, girl, "shine", ChatFormatting.LIGHT_PURPLE);
        Dairy.award(father(level, girl), "sweetheart");
        return true;
    }

    /** A morning near him: courting goes on, a wedding, or (wed) now and then a little one on the way. */
    public static void morning(ServerLevel level, Mob girl, long day) {
        Villager v = villager(level, girl);
        if (sweetheart(girl) == null) {
            if (level.random.nextInt(SHINE_ODDS) != 0) return;
            for (Villager c : level.getEntitiesOfClass(Villager.class, girl.getBoundingBox().inflate(NEAR), c -> free(level, c))) {
                if (court(level, girl, c)) return;
            }
            return;
        }
        if (v == null || v.distanceToSqr(girl) > NEAR * NEAR) return;
        CompoundTag t = girl.getPersistentData();
        if (!wed(girl)) {
            t.putInt(COURTED, courted(girl) + 1);
            if (courted(girl) >= COURT_DAYS) wedding(level, girl, v);
            return;
        }
        if (girl instanceof AbstractMonsterGirl daughter) com.linkyoshi90.harmontide.entity.girl.MovingOut.moveIn(level, daughter, v);   // into her house
        Gestation g = ((Bonded) girl).getGestation();
        if (littleOnes(girl) < LITTLE_ONES && Math.floorMod(day, (long) EVERY) == 0 && g.canConceive()) expecting(girl);
    }

    public static void wedding(ServerLevel level, Mob girl, Villager v) {
        girl.getPersistentData().putBoolean(WED, true);
        hearts(level, girl, 10);
        hearts(level, v, 10);
        level.playSound(null, v.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.NEUTRAL, 1.0F, 1.2F);
        UUID f = ((Bonded) girl).getFather();
        if (f != null) v.getGossips().add(f, GossipType.MAJOR_POSITIVE, GOSSIP);   // the in-law speaks well of her father
        tell(level, girl, "wed", ChatFormatting.GOLD);
        Dairy.award(father(level, girl), "sweethearts_wed");
    }

    /** A little one on the way. She is born devoted to her grandparent, as her mother was to her father. */
    public static void expecting(Mob girl) {
        Bonded b = (Bonded) girl;
        b.getGestation().conceive(b.getFather());
        girl.getPersistentData().putInt(LITTLE, littleOnes(girl) + 1);
        girl.getPersistentData().putBoolean(CARRYING, true);
    }

    /** By day she goes to see him; close by, they look at each other, and she blushes. */
    public static void visit(ServerLevel level, Mob girl) {
        Villager v = villager(level, girl);
        if (v == null || girl.isPassenger() || girl.isLeashed() || girl.getTarget() != null || ((Bonded) girl).isOrderedToSit()) return;
        double d = girl.distanceToSqr(v);
        if (d > VISIT * VISIT) return;
        if (d > 3.0 * 3.0) {
            if (girl.getNavigation().isDone()) girl.getNavigation().moveTo(v, 0.8);
            return;
        }
        girl.getLookControl().setLookAt(v);
        v.getLookControl().setLookAt(girl);
        if (level.random.nextInt(3) == 0) hearts(level, girl, 1);
        ((Bonded) girl).blushFor(60);
    }

    /** She was expecting and isn't any more: the little one is here (or her clutch is laid). */
    public static void checkBirth(ServerLevel level, Mob girl) {
        if (!girl.getPersistentData().getBoolean(CARRYING) || ((Bonded) girl).getGestation().isCarrying()) return;
        girl.getPersistentData().putBoolean(CARRYING, false);
        hearts(level, girl, 6);
        tell(level, girl, "born", ChatFormatting.GREEN);
        Dairy.award(father(level, girl), "grandchild");
    }

    /** He has died: she grieves, and is free to find someone else one day. */
    public static void widowed(ServerLevel level, Mob girl) {
        CompoundTag t = girl.getPersistentData();
        t.remove(SWEETHEART);
        t.remove(COURTED);
        t.remove(WED);
        level.sendParticles(ParticleTypes.FALLING_WATER, girl.getX(), girl.getY() + girl.getBbHeight(), girl.getZ(), 10, 0.3, 0.1, 0.3, 0.0);
        tell(level, girl, "widowed", ChatFormatting.GRAY);
    }

    /** A line for /harmontide bonds and the like: who she is sweet on, or null. */
    @Nullable
    public static Component describe(Mob girl) {
        if (sweetheart(girl) == null) return null;
        return Component.translatable(wed(girl) ? "message.harmontide.sweetheart.describe.wed" : "message.harmontide.sweetheart.describe.courting",
                girl.getDisplayName(), littleOnes(girl));
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob girl) || !(girl.level() instanceof ServerLevel level) || girl.tickCount % 100 != 63 || !eligible(girl)) return;
        checkBirth(level, girl);
        long time = level.getDayTime(), day = time / 24000L;
        long of = time % 24000L;
        if (of < MORNING && girl.getPersistentData().getLong(DAY) != day + 1) {   // stored +1 so 0 means never
            girl.getPersistentData().putLong(DAY, day + 1);
            morning(level, girl, day);
        } else if (of >= MORNING && of < 11000L) {
            visit(level, girl);
        }
    }

    /** A flower given to a villager while your grown daughter is near: you introduce them. */
    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player p = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!(event.getTarget() instanceof Villager v) || !stack.is(ItemTags.SMALL_FLOWERS) || !(p.level() instanceof ServerLevel level)) return;
        if (!HarmontideConfig.sweethearts() || !free(level, v)) return;
        List<Mob> near = level.getEntitiesOfClass(Mob.class, v.getBoundingBox().inflate(INTRODUCE),
                m -> eligible(m) && p.getUUID().equals(((Bonded) m).getFather()) && sweetheart(m) == null);
        if (near.isEmpty()) return;
        near.sort((a, b) -> Double.compare(a.distanceToSqr(v), b.distanceToSqr(v)));
        if (!court(level, near.get(0), v)) return;
        stack.consume(1, p);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Villager v) || !(v.level() instanceof ServerLevel level)) return;
        Mob girl = sweetheartOf(level, v);
        if (girl != null) widowed(level, girl);
    }
}
