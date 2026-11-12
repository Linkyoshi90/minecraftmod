package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Liaison;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModItems;
import com.linkyoshi90.harmontide.registry.ModVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The exchange liaison (entity/Liaison): her desk, her trades, and what the homestay permit does. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class LiaisonTests {
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "liaisonDeskAndTrades")
    public static void liaisonDeskAndTrades(GameTestHelper helper) {
        BlockPos desk = new BlockPos(7, 2, 7);
        helper.setBlock(desk, ModBlocks.EXCHANGE_DESK.get());
        Villager v = helper.spawn(EntityType.VILLAGER, new BlockPos(3, 2, 3));
        v.setNoAi(true);
        v.setVillagerData(v.getVillagerData().setProfession(ModVillagers.LIAISON.get()));
        check(Liaison.isLiaison(v), "a liaison");
        check(v.getOffers().stream().anyMatch(o -> o.getResult().is(ModItems.HOMESTAY_PERMIT.get())), "she issues permits");
        helper.runAfterDelay(3, () -> {
            check(helper.getLevel().getPoiManager().existsAtPosition(ModVillagers.DESK_KEY, helper.absolutePos(desk)), "the desk is her workstation");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "permitKeepsGolemsOff")
    public static void permitKeepsGolemsOff(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(3, 2, 3));
        girl.setNoAi(true);
        IronGolem golem = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(10, 2, 10));
        golem.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        p.getInventory().add(new ItemStack(ModItems.HOMESTAY_PERMIT.get()));
        golem.setTarget(girl);
        check(golem.getTarget() == girl, "a stranger has nobody to vouch for her");
        golem.setTarget(null);
        girl.addAffection(p, 20);
        golem.setTarget(girl);
        check(golem.getTarget() == null, "the permit vouches for a friend");
        p.getInventory().clearContent();
        golem.setTarget(girl);
        check(golem.getTarget() == girl, "no permit, no word for her");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "villagersThinkBetterOfYou")
    public static void villagersThinkBetterOfYou(GameTestHelper helper) {
        Villager v = helper.spawn(EntityType.VILLAGER, new BlockPos(3, 2, 3));
        v.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        p.getInventory().add(new ItemStack(ModItems.HOMESTAY_PERMIT.get()));
        check(Liaison.goodWord(helper.getLevel(), p) == 0, "nobody with them to vouch for");
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(8, 2, 8));
        girl.setNoAi(true);
        girl.addAffection(p, 20);
        int before = v.getPlayerReputation(p);
        check(Liaison.goodWord(helper.getLevel(), p) == 1 && v.getPlayerReputation(p) > before, "the villager thinks better of them");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
