package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.RotationSegment;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Daughters move out nearby (2026-11-10). A grown daughter of the hearth's owner who settles at it doesn't build the little
 * hut every other girl builds by her friend's hearth (Settlement): she builds a house of her own a little farther off, 12 to 20
 * blocks away, seven by seven, with two beds against the back wall (one for the sweetheart she may marry one day), a cradle for
 * her little ones and a chest, and her name on the sign by the path. So the settlement grows into a family village. She tells her
 * father when she starts and when it's done; his first visit is a housewarming. She sleeps at her house rather than by the hearth,
 * and comes to the hearth by day as before. Once she is wed to a villager (Sweethearts) he moves in with her, taking the second
 * bed for his home. Off with the option daughtersMoveOut (daughters then build the usual hut).
 */
public final class MovingOut {
    /** A girl's own hut, and a daughter's house. */
    public static final int HUT = 5, HOUSE = 7;
    /** How close her father comes to her finished house for the housewarming, and how much closer they are after it. */
    public static final double WARM = 6;
    public static final int CLOSER = 4;
    private static final String OWN_HOUSE = "OwnHouse", DONE = "OwnHouseDone", WARMED = "OwnHouseWarmed", MOVED_IN = "OwnHouseMovedIn";

    private MovingOut() {
    }

    /** She is a grown daughter of her hearth's owner: she moves out into a house of her own. */
    public static boolean movesOut(ServerLevel level, AbstractMonsterGirl girl) {
        BlockPos hearth = girl.homeHearth();
        UUID father = girl.getFather();
        if (!HarmontideConfig.daughtersMoveOut() || hearth == null || girl.isBaby() || father == null || father.equals(AbstractAnuran.FROG_FATHER)) return false;
        return level.getBlockEntity(hearth) instanceof HearthBlockEntity h && father.equals(h.owner());
    }

    public static void mark(AbstractMonsterGirl girl, boolean house) {
        CompoundTag pd = girl.getPersistentData();
        pd.putBoolean(OWN_HOUSE, house);
        pd.remove(DONE);
        pd.remove(WARMED);
        pd.remove(MOVED_IN);
    }

    public static boolean ownHouse(AbstractMonsterGirl girl) {
        return girl.hutOrigin() != null && girl.getPersistentData().getBoolean(OWN_HOUSE);
    }

    /** The size of what she builds (or built). */
    public static int size(AbstractMonsterGirl girl) {
        return ownHouse(girl) ? HOUSE : HUT;
    }

    /** The middle of her house's floor, or null if she has none. */
    @Nullable
    public static BlockPos middle(AbstractMonsterGirl girl) {
        BlockPos o = girl.hutOrigin();
        return o == null ? null : o.offset(size(girl) / 2, 0, size(girl) / 2);
    }

    @Nullable
    private static Player father(ServerLevel level, AbstractMonsterGirl girl) {
        return girl.getFather() == null ? null : level.getPlayerByUUID(girl.getFather());
    }

    private static void tell(ServerLevel level, AbstractMonsterGirl girl, String key, ChatFormatting colour, Object... more) {
        Player p = father(level, girl);
        if (p == null) return;
        Object[] args = new Object[more.length + 1];
        args[0] = girl.getDisplayName();
        System.arraycopy(more, 0, args, 1, more.length);
        p.displayClientMessage(Component.translatable("message.harmontide.moving_out." + key, args).withStyle(colour), false);
    }

    /** She has picked her spot: she tells her father she's moving out. */
    static void planned(ServerLevel level, AbstractMonsterGirl girl) {
        if (ownHouse(girl)) tell(level, girl, "planned", ChatFormatting.LIGHT_PURPLE);
    }

    /** The last block of her house is in. */
    static void finished(ServerLevel level, AbstractMonsterGirl girl) {
        if (!ownHouse(girl) || girl.getPersistentData().getBoolean(DONE)) return;
        girl.getPersistentData().putBoolean(DONE, true);
        girl.hearts(level, 5);
        tell(level, girl, "done", ChatFormatting.GOLD);
        Player p = father(level, girl);
        if (p != null) {                                                         // the journal's children page: "her own house"
            CompoundTag journal = p.getData(com.linkyoshi90.harmontide.registry.ModAttachments.JOURNAL);
            net.minecraft.nbt.ListTag list = journal.getList(com.linkyoshi90.harmontide.entity.Marriage.CHILD_LIST, net.minecraft.nbt.Tag.TAG_COMPOUND);
            String id = girl.getUUID().toString();
            for (net.minecraft.nbt.Tag t : list) if (((CompoundTag) t).getString("Id").equals(id)) ((CompoundTag) t).putBoolean("House", true);
            journal.put(com.linkyoshi90.harmontide.entity.Marriage.CHILD_LIST, list);
            p.setData(com.linkyoshi90.harmontide.registry.ModAttachments.JOURNAL, journal);
        }
    }

