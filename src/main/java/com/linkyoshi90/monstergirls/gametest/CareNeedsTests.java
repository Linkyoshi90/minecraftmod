package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.OwlHarpy;
import com.linkyoshi90.monstergirls.entity.Wardrobe;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.CareNeeds;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.MovingOut;
import com.linkyoshi90.monstergirls.entity.girl.Settlement;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Care needs (entity/girl/CareNeeds): a pool for the merfolk, warmth for the lamias, moulting harpies. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class CareNeedsTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aPoolForTheMermaid")
    public static void aPoolForTheMermaid(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(5, 2, 5);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        HearthBlockEntity hearth = (HearthBlockEntity) helper.getBlockEntity(hp);
        hearth.setOwner(p.getUUID());
        AbstractMonsterGirl koi = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(6, 2, 6));
        koi.setNoAi(true);
        koi.setAffection(p.getUUID(), 30);
        check(Settlement.settle(level, koi, hearth), "she lives at the hearth");
        check(CareNeeds.pool(koi) == null && MovingOut.homeSpot(koi).equals(helper.absolutePos(hp)), "no pool: the fire is her place");
        check(CareNeeds.nag(level, koi) && koi.getAffection(p) == 30 + CareNeeds.NO_POOL, "she asks for one, a little put out");
        check(!CareNeeds.nag(level, koi), "once a day");
        for (int x = 1; x <= 5; x++) for (int z = 9; z <= 13; z++) {           // a 3x3 pool in a glass rim
            boolean rim = x == 1 || x == 5 || z == 9 || z == 13;
            helper.setBlock(new BlockPos(x, 2, z), rim ? Blocks.GLASS : Blocks.WATER);
        }
        koi.getPersistentData().remove("PoolChecked");
        BlockPos pool = CareNeeds.pool(koi);
        check(pool != null && level.getFluidState(pool).isSource() && MovingOut.homeSpot(koi).equals(pool), "a pool: she lives in it: " + pool);
        check(!CareNeeds.nag(level, koi), "and is content");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "lamiasFeelTheCold")
    public static void lamiasFeelTheCold(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl lamia = spawn(helper, GirlSpecies.COBRA_LAMIA, new BlockPos(3, 2, 3));
        lamia.setNoAi(true);
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(10, 2, 10));
        wolf.setNoAi(true);
        check(!CareNeeds.chill(level, lamia, false), "warm lands: nothing");
        check(!CareNeeds.chill(level, wolf, true), "a wolf doesn't mind the cold");
        check(CareNeeds.chill(level, lamia, true) && lamia.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "a lamia in the cold slows to a crawl");
        lamia.removeAllEffects();
        lamia.setOutfit(Wardrobe.Outfit.WINTER_COAT.ordinal() + 1);
        check(!CareNeeds.chill(level, lamia, true), "her winter coat keeps her warm");
        lamia.setOutfit(0);
        helper.setBlock(new BlockPos(5, 2, 3), Blocks.CAMPFIRE);
        check(!CareNeeds.chill(level, lamia, true), "so does a fire");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "harpiesMoult")
    public static void harpiesMoult(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        p.getInventory().clearContent();
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(5, 2, 5));
        owl.setNoAi(true);
        check(CareNeeds.moult(level, owl, true) == 0, "no friend, nobody to give them to");
        owl.setAffection(p.getUUID(), CareNeeds.MOULT_FRIEND);
        int n = CareNeeds.moult(level, owl, true);
        int got = 0;
        for (ItemStack s : p.getInventory().items) if (s.is(Items.FEATHER)) got += s.getCount();
        check(n >= CareNeeds.MOULT_MIN && n <= CareNeeds.MOULT_MAX && got == n, "she moults, and her friend gets the feathers: " + n);
        check(CareNeeds.moult(level, owl, false) == 0, "not again for days");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
