package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.FridgeBlockEntity;
import com.linkyoshi90.monstergirls.entity.Expeditions;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.Teamwork;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Roles;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Callings (entity.girl.Roles) and teamwork (entity.Teamwork). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class RoleTests {
    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "aDaughterPicksHerCalling")
    public static void aDaughterPicksHerCalling(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 2, 2), Blocks.SMOKER);
        BlockPos fp = new BlockPos(4, 2, 2);
        helper.setBlock(fp, ModBlocks.FRIDGE.get());
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 5));
        girl.setBaby(true);
        girl.setGrowTicks(AbstractMonsterGirl.GROW_TICKS - 2);
        var level = helper.getLevel();
        int[] w = Roles.weights(level, girl);
        check(w[Roles.Role.COOK.ordinal()] > 0 && w[Roles.Role.FARMHAND.ordinal()] == 0 && w[Roles.Role.GUARD.ordinal()] == 0, "she grew up by a kitchen");
        helper.succeedWhen(() -> {
            check(!girl.isBaby() && Roles.of(girl) != null, "grown, she has picked a calling");
            Roles.set(girl, Roles.Role.COOK);
            var store = ((FridgeBlockEntity) helper.getBlockEntity(fp)).store();
            store.setItem(0, new ItemStack(Items.BEEF, 2));
            ItemStack cooked = Roles.cook(level, girl);
            int steak = 0;
            for (int i = 0; i < store.getContainerSize(); i++) if (store.getItem(i).is(Items.COOKED_BEEF)) steak += store.getItem(i).getCount();
            check(cooked.is(Items.COOKED_BEEF) && steak == 1 && store.getItem(0).getCount() == 1, "a cook cooks what is raw in the fridge");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "guardsAndScouts")
    public static void guardsAndScouts(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 5));
        float base = girl.getMaxHealth();
        Roles.set(girl, Roles.Role.GUARD);
        check(girl.getMaxHealth() == base + 8 && Roles.expedition(girl, Expeditions.DURATION) == Expeditions.DURATION, "a guard is tougher");
        Roles.set(girl, Roles.Role.SCOUT);
        check(girl.getMaxHealth() == base && Roles.expedition(girl, Expeditions.DURATION) == Expeditions.DURATION / 2, "a scout is quicker");
        check(Roles.cook(helper.getLevel(), girl).isEmpty(), "and doesn't cook");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "friendsFightAsOne")
    public static void friendsFightAsOne(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl a = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl b = spawn(helper, GirlSpecies.JACK_JACKALOPE, new BlockPos(7, 2, 3));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(7, 2, 7));
        a.addAffection(p, Orders.AFFECTION);
        b.addAffection(p, Orders.AFFECTION);
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(5, 2, 5));
        var level = helper.getLevel();
        check(Teamwork.hit(level, a, pig, 4) == 4, "the first blow is plain");
        check(Teamwork.hit(level, a, pig, 4) == 4, "she can't set herself up");
        check(Teamwork.hit(level, b, pig, 4) == 4 * Teamwork.BONUS, "her teammate's blow lands harder");
        check(Teamwork.hit(level, stranger, pig, 4) == 4, "a stranger is no teammate");
        var adv = p.server.getAdvancements().get(MonsterGirlsMod.id(Teamwork.ADVANCEMENT));
        check(adv != null && p.getAdvancements().getOrStartProgress(adv).isDone(), "their friend sees it");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theBestiaryFillsIn")
    public static void theBestiaryFillsIn(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 5));
        String id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(girl.getType()).getPath();
        com.linkyoshi90.monstergirls.entity.Journal.record(p, girl, false);
        java.util.function.Supplier<String> page = () -> {
            var pages = com.linkyoshi90.monstergirls.entity.Journal.book(p).pages();
            return pages.get(pages.size() - 1).raw().toString();
        };
        check(page.get().contains("journal.monstergirls.glimpsed") && !page.get().contains(id + ".home"), "a glimpse: just her name");
        for (int i = 0; i < com.linkyoshi90.monstergirls.entity.Journal.KNOWN; i++) com.linkyoshi90.monstergirls.entity.Journal.watch(p, girl);
        check(page.get().contains(id + ".home") && page.get().contains("journal.monstergirls.watch_more") && !page.get().contains(id + ".signature"), "her home, then");
        while (com.linkyoshi90.monstergirls.entity.Journal.watch(p, girl) < com.linkyoshi90.monstergirls.entity.Journal.STUDIED) { }
        check(page.get().contains(id + ".signature") && com.linkyoshi90.monstergirls.entity.Journal.studied(p) == 1, "watched long enough, her whole page");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "herFavouriteTreat")
    public static void herFavouriteTreat(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        girl.addAffection(p, Orders.AFFECTION);
        var level = helper.getLevel();
        girl.getPersistentData().putLong(com.linkyoshi90.monstergirls.entity.girl.Treats.BIRTHDAY_DONE, level.getDayTime() / 24000L + 1);   // not her birthday, for the sums
        var fav = com.linkyoshi90.monstergirls.entity.girl.Treats.favourite(girl);
        check(fav == com.linkyoshi90.monstergirls.entity.girl.Treats.favourite(girl), "always the same favourite");
        check(!com.linkyoshi90.monstergirls.entity.girl.Treats.knows(p, girl), "not found out yet");
        int before = girl.getAffection(p);
        JackalopeTests.interact(girl, p, new ItemStack(fav));
        check(girl.getAffection(p) >= before + com.linkyoshi90.monstergirls.entity.girl.Treats.FAVOURITE_TIMES, "she takes her favourite, and it means a lot");
        check(com.linkyoshi90.monstergirls.entity.girl.Treats.knows(p, girl), "and the journal remembers");
        Item other = fav == Items.BREAD ? Items.COOKIE : Items.BREAD;
        check(com.linkyoshi90.monstergirls.entity.girl.Treats.value(level, girl, p, new ItemStack(other), 1) == 1, "anything else is ordinary");
        int b = com.linkyoshi90.monstergirls.entity.girl.Treats.birthday(girl);
        check(b >= 0 && b < com.linkyoshi90.monstergirls.entity.girl.Festivals.CYCLE
                && com.linkyoshi90.monstergirls.entity.girl.Treats.isBirthday(girl, (b + 24) * 24000L + 5)
                && !com.linkyoshi90.monstergirls.entity.girl.Treats.isBirthday(girl, (b + 1) * 24000L), "a birthday once in every 24 days");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "harpyFriendsComeToFestivals")
    public static void harpyFriendsComeToFestivals(GameTestHelper helper) {
        BlockPos hp = new BlockPos(1, 2, 1);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        var hearth = (com.linkyoshi90.monstergirls.block.HearthBlockEntity) helper.getBlockEntity(hp);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        hearth.setOwner(p.getUUID());
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(4, 2, 2));
        girl.addAffection(p, Orders.AFFECTION);
        check(com.linkyoshi90.monstergirls.entity.girl.Settlement.settle(helper.getLevel(), girl, hearth), "she lives there");
        var crow = helper.spawn(com.linkyoshi90.monstergirls.registry.ModEntities.CROW_HARPY.get(), new BlockPos(2, 2, 5));
        crow.addAffection(p, Orders.AFFECTION);
        var wild = helper.spawn(com.linkyoshi90.monstergirls.registry.ModEntities.CROW_HARPY.get(), new BlockPos(5, 2, 5));
        check(com.linkyoshi90.monstergirls.entity.girl.Festivals.celebrate(helper.getLevel(), p, helper.absolutePos(hp),
                com.linkyoshi90.monstergirls.entity.girl.Festivals.Festival.GIFTS) == 2 && wild.getAffection(p) == 0, "her harpy friend comes along; a stranger doesn't");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