    /** Every five seconds at home: her father's first visit to her finished house is the housewarming. Whether it was now. */
    public static boolean tick(ServerLevel level, AbstractMonsterGirl girl) {
        if (!ownHouse(girl) || !girl.hutDone() || girl.getPersistentData().getBoolean(WARMED)) return false;
        Player p = father(level, girl);
        BlockPos mid = middle(girl);
        if (p == null || mid == null || p.level() != level || mid.distToCenterSqr(p.position()) > WARM * WARM) return false;
        girl.getPersistentData().putBoolean(WARMED, true);
        girl.addAffection(p, CLOSER);
        girl.hearts(level, 8);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, mid.getX() + 0.5, mid.getY() + 1.5, mid.getZ() + 0.5, 20, 1.5, 0.8, 1.5, 0.0);
        level.playSound(null, mid, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.NEUTRAL, 1.0F, 1.2F);
        tell(level, girl, "welcome", ChatFormatting.GOLD);
        Dairy.award(p, "moving_out");
        return true;
    }

    /** Where she goes home to: her own house at night once it stands (unless the hearth's bell calls her), else the hearth. */
    public static BlockPos homeSpot(AbstractMonsterGirl girl) {
        BlockPos pool = CareNeeds.pool(girl);                              // a merfolk lives in the pool by the hearth
        if (pool != null) return pool;
        BlockPos mid = middle(girl);
        if (mid != null && ownHouse(girl) && girl.hutDone() && !girl.calledHome() && !girl.level().isDay()) return mid;
        return girl.homeHearth();
    }

    // --- the house ----------------------------------------------------------------------------------------------------

    /** Inside her house, after the walls and roof: two beds against the back wall, a lantern between them, a cradle and a chest in the back corners. */
    static void furnish(List<Settlement.Piece> out, Block bedBlock, BlockPos mid, Direction back) {
        Direction side = back.getClockWise();
        BlockState bed = bedBlock.defaultBlockState().setValue(BedBlock.FACING, back);
        for (int s : new int[]{-1, 1}) {
            BlockPos foot = mid.relative(back).relative(side, s);
            out.add(new Settlement.Piece(foot, bed.setValue(BedBlock.PART, BedPart.FOOT)));
            out.add(new Settlement.Piece(foot.relative(back), bed.setValue(BedBlock.PART, BedPart.HEAD)));
        }
        out.add(new Settlement.Piece(mid.relative(back, 2), Blocks.LANTERN.defaultBlockState()));
        out.add(new Settlement.Piece(mid.relative(back, 2).relative(side, 2),
                ModBlocks.CRADLE.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, back.getOpposite())));
        out.add(new Settlement.Piece(mid.relative(back, 2).relative(side, -2), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, back.getOpposite())));
    }

    /** The second bed's head: her sweetheart's, once he moves in. */
    public static BlockPos sweetheartBed(AbstractMonsterGirl girl) {
        Direction back = girl.hutDoor().getOpposite();
        return middle(girl).relative(back, 2).relative(back.getClockWise());
    }

    /** What she adds to her finished house over the mornings (Settlement.homely): the sign, a candle, her flower, rugs. */
    static List<Settlement.Piece> homely(BlockPos outside, BlockPos mid, Direction back, Block pot, Block rug) {
        Direction side = back.getClockWise(), door = back.getOpposite();
        return List.of(
                new Settlement.Piece(outside, Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, RotationSegment.convertToSegment(door))),
                new Settlement.Piece(mid.relative(side, -2), Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true)),
                new Settlement.Piece(mid.relative(back, -2).relative(side, 2), pot.defaultBlockState()),
                new Settlement.Piece(mid.relative(back, -1).relative(side), rug.defaultBlockState()),
                new Settlement.Piece(mid.relative(back, -1).relative(side, -1), rug.defaultBlockState()),
                new Settlement.Piece(mid.relative(side), rug.defaultBlockState()),
                new Settlement.Piece(mid.relative(side, -1), rug.defaultBlockState()));
    }

    // --- her sweetheart moves in ----------------------------------------------------------------------------------------

    /** Wed and her house finished: her villager takes the second bed for his home (leaving the bed he had in the village free). Whether he did now. */
    public static boolean moveIn(ServerLevel level, AbstractMonsterGirl girl, Villager v) {
        CompoundTag pd = girl.getPersistentData();
        if (!ownHouse(girl) || !girl.hutDone() || pd.getBoolean(MOVED_IN) || v.isBaby() || !com.linkyoshi90.harmontide.entity.Sweethearts.wed(girl)) return false;
        BlockPos bed = sweetheartBed(girl);
        BlockState there = level.getBlockState(bed);
        if (!(there.getBlock() instanceof BedBlock) || there.getValue(BedBlock.PART) != BedPart.HEAD) return false;
        PoiManager poi = level.getPoiManager();
        GlobalPos old = v.getBrain().getMemory(MemoryModuleType.HOME).orElse(null);
        if (old != null && old.pos().equals(bed) && old.dimension() == level.dimension()) {
            pd.putBoolean(MOVED_IN, true);
            return false;
        }
        if (poi.take(t -> t.is(PoiTypes.HOME), (t, p) -> p.equals(bed), bed, 1).isEmpty()) return false;   // someone else sleeps there
        if (old != null && old.dimension() == level.dimension()) {
            try {
                if (poi.existsAtPosition(PoiTypes.HOME, old.pos())) poi.release(old.pos());
            } catch (IllegalStateException ignored) {                           // his old bed is gone already
            }
        }
        v.getBrain().setMemory(MemoryModuleType.HOME, GlobalPos.of(level.dimension(), bed));
        pd.putBoolean(MOVED_IN, true);
        level.sendParticles(ParticleTypes.HEART, v.getX(), v.getEyeY() + 0.4, v.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        tell(level, girl, "moved_in", ChatFormatting.LIGHT_PURPLE, v.getDisplayName());
        return true;
    }

    public static boolean movedIn(AbstractMonsterGirl girl) {
        return girl.getPersistentData().getBoolean(MOVED_IN);
    }
}
