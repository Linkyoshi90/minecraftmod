package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Marriage;
import com.linkyoshi90.monstergirls.entity.SwanHarpy;
import com.linkyoshi90.monstergirls.entity.TreeFrog;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** Harpies and frogs dress for their wedding too (WeddingVeil): the feather crown, the lily-pad veil; kept on the figurine. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class WeddingLookTests {
    @GameTest(template = PLATFORM, timeoutTicks = 120, batch = "harpyAndFrogBrides")
    public static void harpyAndFrogBrides(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SwanHarpy swan = helper.spawn(ModEntities.SWAN_HARPY.get(), new BlockPos(3, 2, 3));
        TreeFrog frog = helper.spawn(ModEntities.TREE_FROG.get(), new BlockPos(9, 2, 9));
        swan.setNoAi(true);
        frog.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(6, 2, 6));
        swan.setAffection(p.getUUID(), 100);
        frog.setAffection(p.getUUID(), 100);
        check(!swan.isVeiled() && !frog.isVeiled(), "not before the wedding");
        Marriage.wed(level, swan, p, helper.absolutePos(new BlockPos(6, 2, 5)));
        Marriage.wed(level, frog, p, helper.absolutePos(new BlockPos(6, 2, 5)));
        check(swan.isVeiled() && frog.isVeiled(), "the feather crown and the lily-pad veil on the day");
        boolean veiledFigurine = false;
        for (ItemStack s : p.getInventory().items) {
            var data = s.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data != null && data.copyTag().getCompound("Mob").getBoolean("Veiled")) veiledFigurine = true;
        }
        check(veiledFigurine, "the wedding figurine wears it");
        CompoundTag saved = new CompoundTag();
        frog.saveWithoutId(saved);
        check(saved.getBoolean("Veiled"), "saved with her");
        swan.getPersistentData().putLong("VeilUntil", 0L);                          // the day over
        frog.getPersistentData().putLong("VeilUntil", 0L);
        helper.runAfterDelay(Marriage.EVERY + 4, () -> {
            check(!swan.isVeiled() && !frog.isVeiled(), "put away after the day");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }
}
