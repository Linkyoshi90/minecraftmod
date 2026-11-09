package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;

/**
 * A birthday cake (2026-11-09). On her birthday (Treats.birthday), a lit candle cake within {@link #FIND} of a grown girl brings her
 * over: she blows the candle out, makes a wish, and every friend of hers within {@link #PARTY} who is there is {@link #WISH} closer.
 * Once a birthday. The cake is still there to eat.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class BirthdayCake {
    public static final int FIND = 8, PARTY = 12, WISH = 8;
    static final String BLOWN = "CandlesBlown";

    private BirthdayCake() {
    }

    @Nullable
    public static BlockPos litCake(ServerLevel level, BlockPos from) {
        for (BlockPos p : BlockPos.betweenClosed(from.offset(-FIND, -2, -FIND), from.offset(FIND, 2, FIND))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof CandleCakeBlock && s.getValue(CandleCakeBlock.LIT)) return p.immutable();
        }
        return null;
    }

    /** Every few seconds on her birthday: off to the cake, and the candle out once there. How many friends shared the wish (-1: nothing yet). */
    public static int tick(ServerLevel level, AbstractMonsterGirl girl, boolean birthday) {
        long day = level.getDayTime() / 24000L + 1;
        if (!birthday || girl.isBaby() || girl.getPersistentData().getLong(BLOWN) == day) return -1;
        BlockPos cake = litCake(level, girl.blockPosition());
        if (cake == null) return -1;
        if (girl.distanceToSqr(cake.getCenter()) > 2.5 * 2.5) {
            girl.getNavigation().moveTo(cake.getX() + 0.5, cake.getY(), cake.getZ() + 0.5, 1.1);
            return -1;
        }
        girl.getPersistentData().putLong(BLOWN, day);
        level.setBlockAndUpdate(cake, level.getBlockState(cake).setValue(CandleCakeBlock.LIT, false));
        level.playSound(null, cake, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.SMOKE, cake.getX() + 0.5, cake.getY() + 0.7, cake.getZ() + 0.5, 6, 0.05, 0.1, 0.05, 0.01);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 10, 0.5, 0.4, 0.5, 0.0);
        girl.getLookControl().setLookAt(cake.getCenter());
        int n = 0;
        for (Player p : level.getEntitiesOfClass(Player.class, girl.getBoundingBox().inflate(PARTY), p -> !p.isSpectator() && girl.getAffection(p) >= HomeLife.FRIEND)) {
            girl.addAffection(p, WISH);
            p.displayClientMessage(Component.translatable("message.monstergirls.birthday.cake." + girl.getRandom().nextInt(3), girl.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
            n++;
        }
        return n;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.tickCount % 40 == 17 && g.level() instanceof ServerLevel level) {
            tick(level, g, Treats.isBirthday(g, level.getDayTime()));
        }
    }
}
