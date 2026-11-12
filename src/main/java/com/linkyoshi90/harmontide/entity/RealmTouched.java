package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HolyFontBlock;
import com.linkyoshi90.harmontide.block.RealmGrassBlock;
import com.linkyoshi90.harmontide.entity.order.Order;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Realm-touched, after Monster Girl Encyclopedia's incubi: a player who keeps company with the Demon Realm slowly becomes
 * kin to it. Touch ({@link #touch}, 0 to {@link #MAX}, kept through death) grows with time in the realm's air, heartfruit
 * eaten, nights shared with the realm's own and sisters of the Order brought to fall; holy water splashed over them and
 * time near a holy font wash it out again.
 * <ul>
 *   <li>{@link #TOUCHED}: they see in the realm's dark (night vision while in its air at night), and its haze rises from them.</li>
 *   <li>{@link #KINDRED}: the realm's own know them as kin: wild devils and realm-changed girls don't pick a fight with them
 *   (unless struck first), and those girls are a little likelier to share their bed. The Order counts it as one more tie.</li>
 *   <li>{@link #REALM_BORN}: the realm feeds them (no hunger while in its air), and its night makes them strong.</li>
 * </ul>
 * Extended 2026-11-10: a spouse who is one of the realm's own, near them, adds {@link #SPOUSE} a minute (up to {@link #SPOUSE_CAP}
 * of them); KINDRED move quickly in the realm's air (Speed); REALM_BORN have {@link #HEALTH} more health, anywhere. The Order sees it:
 * a standing sister who sees a kindred player she isn't hunting sprinkles holy water on them (-{@link #SPRINKLE}, once every
 * {@link #SPRINKLE_EVERY} ticks at most), and a chaplain won't bless a kindred player's wedding.
 * Never anything beyond that: no change of body, no new model. Off with the option realmTouched.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class RealmTouched {
    public static final int TOUCHED = 25, KINDRED = 50, REALM_BORN = 75, MAX = 100;
    public static final int EVERY = 1200, AIR = 1, HEARTFRUIT = 4, NIGHT = 3, FALLEN = 5, SPLASH = 15, FONT = 1;
    public static final float KIN_BED = 0.15F;
    public static final int SPOUSE = 1, SPOUSE_CAP = 2, HEALTH = 4, SPRINKLE = 5, SPRINKLE_EVERY = 2400, SPRINKLE_RANGE = 12;
    public static final net.minecraft.resources.ResourceLocation REALM_HEALTH = HarmontideMod.id("realm_born");
    private static final String NEXT_SPRINKLE = "NextRealmSprinkle";

    private RealmTouched() {}

    public static int touch(Player player) {
        return player.getData(ModAttachments.REALM_TOUCH);
    }

    public static int stage(int touch) {
        return touch >= REALM_BORN ? 3 : touch >= KINDRED ? 2 : touch >= TOUCHED ? 1 : 0;
    }

    public static boolean kindred(Player player) {
        return HarmontideConfig.realmTouched() && touch(player) >= KINDRED;
    }

    public static void setTouch(Player player, int value) {
        int before = touch(player), after = Math.max(0, Math.min(MAX, value));
        player.setData(ModAttachments.REALM_TOUCH, after);
        int s0 = stage(before), s1 = stage(after);
        if (s1 != s0 && !player.level().isClientSide) {
            String key = s1 > s0 ? "message.harmontide.realm_touch.up." + s1 : "message.harmontide.realm_touch.down." + s1;
            player.displayClientMessage(Component.translatable(key).withStyle(s1 > s0 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA), false);
            if (s1 >= 2) Dairy.award(player, "realm_kindred");
            if (s1 >= 3) Dairy.award(player, "realm_born_player");
        }
    }

    public static void addTouch(Player player, int amount) {
        if (!player.level().isClientSide && HarmontideConfig.realmTouched()) setTouch(player, touch(player) + amount);
    }

    /** A line for /harmontide realm touch. */
    public static Component describe(Player player) {
        return Component.translatable("commands.harmontide.realm.touch." + stage(touch(player)), touch(player));
    }

    /** What the realm does for them this moment, and what it does to them every minute. */
    public static void tick(ServerLevel level, Player player, boolean minute) {
        if (!HarmontideConfig.realmTouched()) {
            health(player, false);
            return;
        }
        boolean air = DemonRealm.realmAir(player), night = !level.isDay();
        if (minute) {
            if (air) addTouch(player, AIR);
            if (HolyFontBlock.warded(level, player.blockPosition())) addTouch(player, -FONT);
            int wed = realmSpouses(level, player);
            if (wed > 0) addTouch(player, SPOUSE * Math.min(SPOUSE_CAP, wed));
        }
        int t = touch(player);
        health(player, t >= REALM_BORN);
        if (t >= KINDRED) {
            if (air) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0, true, false, false));
            sprinkle(level, player);
        }
        if (t >= TOUCHED && air && night) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, false));
            if (level.random.nextInt(4) == 0) level.sendParticles(RealmGrassBlock.HAZE, player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.3, 0.4, 0.3, 0.0);
        }
        if (t >= REALM_BORN && air) {
            if (player.getFoodData().getFoodLevel() < 20) player.getFoodData().eat(1, 0.5F);   // the realm feeds its own
            if (night) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 0, true, false, false));
        }
    }

    /** Their spouses near them (Marriage.NEAR) who are the realm's own. */
    public static int realmSpouses(ServerLevel level, Player player) {
        return (int) Marriage.spousesNear(level, player, player, Marriage.NEAR, null).stream().filter(Order::realmsOwn).count();
    }

    /** Puts on or lifts the realm-born health bonus. */
    public static void health(Player player, boolean on) {
        var attr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (attr == null || attr.hasModifier(REALM_HEALTH) == on) return;
        if (on) attr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(REALM_HEALTH, HEALTH,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        else {
            attr.removeModifier(REALM_HEALTH);
            if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
        }
    }

    /**
     * A sister of the Order who sees a kindred player (one she doesn't hunt) sprinkles holy water on them, to wash it out.
     * The sister who did, or null (none near, none can see them, or too soon since the last time).
     */
    @javax.annotation.Nullable
    public static com.linkyoshi90.harmontide.entity.order.OrderSister sprinkle(ServerLevel level, Player player) {
        if (!kindred(player) || player.isCreative() || player.isSpectator() || Order.notoriety(player) >= Order.MARKED) return null;
        if (level.getGameTime() < player.getPersistentData().getLong(NEXT_SPRINKLE)) return null;
        for (var sister : level.getEntitiesOfClass(com.linkyoshi90.harmontide.entity.order.OrderSister.class, player.getBoundingBox().inflate(SPRINKLE_RANGE),
                s -> s.standing() && !s.isChaplain() && s.hasLineOfSight(player))) {
            player.getPersistentData().putLong(NEXT_SPRINKLE, level.getGameTime() + SPRINKLE_EVERY);
            sister.getLookControl().setLookAt(player);
            sister.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH, player.getX(), player.getY() + player.getBbHeight(), player.getZ(), 20, 0.3, 0.2, 0.3, 0.0);
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_SPLASH, net.minecraft.sounds.SoundSource.NEUTRAL, 0.6F, 1.4F);
            player.displayClientMessage(Component.translatable("message.harmontide.realm_touch.sprinkled", sister.getDisplayName()).withStyle(ChatFormatting.AQUA), false);
            addTouch(player, -SPRINKLE);
            return sister;
        }
        return null;
    }

    /** A girl's extra odds for a kindred player's bed: the realm's own know them. */
    public static float bedBonus(Mob mob, Player player) {
        return Order.realmsOwn(mob) && kindred(player) ? KIN_BED : 0.0F;
    }

    /** Whether a wild one of the realm's own lets this player be. */
    public static boolean spares(Mob attacker, Player player) {
        return Order.realmsOwn(attacker) && kindred(player) && attacker.getLastHurtByMob() != player
                && !(attacker instanceof Bonded b && b.getAffection(player) >= Orders.AFFECTION);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.tickCount % 100 == 17 && player.level() instanceof ServerLevel level) tick(level, player, player.tickCount % EVERY == 17);
    }

    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && event.getNewAboutToBeSetTarget() instanceof Player p && !mob.level().isClientSide && spares(mob, p)) event.setCanceled(true);
    }

    /** Heartfruit eaten by a player. */
    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player p && event.getItem().is(ModItems.HEARTFRUIT.get())) addTouch(p, HEARTFRUIT);
    }

    /** A night beside one of the realm's own. */
    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent event) {
        Player p = event.getEntity();
        if (p.level().isClientSide || event.updateLevel()) return;
        boolean beside = !p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(3),
                m -> Order.realmsOwn(m) && m instanceof Bonded b && b.getAffection(p) >= Bonded.BED_AFFECTION).isEmpty();
        if (beside) addTouch(p, NIGHT);
    }
}
