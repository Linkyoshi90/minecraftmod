package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.HoneymoonTrip;
import com.linkyoshi90.monstergirls.entity.Marriage;
import com.linkyoshi90.monstergirls.entity.Scouting;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The honeymoon trip (entity/HoneymoonTrip): at the wedding she names a place; go there together for a souvenir and a pack of finds. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HoneymoonTripTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "honeymoonTrip")
    public static void honeymoonTrip(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl wife = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(4, 2, 4));
        wife.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        wife.setAffection(p.getUUID(), 100);
        Marriage.wed(level, wife, p, helper.absolutePos(new BlockPos(3, 2, 3)));
        ResourceKey<Biome> wish = HoneymoonTrip.wish(wife);
        CompoundTag entry = Marriage.family(p).getCompound(0);
        check(wish != null && HoneymoonTrip.PLACES.containsKey(wish) && entry.getString("Wish").equals(wish.location().toString()), "at the wedding she names a place: " + entry);
        ResourceKey<Biome> here = level.getBiome(p.blockPosition()).unwrapKey().orElseThrow();
        if (!here.equals(wish)) check(HoneymoonTrip.arrived(level, p) == null, "not there yet");
        HoneymoonTrip.wishFor(wife, here);                                        // as if they'd travelled there
        int before = wife.getAffection(p);
        check(HoneymoonTrip.arrived(level, p) == wife && HoneymoonTrip.wish(wife) == null, "there together: the trip is made");
        boolean souvenir = false, pack = false;
        for (ItemStack s : p.getInventory().items) {
            if (s.has(DataComponents.LORE) && s.has(DataComponents.CUSTOM_NAME)) souvenir = true;
            if (s.is(ModItems.SCOUT_PACK.get()) && Scouting.hasFinds(s)) pack = true;
        }
        check(souvenir && pack, "a souvenir and a pack of finds");
        check(wife.getAffection(p) >= before && Marriage.honeymoon(wife), "closer, and the honeymoon longer");
        entry = Marriage.family(p).getCompound(0);
        check(!entry.contains("Wish") && entry.getList("Trips", Tag.TAG_STRING).getString(0).equals(here.location().toString()), "the family page remembers: " + entry);
        check(HoneymoonTrip.arrived(level, p) == null, "once");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
