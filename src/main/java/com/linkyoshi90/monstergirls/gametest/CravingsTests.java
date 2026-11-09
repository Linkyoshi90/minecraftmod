package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Cravings;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Cravings (entity/girl/Cravings): she tells a friend what she craves, it's fed to her, and she has twins. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class CravingsTests {
    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "cravingAnsweredBringsTwins")
    public static void cravingAnsweredBringsTwins(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7));
        cat.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        check(Cravings.craving(cat) == null && Cravings.tick(level, cat) == null, "nothing to crave before she expects");
        cat.getGestation().conceive(p.getUUID());
        cat.setAffection(p.getUUID(), 30);
        Item want = Cravings.craving(cat);
        check(want != null && Cravings.FOODS.contains(want) && Cravings.craving(cat) == want, "one food, and she sticks to it");
        check(Cravings.tick(level, cat) == p && Cravings.tick(level, cat) == null, "she tells her friend once");
        Item wrong = want == Items.BREAD ? Items.APPLE : Items.BREAD;
        check(!Cravings.feed(level, cat, p, new ItemStack(wrong)), "not that");
        int before = cat.getAffection(p);
        check(Cravings.feed(level, cat, p, new ItemStack(want)) && Cravings.answered(cat) && cat.getAffection(p) == before + Cravings.FED, "exactly that");
        check(!Cravings.feed(level, cat, p, new ItemStack(want)), "one is enough");
        cat.setNoAi(false);
        cat.getGestation().setTicksLeft(0);
        BlockPos at = helper.absolutePos(new BlockPos(7, 2, 7));
        helper.succeedWhen(() -> {
            int kittens = level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(at).inflate(10), g -> g.isBaby() && g.getType() == cat.getType()).size();
            check(!cat.getGestation().isCarrying() && kittens >= 4, "two litters of two or three: " + kittens);
            check(Cravings.craving(cat) == null && !Cravings.answered(cat), "and the craving is forgotten");
            HarpyTestUtil.remove(p);
        });
    }
}
