package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Theropod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The theropods (theropod_field_guide.html), a family of their own since 2026-10-29: the fernstalker (moved from the
 * lizardfolk; her ambush is tested in TwentiethWaveTests), the spinosaur and the australovenator.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class TheropodTests {

    /** A friend for her: a survival player with enough friendship to count. */
    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl girl, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, Orders.AFFECTION);
        return p;
    }

    private static <T extends AbstractMonsterGirl> T still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        T girl = spawn(helper, s, at);
        girl.setNoAi(true);
        return girl;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "theropodsAreTheirOwnFamily")
    public static void theropodsAreTheirOwnFamily(GameTestHelper helper) {
        for (GirlSpecies s : new GirlSpecies[]{GirlSpecies.FERNSTALKER_THEROPOD, GirlSpecies.SPINOSAUR_THEROPOD, GirlSpecies.AUSTRALOVENATOR_THEROPOD}) {
            check(s.family() == GirlSpecies.Family.THEROPOD, s.id() + " is a theropod");
            AbstractMonsterGirl girl = still(helper, s, new BlockPos(1, 2, 1));
            check(girl instanceof Theropod && girl instanceof Lizardfolk, s.id() + " is a Theropod, a cousin of the lizardfolk");
            check(girl.isFood(new ItemStack(Items.BEEF)) && girl.isFood(new ItemStack(Items.EGG)), s.id() + " eats raw meat and eggs");
            girl.discard();
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "spinosaurWadesTheRivers")
    public static void spinosaurWadesTheRivers(GameTestHelper helper) {
        Theropod spino = still(helper, GirlSpecies.SPINOSAUR_THEROPOD, new BlockPos(1, 2, 1));
        check(spino.riverWader(helper.getLevel()) == -1, "no friend, no grace");
        ServerPlayer p = friendOf(helper, spino, new BlockPos(3, 2, 3));
        check(spino.riverWader(helper.getLevel()) == 0, "not on dry land");
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.WATER);
        p.doTick();                                       // the test player isn't ticked by a connection: notice the water
        check(spino.riverWader(helper.getLevel()) == 1, "her friend is in the water");
        check(p.hasEffect(MobEffects.DOLPHINS_GRACE), "and swims like a dolphin");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "australovenatorHuntsWithHerFriend")
    public static void australovenatorHuntsWithHerFriend(GameTestHelper helper) {
        Theropod raptor = still(helper, GirlSpecies.AUSTRALOVENATOR_THEROPOD, new BlockPos(1, 2, 1));
        check(raptor.packHunt(helper.getLevel()) == -1, "no friend, no hunt");
        ServerPlayer p = friendOf(helper, raptor, new BlockPos(3, 2, 3));
        check(raptor.packHunt(helper.getLevel()) == 0, "her friend has struck nothing");
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 5));
        z.setNoAi(true);
        p.setLastHurtMob(z);                              // her friend strikes the zombie
        float before = z.getHealth();
        check(raptor.packHunt(helper.getLevel()) == 1, "she leaps on it too");
        check(z.getHealth() < before, "and wounds it");
        check(raptor.packHunt(helper.getLevel()) == 0, "once per hit");
        Wolf pet = helper.spawn(EntityType.WOLF, new BlockPos(5, 2, 1));
        pet.setNoAi(true);
        pet.tame(p);
        p.setLastHurtMob(pet);                            // a slip of the hand: never her friend's pet
        check(raptor.packHunt(helper.getLevel()) == 0, "she leaves the pet alone");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
