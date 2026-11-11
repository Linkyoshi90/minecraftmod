package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Headgear;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.item.HeadgearItem.Hat;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Headgear (entity/Headgear): who gives which piece, and what each does while worn. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HeadgearTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aDevotedFoxGivesHerMask")
    public static void aDevotedFoxGivesHerMask(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        p.getInventory().clearContent();
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(5, 2, 5));
        fox.setNoAi(true);
        check(Headgear.hatOf(GirlSpecies.RED_KITSUNE) == Hat.KITSUNE_MASK && Headgear.hatOf(GirlSpecies.WOLF_CANID) == null, "foxes give masks, wolves nothing");
        fox.setAffection(p.getUUID(), Headgear.DEVOTED - 1);
        check(!Headgear.give(level, fox, p), "not before she's devoted");
        fox.setAffection(p.getUUID(), Headgear.DEVOTED);
        check(Headgear.give(level, fox, p) && p.getInventory().contains(Headgear.stack(Hat.KITSUNE_MASK)), "devoted: her mask");
        AbstractMonsterGirl other = spawn(helper, GirlSpecies.ARCTIC_KITSUNE, new BlockPos(7, 2, 5));
        other.setNoAi(true);
        other.setAffection(p.getUUID(), 100);
        check(!Headgear.give(level, other, p) && !Headgear.give(level, fox, p), "one mask per player, whoever gives it");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "wearingThem")
    public static void wearingThem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(Headgear.wearing(p) == null, "bare-headed");
        p.setItemSlot(EquipmentSlot.HEAD, Headgear.stack(Hat.KITSUNE_MASK));
        Headgear.tick(level, p);
        check(p.hasEffect(MobEffects.LUCK), "the fox mask: luck");
        p.setItemSlot(EquipmentSlot.HEAD, Headgear.stack(Hat.BUNNY_EARS));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        z.setNoAi(true);
        z.setTarget(p);
        check(Headgear.tick(level, p).contains(z) && z.hasEffect(MobEffects.GLOWING), "the bunny ears hear it coming");
        p.setItemSlot(EquipmentSlot.HEAD, Headgear.stack(Hat.WITCH_HAT));
        check(Headgear.wearing(p) == Hat.WITCH_HAT && Headgear.bonusXp(10) == 5, "the witch hat: half again");
        p.setItemSlot(EquipmentSlot.HEAD, Headgear.stack(Hat.FLOWER_CROWN));
        Bee bee = helper.spawn(EntityType.BEE, new BlockPos(6, 3, 3));
        bee.setNoAi(true);
        bee.setTarget(p);
        check(bee.getTarget() == null, "the flower crown: bees spare you");
        check(ModItems.FLOWER_CROWN.get().hat() == Hat.FLOWER_CROWN, "the crown knows what it is");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
