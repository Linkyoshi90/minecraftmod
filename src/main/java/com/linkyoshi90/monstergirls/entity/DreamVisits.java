package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

import java.util.Comparator;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Dream visits, after Monster Girl Encyclopedia's succubi, who feed on a sleeper's spirit. On waking from a night slept through:
 * <ul>
 * <li>a succubus-kind friend (succubus, demon, Lilith) within {@link #RANGE} who would flirt with them visited their dreams:
 * a sweet dream, Luck and a little Regeneration for the morning, and she is one friendship fonder;</li>
 * <li>otherwise a wild succubus-kind girl nearby, not their friend, slipped into their dreams and fed: she takes a quarter of their
 * experience ({@link #MIN_TAKE} to {@link #MAX_TAKE} points), leaves a heartfruit by the bed, and thinks a little better of them (+{@link #FED}).
 * A tapir (Centaur#dreamWard) keeps her out.</li>
 * </ul>
 * Only grown, full-size visitors, never a player's own daughter; teasing lines only. Behind the dreamVisits option.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class DreamVisits {
    public static final double RANGE = 32;
    public static final int MIN_TAKE = 10, MAX_TAKE = 120, FED = 2, LINES = 4;
    public static final Set<GirlSpecies> VISITORS = Set.of(GirlSpecies.SUCCUBUS_DEVIL, GirlSpecies.DEMON_DEVIL, GirlSpecies.LILITH_DEVIL);
    public enum Dream { NONE, SWEET, FED, WARDED }

    private DreamVisits() {
    }

    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && !event.updateLevel() && p.level() instanceof ServerLevel level && level.getDayTime() % 24000L < 12000L) {
            dream(level, p);   // the night slept through, as Company#goodMorning
        }
    }

    private static boolean visitor(AbstractMonsterGirl g, Player p) {
        return g.isAlive() && VISITORS.contains(g.getSpecies()) && Missing.canLong(g) && !p.getUUID().equals(g.getFather());
    }

    @Nullable
    private static AbstractMonsterGirl nearest(ServerLevel level, Player p, java.util.function.Predicate<AbstractMonsterGirl> test) {
        return level.getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(RANGE), g -> visitor(g, p) && test.test(g)).stream()
                .min(Comparator.comparingDouble(g -> g.distanceToSqr(p))).orElse(null);
    }

    /** The night's dream. What it was. */
    public static Dream dream(ServerLevel level, Player p) {
        if (!MonsterGirlsConfig.dreamVisits() || p.isSpectator()) return Dream.NONE;
        AbstractMonsterGirl friend = nearest(level, p, g -> Flirting.canFlirt(g, p));
        if (friend != null) {
            p.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 0));
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            friend.addAffection(p, 1);
            friend.blushFor(200);
            level.sendParticles(ParticleTypes.HEART, friend.getX(), friend.getEyeY() + 0.3, friend.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
            say(p, "dream_sweet", friend);
            return Dream.SWEET;
        }
        AbstractMonsterGirl wild = nearest(level, p, g -> g.getAffection(p) < HomeLife.FRIEND);
        if (wild == null) return Dream.NONE;
        if (Centaur.dreamWard(p)) {
            say(p, "dream_warded", wild);
            return Dream.WARDED;
        }
        int take = taken(p);
        if (take > 0) p.giveExperiencePoints(-take);
        ItemEntity gift = new ItemEntity(level, p.getX(), p.getY() + 0.5, p.getZ(), new ItemStack(ModItems.HEARTFRUIT.get()));
        gift.setDefaultPickUpDelay();
        level.addFreshEntity(gift);
        wild.addAffection(p, FED);
        level.sendParticles(ParticleTypes.WITCH, p.getX(), p.getY() + 1.0, p.getZ(), 12, 0.4, 0.4, 0.4, 0.0);
        say(p, take > 0 ? "dream_fed" : "dream_empty", wild);
        return Dream.FED;
    }

    /** What a wild one takes from them: a quarter of their experience, within MIN_TAKE and MAX_TAKE, never more than they have. */
    public static int taken(Player p) {
        return Math.min(p.totalExperience, Math.max(MIN_TAKE, Math.min(MAX_TAKE, p.totalExperience / 4)));
    }

    private static void say(Player p, String key, AbstractMonsterGirl g) {
        p.displayClientMessage(Component.translatable("message.monstergirls." + key + "." + g.getRandom().nextInt(LINES), g.getDisplayName()), false);
    }
}
