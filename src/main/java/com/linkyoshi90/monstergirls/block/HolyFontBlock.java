package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.registry.ModVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The holy font: a calcite basin of holy water. Within {@link #RADIUS} blocks of one the Demon Realm can't take hold: its grass
 * won't spread or take root, and no creature is possessed, passes it on or is turned there. A village's ward. It is a point of
 * interest (registry/ModVillagers#FONT), so the check is cheap.
 */
public class HolyFontBlock extends Block {
    public static final int RADIUS = 12;

    public HolyFontBlock(Properties properties) {
        super(properties);
    }

    /** A holy font within RADIUS of this spot. */
    public static boolean warded(ServerLevel level, BlockPos at) {
        return level.getPoiManager().findClosest(h -> h.is(ModVillagers.FONT_KEY), at, RADIUS, PoiManager.Occupancy.ANY).isPresent();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 1.05, pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                    0.0, 0.02, 0.0);
        }
    }
}
