package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.order.OrderSister;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * A sister of the Order (entity/order/OrderSister): the vanilla slim-armed player model, her own skin (64x64, player
 * layout, tools/gen_order_assets.py), worn armour, the Order's banner on a captain's helm. Kneeling, she crouches.
 */
public class OrderSisterRenderer extends HumanoidMobRenderer<OrderSister, PlayerModel<OrderSister>> {
    public OrderSisterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                new HumanoidArmorModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)), ctx.getModelManager()));
    }

    @Override
    public void render(OrderSister sister, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        this.getModel().crouching = sister.isCrouching();
        super.render(sister, yaw, partialTicks, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(OrderSister sister) {
        return HarmontideMod.id("textures/entity/order/" + sister.skin() + ".png");
    }
}
