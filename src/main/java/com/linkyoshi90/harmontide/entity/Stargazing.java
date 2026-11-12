package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Treats;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Stargazing: on a clear night, a player standing still under open sky, looking up, for {@link #SETTLE} seconds is joined by their friends
 * within {@link #RANGE}, who sit down beside them and look up. Before long a shooting star crosses the sky (one a night): each
 * friend there gains {@link #WISH} friendship, and one of them whispers her wish, which tells the player her favourite treat.
 * Each star falls through a constellation they have not seen yet; the journal keeps the chart, and all twelve is an advancement.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Stargazing {
    public static final int SETTLE = 20, STAR_CHANCE = 30, STAR_LATEST = 90, WISH = 3, SIT = 300;
    public static final double RANGE = 12, BESIDE = 3;
    public static final float LOOK_UP = -40;
    public static final String STILL = "StargazeStill", AT = "StargazeAt", NIGHT = "StarNight", CHART = "Constellations", ADVANCEMENT = "stargazer";
    /** What the shooting star falls through: one new constellation a night, until a player has seen them all. */
    public static final List<String> CONSTELLATIONS = List.of("harpy", "fox", "serpent", "frog", "mermaid", "spider", "centaur", "dragon",
            "rabbit", "bat", "lantern", "hearth");

    private Stargazing() {
    }

    public static boolean clearNight(ServerLevel level) {
        return level.dimensionType().natural() && level.isNight() && !level.isRaining();
    }

    private static long night(ServerLevel level) {
        return (level.getDayTime() + 12000) / 24000L + 1;                       // the same number from dusk to dawn
    }

    public static boolean seenTonight(ServerLevel level, ServerPlayer p) {
        return p.getData(ModAttachments.JOURNAL).getLong(NIGHT) == night(level);
    }

    /** Friends near a player who can come and look at the stars. */
    public static List<Mob> company(ServerLevel level, ServerPlayer p) {
        return HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(RANGE)).stream()
                .filter(m -> !m.isSleeping() && !m.isPassenger() && m.getTarget() == null).toList();
    }

    /** Once a second: how long they have stood still under the stars (0 when they moved, or it is no night for it). */
    public static int step(ServerLevel level, ServerPlayer p) {
        CompoundTag pd = p.getPersistentData();
        long at = p.blockPosition().asLong();
        boolean ok = HarmontideConfig.stargazing() && clearNight(level) && level.canSeeSky(p.blockPosition().above()) && !p.isSpectator()
                && p.getXRot() <= LOOK_UP;                                         // looking up at the sky
        if (!ok || pd.getLong(AT) != at || seenTonight(level, p)) {
            pd.putLong(AT, at);
            pd.putInt(STILL, 0);
            return 0;
        }
        int still = pd.getInt(STILL) + 1;
        pd.putInt(STILL, still);
        if (still < SETTLE) return still;
        List<Mob> friends = company(level, p);
        if (friends.isEmpty()) return still;
        for (Mob m : friends) {
            if (m.distanceToSqr(p) > BESIDE * BESIDE) {
                m.getNavigation().moveTo(p, 1.0);
            } else if (m instanceof AbstractMonsterGirl girl && girl.getAction() != AbstractMonsterGirl.Action.SITTING) {
                m.getNavigation().stop();
                girl.startTimedAction(AbstractMonsterGirl.Action.SITTING, SIT);
            }
        }
        if (still == SETTLE) p.displayClientMessage(Component.translatable("message.harmontide.stargazing.join", friends.get(0).getDisplayName()), true);
        if (still >= SETTLE + STAR_LATEST || level.random.nextInt(STAR_CHANCE) == 0) shootingStar(level, p);
        return still;
    }

    /** A shooting star over a player: friends beside them make their wishes. How many were there to see it. */
    public static int shootingStar(ServerLevel level, ServerPlayer p) {
        CompoundTag data = p.getData(ModAttachments.JOURNAL);
        data.putLong(NIGHT, night(level));
        p.setData(ModAttachments.JOURNAL, data);
        p.getPersistentData().putInt(STILL, 0);
        double a = level.random.nextDouble() * Math.PI * 2;
        Vec3 from = p.position().add(Math.cos(a) * 30, 40, Math.sin(a) * 30), to = p.position().add(-Math.cos(a) * 20, 28, -Math.sin(a) * 20);
        for (int i = 0; i <= 40; i++) {
            Vec3 q = from.lerp(to, i / 40.0);
            level.sendParticles(p, ParticleTypes.END_ROD, true, q.x, q.y, q.z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 1.0F, 2.0F);
        chart(p);
        int seen = 0;
        AbstractMonsterGirl whisper = null;
        for (Mob m : company(level, p)) {
            if (m.distanceToSqr(p) > (BESIDE + 2) * (BESIDE + 2)) continue;
            Bonded b = (Bonded) m;
            b.addAffection(p, WISH);
            b.blushFor(60);
            level.sendParticles(ParticleTypes.HEART, m.getX(), m.getEyeY() + 0.4, m.getZ(), 3, 0.3, 0.2, 0.3, 0);
            if (whisper == null && m instanceof AbstractMonsterGirl girl && !Treats.knows(p, girl)) whisper = girl;
            seen++;
        }
        if (whisper != null) {
            Treats.learn(p, whisper, "message.harmontide.stargazing.wish");
        } else {
            p.displayClientMessage(Component.translatable(seen > 0 ? "message.harmontide.stargazing.star" : "message.harmontide.stargazing.alone").withStyle(ChatFormatting.AQUA), false);
        }
        return seen;
    }

    /** The constellations a player has seen. */
    public static List<String> seen(ServerPlayer p) {
        List<String> out = new ArrayList<>();
        for (Tag t : p.getData(ModAttachments.JOURNAL).getList(CHART, Tag.TAG_STRING)) out.add(t.getAsString());
        return out;
    }

    /** A new constellation on their chart (and the advancement once all are), or null when they have them all. */
    @Nullable
    public static String chart(ServerPlayer p) {
        List<String> seen = seen(p);
        List<String> left = CONSTELLATIONS.stream().filter(c -> !seen.contains(c)).toList();
        if (left.isEmpty()) return null;
        String c = left.get(p.getRandom().nextInt(left.size()));
        CompoundTag data = p.getData(ModAttachments.JOURNAL);
        ListTag list = data.getList(CHART, Tag.TAG_STRING);
        list.add(StringTag.valueOf(c));
        data.put(CHART, list);
        p.setData(ModAttachments.JOURNAL, data);
        p.displayClientMessage(Component.translatable("message.harmontide.constellation", Component.translatable("constellation.harmontide." + c), list.size(), CONSTELLATIONS.size()).withStyle(ChatFormatting.DARK_AQUA), false);
        if (list.size() >= CONSTELLATIONS.size()) {
            AdvancementHolder a = p.server.getAdvancements().get(HarmontideMod.id(ADVANCEMENT));
            if (a != null) p.getAdvancements().award(a, "done");
        }
        return c;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level() instanceof ServerLevel level && p.tickCount % 20 == 11) step(level, p);
    }
}
