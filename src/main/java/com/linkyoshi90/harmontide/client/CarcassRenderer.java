package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.Carcass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * A carcass (entity/Carcass) is drawn as the animal it was, through that animal's own renderer: fallen on its side and red as a
 * hurt mob is (a dead stand-in with its death pose held), a sheep shorn once half the meat is gone. Once only bones are left, a few
 * bones lie scattered where it was.
 */
public class CarcassRenderer extends EntityRenderer<Carcass> {
    private static final Map<EntityType<?>, LivingEntity> STAND_INS = new HashMap<>();
    private static Level standInLevel;

    public CarcassRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.4F;
    }

    private static LivingEntity standIn(Carcass c) {
        if (standInLevel != c.level()) {
            STAND_INS.clear();
            standInLevel = c.level();
        }
        return STAND_INS.computeIfAbsent(c.animal(), t -> t.create(c.level()) instanceof LivingEntity l ? l : null);
    }

    @Override
    public void render(Carcass c, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (c.stage() == Carcass.Stage.BONES) {
            renderBones(c, pose, buffers, light);
            return;
        }
        LivingEntity body = standIn(c);
        if (body == null) return;
        body.yBodyRot = body.yBodyRotO = c.getYRot();
        body.yHeadRot = body.yHeadRotO = c.getYRot();
        body.setYRot(c.getYRot());
        body.setXRot(0);
        body.deathTime = 20;                                                    // the death pose (on its side) and the hurt tint
        body.hurtTime = 0;
        Carcass.Kind kind = c.kind();
        if (body instanceof AgeableMob young && kind != null) young.setAge(c.full() < kind.meat_max() ? -24000 : 0);
        if (body instanceof Sheep sheep) sheep.setSheared(c.meat() * 2 < c.full());
        body.setPos(c.getX(), c.getY(), c.getZ());
        body.xo = body.getX();
        body.yo = body.getY();
        body.zo = body.getZ();
        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(body);
        pose.pushPose();
        renderer.render(body, c.getYRot(), 1.0F, pose, buffers, light);
        pose.popPose();
    }

    private static void renderBones(Carcass c, PoseStack pose, MultiBufferSource buffers, int light) {
        var items = Minecraft.getInstance().getItemRenderer();
        ItemStack bone = new ItemStack(Items.BONE);
        long seed = c.getUUID().getLeastSignificantBits();
        for (int i = 0; i < 3; i++) {
            pose.pushPose();
            pose.translate(((seed >> (i * 4)) & 7) / 16.0 - 0.2, 0.03 + i * 0.01, ((seed >> (i * 4 + 12)) & 7) / 16.0 - 0.2);
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.mulPose(Axis.ZP.rotationDegrees((seed >> (i * 8)) & 255));
            pose.scale(0.6F, 0.6F, 0.6F);
            items.renderStatic(bone, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, pose, buffers, c.level(), 0);
            pose.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Carcass c) {
        return ResourceLocation.withDefaultNamespace("textures/item/bone.png");
    }
}
