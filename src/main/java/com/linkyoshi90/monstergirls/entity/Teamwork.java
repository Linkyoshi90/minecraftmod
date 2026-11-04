package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Teamwork: two friends of the same player (Orders.companions) fighting the same foe set each other up. When one strikes a
 * foe a teammate struck within the last three seconds, the blow lands half again as hard. Some open the way in their own
 * fashion: a harpy knocks the foe up into the air, a slime or a spider girl slows it. Their friend earns an advancement
 * the first time.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Teamwork {
    public static final int WINDOW = 60, OPENER_EVERY = 100;
    public static final float BONUS = 1.5F;
    public static final String BY = "TeamHitBy", AT = "TeamHitAt", OPENED = "TeamOpenedAt", ADVANCEMENT = "teamwork";

    private Teamwork() {
    }

    /** A hit by a fighter on a foe: the damage after teamwork. */
    public static float hit(ServerLevel level, Mob fighter, LivingEntity foe, float amount) {
        if (!MonsterGirlsConfig.teamwork() || !(fighter instanceof Bonded) || foe instanceof Player || Orders.companions(fighter, foe)) return amount;
        long now = level.getGameTime();
        var data = foe.getPersistentData();
        Entity last = data.hasUUID(BY) && now - data.getLong(AT) <= WINDOW ? level.getEntity(data.getUUID(BY)) : null;
        data.putUUID(BY, fighter.getUUID());
        data.putLong(AT, now);
        if (last != null && last != fighter && last.isAlive() && Orders.companions(fighter, last)) {
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, foe.getX(), foe.getY() + foe.getBbHeight() * 0.6, foe.getZ(), 12, 0.3, 0.3, 0.3, 0.2);
            level.playSound(null, foe.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 1.0F, 1.2F);
            ServerPlayer friend = sharedFriend(level, fighter, last);
            if (friend != null) {
                AdvancementHolder a = friend.server.getAdvancements().get(MonsterGirlsMod.id(ADVANCEMENT));
                if (a != null) friend.getAdvancements().award(a, "done");
            }
            return amount * BONUS;
        }
        if (now - data.getLong(OPENED) >= OPENER_EVERY) {                    // she opens: her family's way of setting a foe up
            data.putLong(OPENED, now);
            open(fighter, foe);
        }
        return amount;
    }

    /** A harpy knocks it up, a slime or a spider girl slows it. */
    static void open(Mob fighter, LivingEntity foe) {
        if (fighter instanceof AbstractHarpy) {
            foe.setDeltaMovement(foe.getDeltaMovement().add(0, 0.55, 0));
            foe.hurtMarked = true;
        } else if (fighter instanceof AbstractMonsterGirl g && (g.getSpecies().family() == GirlSpecies.Family.SLIME || g.getSpecies().family() == GirlSpecies.Family.ARACHNE)) {
            foe.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        }
    }

    @Nullable
    private static ServerPlayer sharedFriend(ServerLevel level, Entity a, Entity b) {
        for (ServerPlayer p : level.players()) {
            if (a instanceof Bonded ba && b instanceof Bonded bb && ba.getAffection(p) >= Orders.AFFECTION && bb.getAffection(p) >= Orders.AFFECTION) return p;
        }
        return null;
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getSource().getEntity() instanceof Mob fighter && fighter instanceof Bonded) {
            event.setAmount(hit(level, fighter, event.getEntity(), event.getAmount()));
        }
    }
}
