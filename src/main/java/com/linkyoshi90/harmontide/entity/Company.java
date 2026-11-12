package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Small things friends do just by being around. Good morning: waking from a night's sleep with friends near, a player is well rested
 * (absorption for a few minutes). A fishing buddy: a friend within {@link #BUDDY} cheers them on, and one catch in {@link #EXTRA} brings
 * up a second. Thunder: timid girls (rabbits, rodents, cats, marsupials), and any little one to her parent, run to a friend in a thunderstorm and huddle against them
 * once a storm, for {@link #HUDDLE} friendship. A swim buddy: a sea-folk friend within {@link #BUDDY} of a swimming player keeps them
 * company in the water (dolphin's grace and water breathing while she is near). Warmth: a fire-proof friend (or a bear or one of her furry kin) within
 * {@link #WARM} thaws a freezing player. Sure-footed: walking beside a hoofed friend (cattle, goats, deer, camels, horses and their kin), a player steps up whole
 * blocks the way she does. A soft landing: falling beside a slime friend, a player lands on her and it hurts a quarter as much. A steady anvil:
 * with a construct friend beside them, an anvil a player works never wears down.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Company {
    public static final double MORNING = 12, BUDDY = 8, STORM = 24, CLOSE = 2.5, WARM = 4;
    public static final int EXTRA = 3, HUDDLE = 3, RESTED = 3600;
    public static final String STORM_DAY = "HuddledOn";
    static final Set<GirlSpecies.Family> TIMID = Set.of(GirlSpecies.Family.JACKALOPE, GirlSpecies.Family.RODENT, GirlSpecies.Family.NEKOMATA, GirlSpecies.Family.MARSUPIAL);

    private Company() {
    }

    /** A night's sleep over: friends near say good morning. How many. */
    public static int goodMorning(ServerLevel level, ServerPlayer p) {
        List<Mob> friends = HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(MORNING));
        if (friends.isEmpty() || !HarmontideConfig.comfort()) return 0;
        p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, RESTED, friends.size() >= 3 ? 1 : 0));
        p.displayClientMessage(Component.translatable(friends.size() == 1 ? "message.harmontide.morning" : "message.harmontide.morning.many",
                friends.get(0).getDisplayName(), friends.size() - 1), true);
        return friends.size();
    }

    @Nullable
    public static Mob buddy(ServerLevel level, ServerPlayer p) {
        List<Mob> friends = HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(BUDDY));
        return friends.isEmpty() ? null : friends.get(0);
    }

    public static boolean timid(Mob mob) {
        return mob instanceof AbstractMonsterGirl g && TIMID.contains(g.getSpecies().family());
    }

    /** Every five seconds in a thunderstorm: timid friends come to them, and huddle once they are close. How many huddled. */
    public static int thunder(ServerLevel level, ServerPlayer p) {
        if (!level.isThundering() || !HarmontideConfig.comfort()) return 0;
        long day = level.getDayTime() / 24000L + 1;
        int n = 0;
        java.util.List<Mob> near = new java.util.ArrayList<>(HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(STORM)));
        near.addAll(level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(STORM),          // friendsOf leaves out the little ones
                m -> m.isAlive() && m.isBaby() && m instanceof Bonded b && p.getUUID().equals(b.getFather())));
        for (Mob m : near) {
            boolean little = m.isBaby() && p.getUUID().equals(((Bonded) m).getFather());   // any little one of theirs runs to them too
            if (!(timid(m) || little) || m.isSleeping() || m.isPassenger() || m.getPersistentData().getLong(STORM_DAY) == day) continue;
            if (m.distanceToSqr(p) > CLOSE * CLOSE) {
                m.getNavigation().moveTo(p, 1.3);
                continue;
            }
            m.getPersistentData().putLong(STORM_DAY, day);
            Bonded g = (Bonded) m;
            m.getNavigation().stop();
            m.getLookControl().setLookAt(p);
            if (!little) g.blushFor(80);
            g.addAffection(p, HUDDLE);
            level.sendParticles(ParticleTypes.HEART, (m.getX() + p.getX()) / 2, m.getEyeY() + 0.3, (m.getZ() + p.getZ()) / 2, 3, 0.3, 0.2, 0.3, 0);
            p.displayClientMessage(Component.translatable(little ? "message.harmontide.thunder.little" : "message.harmontide.thunder", m.getDisplayName()), true);
            n++;
        }
        return n;
    }

    /** Every two seconds for a swimming player: a sea-folk friend near lends them her ease in the water. Who, or null. */
    @Nullable
    public static Mob swim(ServerLevel level, ServerPlayer p) {
        if (!(p.isInWater() || level.getFluidState(p.blockPosition()).is(net.minecraft.tags.FluidTags.WATER)) || !HarmontideConfig.comfort()) return null;
        for (Mob m : HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(BUDDY))) {
            if (!(m instanceof AbstractMonsterGirl g) || g.getSpecies().family() != GirlSpecies.Family.MERFOLK || !g.isInWater()) continue;
            if (!p.hasEffect(MobEffects.DOLPHINS_GRACE)) p.displayClientMessage(Component.translatable("message.harmontide.swim_buddy", g.getDisplayName()), true);
            p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, true, false));
            p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false));
            return g;
        }
        return null;
    }

    public static final java.util.Set<Clade> HOOVES = java.util.EnumSet.of(Clade.BOVIDAE, Clade.CERVIDAE, Clade.CAMELIDAE, Clade.PERISSODACTYLA);
    public static final net.minecraft.resources.ResourceLocation SURE_FOOTED = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(HarmontideMod.MODID, "sure_footed");
    public static final double STEP = 0.5;

    /** Every two seconds: a hoofed friend walking near a player on foot lends them her footing (a whole block's step), and it goes when she does. Who, or null. */
    @Nullable
    public static Mob sureFooted(ServerLevel level, ServerPlayer p) {
        var step = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT);
        if (step == null) return null;
        Mob with = null;
        if (HarmontideConfig.comfort() && !p.isPassenger() && !p.isInWater() && !p.getAbilities().flying) {
            for (Mob m : HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(BUDDY))) {
                if (m instanceof AbstractMonsterGirl g && HOOVES.contains(g.getSpecies().clade()) && g.getSpecies().family() != GirlSpecies.Family.MERFOLK
                        && !g.isInWater() && !g.isSleeping()) {
                    with = g;
                    break;
                }
            }
        }
        boolean had = step.hasModifier(SURE_FOOTED);
        if (with != null && !had) {
            step.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(SURE_FOOTED, STEP,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
            p.displayClientMessage(Component.translatable("message.harmontide.sure_footed", with.getDisplayName()), true);
        } else if (with == null && had) step.removeModifier(SURE_FOOTED);
        return with;
    }

    /** Bears, raccoons, weasels and their kin out of the water: thick fur to huddle into. */
    public static boolean furred(Mob m) {
        return m instanceof AbstractMonsterGirl g && g.getSpecies().clade() == Clade.ARCTOIDEA && g.getSpecies().family() != GirlSpecies.Family.MERFOLK;
    }

    /** Every two seconds for a freezing player: a friend who is warm to the touch (fire never hurts her) close by thaws them. Who, or null. */
    @Nullable
    public static Mob warm(ServerLevel level, ServerPlayer p) {
        if (p.getTicksFrozen() <= 0 || !HarmontideConfig.comfort()) return null;
        for (Mob m : HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(WARM))) {
            if (!m.fireImmune() && !furred(m)) continue;
            p.setTicksFrozen(0);
            level.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1.0, p.getZ(), 6, 0.3, 0.4, 0.3, 0.01);
            p.displayClientMessage(Component.translatable("message.harmontide.warm", m.getDisplayName()), true);
            return m;
        }
        return null;
    }

    // --- a soft landing, a steady anvil ------------------------------------------------------------------------------

    public static final double CUSHION = 4;
    public static final float CUSHIONED = 0.25F;

    /** A friend of this branch within reach of the player, or null. */
    @Nullable
    public static Mob friendOf(ServerLevel level, ServerPlayer p, Clade clade, double reach) {
        for (Mob m : HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(reach))) if (m instanceof AbstractMonsterGirl g && g.getSpecies().clade() == clade) return m;
        return null;
    }

    /** A fall that would hurt, with a slime friend within {@link #CUSHION}: she breaks it (a quarter of the damage). The slime, or null. */
    @Nullable
    public static Mob cushion(ServerLevel level, ServerPlayer p, float distance) {
        if (!HarmontideConfig.comfort() || distance <= 4.0F) return null;                     // (a hop off a ledge needs no cushion)
        Mob slime = friendOf(level, p, Clade.SLIME, CUSHION);
        if (slime == null) return null;
        level.playSound(null, p.blockPosition(), net.minecraft.sounds.SoundEvents.SLIME_JUMP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.9F);
        level.sendParticles(ParticleTypes.ITEM_SLIME, p.getX(), p.getY() + 0.2, p.getZ(), 12, 0.4, 0.1, 0.4, 0.1);
        p.displayClientMessage(Component.translatable("message.harmontide.cushioned", slime.getDisplayName()), true);
        return slime;
    }

    /** A construct friend within {@link #BUDDY} keeps the anvil they work steady. Her, or null. */
    @Nullable
    public static Mob steadyAnvil(ServerLevel level, ServerPlayer p) {
        return HarmontideConfig.comfort() ? friendOf(level, p, Clade.CONSTRUCT, BUDDY) : null;
    }

    @SubscribeEvent
    public static void onFall(net.neoforged.neoforge.event.entity.living.LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level() instanceof ServerLevel level && cushion(level, p, event.getDistance()) != null)
            event.setDamageMultiplier(event.getDamageMultiplier() * CUSHIONED);
    }

    @SubscribeEvent
    public static void onAnvil(net.neoforged.neoforge.event.entity.player.AnvilRepairEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level() instanceof ServerLevel level && event.getBreakChance() > 0) {
            Mob smith = steadyAnvil(level, p);
            if (smith == null) return;
            event.setBreakChance(0.0F);
            p.displayClientMessage(Component.translatable("message.harmontide.steady_anvil", smith.getDisplayName()), true);
        }
    }

    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && !event.updateLevel() && p.level() instanceof ServerLevel level && level.getDayTime() % 24000L < 12000L) goodMorning(level, p);   // the night slept through (the sky has not caught up yet)
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFished(ItemFishedEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel level) || event.getDrops().isEmpty()) return;
        Mob buddy = buddy(level, p);
        if (buddy == null || level.random.nextInt(EXTRA) != 0) return;
        ItemStack again = event.getDrops().get(0).copy();
        if (!p.addItem(again)) p.drop(again, false);
        p.displayClientMessage(Component.translatable("message.harmontide.fishing_buddy", buddy.getDisplayName()), true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level() instanceof ServerLevel level && p.tickCount % 100 == 61) thunder(level, p);
        if (event.getEntity() instanceof ServerPlayer p && p.level() instanceof ServerLevel level && p.tickCount % 40 == 17) {
            swim(level, p);
            warm(level, p);
            sureFooted(level, p);
            com.linkyoshi90.harmontide.entity.girl.Habits.ringLuck(level, p);
        }
    }
}
