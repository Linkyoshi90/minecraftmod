package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.Journal;
import com.linkyoshi90.monstergirls.entity.Letters;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.CampfireBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Stories by the fire: at night, a player beside a lit campfire with {@link #CROWD} or more grown friends near gets a story
 * (one a night). The teller sits down and tells of a kind the player has not met, from her own people first: its page opens in
 * the journal as if it had been seen a few times (where it lives, when it comes out). Everyone listening grows {@link #CLOSER}.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Stories {
    public static final int CROWD = 2, CLOSER = 2, SIT = 400;
    public static final double FIRE = 4, CIRCLE = 8;
    public static final String NIGHT = "StoryNight";

    private Stories() {
    }

    private static long night(ServerLevel level) {
        return (level.getDayTime() + 12000) / 24000L + 1;
    }

    /** A lit campfire within reach of them, or null. */
    @Nullable
    public static BlockPos fire(ServerLevel level, ServerPlayer p) {
        for (BlockPos q : BlockPos.betweenClosed(p.blockPosition().offset(-(int) FIRE, -2, -(int) FIRE), p.blockPosition().offset((int) FIRE, 2, (int) FIRE))) {
            if (CampfireBlock.isLitCampfire(level.getBlockState(q))) return q.immutable();
        }
        return null;
    }

    public static List<AbstractMonsterGirl> circle(ServerLevel level, ServerPlayer p, BlockPos fire) {
        List<AbstractMonsterGirl> out = new ArrayList<>();
        for (Mob m : HomeLife.friendsOf(level, p, new net.minecraft.world.phys.AABB(fire).inflate(CIRCLE))) {
            if (m instanceof AbstractMonsterGirl g && !g.isSleeping() && g.getTarget() == null) out.add(g);
        }
        return out;
    }

    /** Who her story is about: a kind they have not heard of, from her own people if she can. Null when they know them all. */
    @Nullable
    public static GirlSpecies subject(ServerLevel level, ServerPlayer p, AbstractMonsterGirl teller) {
        String home = Letters.country(teller.getSpecies().family());
        List<GirlSpecies> near = new ArrayList<>(), far = new ArrayList<>();
        for (GirlSpecies s : GirlSpecies.values()) {
            if (s == teller.getSpecies() || Journal.sightings(p, s.id()) >= Journal.KNOWN || Journal.friends(p).contains(s.id())) continue;
            (Letters.country(s.family()).equals(home) ? near : far).add(s);
        }
        List<GirlSpecies> pool = near.isEmpty() ? far : near;
        return pool.isEmpty() ? null : pool.get(level.random.nextInt(pool.size()));
    }

    /** Every half minute: a story, if the night, the fire and the company are right. Who told it, or null. */
    @Nullable
    public static AbstractMonsterGirl check(ServerLevel level, ServerPlayer p) {
        if (!level.isNight() || p.isSpectator()) return null;
        CompoundTag data = p.getData(ModAttachments.JOURNAL);
        if (data.getLong(NIGHT) == night(level)) return null;
        BlockPos fire = fire(level, p);
        if (fire == null) return null;
        List<AbstractMonsterGirl> circle = circle(level, p, fire);
        if (circle.size() < CROWD) return null;
        data.putLong(NIGHT, night(level));
        p.setData(ModAttachments.JOURNAL, data);
        AbstractMonsterGirl teller = circle.get(level.random.nextInt(circle.size()));
        for (AbstractMonsterGirl g : circle) {
            g.addAffection(p, CLOSER);
            g.getNavigation().stop();
            g.getLookControl().setLookAt(teller);
            if (g.getAction() == AbstractMonsterGirl.Action.NONE) g.startTimedAction(AbstractMonsterGirl.Action.SITTING, SIT);
        }
        teller.getLookControl().setLookAt(p);
        level.sendParticles(ParticleTypes.LAVA, fire.getX() + 0.5, fire.getY() + 0.6, fire.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0);
        GirlSpecies about = subject(level, p, teller);
        if (about == null) {
            p.displayClientMessage(Component.translatable("message.monstergirls.story.old", teller.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
        } else {
            Journal.hearOf(p, about.id());
            p.displayClientMessage(Component.translatable("message.monstergirls.story", teller.getDisplayName(),
                    Component.translatable("entity.monstergirls." + about.id()), Component.translatable("journal.monstergirls." + about.id() + ".home")).withStyle(ChatFormatting.GOLD), false);
        }
        return teller;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level() instanceof ServerLevel level && p.tickCount % 600 == 233) check(level, p);
    }
}
