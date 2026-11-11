package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Headgear a devoted friend gives (entity/Headgear): worn on the head, drawn by GeckoLib from geo/item/armor/&lt;name&gt;.geo.json
 * (tools/gen_headgear_assets.py). What each does while worn is in entity/Headgear.
 */
public class HeadgearItem extends ArmorItem implements GeoItem {
    public enum Hat { KITSUNE_MASK, BUNNY_EARS, WITCH_HAT, FLOWER_CROWN;

        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private final Hat hat;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HeadgearItem(Hat hat, Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties);
        this.hat = hat;
    }

    public Hat hat() {
        return this.hat;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        String model = this.hat.id();
        consumer.accept(new GeoRenderProvider() {
            @Nullable
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T entity, ItemStack stack, @Nullable EquipmentSlot slot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null) this.renderer = new GeoArmorRenderer<>(new DefaultedItemGeoModel<HeadgearItem>(MonsterGirlsMod.id("armor/" + model)));
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.monstergirls." + this.hat.id() + ".tip").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
