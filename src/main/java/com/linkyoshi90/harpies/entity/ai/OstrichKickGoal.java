package com.linkyoshi90.harpies.entity.ai;

import com.linkyoshi90.harpies.entity.AbstractHarpy;
import com.linkyoshi90.harpies.entity.OstrichHarpy;
import com.linkyoshi90.harpies.registry.ModSounds;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;

/**
 * The charge: runs down her target and kicks (with the kick animation and a heavy thud). Mobs get her full
 * strength. Players are only taught a lesson: a few kicks at most, never taking them below two hearts, after
 * which she spares them and goes back to glaring.
 */
public class OstrichKickGoal extends MeleeAttackGoal {
    /** Health a kicked player always keeps (two hearts). */
    private static final float PLAYER_HEALTH_FLOOR = 4.0F;
    /** Hard difficulty scales mob damage to players by 1.5; leave room for it. */
    private static final float HARD_SCALING = 1.5F;

    private final OstrichHarpy ostrich;

    public OstrichKickGoal(OstrichHarpy ostrich, double speed) {
        super(ostrich, speed, true);
        this.ostrich = ostrich;
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (!this.canPerformAttack(target)) return;
        this.resetAttackCooldown();
        this.ostrich.startTimedAction(AbstractHarpy.Action.KICKING, 24); // animation.ostrich_harpy.kick is 1.2 s
        this.ostrich.playSound(ModSounds.OSTRICH_KICK.get(), 1.0F, ModSounds.wobble(this.ostrich));
        if (target instanceof Player player) {
            this.kickPlayer(player);
        } else {
            this.ostrich.doHurtTarget(target);
        }
    }

    private void kickPlayer(Player player) {
        float strength = (float) this.ostrich.getAttributeValue(Attributes.ATTACK_DAMAGE);
        // Half a heart of margin so rounding (and Hard's scaling) can never dip below the floor.
        float damage = Math.min(strength, (player.getHealth() - PLAYER_HEALTH_FLOOR - 1.0F) / HARD_SCALING);
        if (damage >= 1.0F && player.hurt(this.ostrich.damageSources().mobAttack(this.ostrich), damage)) {
            float yaw = this.ostrich.getYRot() * Mth.DEG_TO_RAD;
            player.knockback((float) this.ostrich.getAttributeValue(Attributes.ATTACK_KNOCKBACK) * 0.5F, Mth.sin(yaw), -Mth.cos(yaw));
            this.ostrich.onKicked(player);
        } else {
            this.ostrich.spare(player); // she's made her point
        }
    }
}
