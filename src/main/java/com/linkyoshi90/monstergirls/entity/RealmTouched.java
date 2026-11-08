package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HolyFontBlock;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.order.Order;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import com.linkyoshi90.monstergirls.registry.ModItems;
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
 * Never anything beyond that: no change of body, no new model. Off with the option realmTouched.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class RealmTouched {
    public static final int TOUCHED = 25, KINDRED = 50, REALM_BORN = 75, MAX = 100;
    public static final int EVERY = 1200, AIR = 1, HEARTFRUIT = 4, NIGHT = 3, FALLEN = 5, SPLASH = 15, FONT = 1;
    public static final float KIN_BED = 0.15F;

    private RealmTouched() {}

    public static int touch(Player player) {
        return player.getData(ModAttachments.REALM_TOUCH);
    }

    public static int stage(int touch) {
        return touch >= REALM_BORN ? 3 : touch >= KINDRED ? 2 : touch >= TOUCHED ? 1 : 0;
    }

    public static boolean kindred(Player player) {
        return MonsterGirlsConfig.realmTouched() && touch(player) >= KINDRED;
    }

    public static void setTouch(Player player, int value) {
        int before = touch(player), after = Math.max(0, Math.min(MAX, value));
        player.setData(ModAttachments.REALM_TOUCH, after);
        int s0 = stage(before), s1 = stage(after);
        if (s1 != s0 && !player.level().isClientSide) {
            String key = s1 > s0 ? "message.monstergirls.realm_touch.up." + s1 : "message.monstergirls.realm_touch.down." + s1;
            player.displayClientMessage(Component.translatable(key).withStyle(s1 > s0 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA), false);
            if (s1 >= 2) Dairy.award(player, "realm_kindred");
            if (s1 >= 3) Dairy.award(player, "realm_born_player");
        }
    }

    public static void addTouch(Player player, int amount) {
        if (!player.level().isClientSide && MonsterGirlsConfig.realmTouched()) setTouch(player, touch(player) + amount);
    }

    /** A line for /monstergirls realm touch. */
    public static Component describe(Player player) {
        return Component.translatable("commands.monstergirls.realm.touch." + stage(touch(player)), touch(player));
    }

    /** What the realm does for them this moment, and what it does to them every minute. */
    public static void tick(ServerLevel level, Player player, boolean minute) {
        if (!MonsterGirlsConfig.realmTouched()) return;
        boolean air = DemonRealm.realmAir(player), night = !level.isDay();
        if (minute) {
            if (air) addTouch(player, AIR);
            if (HolyFontBlock.warded(level, player.blockPosition())) addTouch(player, -FONT);
        }
        int t = touch(player);
        if (t >= TOUCHED && air && night) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, false));
            if (level.random.nextInt(4) == 0) level.sendParticles(RealmGrassBlock.HAZE, player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.3, 0.4, 0.3, 0.0);
        }
        if (t >= REALM_BORN && air) {
            if (player.getFoodData().getFoodLevel() < 20) player.getFoodData().eat(1, 0.5F);   // the realm feeds its own
            if (night) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 0, true, false, false));
        }
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
