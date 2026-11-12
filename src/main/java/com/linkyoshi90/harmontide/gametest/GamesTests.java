package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.Snowballs;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.HideAndSeek;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Games with friends: hide and seek (entity.girl.HideAndSeek) and snowball fights (entity.Snowballs). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class GamesTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "hideAndSeek")
    public static void hideAndSeek(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        check(!HideAndSeek.start(level, girl, p), "a stranger won't play");
        girl.addAffection(p, Orders.AFFECTION);
        check(HideAndSeek.start(level, girl, p) && HideAndSeek.playing(girl) && HideAndSeek.spot(girl) != null, "a friend runs off to hide");
        check(p.hasEffect(MobEffects.BLINDNESS) && HideAndSeek.spot(girl).distSqr(p.blockPosition()) >= 7 * 7, "while they count, eyes shut; she hides well away");
        check(!HideAndSeek.start(level, girl, p), "one game at a time");
        int before = girl.getAffection(p);
        p.teleportTo(girl.getX() + 1, girl.getY(), girl.getZ());
        check(HideAndSeek.found(level, girl, p) && !HideAndSeek.playing(girl) && girl.getAffection(p) == before + HideAndSeek.REWARD, "found her!");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "snowballFight")
    public static void snowballFight(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 5));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 2, 9));
        friend.addAffection(p, Orders.AFFECTION);
        var level = helper.getLevel();
        check(!Snowballs.hit(level, stranger, p) && !Snowballs.fighting(stranger), "a stranger doesn't play along");
        check(Snowballs.hit(level, friend, p) && Snowballs.fighting(friend), "a friend takes up the fight");
        var ball = Snowballs.throwAt(level, friend, p);
        check(ball.isAlive() && ball.getOwner() == friend && ball.getDeltaMovement().lengthSqr() > 0.1, "and throws one back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
