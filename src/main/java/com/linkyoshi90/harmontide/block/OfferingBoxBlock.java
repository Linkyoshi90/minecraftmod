package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.Dairy;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;

/**
 * An offering box (2026-11-08, the kitsune shrine): leave a little food in it and draw a fortune, once a day. The slip is yours
 * to keep, and its blessing (or its curse) goes with you a while: a great blessing (Luck II and Regeneration), a blessing (Luck),
 * a small blessing (Speed), or now and then a curse (Bad Luck; better luck tomorrow).
 */
public class OfferingBoxBlock extends Block {
    public static final MapCodec<OfferingBoxBlock> CODEC = simpleCodec(OfferingBoxBlock::new);
    private static final VoxelShape SHAPE = Block.box(1, 0, 3, 15, 10, 13);
    public static final String DAY = "FortuneDay";

    public enum Fortune { GREAT_BLESSING, BLESSING, SMALL_BLESSING, CURSE;

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public OfferingBoxBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static Fortune roll(net.minecraft.util.RandomSource random) {
        int r = random.nextInt(20);
        return r < 3 ? Fortune.GREAT_BLESSING : r < 10 ? Fortune.BLESSING : r < 17 ? Fortune.SMALL_BLESSING : Fortune.CURSE;
    }

    /** The fortune and its effect on the player, and the slip in their hand. */
    public static ItemStack draw(ServerLevel level, BlockPos pos, Player p, Fortune f) {
        switch (f) {
            case GREAT_BLESSING -> {
                p.addEffect(new MobEffectInstance(MobEffects.LUCK, 24000, 1));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0));
                Dairy.award(p, "great_blessing");
            }
            case BLESSING -> p.addEffect(new MobEffectInstance(MobEffects.LUCK, 12000, 0));
            case SMALL_BLESSING -> p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 0));
            case CURSE -> p.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 6000, 0));
        }
        ItemStack slip = new ItemStack(Items.PAPER);
        slip.set(DataComponents.ITEM_NAME, Component.translatable("item.harmontide.fortune." + f.key()));
        if (!p.getInventory().add(slip.copy())) p.drop(slip.copy(), false);
        p.displayClientMessage(Component.translatable("message.harmontide.fortune." + f.key()).withStyle(f == Fortune.CURSE ? ChatFormatting.GRAY : ChatFormatting.GOLD), false);
        level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 0.6F, 1.6F);
        level.sendParticles(f == Fortune.CURSE ? ParticleTypes.SMOKE : ParticleTypes.CHERRY_LEAVES, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 12, 0.4, 0.3, 0.4, 0.02);
        Dairy.award(p, "kitsune_shrine");
        return slip;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.has(DataComponents.FOOD)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level instanceof ServerLevel server)) return ItemInteractionResult.SUCCESS;
        long day = server.getDayTime() / 24000L;
        if (player.getPersistentData().getLong(DAY) == day + 1) {             // stored +1 so 0 means never
            player.displayClientMessage(Component.translatable("message.harmontide.fortune.again"), true);
            return ItemInteractionResult.SUCCESS;
        }
        player.getPersistentData().putLong(DAY, day + 1);
        stack.consume(1, player);
        draw(server, pos, player, roll(server.random));
        return ItemInteractionResult.SUCCESS;
    }

    /** Mobs path round it (or over it) instead of walking into it: a collision shape short of a full cube otherwise reads as open ground to the pathfinder. */
    @Override
    protected boolean isPathfindable(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return type != net.minecraft.world.level.pathfinder.PathComputationType.LAND && super.isPathfindable(state, type);
    }
}
