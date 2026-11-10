package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mining trips. A friend who knows how to dig (MINERS) takes a pickaxe from her friend's hand and holds it. Shown a sample of
 * something that comes out of the ground (an ore, raw metal, an ingot, a gem, coal, stone...), she digs in where she was told
 * to stay (her patrol spot) or where she stands, and is gone, underground, like a girl on an expedition, until she has a stack
 * of it. Then she climbs out at the same spot with the stack and the sample: into a chest or barrel beside the spot if there
 * is one, else she keeps it until her friend comes near. Her pickaxe must be able to mine it (an iron one for diamonds). How
 * long it takes and how much of the pickaxe it wears down depend on how rare the thing is: a few hours for stone, five days
 * for diamonds; Efficiency and Fortune make her quicker, Unbreaking spares the pickaxe. A pickaxe too worn to last brings
 * her back early with what she found when it broke. She leaves a real shaft beside the spot (MineShaft), deeper the longer she's down.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Mining {
    public static final int STACK = 64, REACH = 4;
    public static final long DAY = 24000L;
    /** Pickaxe wear per day of digging. */
    public static final int WEAR = 250;
    public static final String GIVEN = "PickaxeGiven", LIST = "Mining", HAUL = "MinersHaul", HAUL_FOR = "MinersHaulFor", ADVANCEMENT = "miner";

    /** The girls who can dig: dwarves, gnomes, knockers, kobolds, moles and badgers, ants, armadillos, constructs, giants... */
    public static final Set<GirlSpecies> MINERS = EnumSet.of(
            GirlSpecies.DWARF_ELF, GirlSpecies.GNOME_ELF, GirlSpecies.KNOCKER_ELF,
            GirlSpecies.KOBOLD_CANID, GirlSpecies.IRON_KOBOLD_LIZARDFOLK, GirlSpecies.CAVE_GEKKO_LIZARDFOLK,
            GirlSpecies.MOLE_RODENT, GirlSpecies.BAMBADGER_RODENT, GirlSpecies.MARMOTAUR_RODENT, GirlSpecies.ROCKHYRAX_RODENT,
            GirlSpecies.MEERKAT_RODENT, GirlSpecies.PINESKRELL_RODENT,
            GirlSpecies.ANT_BUG, GirlSpecies.QUEEN_ANT_BUG, GirlSpecies.ANTLION_BUG, GirlSpecies.SCARABRONZE_BUG,
            GirlSpecies.ARMADILLO_ARMOURED, GirlSpecies.PANGOLIN_ARMOURED, GirlSpecies.ANTEATER_ARMOURED, GirlSpecies.RIVERDILLO_ARMOURED,
            GirlSpecies.GOLEM_CONSTRUCT, GirlSpecies.AUTOMATON_CONSTRUCT, GirlSpecies.GARGOYLE_CONSTRUCT, GirlSpecies.TALOSLING_CONSTRUCT,
            GirlSpecies.TIN_JACK_CONSTRUCT, GirlSpecies.TIN_SQUIRREL_CONSTRUCT,
            GirlSpecies.GOBLIN_DEVIL, GirlSpecies.TROLL_DEVIL, GirlSpecies.OGRE_DEVIL, GirlSpecies.CYCLOPS_DEVIL, GirlSpecies.FIRE_GIANT_DEVIL,
            GirlSpecies.AMETHYST_SLIME, GirlSpecies.COPPER_SLIME,
            GirlSpecies.SANDWORM_LAMIA, GirlSpecies.CENTIPEDE_LAMIA, GirlSpecies.WYRM_DRAGON,
            GirlSpecies.MINOAN_MINOTAUR, GirlSpecies.BRONZEJAW_MINOTAUR, GirlSpecies.BRASSHORN_MINOTAUR,
            GirlSpecies.FENNEC_KITSUNE, GirlSpecies.DUNEFENNEC_KITSUNE);

    /** How long a stack takes (days) and the block it comes out of (for the pickaxe check). */
    public record Vein(double days, Block block) {
    }

    private static final Map<Item, Vein> VEINS = Map.ofEntries(
            Map.entry(Items.COAL, new Vein(0.5, Blocks.COAL_ORE)),
            Map.entry(Items.RAW_COPPER, new Vein(0.5, Blocks.COPPER_ORE)), Map.entry(Items.COPPER_INGOT, new Vein(0.5, Blocks.COPPER_ORE)),
            Map.entry(Items.RAW_IRON, new Vein(1, Blocks.IRON_ORE)), Map.entry(Items.IRON_INGOT, new Vein(1, Blocks.IRON_ORE)),
            Map.entry(Items.IRON_NUGGET, new Vein(0.25, Blocks.IRON_ORE)),
            Map.entry(Items.RAW_GOLD, new Vein(2, Blocks.GOLD_ORE)), Map.entry(Items.GOLD_INGOT, new Vein(2, Blocks.GOLD_ORE)),
            Map.entry(Items.GOLD_NUGGET, new Vein(0.5, Blocks.NETHER_GOLD_ORE)),
            Map.entry(Items.REDSTONE, new Vein(0.75, Blocks.REDSTONE_ORE)), Map.entry(Items.LAPIS_LAZULI, new Vein(1, Blocks.LAPIS_ORE)),
            Map.entry(Items.DIAMOND, new Vein(5, Blocks.DIAMOND_ORE)), Map.entry(Items.EMERALD, new Vein(6, Blocks.EMERALD_ORE)),
            Map.entry(Items.QUARTZ, new Vein(1, Blocks.NETHER_QUARTZ_ORE)), Map.entry(Items.GLOWSTONE_DUST, new Vein(1, Blocks.GLOWSTONE)),
            Map.entry(Items.AMETHYST_SHARD, new Vein(1, Blocks.AMETHYST_CLUSTER)), Map.entry(Items.OBSIDIAN, new Vein(2, Blocks.OBSIDIAN)),
            Map.entry(Items.CRYING_OBSIDIAN, new Vein(3, Blocks.CRYING_OBSIDIAN)),
            Map.entry(Items.ANCIENT_DEBRIS, new Vein(10, Blocks.ANCIENT_DEBRIS)), Map.entry(Items.NETHERITE_SCRAP, new Vein(10, Blocks.ANCIENT_DEBRIS)),
            Map.entry(Items.NETHERITE_INGOT, new Vein(40, Blocks.ANCIENT_DEBRIS)));
    /** Storage blocks: a stack of these is nine (or four) stacks of what they are made of. */
    private static final Map<Item, Map.Entry<Item, Integer>> COMPACT = Map.ofEntries(
            Map.entry(Items.COAL_BLOCK, Map.entry(Items.COAL, 9)), Map.entry(Items.IRON_BLOCK, Map.entry(Items.IRON_INGOT, 9)),
            Map.entry(Items.RAW_IRON_BLOCK, Map.entry(Items.RAW_IRON, 9)), Map.entry(Items.GOLD_BLOCK, Map.entry(Items.GOLD_INGOT, 9)),
            Map.entry(Items.RAW_GOLD_BLOCK, Map.entry(Items.RAW_GOLD, 9)), Map.entry(Items.COPPER_BLOCK, Map.entry(Items.COPPER_INGOT, 9)),
            Map.entry(Items.RAW_COPPER_BLOCK, Map.entry(Items.RAW_COPPER, 9)), Map.entry(Items.REDSTONE_BLOCK, Map.entry(Items.REDSTONE, 9)),
            Map.entry(Items.LAPIS_BLOCK, Map.entry(Items.LAPIS_LAZULI, 9)), Map.entry(Items.DIAMOND_BLOCK, Map.entry(Items.DIAMOND, 9)),
            Map.entry(Items.EMERALD_BLOCK, Map.entry(Items.EMERALD, 9)), Map.entry(Items.NETHERITE_BLOCK, Map.entry(Items.NETHERITE_INGOT, 9)),
            Map.entry(Items.QUARTZ_BLOCK, Map.entry(Items.QUARTZ, 4)), Map.entry(Items.AMETHYST_BLOCK, Map.entry(Items.AMETHYST_SHARD, 4)),
            Map.entry(Items.GLOWSTONE, Map.entry(Items.GLOWSTONE_DUST, 4)));

    private Mining() {
    }

    public static boolean isMiner(Entity e) {
        return e instanceof AbstractMonsterGirl g && MINERS.contains(g.getSpecies());
    }

    /** How long a stack of this takes, and what it is mined from; null if it doesn't come out of the ground. */
    @Nullable
    public static Vein vein(ItemStack stack) {
        Item item = stack.getItem();
        Vein v = VEINS.get(item);
        if (v != null) return v;
        var compact = COMPACT.get(item);
        if (compact != null) {
            Vein of = VEINS.get(compact.getKey());
            return new Vein(of.days() * compact.getValue(), of.block());
        }
        Block block = item instanceof BlockItem bi ? bi.getBlock() : null;
        if (block != null && stack.is(Tags.Items.ORES)) return new Vein(2, block);   // an ore block itself (silk touch), or a modded ore
        if (block != null && (stack.is(Tags.Items.STONES) || stack.is(Tags.Items.COBBLESTONES) || stack.is(Tags.Items.NETHERRACKS)
                || stack.is(Tags.Items.END_STONES) || stack.is(Items.TUFF) || stack.is(Items.CALCITE) || stack.is(Items.DRIPSTONE_BLOCK)
                || stack.is(Items.BASALT) || stack.is(Items.BLACKSTONE) || stack.is(Items.COBBLED_DEEPSLATE) || stack.is(Items.DEEPSLATE))) {
            return new Vein(0.25, block);
        }
        if (stack.is(Tags.Items.RAW_MATERIALS) || stack.is(Tags.Items.GEMS) || stack.is(Tags.Items.INGOTS) || stack.is(Tags.Items.DUSTS)) {
            return new Vein(2, Blocks.IRON_ORE);                                         // something modded: an iron pickaxe's worth
        }
        return null;
    }

    private static int enchant(Level level, ItemStack pick, ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        var reg = level.registryAccess().lookup(Registries.ENCHANTMENT);
        return reg.flatMap(r -> r.get(key)).map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, pick)).orElse(0);
    }

    /** The plan for a trip: how long (ticks), how many she brings, and the wear on the pickaxe (more than it has left: it breaks). */
    public record Trip(long ticks, int found, int wear, boolean breaks) {
    }

    public static Trip plan(Level level, ItemStack pick, Vein vein) {
        double speed = 1 + 0.2 * enchant(level, pick, Enchantments.EFFICIENCY) + 0.3 * enchant(level, pick, Enchantments.FORTUNE);
        int wear = (int) Math.max(16, Math.round(vein.days() * WEAR / (1 + enchant(level, pick, Enchantments.UNBREAKING))));
        int left = pick.isDamageableItem() ? pick.getMaxDamage() - pick.getDamageValue() : Integer.MAX_VALUE;
        double share = Math.min(1.0, (double) left / wear);
        long ticks = Math.max(1200, Math.round(vein.days() * DAY / speed * share));
        return new Trip(ticks, Math.max(1, (int) Math.floor(STACK * share)), Math.min(wear, left), share < 1.0);
    }

    /** Her friend hands her a pickaxe: she holds it. Whether she took it. */
    public static boolean takePickaxe(AbstractMonsterGirl girl, Player player, ItemStack pick) {
        if (!isMiner(girl) || girl.isBaby() || !(pick.getItem() instanceof PickaxeItem) || girl.getAffection(player) < Orders.AFFECTION) return false;
        ItemStack held = girl.getMainHandItem();
        if (!held.isEmpty() && (!(held.getItem() instanceof PickaxeItem) || girl.getPersistentData().getBoolean(GIVEN)) && !player.addItem(held.copy())) {
            player.drop(held.copy(), false);                                        // what a friend gave her goes back (a dwarf's own pick doesn't)
        }
        girl.setItemSlot(EquipmentSlot.MAINHAND, pick.split(1));
        girl.getPersistentData().putBoolean(GIVEN, true);
        girl.setDropChance(EquipmentSlot.MAINHAND, 2.0F);                         // hers to carry: it always drops if she dies
        girl.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.monstergirls.mining.pickaxe", girl.getDisplayName()), true);
        return true;
    }

    /** Shown a sample, with a pickaxe in hand, she digs in. Whether she went (else her friend hears why not). */
    public static boolean send(ServerLevel level, AbstractMonsterGirl girl, Player player, ItemStack sample) {
        if (!isMiner(girl) || girl.isBaby() || girl.getAffection(player) < Orders.AFFECTION || girl.isPassenger() || girl.isLeashed()) return false;
        ItemStack pick = girl.getMainHandItem();
        Vein vein = vein(sample);
        if (vein == null) return false;
        if (!(pick.getItem() instanceof PickaxeItem) || !girl.getPersistentData().getBoolean(GIVEN)) {   // her own (a dwarf's) doesn't count: it's for helping out
            player.displayClientMessage(Component.translatable("message.monstergirls.mining.no_pickaxe", girl.getDisplayName()), true);
            return true;
        }
        if (!pick.isCorrectToolForDrops(vein.block().defaultBlockState())) {
            player.displayClientMessage(Component.translatable("message.monstergirls.mining.weak", girl.getDisplayName(), sample.getHoverName()), true);
            return true;                                                             // handled: she shakes her head
        }
        Trip trip = plan(level, pick, vein);
        if (trip.found() < 4) {
            player.displayClientMessage(Component.translatable("message.monstergirls.mining.worn", girl.getDisplayName()), true);
            return true;
        }
        BlockPos at = girl.getOrders().command(girl) == Orders.Command.PATROL && girl.getOrders().anchor() != null ? girl.getOrders().anchor() : girl.blockPosition();
        CompoundTag saved = new CompoundTag();
        if (!girl.saveAsPassenger(saved)) return false;
        CompoundTag e = new CompoundTag();
        e.put("Girl", saved);
        e.putLong("Back", level.getGameTime() + trip.ticks());
        e.putString("Dim", level.dimension().location().toString());
        e.putLong("At", at.asLong());
        e.put("Sample", sample.copyWithCount(1).save(level.registryAccess()));
        e.putInt("Found", trip.found());
        e.putInt("Wear", trip.wear());
        e.putBoolean("Breaks", trip.breaks());
        MineShaft.start(level, e, at, vein.days());                              // the mouth of her mine, beside the spot
        CompoundTag data = player.getData(ModAttachments.EXPEDITIONS);
        ListTag list = data.getList(LIST, Tag.TAG_COMPOUND);
        list.add(e);
        data.put(LIST, list);
        player.setData(ModAttachments.EXPEDITIONS, data);
        sample.consume(1, player);
        BlockState ground = level.getBlockState(girl.blockPosition().below());
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground.isAir() ? Blocks.STONE.defaultBlockState() : ground),
                girl.getX(), girl.getY() + 0.2, girl.getZ(), 40, 0.4, 0.2, 0.4, 0.15);
        level.playSound(null, girl.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.NEUTRAL, 1.0F, 0.8F);
        long minutes = Math.max(1, trip.ticks() / 1200);
        player.displayClientMessage(Component.translatable("message.monstergirls.mining.off", girl.getDisplayName(), sample.getHoverName(), minutes), false);
        girl.discard();
        return true;
    }

    /** How many of this player's girls are down a mine. */
    public static int away(Player player) {
        return player.getData(ModAttachments.EXPEDITIONS).getList(LIST, Tag.TAG_COMPOUND).size();
    }

    /** Brings up every miner whose time is up (all of them, with now) whose spot is loaded. The girls who came back. */
    public static List<AbstractMonsterGirl> returnDue(ServerPlayer player, boolean now) {
        List<AbstractMonsterGirl> back = new ArrayList<>();
        CompoundTag data = player.getData(ModAttachments.EXPEDITIONS);
        ListTag list = data.getList(LIST, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return back;
        ListTag still = new ListTag();
        for (Tag t : list) {
            CompoundTag e = (CompoundTag) t;
            ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(e.getString("Dim"))));
            BlockPos at = BlockPos.of(e.getLong("At"));
            boolean due = level != null && (now || level.getGameTime() >= e.getLong("Back"));
            AbstractMonsterGirl girl = due && level.isLoaded(at) ? climbOut(level, player, e, at) : null;
            if (girl != null) {
                back.add(girl);
                continue;
            }
            if (due && !e.getBoolean("Told")) {                                   // done, but her spot is far away: she waits there
                e.putBoolean("Told", true);
                Component name = EntityType.create(e.getCompound("Girl"), level).map(Entity::getDisplayName).orElse(Component.literal("?"));
                player.displayClientMessage(Component.translatable("message.monstergirls.mining.waiting", name, at.getX(), at.getY(), at.getZ()), false);
            }
            if (level != null && !due) MineShaft.progress(level, e);              // deeper while she's down there
            still.add(e);
        }
        data.put(LIST, still);
        player.setData(ModAttachments.EXPEDITIONS, data);
        return back;
    }

    @Nullable
    private static AbstractMonsterGirl climbOut(ServerLevel level, ServerPlayer player, CompoundTag e, BlockPos at) {
        Entity entity = EntityType.loadEntityRecursive(e.getCompound("Girl"), level, x -> x);
        if (!(entity instanceof AbstractMonsterGirl girl)) return null;
        BlockPos spot = level.noCollision(girl.getType().getSpawnAABB(at.getX() + 0.5, at.getY(), at.getZ() + 0.5)) ? at
                : level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, at);
        girl.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, girl.getRandom().nextFloat() * 360F, 0);
        girl.setDeltaMovement(Vec3.ZERO);
        ItemStack pick = girl.getMainHandItem();
        if (e.getBoolean("Breaks")) {
            girl.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            girl.getPersistentData().remove(GIVEN);
        } else if (pick.isDamageableItem()) {
            pick.setDamageValue(pick.getDamageValue() + e.getInt("Wear"));
        }
        if (!level.addFreshEntity(girl)) return null;
        com.linkyoshi90.monstergirls.entity.Missing.forgive(girl, player);
        ItemStack sample = ItemStack.parseOptional(level.registryAccess(), e.getCompound("Sample"));
        List<ItemStack> haul = new ArrayList<>();
        haul.add(sample.copyWithCount(e.getInt("Found")));
        haul.add(sample.copyWithCount(1));                                          // and the sample back
        haul.addAll(MineShaft.finish(level, e));                                    // and the ore she struck on the way down
        haul = stow(level, at, haul);
        if (!haul.isEmpty()) keep(level, girl, player, haul);
        BlockState ground = level.getBlockState(spot.below());
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground.isAir() ? Blocks.STONE.defaultBlockState() : ground),
                girl.getX(), girl.getY() + 0.2, girl.getZ(), 40, 0.4, 0.2, 0.4, 0.15);
        level.playSound(null, spot, SoundEvents.STONE_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
        girl.hearts(level, 4);
        player.displayClientMessage(Component.translatable(e.getBoolean("Breaks") ? "message.monstergirls.mining.back_broke" : "message.monstergirls.mining.back",
                girl.getDisplayName(), e.getInt("Found"), sample.getHoverName()).withStyle(ChatFormatting.GOLD), false);
        AdvancementHolder a = player.server.getAdvancements().get(MonsterGirlsMod.id(ADVANCEMENT));
        if (a != null) player.getAdvancements().award(a, "done");
        return girl;
    }

    /** Into a chest or barrel within reach of the spot. What didn't fit. */
    static List<ItemStack> stow(ServerLevel level, BlockPos at, List<ItemStack> haul) {
        List<ItemStack> left = new ArrayList<>();
        for (ItemStack s : haul) {
            ItemStack rest = s;
            for (BlockPos p : BlockPos.betweenClosed(at.offset(-REACH, -2, -REACH), at.offset(REACH, 2, REACH))) {
                if (rest.isEmpty()) break;
                if (level.getBlockEntity(p) instanceof Container box) rest = HopperBlockEntity.addItem(null, box, rest, null);
            }
            if (!rest.isEmpty()) left.add(rest);
        }
        return left;
    }

    /** Her friend's crouching empty hand: the pickaxe they gave her, back. Whether there was one. */
    public static boolean givePickaxeBack(AbstractMonsterGirl girl, Player player) {
        if (!(girl.getMainHandItem().getItem() instanceof PickaxeItem) || !girl.getPersistentData().getBoolean(GIVEN)) return false;
        ItemStack pick = girl.getMainHandItem().copy();
        girl.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        girl.getPersistentData().remove(GIVEN);
        if (!player.addItem(pick)) player.drop(pick, false);
        return true;
    }

    /** She keeps the haul for her friend until they come near. */
    static void keep(ServerLevel level, AbstractMonsterGirl girl, Player player, List<ItemStack> haul) {
        ListTag list = girl.getPersistentData().getList(HAUL, Tag.TAG_COMPOUND);
        for (ItemStack s : haul) list.add(s.save(level.registryAccess()));
        girl.getPersistentData().put(HAUL, list);
        girl.getPersistentData().putUUID(HAUL_FOR, player.getUUID());
    }

    public static boolean holdsHaul(AbstractMonsterGirl girl) {
        return !girl.getPersistentData().getList(HAUL, Tag.TAG_COMPOUND).isEmpty();
    }

    /** Every second while she holds a haul: her friend near takes it. Whether she handed it over. */
    public static boolean handOver(ServerLevel level, AbstractMonsterGirl girl) {
        if (!holdsHaul(girl) || !girl.getPersistentData().hasUUID(HAUL_FOR)) return false;
        Player p = level.getPlayerByUUID(girl.getPersistentData().getUUID(HAUL_FOR));
        if (p == null || !p.isAlive() || p.distanceToSqr(girl) > REACH * REACH) return false;
        for (Tag t : girl.getPersistentData().getList(HAUL, Tag.TAG_COMPOUND)) {
            ItemStack s = ItemStack.parseOptional(level.registryAccess(), (CompoundTag) t);
            if (!s.isEmpty() && !p.addItem(s)) p.drop(s, false);
        }
        girl.getPersistentData().remove(HAUL);
        girl.getPersistentData().remove(HAUL_FOR);
        girl.getLookControl().setLookAt(p);
        level.playSound(null, girl.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.8F, 1.0F);
        return true;
    }

    /** She drops what she was keeping if she dies. */
    public static void dropHaul(ServerLevel level, AbstractMonsterGirl girl) {
        for (Tag t : girl.getPersistentData().getList(HAUL, Tag.TAG_COMPOUND)) {
            ItemStack s = ItemStack.parseOptional(level.registryAccess(), (CompoundTag) t);
            if (!s.isEmpty()) girl.spawnAtLocation(s);
        }
        girl.getPersistentData().remove(HAUL);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 100 == 13) returnDue(player, false);
    }

    @SubscribeEvent
    public static void onDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof AbstractMonsterGirl girl && girl.level() instanceof ServerLevel level && holdsHaul(girl)) dropHaul(level, girl);
    }
}
