package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * More of the work girls lend at the blocks around a home (with entity/girl/Jobs).
 *
 * <ul>
 *   <li><b>Elves at the enchanting table.</b> An elf who is fond of anyone, within {@link #NEAR} of the table, knows what the
 *   missing bookshelves would have told it: the table offers as if fifteen stood around it.</li>
 *   <li><b>Smiths at the anvil.</b> A dwarf, cyclops, iron kobold or anvil tortoise friend (10+) at your side gives back half
 *   the levels an anvil job cost.</li>
 *   <li><b>The dinner bell.</b> Ring a bell and your friends within {@link #BELL_REACH} who are free to come (not sitting or on
 *   patrol) hurry over.</li>
 *   <li><b>Wings to catch you.</b> A winged friend (valkyrie, angel, gandharva, ophanim, sylph; a moth, mothman, papillon or
 *   dragonfly) within {@link #WINGS} of a falling friend catches them before they land.</li>
 *   <li><b>A path of ice.</b> A yuki-onna friend (10+) near you freezes the water under your feet as you walk over it.</li>
 *   <li><b>A crust on the lava.</b> A fire kind friend (10+) near you (salamander, blaze cat, magma slime, ifrit, fire giant,
 *   hellhound) cools the lava under your feet into a crust that melts back a few seconds later.</li>
 * </ul>
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Workshop {
    public static final double NEAR = 8, BELL_REACH = 48, WINGS = 12, ICE_REACH = 16;
    public static final int BELL_REST = 200;
    /** The smiths: dwarves and the cyclops, and the kobold and anvil tortoise who mend gear themselves. */
    public static final Set<GirlSpecies> SMITHS = Set.of(GirlSpecies.DWARF_ELF, GirlSpecies.CYCLOPS_DEVIL, GirlSpecies.IRON_KOBOLD_LIZARDFOLK, GirlSpecies.ANVIL_TORTOISE_LIZARDFOLK);
    public static final Set<GirlSpecies> FIRE_WALKERS = Set.of(GirlSpecies.SALAMANDER_LIZARDFOLK, GirlSpecies.BLAZE_CAT_NEKOMATA, GirlSpecies.MAGMA_SLIME,
            GirlSpecies.IFRIT_DEVIL, GirlSpecies.FIRE_GIANT_DEVIL, GirlSpecies.HELLHOUND_CANID);
    public static final Set<GirlSpecies> WINGED = Set.of(GirlSpecies.VALKYRIE_ELF, GirlSpecies.ANGEL_ELF, GirlSpecies.GANDHARVA_ELF,
            GirlSpecies.OPHANIM_ELF, GirlSpecies.SYLPH_ELF, GirlSpecies.PHANTOM_UNDEAD, GirlSpecies.MOTHMAN_BUG, GirlSpecies.MOTH_BUG, GirlSpecies.PAPILLON_BUG, GirlSpecies.DRAGONFLY_BUG);

    private Workshop() {}

    @Nullable
    static AbstractMonsterGirl near(Level level, AABB box, java.util.function.Predicate<AbstractMonsterGirl> who) {
        if (!com.linkyoshi90.harmontide.HarmontideConfig.workshop()) return null;
        List<AbstractMonsterGirl> found = level.getEntitiesOfClass(AbstractMonsterGirl.class, box, g -> g.isAlive() && !g.isBaby() && who.test(g));
        return found.isEmpty() ? null : found.get(0);
    }

    // --- elves at the enchanting table -------------------------------------------------------------------------------

    /** A befriended elf near this table, or null. */
    @Nullable
    public static AbstractMonsterGirl enchantingElf(Level level, BlockPos table) {
        return near(level, new AABB(table).inflate(NEAR), g -> g.getSpecies().family() == GirlSpecies.Family.ELF && g.hasAnyAffection());
    }

    @SubscribeEvent
    public static void onEnchantLevels(EnchantmentLevelSetEvent event) {
        if (event.getPower() >= 15 || event.getOriginalLevel() <= 0 || !(event.getLevel() instanceof ServerLevel level)) return;
        if (enchantingElf(level, event.getPos()) == null) return;
        RandomSource seeded = RandomSource.create(event.getPos().asLong() * 31 + event.getEnchantRow() + level.getGameTime() / 1200);
        int level15 = EnchantmentHelper.getEnchantmentCost(seeded, event.getEnchantRow(), 15, event.getItem());
        event.setEnchantLevel(Math.max(event.getEnchantLevel(), level15));
    }

    // --- dwarves at the anvil ----------------------------------------------------------------------------------------

    /** A dwarf friend of this player near them, or null. */
    @Nullable
    public static AbstractMonsterGirl anvilDwarf(Player p) {
        return near(p.level(), p.getBoundingBox().inflate(NEAR), g -> SMITHS.contains(g.getSpecies()) && g.getAffection(p) >= Orders.AFFECTION);
    }

    /** Levels a dwarf hands back for an anvil job of this cost. */
    public static int dwarfRefund(int cost) {
        return cost / 2;
    }

    @SubscribeEvent
    public static void onAnvilTaken(AnvilRepairEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.getAbilities().instabuild || !(p.containerMenu instanceof AnvilMenu menu)) return;
        int back = dwarfRefund(menu.getCost());
        AbstractMonsterGirl dwarf = back > 0 ? anvilDwarf(p) : null;
        if (dwarf == null) return;
        p.giveExperienceLevels(back);
        p.displayClientMessage(Component.translatable("message.harmontide.dwarf_anvil", dwarf.getDisplayName(), back), true);
    }

    // --- the dinner bell ---------------------------------------------------------------------------------------------

    /** Everyone who answers this player's bell: grown friends free to come. How many set off. */
    public static int ringBell(ServerLevel level, Player p, BlockPos bell) {
        int came = 0;
        if (!com.linkyoshi90.harmontide.HarmontideConfig.workshop()) return 0;
        for (Mob m : HomeLife.friendsOf(level, p, new AABB(bell).inflate(BELL_REACH))) {
            if (Orders.holdsPlace(m) || m.isPassenger() || m.isVehicle() || m.getTarget() != null) continue;
            m.getPersistentData().putLong(BELL_AT, bell.asLong());                  // she keeps coming for half a minute (answerBell)
            m.getPersistentData().putLong(BELL_UNTIL, level.getGameTime() + 600);
            m.getNavigation().moveTo(bell.getX() + 0.5, bell.getY(), bell.getZ() + 0.5, 1.25);
            came++;
        }
        return came;
    }

    @SubscribeEvent
    public static void onBell(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(level.getBlockState(event.getPos()).getBlock() instanceof BellBlock)) return;
        Player p = event.getEntity();
        long now = level.getGameTime();
        if (now < p.getPersistentData().getLong("BellRestUntil")) return;
        p.getPersistentData().putLong("BellRestUntil", now + BELL_REST);
        int came = ringBell(level, p, event.getPos());
        if (came > 0) p.displayClientMessage(Component.translatable("message.harmontide.dinner_bell", came), true);
    }

    public static final String BELL_AT = "BellAt", BELL_UNTIL = "BellUntil";

    /** Every tick for one who heard a bell: on her way until she is there (or half a minute has passed). Whether she still is. */
    public static boolean answerBell(Mob m) {
        var d = m.getPersistentData();
        if (!d.contains(BELL_UNTIL)) return false;
        BlockPos bell = BlockPos.of(d.getLong(BELL_AT));
        if (m.level().getGameTime() > d.getLong(BELL_UNTIL) || m.blockPosition().closerThan(bell, 3.0) || m.getTarget() != null) {
            d.remove(BELL_UNTIL);
            d.remove(BELL_AT);
            return false;
        }
        if (m.getNavigation().isDone() || m.tickCount % 20 == 0) m.getNavigation().moveTo(bell.getX() + 0.5, bell.getY(), bell.getZ() + 0.5, 1.25);
        return true;
    }

    // --- wings to catch you ------------------------------------------------------------------------------------------

    @Nullable
    public static AbstractMonsterGirl wingsFor(Player p) {
        return near(p.level(), p.getBoundingBox().inflate(WINGS), g -> WINGED.contains(g.getSpecies()) && g.getAffection(p) >= Orders.AFFECTION);
    }

    /** After the slime's bounce and the spiderling's line (entity/Traversal), before the soft-landing cushion. */
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH)
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || event.getDistance() <= 4.0F) return;
        AbstractMonsterGirl wings = wingsFor(p);
        if (wings == null) return;
        event.setDistance(0.0F);
        event.setCanceled(true);
        wings.teleportTo(p.getX() + 0.6, p.getY() + 0.5, p.getZ());
        p.displayClientMessage(Component.translatable("message.harmontide.winged_catch", wings.getDisplayName()), true);
    }

    // --- a path of ice -----------------------------------------------------------------------------------------------

    /** Freezes the water just under a walking player into frosted ice (like Frost Walker). How many blocks froze. */
    public static int freezePath(ServerLevel level, Player p) {
        if (!p.onGround() || p.isSpectator() || !com.linkyoshi90.harmontide.HarmontideConfig.workshop()) return 0;
        BlockState ice = Blocks.FROSTED_ICE.defaultBlockState();
        BlockPos under = BlockPos.containing(p.getX(), p.getY() - 0.5, p.getZ());
        int n = 0;
        for (BlockPos q : BlockPos.betweenClosed(under.offset(-2, 0, -2), under.offset(2, 0, 2))) {
            if (q.distSqr(under) > 6 || !level.getBlockState(q.above()).isAir()) continue;
            BlockState s = level.getBlockState(q);
            if (s.is(Blocks.WATER) && s.getFluidState().isSource() && ice.canSurvive(level, q) && level.isUnobstructed(ice, q, CollisionContext.empty())) {
                level.setBlockAndUpdate(q, ice);
                level.scheduleTick(q, Blocks.FROSTED_ICE, Math.round(60 + level.random.nextFloat() * 60));
                n++;
            }
        }
        return n;
    }

    /** Cools the lava just under a walking player into a crust that melts back (block/LavaCrustBlock). How many blocks cooled. */
    public static int crustPath(ServerLevel level, Player p) {
        if (!p.onGround() || p.isSpectator() || !com.linkyoshi90.harmontide.HarmontideConfig.workshop()) return 0;
        BlockState crust = com.linkyoshi90.harmontide.registry.ModBlocks.LAVA_CRUST.get().defaultBlockState();
        BlockPos under = BlockPos.containing(p.getX(), p.getY() - 0.5, p.getZ());
        int n = 0;
        for (BlockPos q : BlockPos.betweenClosed(under.offset(-2, 0, -2), under.offset(2, 0, 2))) {
            if (q.distSqr(under) > 6 || !level.getBlockState(q.above()).isAir()) continue;
            BlockState s = level.getBlockState(q);
            if (s.is(Blocks.LAVA) && s.getFluidState().isSource() && level.isUnobstructed(crust, q, CollisionContext.empty())) {
                level.setBlockAndUpdate(q, crust);
                n++;
            }
        }
        return n;
    }

    /** The path-makers' tick (server, from entity/Instincts): ice or a crust under her friend's feet. */
    public static void pathMaker(ServerLevel level, AbstractMonsterGirl g) {
        if (g.isBaby() || g.tickCount % 2 != 0) return;
        boolean ice = g.getSpecies() == GirlSpecies.YUKI_ONNA_UNDEAD;
        if (!ice && !FIRE_WALKERS.contains(g.getSpecies())) return;
        for (Player p : level.getEntitiesOfClass(Player.class, g.getBoundingBox().inflate(ICE_REACH), p -> g.getAffection(p) >= Orders.AFFECTION)) {
            if (ice) freezePath(level, p);
            else crustPath(level, p);
        }
    }

    // --- reminders on the items these use (the client fires this event only) -------------------------------------------

    @SubscribeEvent
    public static void onTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        net.minecraft.world.item.ItemStack s = event.getItemStack();
        String tip = s.is(net.minecraft.world.item.Items.STRING) ? "string" : s.is(net.minecraft.world.item.Items.COMPASS) ? "compass"
                : s.is(net.minecraft.tags.ItemTags.SAPLINGS) ? "sapling" : s.is(com.linkyoshi90.harmontide.registry.ModItems.CRAB_ARMOUR_PIECE.get()) ? "crab_armour_piece"
                : s.is(net.minecraft.world.item.Items.BELL) ? "bell" : s.is(net.minecraft.world.item.Items.LANTERN) || s.is(net.minecraft.world.item.Items.SOUL_LANTERN) ? "lantern"
                : s.is(net.minecraft.world.item.Items.TORCH) ? "torch" : null;
        if (tip != null) event.getToolTip().add(Component.translatable("tip.harmontide." + tip).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
