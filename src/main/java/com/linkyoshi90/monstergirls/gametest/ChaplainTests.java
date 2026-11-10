package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Marriage;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.order.Chaplain;
import com.linkyoshi90.monstergirls.entity.order.Order;
import com.linkyoshi90.monstergirls.entity.order.OrderSister;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The Order's chaplain (entity/order/Chaplain): a spared sister comes back unarmed to the hearth and blesses the wedding there. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ChaplainTests {
    @GameTest(template = PLATFORM, timeoutTicks = 80, batch = "aSparedSisterBlessesTheWedding")
    public static void aSparedSisterBlessesTheWedding(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(7, 2, 7);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        ((HearthBlockEntity) helper.getBlockEntity(hp)).setOwner(p.getUUID());
        OrderSister knight = helper.spawn(Order.type(OrderSister.Rank.KNIGHT), new BlockPos(3, 2, 3));
        knight.setNoAi(true);
        knight.kneel(p);
        knight.spare(level, p);
        check(!Chaplain.due(level, p), "she doesn't come back at once");
        Chaplain.dueNow(level, p);
        check(Chaplain.due(level, p), "a couple of days later she is due");
        Order.setNotoriety(p, Order.MARKED);
        check(!Chaplain.due(level, p), "not while the Order has them marked");
        Order.setNotoriety(p, 30);
        helper.runAfterDelay(2, () -> {                                     // the hearth counts as loaded from the next tick
            BlockPos hearth = helper.absolutePos(hp);
            OrderSister chaplain = Chaplain.visit(level, p, hearth);
            check(chaplain != null && chaplain.isChaplain() && chaplain.getMainHandItem().is(Items.BOOK) && chaplain.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty(),
                    "she comes back unarmed, with a book");
            check(!Chaplain.due(level, p) && chaplain.hasCustomName() && chaplain.chaplainName().equals(Chaplain.nameOf(knight)), "the same sister, by name: " + chaplain.getCustomName());
            check(!Order.hunts(chaplain, p), "she hunts nobody");
            Zombie zombie = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(1, 2, 1));
            zombie.setTarget(chaplain);
            check(zombie.getTarget() == null, "and nothing hunts her");
            AbstractMonsterGirl bride = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(6, 2, 6));
            bride.setNoAi(true);
            bride.setAffection(p.getUUID(), 100);
            Marriage.wed(level, bride, p, hearth);
            CompoundTag entry = Marriage.family(p).getCompound(0);
            check(entry.getString("Blessed").equals(chaplain.chaplainName()), "the family page remembers who blessed them: " + entry);
            check(p.hasEffect(MobEffects.LUCK) && Order.notoriety(p) == 30 - Chaplain.MERCY, "luck, and the Order thinks better of them");
            CompoundTag saved = new CompoundTag();
            chaplain.saveWithoutId(saved);
            check(saved.getString("Chaplain").equals(chaplain.chaplainName()) && saved.contains("ChaplainHearth"), "saved as a chaplain");
            chaplain.leaveAt(level.getGameTime());
            helper.runAfterDelay(3, () -> {
                check(chaplain.leaving(), "her stay over, she goes home");
                HarpyTestUtil.remove(p);
                helper.succeed();
            });
        });
    }
}
