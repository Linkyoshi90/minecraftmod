package com.linkyoshi90.harmontide.leviathan;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The working parts of the inside of the Dream-Leviathan (leviathan/Interior builds them into her rooms and runs them). None of them
 * can be broken in survival; they only change state when the room's puzzle or fight says so.
 */
public final class LeviathanBlocks {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final BooleanProperty CURED = BooleanProperty.create("cured");
    public static final BooleanProperty RETRACTED = BooleanProperty.create("retracted");

    private LeviathanBlocks() {
    }

    /** A sphincter of membrane closing a passage between rooms: it opens once the room before it is cleared or solved. */
    public static class Membrane extends Block {
        public Membrane(Properties p) {
            super(p);
            this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(OPEN);
        }

        @Override
        protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
            return s.getValue(OPEN) ? Shapes.empty() : Shapes.block();
        }

        @Override
        protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
            return s.getValue(OPEN) ? Shapes.empty() : Shapes.block();
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!s.getValue(OPEN) && !level.isClientSide) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.harmontide.leviathan.membrane_shut"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    /** A twitching nerve: when lit it is the one to strike (by hand or an arrow); striking a dark one stings and starts over. */
    public static class NerveNode extends Block {
        public NerveNode(Properties p) {
            super(p);
            this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(LIT);
        }

        @Override
        protected void attack(BlockState s, Level level, BlockPos pos, Player player) {
            if (level instanceof ServerLevel server) Interior.onNerve(server, pos, player);
        }

        @Override
        protected void onProjectileHit(Level level, BlockState s, BlockHitResult hit, Projectile projectile) {
            if (level instanceof ServerLevel server && projectile.getOwner() instanceof Player player) Interior.onNerve(server, hit.getBlockPos(), player);
        }
    }

    /** Stomach acid: you wade in it slowly and it eats at you. */
    public static class Acid extends Block {
        public Acid(Properties p) {
            super(p);
        }

        @Override
        protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
            return Shapes.empty();
        }

        @Override
        protected void entityInside(BlockState s, Level level, BlockPos pos, Entity entity) {
            entity.makeStuckInBlock(s, new Vec3(0.45, 0.6, 0.45));
            if (!level.isClientSide && entity instanceof LivingEntity living && !(entity instanceof Parasites.Infested) && entity.tickCount % 10 == 0) {
                living.hurt(level.damageSources().source(LeviathanRegistry.STOMACH_ACID), 2.0F);
            }
        }
    }

    /** Cilia standing up as a bridge: a few moments after you step on them they fold away, then stand up again. Keep moving. */
    public static class Cilia extends Block {
        public static final int FOLDS_AFTER = 12, STAYS_DOWN = 50;

        public Cilia(Properties p) {
            super(p);
            this.registerDefaultState(this.stateDefinition.any().setValue(RETRACTED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(RETRACTED);
        }

        @Override
        protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
            return s.getValue(RETRACTED) ? Shapes.empty() : Shapes.block();
        }

        @Override
        protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
            return s.getValue(RETRACTED) ? Block.box(0, 0, 0, 16, 2, 16) : Shapes.block();
        }

        @Override
        public void stepOn(Level level, BlockPos pos, BlockState s, Entity entity) {
            if (!level.isClientSide && !s.getValue(RETRACTED) && entity instanceof Player && !level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.scheduleTick(pos, this, FOLDS_AFTER);
            }
        }

        @Override
        protected void tick(BlockState s, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
            if (s.getValue(RETRACTED)) {
                level.setBlock(pos, s.setValue(RETRACTED, false), 3);
            } else {
                level.setBlock(pos, s.setValue(RETRACTED, true), 3);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.SLIME_SQUISH_SMALL, net.minecraft.sounds.SoundSource.BLOCKS, 0.6F, 0.7F);
                level.scheduleTick(pos, this, STAYS_DOWN);
            }
        }
    }

    /** A wall of muscle that opens and clenches shut with her heartbeat (leviathan/Interior runs the beat). */
    public static class PulseWall extends Block {
        public PulseWall(Properties p) {
            super(p);
            this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(OPEN);
        }

        @Override
        protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
            return s.getValue(OPEN) ? Shapes.empty() : Shapes.block();
        }

        @Override
        protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
            return s.getValue(OPEN) ? Shapes.empty() : Shapes.block();
        }
    }

    /** A pressure valve: squeeze it and the flesh rises out of the acid for a while. */
    public static class Valve extends Block {
        public Valve(Properties p) {
            super(p);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (level instanceof ServerLevel server) Interior.onValve(server, pos, player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    /** An infected cyst: it breeds parasites until an antibody is pressed into it. */
    public static class Cyst extends Block {
        public Cyst(Properties p) {
            super(p);
            this.registerDefaultState(this.stateDefinition.any().setValue(CURED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(CURED);
        }

        @Override
        protected ItemInteractionResult useItemOn(ItemStack stack, BlockState s, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (s.getValue(CURED) || !stack.is(LeviathanRegistry.ANTIBODY.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (level instanceof ServerLevel server) {
                level.setBlock(pos, s.setValue(CURED, true), 3);
                stack.consume(1, player);
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.5, 0.5, 0.5, 0.0);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.HONEY_BLOCK_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.8F);
                Interior.onCystCured(server, pos, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!level.isClientSide && !s.getValue(CURED)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.harmontide.leviathan.cyst_needs_antibody"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    /** A gland that makes antibodies: squeeze one out to cure a cyst with. */
    public static class Gland extends Block {
        public Gland(Properties p) {
            super(p);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!level.isClientSide) {
                if (player.getInventory().countItem(LeviathanRegistry.ANTIBODY.get()) >= 1) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.harmontide.leviathan.gland_has"), true);
                } else {
                    player.getInventory().placeItemBackInInventory(new ItemStack(LeviathanRegistry.ANTIBODY.get()));
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.HONEY_DRINK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    /** Her egg-cell, at the centre of the round chamber: touch it once she is clean of the infection. */
    public static class Heart extends Block {
        public Heart(Properties p) {
            super(p);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) Interior.onHeart(server, pos, sp);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    /** The way back out (her throat, or the other end): use it to leave. */
    public static class Exit extends Block {
        public Exit(Properties p) {
            super(p);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (player instanceof ServerPlayer sp) Interior.leave(sp, "message.harmontide.leviathan.left");
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }
}
