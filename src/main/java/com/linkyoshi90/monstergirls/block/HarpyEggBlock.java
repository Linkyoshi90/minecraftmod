package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.UUID;

/**
 * A woven nest with a clutch of fertilised harpy eggs, built and laid by a harpy who shared the bed of someone she
 * trusts. The species and the number of eggs (1-4) are block states, so one block covers all eight species. It
 * slowly cracks (about a day on average) and hatches one hatchling per egg, each of her mother's species in a random
 * colour, devoted to her father from the start (see {@link ParentageBlockEntity}). Hatchlings are small for five
 * days and then grow up.
 */
public class HarpyEggBlock extends Block implements EntityBlock {
    public static final MapCodec<HarpyEggBlock> CODEC = simpleCodec(HarpyEggBlock::new);
    public static final int MAX_HATCH = 3;
    public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, MAX_HATCH);
    public static final EnumProperty<Species> SPECIES = EnumProperty.create("species", Species.class);
    public static final int MAX_EGGS = 4;
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 1, MAX_EGGS);
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 8.0, 15.0);

    public enum Species implements StringRepresentable {
        SPARROW("sparrow_harpy"), CROW("crow_harpy"), OSTRICH("ostrich_harpy"), OWL("owl_harpy"),
        SEAGULL("seagull_harpy"), ALBATROSS("albatross_harpy"), RIFLEBIRD("riflebird_harpy"), PHOENIX("phoenix_harpy"),
        TENGU("tengu_harpy"), TOUCAN("toucan_harpy"), PENGUIN("penguin_harpy"), ZHUQUE("zhuque_harpy"), FINCH("finch_harpy"), FLAMINGO("flamingo_harpy"), MAGPIE("coin_magpie_harpy"),
        RAVEN("trickster_raven_harpy"), ROOK("rookmourn_harpy"), CASSOWRATH("cassowrath_harpy"), DODOBOLD("dodobold_harpy"), PEAFLARE("peaflare_harpy"),
        PUMPKINGULL("pumpkingull_harpy"), CONDORUNE("condorune_harpy"), MIRROR_CRANE("mirror_crane_harpy"), EMBERU("emberu_harpy"),
        IRONBEAK("ironbeak_harpy"), IBISET("ibiset_harpy"), STIGOWL("stigowl_harpy"), YATAGARASU("yatagarasu_harpy"),
        ROC("roc_harpy"), ZIZ("ziz_harpy"), BENNU("bennu_harpy"), GARUDA("garuda_harpy"), ALKONOST("alkonost_harpy"), EMU("emu_harpy"), LECHUZA("lechuza_harpy"), MACAW("macaw_harpy");

        private final String entityId;

        Species(String entityId) {
            this.entityId = entityId;
        }

        /** The registry id of the harpy that hatches, e.g. "sparrow_harpy". */
        public String entityId() {
            return this.entityId;
        }

        @Override
        public String getSerializedName() {
            return this.entityId;
        }

        public static Species forEntityId(String id) {
            for (Species s : values()) if (s.entityId.equals(id)) return s;
            return null;
        }
    }

    public HarpyEggBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(SPECIES, Species.SPARROW).setValue(EGGS, 1));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, SPECIES, EGGS);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ParentageBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return !state.canSurvive(level, pos) ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** Each random tick has a one in four chance to crack the egg a stage further; four stages then hatch (about a day). */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) return;
        int hatch = state.getValue(HATCH);
        if (hatch < MAX_HATCH) {
            level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
            level.setBlock(pos, state.setValue(HATCH, hatch + 1), 2);
        } else {
            hatch(level, pos, state);
        }
    }

    /** Breaks the eggs open and lets the hatchlings out: one per egg, each in a colour of her own. */
    public static void hatch(ServerLevel level, BlockPos pos, BlockState state) {
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 1.0F);
        UUID father = ParentageBlockEntity.fatherAt(level, pos);
        int paint = ParentageBlockEntity.paintAt(level, pos);
        level.removeBlock(pos, false);
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(MonsterGirlsMod.id(state.getValue(SPECIES).entityId()));
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(pos);
        for (int i = 0; i < state.getValue(EGGS); i++) {
            if (!(type.create(level) instanceof AbstractHarpy chick)) return;
            chick.moveTo(pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6, pos.getY(), pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6,
                    level.random.nextFloat() * 360.0F, 0.0F);
            chick.finalizeSpawn(level, difficulty, MobSpawnType.BREEDING, null);
            chick.randomizeVariant();
            chick.setBaby(true);
            chick.setPaint(paint);                                      // a painted mother's young are painted too
            chick.setPersistenceRequired();
            level.addFreshEntity(chick);
            chick.bornTo(father);
        }
    }
}
