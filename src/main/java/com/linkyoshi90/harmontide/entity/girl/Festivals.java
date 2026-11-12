package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.FridgeBlockEntity;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.JukeboxSensing;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * Festivals: three days in every 24 (by the world's day count, no seasons mod needed) the girls who live at a hearth gather
 * round it at dusk. Its owner joins them there and they celebrate, once a festival: the Midsummer Dance wants a jukebox playing
 * near the hearth (each friend dances and is the fonder for it), the Harvest Feast wants food in a fridge near it (each eats,
 * and the owner feasts: regeneration), at the Day of Hearts each takes a slice of the cake set out by the hearth and gives her friend a
 * sweetheart cookie (a wife blushes all evening), and at the Gift Exchange each brings her friend a present. The morning of a festival
 * every player hears of it; her journal has the calendar.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Festivals {
    public enum Festival {
        DANCE(7), HARVEST(15), HEARTS(19), GIFTS(23);

        /** Which day of the cycle (from 0). */
        public final int day;

        Festival(int day) {
            this.day = day;
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final int CYCLE = 24, DUSK = 11500, LATE = 14000, RANGE = 8, FOOD_RANGE = 12, BONUS = 10;
    public static final String TOLD = "FestivalTold", DONE = "FestivalDone", FIRST = "first_festival", EVERY = "every_festival";

    private Festivals() {
    }

    /** The festival on a day (from the world's day time), or null. */
    @Nullable
    public static Festival on(long dayTime) {
        int d = (int) Math.floorMod(dayTime / 24000L, (long) CYCLE);
        for (Festival f : Festival.values()) if (f.day == d) return f;
        return null;
    }

    /** Whether it is the festival's gathering: dusk on a festival day. */
    public static boolean gathering(long dayTime) {
        long t = Math.floorMod(dayTime, 24000L);
        return on(dayTime) != null && t >= DUSK && t < LATE;
    }

    /** Days until the next festival (0 on one), and which. */
    public static Festival next(long dayTime) {
        int d = (int) Math.floorMod(dayTime / 24000L, (long) CYCLE);
        for (Festival f : Festival.values()) if (f.day >= d) return f;
        return Festival.DANCE;
    }

    public static int daysUntil(long dayTime) {
        int d = (int) Math.floorMod(dayTime / 24000L, (long) CYCLE);
        return Math.floorMod(next(dayTime).day - d, CYCLE);
    }

    private static boolean on() {
        return HarmontideConfig.festivals();
    }

    /** The girls at home for the festival: residents of the hearth, grown, within reach of it. */
    public static List<AbstractMonsterGirl> guests(ServerLevel level, BlockPos hearth) {
        List<AbstractMonsterGirl> out = new ArrayList<>();
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(hearth).inflate(RANGE), AbstractMonsterGirl::isAlive)) {
            if (!g.isBaby() && hearth.equals(g.homeHearth())) out.add(g);
        }
        return out;
    }

    /** Everyone at the festival: the residents, and the owner's harpy and frog friends who have come along (they don't live at hearths). */
    public static List<net.minecraft.world.entity.Mob> party(ServerLevel level, BlockPos hearth, Player owner) {
        List<net.minecraft.world.entity.Mob> out = new ArrayList<>(guests(level, hearth));
        for (net.minecraft.world.entity.Mob m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new AABB(hearth).inflate(RANGE), net.minecraft.world.entity.Mob::isAlive)) {
            if (!(m instanceof AbstractMonsterGirl) && m instanceof com.linkyoshi90.harmontide.entity.Bonded b && !m.isBaby()
                    && b.getAffection(owner) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION) out.add(m);
        }
        return out;
    }

    /** What the festival still wants, as a hint key, or null if it is ready. */
    @Nullable
    public static String wants(ServerLevel level, BlockPos hearth, Festival f) {
        return switch (f) {
            case DANCE -> JukeboxSensing.findPlaying(level, hearth, 16) == null ? "message.harmontide.festival.wants_music" : null;
            case HARVEST -> fridge(level, hearth) == null ? "message.harmontide.festival.wants_food" : null;
            case HEARTS -> cake(level, hearth) == null ? "message.harmontide.festival.wants_cake" : null;
            case GIFTS -> null;
        };
    }

    /** A cake (with slices left) set out within RANGE of the hearth. */
    @Nullable
    public static BlockPos cake(ServerLevel level, BlockPos hearth) {
        for (BlockPos p : BlockPos.betweenClosed(hearth.offset(-RANGE, -2, -RANGE), hearth.offset(RANGE, 2, RANGE))) {
            if (level.getBlockState(p).getBlock() instanceof net.minecraft.world.level.block.CakeBlock) return p.immutable();
        }
        return null;
    }

    /** A slice of the Day of Hearts cake: one bite off it, and the last bite takes the plate. */
    static void slice(ServerLevel level, BlockPos cake) {
        var state = level.getBlockState(cake);
        if (!(state.getBlock() instanceof net.minecraft.world.level.block.CakeBlock)) return;
        int bites = state.getValue(net.minecraft.world.level.block.CakeBlock.BITES);
        if (bites < 6) level.setBlockAndUpdate(cake, state.setValue(net.minecraft.world.level.block.CakeBlock.BITES, bites + 1));
        else level.removeBlock(cake, false);
        level.playSound(null, cake, SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.6F, 1.2F);
    }

    @Nullable
    private static FridgeBlockEntity fridge(ServerLevel level, BlockPos hearth) {
        for (BlockPos p : BlockPos.betweenClosed(hearth.offset(-FOOD_RANGE, -3, -FOOD_RANGE), hearth.offset(FOOD_RANGE, 3, FOOD_RANGE))) {
            if (!(level.getBlockEntity(p) instanceof FridgeBlockEntity f)) continue;
            for (int i = 0; i < f.store().getContainerSize(); i++) if (FridgeBlockEntity.isFood(f.store().getItem(i))) return f;
        }
        return null;
    }

    /** The owner celebrates with the girls at her hearth. How many celebrated (0: no one home, or the festival isn't ready). */
    public static int celebrate(ServerLevel level, ServerPlayer player, BlockPos hearth, Festival f) {
        List<net.minecraft.world.entity.Mob> guests = party(level, hearth, player);
        if (guests.isEmpty() || wants(level, hearth, f) != null) return 0;
        for (net.minecraft.world.entity.Mob g : guests) {
            switch (f) {
                case DANCE -> {
                    g.getJumpControl().jump();
                    level.sendParticles(ParticleTypes.NOTE, g.getX(), g.getY() + g.getBbHeight() + 0.3, g.getZ(), 4, 0.4, 0.2, 0.4, 1.0);
                }
                case HARVEST -> {
                    FridgeBlockEntity fridge = fridge(level, hearth);
                    if (fridge != null) fridge.serve(level, g);
                }
                case HEARTS -> {
                    BlockPos cake = cake(level, hearth);
                    if (cake != null) slice(level, cake);
                    ItemStack cookie = new ItemStack(net.minecraft.world.item.Items.COOKIE);
                    cookie.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("item.harmontide.sweetheart_cookie", g.getName()));
                    if (!player.addItem(cookie)) player.drop(cookie, false);
                    if (com.linkyoshi90.harmontide.entity.Marriage.married(g, player)) ((com.linkyoshi90.harmontide.entity.Bonded) g).blushFor(2400);
                }
                case GIFTS -> {
                    ItemStack gift = g instanceof com.linkyoshi90.harmontide.entity.GiftGiver giver ? giver.rollGift(player) : ItemStack.EMPTY;
                    if (!gift.isEmpty() && !player.addItem(gift)) player.drop(gift, false);
                }
            }
            ((com.linkyoshi90.harmontide.entity.Bonded) g).addAffection(player, BONUS);
            level.sendParticles(ParticleTypes.HEART, g.getX(), g.getY() + g.getBbHeight() + 0.3, g.getZ(), 4, 0.4, 0.2, 0.4, 0);
        }
        if (f == Festival.HARVEST) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 10, 0));
        }
        level.sendParticles(ParticleTypes.FIREWORK, hearth.getX() + 0.5, hearth.getY() + 2.5, hearth.getZ() + 0.5, 30, 1.0, 0.6, 1.0, 0.05);
        level.playSound(null, hearth, SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.harmontide.festival.celebrate." + f.key(), guests.size()).withStyle(ChatFormatting.GOLD), false);
        award(player, FIRST, "done");
        award(player, EVERY, f.key());
        return guests.size();
    }

    private static void award(ServerPlayer sp, String id, String criterion) {
        AdvancementHolder a = sp.server.getAdvancements().get(HarmontideMod.id(id));
        if (a != null) sp.getAdvancements().award(a, criterion);
    }

    /** Every second: tell her of a festival day, and celebrate when she is at her hearth with her friends at dusk. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp) || sp.tickCount % 20 != 0 || !on() || !(sp.level() instanceof ServerLevel level)) return;
        long time = level.getDayTime(), day = time / 24000L + 1;                  // + 1: 0 means never
        Festival f = on(time);
        if (f == null || !level.dimensionType().natural()) return;
        CompoundTag data = sp.getData(ModAttachments.JOURNAL);
        if (data.getLong(TOLD) != day) {
            data.putLong(TOLD, day);
            sp.setData(ModAttachments.JOURNAL, data);
            sp.displayClientMessage(Component.translatable("message.harmontide.festival.today." + f.key()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        if (!gathering(time) || data.getLong(DONE) == day) return;
        for (BlockPos pos : List.copyOf(HearthBlockEntity.loaded(level))) {
            if (pos.distToCenterSqr(sp.position()) > RANGE * RANGE || !(level.getBlockEntity(pos) instanceof HearthBlockEntity h)
                    || !sp.getUUID().equals(h.owner())) continue;
            if (celebrate(level, sp, pos, f) > 0) {
                data.putLong(DONE, day);
                sp.setData(ModAttachments.JOURNAL, data);
                return;
            }
            String hint = wants(level, pos, f);
            if (hint != null && sp.tickCount % 200 == 0) sp.displayClientMessage(Component.translatable(hint), true);
        }
    }

    /** At a festival's dusk she goes to her hearth and stays near it. */
    public static class GatherGoal extends Goal {
        private final AbstractMonsterGirl girl;

        public GatherGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        private boolean due() {
            BlockPos home = this.girl.homeHearth();
            return home != null && on() && gathering(this.girl.level().getDayTime()) && this.girl.getTarget() == null && !this.girl.isBaby()
                    && this.girl.getOrders().command(this.girl) == com.linkyoshi90.harmontide.entity.Orders.Command.FREE_ROAM;
        }

        @Override
        public boolean canUse() {
            BlockPos home = this.girl.homeHearth();
            return this.girl.tickCount % 20 == 0 && this.due() && home.distToCenterSqr(this.girl.position()) > 16;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos home = this.girl.homeHearth();
            return this.due() && !this.girl.getNavigation().isDone() && home.distToCenterSqr(this.girl.position()) > 6;
        }

        @Override
        public void start() {
            BlockPos home = this.girl.homeHearth();
            if (home != null) this.girl.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);
        }
    }
}
