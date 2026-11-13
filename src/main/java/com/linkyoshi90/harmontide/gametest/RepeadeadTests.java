package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.CursedGear;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Repeadead;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The repeadead (entity/girl/Repeadead) and cursed gear (entity/CursedGear). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class RepeadeadTests {
    @GameTest(template = PLATFORM, timeoutTicks = 120, batch = "repeadeadScreamsAndClings")
    public static void repeadeadScreamsAndClings(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl her = spawn(helper, GirlSpecies.REPEADEAD_UNDEAD, new BlockPos(7, 2, 3));
        her.setNoAi(true);
        her.setYRot(0);
        her.setYHeadRot(0);                                                  // facing +Z
        ServerPlayer front = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        ServerPlayer behind = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 0));
        check(Repeadead.inFront(her, front) && !Repeadead.inFront(her, behind), "she sees only who walks in front of her");
        check(Repeadead.scream(level, her, false) == 2 && Repeadead.paralysed(front) && front.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0.0, "the scream freezes them in place");
        check(!Repeadead.canScream(her), "not again straight away");
        Repeadead.release(front);
        Repeadead.release(behind);
        check(!Repeadead.paralysed(front), "it wears off");
        helper.runAfterDelay(70, () -> {                                    // a new player can't be hurt for their first 60 ticks
            front.teleportTo(her.getX(), her.getY(), her.getZ() + 1.0);
            her.setHealth(10);
            front.invulnerableTime = 0;                                     // she may have been at them already
            float hp = front.getHealth();
            check(Repeadead.drain(level, her, front, false) && front.getHealth() < hp && her.getHealth() > 10, "she drinks their life");
            check(Repeadead.paralysed(front), "and holds them fast");
            Repeadead.release(front);
            HarpyTestUtil.remove(front);
            HarpyTestUtil.remove(behind);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "befriendedRepeadeadHuntsMonsters")
    public static void befriendedRepeadeadHuntsMonsters(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl her = spawn(helper, GirlSpecies.REPEADEAD_UNDEAD, new BlockPos(7, 2, 7));
        her.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        her.addAffection(p, 20);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 2, 7));
        zombie.setNoAi(true);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(10, 2, 4));
        cow.setNoAi(true);
        check(Repeadead.scream(level, her, true) == 1 && Repeadead.paralysed(zombie) && !Repeadead.paralysed(cow) && !Repeadead.paralysed(p), "befriended, she screams at monsters only");
        check(!Repeadead.waiting(level, her), "a friend doesn't lurk");
        Repeadead.release(zombie);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "cursedGearStepsOut")
    public static void cursedGearStepsOut(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        ItemStack blade = new ItemStack(ModItems.CURSED_BLADE.get());
        check(CursedGear.fonder(level, p, blade, 40) == null && CursedGear.fondness(blade) == 40, "fonder of its bearer");
        AbstractMonsterGirl girl = CursedGear.fonder(level, p, blade, 60);
        check(girl != null && girl.getSpecies() == GirlSpecies.CURSED_BLADE_UNDEAD && blade.isEmpty(), "at full fondness she steps out");
        check(girl.getAffection(p) >= 100, "devoted from the first");
        ItemStack armour = new ItemStack(ModItems.LIVING_ARMOR.get());
        AbstractMonsterGirl knight = CursedGear.fonder(level, p, armour, CursedGear.FOND);
        check(knight != null && knight.getSpecies() == GirlSpecies.LIVING_ARMOR_UNDEAD, "the armour too");
        float hp = knight.getHealth();
        knight.hurt(level.damageSources().generic(), 10.0F);
        check(knight.getHealth() > hp - 10.0F, "she takes blows lightly: " + (hp - knight.getHealth()));
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
