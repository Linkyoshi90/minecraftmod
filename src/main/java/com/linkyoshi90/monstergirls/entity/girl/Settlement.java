package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A girl's life at a hearth (HearthBlock): she settles there when her friend's hearth is near and she roams free,
 * wanders home when she has strayed (and at night), and a day after settling builds herself a hut, a block at a
 * time: a five-by-five room in her family's materials, a door facing the hearth, a slab roof, a bed and a lantern.
 */
public final class Settlement {
    /** How near a hearth must be for her to settle at it. */
    public static final double SETTLE_RANGE = 16.0;
    /** How long she lives at the hearth before she builds her hut. */
    public static final long HUT_AFTER = 24000L;

    private Settlement() {
    }

    /** Every five seconds: settle, or check that her hearth still stands. */
    static void tick(ServerLevel level, AbstractMonsterGirl girl) {
        BlockPos home = girl.homeHearth();
        if (home != null) {
            if (level.isLoaded(home) && !(level.getBlockEntity(home) instanceof HearthBlockEntity h && h.isResident(girl.getUUID()))) girl.leaveHearth();
            else if (girl.hutOrigin() == null && level.getGameTime() - girl.settledSince() >= HUT_AFTER) planHut(level, girl);
            return;
        }
        if (girl.isBaby() || girl.getOrders().command(girl) != Orders.Command.FREE_ROAM) return;
        for (BlockPos pos : List.copyOf(HearthBlockEntity.loaded(level))) {
            if (pos.distToCenterSqr(girl.position()) > SETTLE_RANGE * SETTLE_RANGE) continue;
            if (level.getBlockEntity(pos) instanceof HearthBlockEntity h && settle(level, girl, h)) return;
        }
    }

    /** She settles at the hearth if its owner is her friend. Whether she did. */
    public static boolean settle(ServerLevel level, AbstractMonsterGirl girl, HearthBlockEntity hearth) {
        if (hearth.owner() == null || girl.getAffection(hearth.owner()) < Orders.AFFECTION || girl.isBaby()) return false;
        hearth.addResident(girl.getUUID());
        girl.settleAt(hearth.getBlockPos(), level.getGameTime());
        girl.hearts(level, 3);
        Player owner = level.getPlayerByUUID(hearth.owner());
        if (owner != null && owner.distanceToSqr(girl) < 64 * 64) {
            owner.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.settled", girl.getDisplayName()), true);
        }
        return true;
    }

    // --- her hut ----------------------------------------------------------------------------------------------------

    /** Corner posts, walls, roof slab and bed colour for her family. */
    record Style(Block post, Block wall, Block roof, Block bed) {
    }

    static Style style(GirlSpecies s) {
        return switch (s.family()) {
            case MERFOLK, MOLLUSK, CRUSTACEAN -> new Style(Blocks.DARK_PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICK_SLAB, Blocks.CYAN_BED);
            case UNDEAD, DEVIL, RIDER -> new Style(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.BLACKSTONE_SLAB, Blocks.PURPLE_BED);
            case SLIME -> new Style(Blocks.MUD_BRICKS, Blocks.PACKED_MUD, Blocks.MUD_BRICK_SLAB, Blocks.LIME_BED);
            case CONSTRUCT, DRAGON -> new Style(Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.GRAY_BED);
            case LIZARDFOLK, LAMIA, THEROPOD -> new Style(Blocks.CUT_SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE_SLAB, Blocks.GREEN_BED);
            case KITSUNE, NEKOMATA -> new Style(Blocks.CHERRY_LOG, Blocks.CHERRY_PLANKS, Blocks.CHERRY_SLAB, Blocks.PINK_BED);
            case ELF, FLORA, BUG -> new Style(Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS, Blocks.MOSS_BLOCK, Blocks.LIME_BED);
            case BAT, ARACHNE -> new Style(Blocks.MANGROVE_LOG, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_SLAB, Blocks.BLACK_BED);
            case MARSUPIAL, ARMOURED, RODENT -> new Style(Blocks.ACACIA_LOG, Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB, Blocks.ORANGE_BED);
            case CANID, MINOTAUR, CENTAUR, JACKALOPE -> new Style(Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB, Blocks.BROWN_BED);
        };
    }

    /** One block of her hut. */
    public record Piece(BlockPos pos, BlockState state) {
    }

