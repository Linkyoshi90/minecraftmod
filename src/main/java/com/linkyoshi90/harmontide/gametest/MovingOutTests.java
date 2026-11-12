package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.Sweethearts;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.MovingOut;
import com.linkyoshi90.harmontide.entity.girl.Settlement;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Daughters move out nearby (entity/girl/MovingOut): a grown daughter builds her own house, has a housewarming, and her sweetheart moves in. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class MovingOutTests {
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "daughterBuildsAHouseOfHerOwn")
    public static void daughterBuildsAHouseOfHerOwn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(3, 2, 3);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 3));
        HearthBlockEntity hearth = (HearthBlockEntity) helper.getBlockEntity(hp);
        hearth.setOwner(p.getUUID());

        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.FENNEC_KITSUNE, new BlockPos(4, 2, 5));
        friend.setNoAi(true);
        friend.addAffection(p, 200);
        check(Settlement.settle(level, friend, hearth) && !MovingOut.movesOut(level, friend), "a friend who settles builds the usual hut");

        AbstractMonsterGirl daughter = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(5, 2, 3));
        daughter.setNoAi(true);
        daughter.bornTo(p.getUUID());
        daughter.addAffection(p, 60);
        check(Settlement.settle(level, daughter, hearth) && MovingOut.movesOut(level, daughter), "his grown daughter moves out");

        BlockPos origin = helper.absolutePos(new BlockPos(7, 2, 7));
        MovingOut.mark(daughter, true);
        daughter.planHutAt(origin, Direction.WEST);
        List<Settlement.Piece> plan = Settlement.plan(daughter);
        check(MovingOut.size(daughter) == MovingOut.HOUSE && plan.size() > Settlement.hut(GirlSpecies.SERVAL_NEKOMATA, origin, Direction.WEST).size(), "a house, bigger than a hut");
        for (int i = 0; i < plan.size() && !daughter.hutDone(); i++) Settlement.buildStep(level, daughter);
        check(daughter.hutDone(), "she builds it all");
        int halves = 0, cradles = 0, chests = 0;
        for (Settlement.Piece pc : plan) {
            var st = level.getBlockState(pc.pos());
            check(st.is(pc.state().getBlock()), "every piece in its place: " + pc);
            if (st.getBlock() instanceof BedBlock) halves++;
            if (st.is(ModBlocks.CRADLE.get())) cradles++;
            if (st.is(Blocks.CHEST)) chests++;
        }
        check(halves == 4 && cradles == 1 && chests == 1, "two beds, a cradle and a chest: " + halves + "/" + cradles + "/" + chests);

        List<Settlement.Piece> homely = Settlement.homely(daughter);
        for (Settlement.Piece pc : homely) check(plan.stream().noneMatch(q -> q.pos().equals(pc.pos())), "her things go where nothing of the house is: " + pc);
        for (int i = 0; i < homely.size(); i++) check(Settlement.makeHomely(level, daughter) != null, "one thing each morning");
        check(Settlement.makeHomely(level, daughter) == null, "and then it's homely");

        BlockPos mid = MovingOut.middle(daughter);
        check(mid != null && !MovingOut.tick(level, daughter), "no housewarming while her father is over at the hearth");
        int before = daughter.getAffection(p);
        p.teleportTo(mid.getX() + 0.5, mid.getY(), mid.getZ() + 0.5);
        check(MovingOut.tick(level, daughter), "his first visit is the housewarming: " + p.position() + " vs " + mid);
        check(daughter.getAffection(p) >= before, "closer (or as close as can be)");
        check(!MovingOut.tick(level, daughter), "once");

        Villager sweetheart = helper.spawn(EntityType.VILLAGER, new BlockPos(3, 2, 12));
        sweetheart.setNoAi(true);
        check(Sweethearts.court(level, daughter, sweetheart), "she has a sweetheart");
        check(!MovingOut.moveIn(level, daughter, sweetheart), "he doesn't move in before the wedding");
        Sweethearts.wedding(level, daughter, sweetheart);
        check(MovingOut.moveIn(level, daughter, sweetheart), "wed, he moves in");
        GlobalPos home = sweetheart.getBrain().getMemory(MemoryModuleType.HOME).orElse(null);
        check(home != null && home.pos().equals(MovingOut.sweetheartBed(daughter)), "the second bed is his home now: " + home);
        check(!MovingOut.moveIn(level, daughter, sweetheart) && MovingOut.movedIn(daughter), "once");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
