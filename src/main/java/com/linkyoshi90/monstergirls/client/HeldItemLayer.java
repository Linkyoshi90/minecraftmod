package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

/** Draws the item a harpy is carrying in her right hand (the "hand_r" bone every harpy model has). */
public class HeldItemLayer<T extends AbstractHarpy> extends BlockAndItemGeoLayer<T> {
    private static final String HAND_BONE = "hand_r";

    public HeldItemLayer(GeoRenderer<T> renderer) {
        super(renderer, (bone, harpy) -> HAND_BONE.equals(bone.getName()) ? harpy.getMainHandItem() : null, (bone, harpy) -> null);
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, T animatable) {
        return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable,
                                      MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        // Bone origin is the wrist; move into the palm and hold the item like a player does.
        poseStack.translate(0.0, -0.12, -0.02);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(0.7F, 0.7F, 0.7F);
        super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
