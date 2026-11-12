package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Dates (2026-11-08). Sneak and use a flower on a close friend (one you could flirt with: grown, full size, never your
 * daughter): she blushes and tells you where she'd love to go, always the same kind of place for her: somewhere with lots of
 * flowers, the water's edge, a high place with a view, or under the open night sky. She keeps at your side. Stand there
 * together for {@link #TOGETHER} ticks and the date was lovely: affection, hearts, Regeneration and Luck. She waits
 * {@link #LENGTH} ticks for you to find a place, then sighs and calls it a day; one date a day each. Off with the option dates.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Dates {
    public static final int TOGETHER = 600, LENGTH = 12000, AFFECTION = 8, BESIDE = 4, FLOWERS = 6, FLOWER_LOOK = 6, WATER_LOOK = 4, VIEW_ABOVE_SEA = 40;
    private static final String WITH = "DateWith", UNTIL = "DateUntil", TIME = "DateTime", DAY = "DateDay";

    private Dates() {}

    public enum Place { FLOWERS, SHORE, VIEW, STARS }

    /** Where she'd love to go: always the same for her. */
    public static Place favourite(Mob girl) {
        UUID id = girl.getUUID();
        return Place.values()[(int) Math.floorMod(id.getLeastSignificantBits() ^ id.getMostSignificantBits(), (long) Place.values().length)];
    }

    public static boolean canDate(Mob girl, Player player) {
        return HarmontideConfig.dates() && Flirting.canFlirt(girl, player) && girl.isAlive() && !girl.isSleeping();
    }

    @Nullable
    public static UUID dateWith(Mob girl) {
        CompoundTag t = girl.getPersistentData();
        return t.hasUUID(WITH) ? t.getUUID(WITH) : null;
    }

    public static int together(Mob girl) {
        return girl.getPersistentData().getInt(TIME);
    }

    /** Asked out: she says where she'd love to go. False if she can't (not close enough, already out with you today). */
    public static boolean ask(ServerLevel level, Mob girl, Player player) {
        if (!canDate(girl, player) || dateWith(girl) != null) return false;
        long day = level.getDayTime() / 24000L;
        CompoundTag t = girl.getPersistentData();
        if (t.getLong(DAY) == day + 1) {                                  // stored +1 so 0 means never
            player.displayClientMessage(Component.translatable("message.harmontide.date.again", girl.getDisplayName()), true);
            return false;
        }
        t.putLong(DAY, day + 1);
        t.putUUID(WITH, player.getUUID());
        t.putLong(UNTIL, level.getGameTime() + LENGTH);
        t.putInt(TIME, 0);
        if (girl instanceof Bonded b) {
            b.setOrderedToSit(false);
            b.blushFor(200);
        }
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 4, 0.25, 0.15, 0.25, 0.0);
        player.displayClientMessage(Component.translatable("message.harmontide.date.ask." + favourite(girl).name().toLowerCase(), girl.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return true;
    }

    /** Whether this spot is her kind of place. */
    public static boolean fits(ServerLevel level, BlockPos pos, Place place) {
        return switch (place) {
            case FLOWERS -> {
                int n = 0;
                for (BlockPos p : BlockPos.betweenClosed(pos.offset(-FLOWER_LOOK, -2, -FLOWER_LOOK), pos.offset(FLOWER_LOOK, 2, FLOWER_LOOK))) {
                    if (level.getBlockState(p).is(BlockTags.FLOWERS) && ++n >= FLOWERS) yield true;
                }
                yield false;
            }
            case SHORE -> {
                if (!level.canSeeSky(pos.above())) yield false;
                for (BlockPos p : BlockPos.betweenClosed(pos.offset(-WATER_LOOK, -2, -WATER_LOOK), pos.offset(WATER_LOOK, 1, WATER_LOOK))) {
                    if (level.getFluidState(p).is(FluidTags.WATER)) yield true;
                }
                yield false;
            }
            case VIEW -> pos.getY() >= level.getSeaLevel() + VIEW_ABOVE_SEA && level.canSeeSky(pos.above());
            case STARS -> {
                long t = level.getDayTime() % 24000L;
                yield t >= 13000L && t < 23000L && !level.isRaining() && level.dimension() == Level.OVERWORLD && level.canSeeSky(pos.above());
            }
        };
    }

    /** Every second of the date: she keeps at your side, and time together at her kind of place adds up. */
    public static void tick(ServerLevel level, Mob girl, int ticks) {
        UUID with = dateWith(girl);
        if (with == null) return;
        CompoundTag t = girl.getPersistentData();
        Player p = level.getPlayerByUUID(with);
        if (p == null || !canDate(girl, p) || p.level() != level) {
            end(girl);
            return;
        }
        if (level.getGameTime() >= t.getLong(UNTIL)) {
            end(girl);
            p.displayClientMessage(Component.translatable("message.harmontide.date.bored", girl.getDisplayName()).withStyle(ChatFormatting.GRAY), false);
            return;
        }
        double d = girl.distanceToSqr(p);
        if (d > BESIDE * BESIDE) {
            if (!girl.isPassenger()) girl.getNavigation().moveTo(p, 1.15);
            return;
        }
        girl.getLookControl().setLookAt(p);
        if (!fits(level, p.blockPosition(), favourite(girl))) return;
        t.putInt(TIME, together(girl) + ticks);
        if (level.random.nextInt(4) == 0) level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 1, 0.2, 0.1, 0.2, 0.0);
        if (together(girl) >= TOGETHER) lovely(level, girl, p);
    }

    /** The date was lovely. */
    public static void lovely(ServerLevel level, Mob girl, Player p) {
        end(girl);
        if (girl instanceof Bonded b) {
            b.addAffection(p, AFFECTION);
            b.blushFor(400);
        }
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0));
        p.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 0));
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 12, 0.5, 0.4, 0.5, 0.0);
        p.displayClientMessage(Component.translatable("message.harmontide.date.lovely." + favourite(girl).name().toLowerCase(), girl.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        Dairy.award(p, "first_date");
        Marriage.sheAsks(level, girl, p);                                       // a devoted girl may ask first
    }

    public static void end(Mob girl) {
        CompoundTag t = girl.getPersistentData();
        t.remove(WITH);
        t.remove(UNTIL);
        t.remove(TIME);
        girl.getNavigation().stop();
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob girl && girl.tickCount % 20 == 7 && girl.level() instanceof ServerLevel level && girl instanceof Bonded) tick(level, girl, 20);
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player p = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!p.isShiftKeyDown() || !stack.is(ItemTags.SMALL_FLOWERS) || !(event.getTarget() instanceof Mob girl) || !(girl instanceof Bonded)
                || !(p.level() instanceof ServerLevel level) || !canDate(girl, p)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (ask(level, girl, p)) stack.consume(1, p);
    }
}
