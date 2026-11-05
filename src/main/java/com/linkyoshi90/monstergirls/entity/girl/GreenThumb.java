package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * A green thumb: a plant girl or a pollinator (bugs) on patrol for a friend tends the crops around her post. Every half minute one
 * crop there that is still growing grows a stage, with the bone meal sparkle.
 */
public final class GreenThumb {
    static final Set<GirlSpecies.Family> GARDENERS = Set.of(GirlSpecies.Family.FLORA, GirlSpecies.Family.BUG);

    private GreenThumb() {
    }

    public static boolean gardens(AbstractMonsterGirl girl) {
        return GARDENERS.contains(girl.getSpecies().family());
    }

    /** Every half minute (AbstractMonsterGirl's server tick): one growing crop near her post, a stage on. Where, or null. */
    @Nullable
    public static BlockPos tend(ServerLevel level, AbstractMonsterGirl girl) {
        if (!MonsterGirlsConfig.chores() || !gardens(girl) || girl.isBaby()) return null;
        Orders o = girl.getOrders();
        Player boss = o.commander(girl);
        BlockPos post = o.anchor();
        if (o.command(girl) != Orders.Command.PATROL || post == null || boss == null || girl.getAffection(boss) < Orders.AFFECTION) return null;
        int r = (int) Orders.PATROL_RADIUS, seen = 0;
        BlockPos pick = null;
        for (BlockPos p : BlockPos.betweenClosed(post.offset(-r, -2, -r), post.offset(r, 2, r))) {   // one growing crop, picked at random
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof CropBlock crop && !crop.isMaxAge(st) && level.random.nextInt(++seen) == 0) pick = p.immutable();
        }
        if (pick != null) {
            BlockState st = level.getBlockState(pick);
            CropBlock crop = (CropBlock) st.getBlock();
            level.setBlock(pick, crop.getStateForAge(crop.getAge(st) + 1), 2);
            level.levelEvent(1505, pick, 3);                                         // the bone meal sparkle
            return pick;
        }
        return null;
    }
}
