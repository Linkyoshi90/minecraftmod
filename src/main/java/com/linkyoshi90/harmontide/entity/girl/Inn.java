package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.entity.ScoutMaps;
import com.linkyoshi90.harmontide.registry.ModGirls;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The wayside inn (2026-11-08, world/WaysideInnStructure). Its innkeeper, a holstaur, keeps it. Every morning travellers come
 * in, a girl or two of families from far away ({@link #GUESTS} at most at once), rest the day and are gone the next morning
 * unless someone has made a friend of them: the inn is a crossroads for meeting girls. An emerald to the innkeeper buys a hot
 * meal (a stew, bread, a little Regeneration and saturation), and one time in {@link #RUMOUR} a traveller's map with it.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Inn {
    public static final int GUESTS = 2, RUMOUR = 3, STAY = 24000, ROOM = 8;
    public static final String KEEPER = "Innkeeper", GUEST = "InnGuestLeaves", DAY = "InnDay";

    private Inn() {}

    public static boolean innkeeper(Mob m) {
        return m.getPersistentData().contains(KEEPER);
    }

    @Nullable
    public static BlockPos innOf(Mob keeper) {
        return innkeeper(keeper) ? BlockPos.of(keeper.getPersistentData().getLong(KEEPER)) : null;
    }

    public static void makeKeeper(Mob m, BlockPos inn) {
        m.getPersistentData().putLong(KEEPER, inn.asLong());
    }

    public static boolean guest(Mob m) {
        return m.getPersistentData().contains(GUEST);
    }

    public static List<Mob> guests(ServerLevel level, BlockPos inn) {
        return level.getEntitiesOfClass(Mob.class, new AABB(inn).inflate(ROOM * 2), Inn::guest);
    }

    /** The morning's travellers come in. Who came. */
    public static List<AbstractMonsterGirl> morning(ServerLevel level, BlockPos inn) {
        List<AbstractMonsterGirl> came = new ArrayList<>();
        int room = GUESTS - guests(level, inn).size();
        List<GirlSpecies> from = Market.candidates(level, inn);
        for (int i = 0; i < room && !from.isEmpty(); i++) {
            AbstractMonsterGirl girl = ModGirls.type(from.get(level.random.nextInt(from.size()))).create(level);
            if (girl == null) continue;
            BlockPos at = Market.spot(level, inn, girl);
            girl.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360F, 0);
            girl.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
            girl.setBaby(false);
            girl.setPersistenceRequired();
            girl.getPersistentData().putLong(GUEST, level.getGameTime() + STAY);
            girl.restrictTo(inn, ROOM);
            if (!level.addFreshEntity(girl)) continue;
            level.sendParticles(ParticleTypes.CLOUD, girl.getX(), girl.getY() + 0.5, girl.getZ(), 8, 0.3, 0.2, 0.3, 0.02);
            came.add(girl);
        }
        return came;
    }

    /** A traveller's day is over: she goes on, unless she's made a friend here (then she stays, like any girl). True if she left. */
    public static boolean moveOn(ServerLevel level, Mob m, boolean force) {
        if (!guest(m)) return false;
        if (m instanceof AbstractMonsterGirl g && g.hasAnyAffection()) {
            m.getPersistentData().remove(GUEST);
            m.clearRestriction();
            return false;
        }
        if (!force && level.getGameTime() < m.getPersistentData().getLong(GUEST)) return false;
        level.sendParticles(ParticleTypes.CLOUD, m.getX(), m.getY() + 0.5, m.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
        m.discard();
        return true;
    }

    /** An emerald for the innkeeper: a hot meal, and sometimes a rumour on a map. The map, if any. */
    @Nullable
    public static ItemStack meal(ServerLevel level, Mob keeper, Player p, ItemStack emerald, boolean rumour) {
        emerald.consume(1, p);
        give(p, new ItemStack(Items.RABBIT_STEW));
        give(p, new ItemStack(Items.BREAD, 2));
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
        p.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20, 0));
        level.playSound(null, keeper.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8F, 1.3F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, keeper.getX(), keeper.getEyeY(), keeper.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        Dairy.award(p, "wayside_inn");
        if (!rumour) {
            p.displayClientMessage(Component.translatable("message.harmontide.inn.meal", keeper.getDisplayName()), true);
            return null;
        }
        ItemStack map = ScoutMaps.chart(level, keeper.blockPosition(), ScoutMaps.ALL, level.random.nextInt(ScoutMaps.ALL.size()));
        if (map.isEmpty()) return null;
        give(p, map.copy());
        p.displayClientMessage(Component.translatable("message.harmontide.inn.rumour", keeper.getDisplayName(), map.getHoverName()).withStyle(ChatFormatting.GOLD), false);
        return map;
    }

    private static void give(Player p, ItemStack s) {
        if (!p.getInventory().add(s)) p.drop(s, false);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob m) || m.tickCount % 100 != 59 || !(m.level() instanceof ServerLevel level)) return;
        if (guest(m)) {
            moveOn(level, m, false);
            return;
        }
        BlockPos inn = innOf(m);
        if (inn == null) return;
        if (!m.hasRestriction()) m.restrictTo(inn, ROOM);                     // she keeps her inn, friend or not (a leash isn't saved)
        if (level.getDayTime() % 24000L >= 2000L) return;
        long day = level.getDayTime() / 24000L;
        if (m.getPersistentData().getLong(DAY) == day + 1) return;          // stored +1 so 0 means never
        m.getPersistentData().putLong(DAY, day + 1);
        morning(level, inn);
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.EMERALD) || !(event.getTarget() instanceof Mob m) || !innkeeper(m) || !(event.getLevel() instanceof ServerLevel level)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        meal(level, m, event.getEntity(), stack, level.random.nextInt(RUMOUR) == 0);
    }
}
