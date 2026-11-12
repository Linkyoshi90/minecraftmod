package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.HomeLife;
import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Every few seconds, a round for the friends of whoever is near the post. */
public class SparringPostBlockEntity extends BlockEntity {
    public SparringPostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPARRING_POST.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SparringPostBlockEntity post) {
        if (level instanceof ServerLevel server && server.getGameTime() % 100 == 0) round(server, pos);
    }

    /** How many got better. */
    public static int round(ServerLevel level, BlockPos pos) {
        int trained = 0;
        for (Player p : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(16), Player::isAlive)) {
            for (Mob m : HomeLife.friendsOf(level, p, new AABB(pos).inflate(5))) {
                m.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                if (HomeLife.train(m)) trained++;
            }
        }
        return trained;
    }
}