    /** Her hut's blocks in building order, from its corner (the low north-west corner of the room) and the side the door is on. */
    public static List<Piece> hut(GirlSpecies species, BlockPos origin, Direction door) {
        Style st = style(species);
        List<Piece> out = new ArrayList<>();
        BlockPos doorAt = origin.offset(2 + door.getStepX() * 2, 0, 2 + door.getStepZ() * 2);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                for (int z = 0; z < 5; z++) {
                    boolean edgeX = x == 0 || x == 4, edgeZ = z == 0 || z == 4;
                    if (!edgeX && !edgeZ) continue;
                    BlockPos p = origin.offset(x, y, z);
                    if (y < 2 && p.getX() == doorAt.getX() && p.getZ() == doorAt.getZ()) continue;    // the doorway
                    boolean window = y == 1 && !(edgeX && edgeZ) && (x == 2 || z == 2);
                    out.add(new Piece(p, window ? Blocks.GLASS_PANE.defaultBlockState() : (edgeX && edgeZ ? st.post() : st.wall()).defaultBlockState()));
                }
            }
        }
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                BlockState roof = st.roof().defaultBlockState();
                if (roof.hasProperty(SlabBlock.TYPE)) roof = roof.setValue(SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.BOTTOM);
                out.add(new Piece(origin.offset(x, 3, z), roof));
            }
        }
        Direction bed = door.getOpposite();                                         // the bed against the back wall, its head to it
        BlockPos foot = origin.offset(2, 0, 2);                                    // (one further back, its head was in the wall, which went up first)
        BlockState b = st.bed().defaultBlockState().setValue(BedBlock.FACING, bed);
        out.add(new Piece(foot, b.setValue(BedBlock.PART, BedPart.FOOT)));
        out.add(new Piece(foot.relative(bed), b.setValue(BedBlock.PART, BedPart.HEAD)));
        Direction side = bed.getClockWise();
        out.add(new Piece(origin.offset(2 + side.getStepX(), 0, 2 + side.getStepZ()), Blocks.LANTERN.defaultBlockState()));
        return out;
    }

    /** She picks a flat, open spot 6 to 14 blocks from her hearth. Where (null if none yet: she tries again later). */
    @Nullable
    public static BlockPos planHut(ServerLevel level, AbstractMonsterGirl girl) {
        BlockPos hearth = girl.homeHearth();
        if (hearth == null || !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return null;
        for (int tries = 0; tries < 24; tries++) {
            double a = girl.getRandom().nextDouble() * Math.PI * 2, r = 6 + girl.getRandom().nextDouble() * 8;
            int cx = hearth.getX() + (int) Math.round(Math.cos(a) * r), cz = hearth.getZ() + (int) Math.round(Math.sin(a) * r);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
            if (Math.abs(y - hearth.getY()) > 4) continue;
            BlockPos origin = new BlockPos(cx - 2, y, cz - 2);
            if (!flatAndOpen(level, origin)) continue;
            int dx = hearth.getX() - cx, dz = hearth.getZ() - cz;
            Direction door = Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
            girl.planHutAt(origin, door);
            return origin;
        }
        return null;
    }

    private static boolean flatAndOpen(ServerLevel level, BlockPos origin) {
        for (int x = -1; x < 6; x++) {
            for (int z = -1; z < 6; z++) {
                BlockPos g = origin.offset(x, -1, z);
                boolean inside = x >= 0 && x < 5 && z >= 0 && z < 5;
                if (inside && (!level.getBlockState(g).isFaceSturdy(level, g, Direction.UP) || !level.getFluidState(g.above()).isEmpty())) return false;
                for (int y = 0; y < 5; y++) {
                    BlockState s = level.getBlockState(origin.offset(x, y, z));
                    if (!s.canBeReplaced() || s.getBlock() instanceof BedBlock || s.is(Blocks.WATER)) return false;   // others' huts keep a gap
                }
            }
        }
        return true;
    }

    /** Every half second while she has a hut planned: the next block of it (skipping any spot something now fills). */
    public static void buildStep(ServerLevel level, AbstractMonsterGirl girl) {
        BlockPos origin = girl.hutOrigin();
        if (origin == null || girl.hutDone() || !level.isLoaded(origin)) return;
        List<Piece> plan = hut(girl.getSpecies(), origin, girl.hutDoor());
        int i = girl.hutStep();
        while (i < plan.size()) {
            Piece p = plan.get(i++);
            if (!level.getBlockState(p.pos()).canBeReplaced()) continue;
            level.setBlock(p.pos(), p.state(), Block.UPDATE_ALL);
            level.playSound(null, p.pos(), p.state().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
            break;
        }
        girl.setHutStep(i);
    }

    // --- a homely hut ----------------------------------------------------------------------------------------------

    /** Her flower in a pot (the same order as HearthBlockEntity.GARDEN_FLOWERS). */
    static final List<Block> POTTED = List.of(Blocks.POTTED_DANDELION, Blocks.POTTED_POPPY, Blocks.POTTED_BLUE_ORCHID, Blocks.POTTED_ALLIUM,
            Blocks.POTTED_AZURE_BLUET, Blocks.POTTED_RED_TULIP, Blocks.POTTED_ORANGE_TULIP, Blocks.POTTED_WHITE_TULIP, Blocks.POTTED_PINK_TULIP,
            Blocks.POTTED_OXEYE_DAISY, Blocks.POTTED_CORNFLOWER, Blocks.POTTED_LILY_OF_THE_VALLEY, Blocks.POTTED_TORCHFLOWER);

    /** What she adds to her finished hut over the mornings, in order, each in its own spot on the floor (the way from the door to
     *  the bed stays clear): a candle across from the lantern, her flower in a pot in a back corner, then rugs in her bed's colour. */
    public static List<Piece> homely(AbstractMonsterGirl girl) {
        BlockPos origin = girl.hutOrigin();
        if (origin == null) return List.of();
        Direction back = girl.hutDoor().getOpposite(), side = back.getClockWise();
        BlockPos mid = origin.offset(2, 0, 2);
        int flower = HearthBlockEntity.GARDEN_FLOWERS.indexOf(HearthBlockEntity.flowerOf(girl));
        net.minecraft.world.item.DyeColor colour = ((BedBlock) style(girl.getSpecies()).bed()).getColor();
        Block rug = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace(colour.getName() + "_carpet"));
        BlockPos front = mid.relative(back.getOpposite());
        return List.of(
                new Piece(mid.relative(side.getOpposite()), Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true)),
                new Piece(mid.relative(back).relative(side), POTTED.get(Math.max(0, flower)).defaultBlockState()),
                new Piece(mid.relative(back).relative(side.getOpposite()), rug.defaultBlockState()),
                new Piece(front.relative(side), rug.defaultBlockState()),
                new Piece(front.relative(side.getOpposite()), rug.defaultBlockState()));
    }

    /** A morning at home in her finished hut: she adds the next thing it lacks (into an empty spot only). What she added, or null. */
    @Nullable
    public static Piece makeHomely(ServerLevel level, AbstractMonsterGirl girl) {
        if (!girl.hutDone() || !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return null;
        for (Piece p : homely(girl)) {
            BlockState there = level.getBlockState(p.pos());
            if (there.is(p.state().getBlock())) continue;
            if (!there.isAir()) continue;                                              // her friend put something there: leave it
            level.setBlock(p.pos(), p.state(), Block.UPDATE_ALL);
            level.playSound(null, p.pos(), p.state().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
            return p;
        }
        return null;
    }

    /** She wanders home when she has strayed far (by day) or is out after dark. */
    static class HomeGoal extends Goal {
        private final AbstractMonsterGirl girl;

        HomeGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        private double limit() {
            return this.girl.level().isDay() ? 24.0 : 6.0;
        }

        @Override
        public boolean canUse() {
            BlockPos home = this.girl.homeHearth();
            if (home == null || this.girl.getTarget() != null || this.girl.getOrders().command(this.girl) != Orders.Command.FREE_ROAM
                    || this.girl.getRandom().nextInt(reducedTickDelay(40)) != 0) return false;
            return home.distToCenterSqr(this.girl.position()) > this.limit() * this.limit();
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos home = this.girl.homeHearth();
            return home != null && !this.girl.getNavigation().isDone() && home.distToCenterSqr(this.girl.position()) > 9;
        }

        @Override
        public void start() {
            BlockPos home = this.girl.homeHearth();
            if (home != null) this.girl.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);
        }
    }
}
