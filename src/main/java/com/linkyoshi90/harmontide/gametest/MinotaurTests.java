package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.registry.ModGirls;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The cattle family (minotaur_field_guide.html): milk, bulls, farm breeding. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class MinotaurTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "minotaurCowGivesMilk")
    public static void minotaurCowGivesMilk(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Minotaur cow = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(7, 2, 7));
        cow.setMale(false);
        interact(cow, player, new ItemStack(Items.BUCKET));
        check(player.getInventory().countItem(Items.MILK_BUCKET) == 1, "a bucket of milk");
        player.getInventory().selected = 8;                                  // keep the milk, hold a fresh bucket
        interact(cow, player, new ItemStack(Items.BUCKET));
        check(player.getInventory().countItem(Items.MILK_BUCKET) == 1, "not again right away");
        Minotaur bull = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(10, 2, 10));
        bull.setMale(true);
        player.getInventory().selected = 7;
        interact(bull, player, new ItemStack(Items.BUCKET));
        check(player.getInventory().countItem(Items.MILK_BUCKET) == 1, "bulls give no milk");
        check(!bull.canShareBed(), "bulls never share a player's bed");
        check(bull.getBbHeight() > cow.getBbHeight(), "bulls are taller");
        CompoundTag tag = new CompoundTag();
        bull.saveWithoutId(tag);
        AbstractMonsterGirl copy = ModGirls.type(GirlSpecies.CATTLE_MINOTAUR).create(helper.getLevel());
        copy.load(tag);
        check(copy.hasFlag(AbstractMonsterGirl.FLAG_MALE), "a bull stays a bull");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "minotaurHerdBreedsWithWheat")
    public static void minotaurHerdBreedsWithWheat(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Minotaur cow = spawn(helper, GirlSpecies.BISON_MINOTAUR, new BlockPos(6, 2, 7));
        Minotaur bull = spawn(helper, GirlSpecies.BISON_MINOTAUR, new BlockPos(9, 2, 7));
        cow.setMale(false);
        bull.setMale(true);
        interact(cow, player, new ItemStack(Items.WHEAT));
        interact(bull, player, new ItemStack(Items.WHEAT));
        check(cow.isInLove() && bull.isInLove(), "wheat puts them in the mood");
        helper.succeedWhen(() -> {
            check(cow.getGestation().isCarrying(), "the cow should be expecting a calf");
            check(bull.getUUID().equals(cow.getGestation().getFather()), "by the bull");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "aurochsCannotBePushed")
    public static void aurochsCannotBePushed(GameTestHelper helper) {
        Minotaur aurochs = spawn(helper, GirlSpecies.AUROCHS_MINOTAUR, new BlockPos(7, 2, 7));
        check(aurochs.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) >= 1.0, "nothing knocks an aurochs back");
        helper.succeed();
    }
}
