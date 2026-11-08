package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.MailboxBlock;
import com.linkyoshi90.monstergirls.block.MailboxBlockEntity;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Courier girls (block/MailboxBlockEntity). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class MailboxTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aCentaurCarriesTheMail")
    public static void aCentaurCarriesTheMail(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 1, 12));
        helper.setBlock(new BlockPos(2, 1, 2), ModBlocks.MAILBOX.get());
        helper.setBlock(new BlockPos(12, 1, 2), ModBlocks.MAILBOX.get());
        BlockPos a = helper.absolutePos(new BlockPos(2, 1, 2)), b = helper.absolutePos(new BlockPos(12, 1, 2));
        MailboxBlockEntity from = (MailboxBlockEntity) level.getBlockEntity(a), to = (MailboxBlockEntity) level.getBlockEntity(b);
        from.setOwner(p.getUUID());
        MailboxBlock.link(level, a, p);
        MailboxBlock.link(level, b, p);
        check(b.equals(from.target()) && a.equals(to.target()), "linked both ways");
        from.store().setItem(0, new ItemStack(Items.APPLE, 5));
        check(!from.send(level), "nobody to carry it yet");
        AbstractMonsterGirl centaur = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(4, 1, 4));
        centaur.addAffection(p, HomeLife.FRIEND);
        check(from.send(level) && from.store().isEmpty() && from.parcels().size() == 1, "the centaur sets off with it");
        MailboxBlockEntity.Parcel parcel = from.parcels().get(0);
        from.parcels().set(0, new MailboxBlockEntity.Parcel(parcel.items(), 0L, parcel.to(), parcel.courier()));   // she's quick today
        check(from.deliver(level) == 1 && to.store().countItem(Items.APPLE) == 5, "delivered to the other mailbox");
        MailboxBlock.link(level, a, p);
        MailboxBlock.link(level, a, p);
        check(from.target() == null, "a feather twice: unlinked");
        from.store().setItem(0, new ItemStack(Items.BREAD, 2));
        from.send(level);
        MailboxBlockEntity.Parcel q = from.parcels().get(0);
        from.parcels().set(0, new MailboxBlockEntity.Parcel(q.items(), 0L, null, q.courier()));
        from.deliver(level);
        check(p.getInventory().countItem(Items.BREAD) == 2, "unlinked, the mail comes to the owner");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
