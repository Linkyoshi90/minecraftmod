package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Nectar: a slow, golden fluid from flora girls (bucket: three nectar bottles and a bucket). In a field it works like
 * water (FluidType#canHydrate keeps farmland wet) and more: crops within four blocks of it grow on half of their
 * growth ticks. It never makes an infinite source.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MonsterGirlsMod.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MonsterGirlsMod.MODID);

    /** How far (horizontally) nectar reaches the crops of a field, like water's four blocks. */
    public static final int REACH = 4;

    public static final DeferredHolder<FluidType, FluidType> NECTAR_TYPE = FLUID_TYPES.register("nectar", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.monstergirls.nectar").canHydrate(true).canSwim(true).canDrown(true).canExtinguish(true).supportsBoating(true)
            .density(1300).viscosity(3000).motionScale(0.006)));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> NECTAR = FLUIDS.register("nectar", () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_NECTAR = FLUIDS.register("flowing_nectar", () -> new BaseFlowingFluid.Flowing(properties()));

    private ModFluids() {}

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(NECTAR_TYPE, NECTAR, FLOWING_NECTAR).bucket(ModItems.NECTAR_BUCKET).block(ModBlocks.NECTAR)
                .slopeFindDistance(2).levelDecreasePerBlock(2).tickRate(15);
    }

    /** Whether there is nectar within reach of a crop (at its own height or the farmland's below). */
    public static boolean nectarNear(Level level, BlockPos crop) {
        for (BlockPos p : BlockPos.betweenClosed(crop.offset(-REACH, -1, -REACH), crop.offset(REACH, 0, REACH))) {
            if (level.getFluidState(p).getFluidType() == NECTAR_TYPE.get()) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onCropGrow(CropGrowEvent.Pre event) {
        if (event.getResult() == CropGrowEvent.Pre.Result.DEFAULT && event.getLevel() instanceof Level level && level.random.nextBoolean()
                && nectarNear(level, event.getPos())) {
            event.setResult(CropGrowEvent.Pre.Result.GROW);
        }
    }
}
