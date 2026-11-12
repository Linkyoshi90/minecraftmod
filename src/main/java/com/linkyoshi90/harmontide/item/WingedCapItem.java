package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A leather cap with a feather wing over each ear (geo/item/armor/winged_cap.geo.json). The wings beat while its
 * wearer falls (GearEvents#capFlapping, which also slows the fall) and lie folded the rest of the time.
 */
public class WingedCapItem extends ArmorItem implements GeoItem {
    private static final RawAnimation FLAP = RawAnimation.begin().thenLoop("animation.winged_cap.flap");
    private static final RawAnimation REST = RawAnimation.begin().thenLoop("animation.winged_cap.rest");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public WingedCapItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            @Nullable
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T entity, ItemStack stack, @Nullable EquipmentSlot slot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null) this.renderer = new GeoArmorRenderer<>(new DefaultedItemGeoModel<WingedCapItem>(HarmontideMod.id("armor/winged_cap")));
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "wings", 3, state -> {
            boolean flapping = state.getData(DataTickets.ENTITY) instanceof Player p && GearEvents.capFlapping(p);
            return state.setAndContinue(flapping ? FLAP : REST);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
