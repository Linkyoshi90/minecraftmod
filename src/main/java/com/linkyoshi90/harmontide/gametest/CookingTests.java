package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Cooking;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Who can't cook (entity/Cooking). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class CookingTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyCookHerOwnWay")
    public static void everyCookHerOwnWay(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 5));
        girl.setNoAi(true);
        check(Cooking.skill(girl) == Cooking.skill(girl), "always the same cook");
        Cooking.setSkill(girl, Cooking.Skill.MASTERFUL);
        ItemStack feast = Cooking.dish(girl, new ItemStack(Items.BEEF));
        var food = feast.get(DataComponents.FOOD);
        check(feast.is(Items.COOKED_BEEF) && food != null && food.nutrition() == 8 + 3 && !food.effects().isEmpty(), "a feast of a steak");
        check(food.saturation() > 12.8F, "and filling: " + food.saturation());
        Cooking.setSkill(girl, Cooking.Skill.HOPELESS);
        ItemStack burnt = Cooking.dish(girl, new ItemStack(Items.BEEF));
        check(burnt.get(DataComponents.FOOD).nutrition() == 1 && burnt.get(DataComponents.FOOD).effects().size() == 2, "charred love food");
        check(Cooking.dish(girl, new ItemStack(Items.STONE)) == null, "she won't cook a stone");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        ItemStack raw = new ItemStack(Items.BEEF, 2);
        check(Cooking.cookFor(level, girl, p, raw) == null && raw.getCount() == 2, "only for a friend");
        girl.addAffection(p, Cooking.FRIEND);
        check(Cooking.cookFor(level, girl, p, raw) != null && raw.getCount() == 1, "she cooks one");
        check(Cooking.cookFor(level, girl, p, raw) == null, "not again straight away");
        int before = girl.getAffection(p);
        check(Cooking.eaten(level, p, burnt) == girl && girl.getAffection(p) == before + Cooking.ATE_BURNT, "you ate it all: she is overjoyed");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
