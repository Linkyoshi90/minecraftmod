package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.FridgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Roles: a daughter who grows up picks a calling from what she grew up around. Farmland makes a farmhand (crops in her patrol
 * area grow faster, as with a flora girl's touch), a kitchen (a furnace, smoker, campfire or fridge) a cook (now and then she
 * cooks something raw in a fridge near her), a bell or an iron golem a guard (tougher and harder-hitting), and the wide world
 * a scout (her expeditions take half the time and bring an extra find). Some of each is luck. Her friends hear what she chose.
 */
public final class Roles {
    public enum Role {
        FARMHAND, COOK, GUARD, SCOUT;

        public String key() {
            return "role.monstergirls." + name().toLowerCase(Locale.ROOT);
        }
    }

    public static final String KEY = "Role";
    public static final int LOOK = 12, COOK_EVERY = 1200;
    private static final ResourceLocation GUARD_HEALTH = MonsterGirlsMod.id("guard_health"), GUARD_ATTACK = MonsterGirlsMod.id("guard_attack");

    private Roles() {
    }

    @Nullable
    public static Role of(AbstractMonsterGirl girl) {
        String r = girl.getPersistentData().getString(KEY);
        if (r.isEmpty()) return null;
        try {
            return Role.valueOf(r);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean is(Object mob, Role role) {
        return mob instanceof AbstractMonsterGirl g && of(g) == role;
    }

    /** What she saw growing up, as weights for each role. */
    public static int[] weights(ServerLevel level, AbstractMonsterGirl girl) {
        int farm = 0, kitchen = 0, guard = 0;
        BlockPos at = girl.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-LOOK, -3, -LOOK), at.offset(LOOK, 3, LOOK))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof FarmBlock) farm++;
            else if (s.getBlock() instanceof AbstractFurnaceBlock || s.getBlock() instanceof CampfireBlock || level.getBlockEntity(p) instanceof FridgeBlockEntity) kitchen++;
            else if (s.getBlock() instanceof BellBlock) guard++;
        }
        guard += level.getEntitiesOfClass(IronGolem.class, new AABB(at).inflate(LOOK)).size();
        return new int[]{Math.min(farm, 24) / 3, Math.min(kitchen, 4) * 3, Math.min(guard, 3) * 4, 2};
    }

    /** She has grown up: she picks her calling, and her friends near hear of it. Her role. */
    public static Role grownUp(ServerLevel level, AbstractMonsterGirl girl) {
        int[] w = weights(level, girl);
        int total = 0;
        for (int x : w) total += x;
        int roll = girl.getRandom().nextInt(total);
        Role pick = Role.SCOUT;
        for (int i = 0; i < w.length; i++) {
            if (roll < w[i]) {
                pick = Role.values()[i];
                break;
            }
            roll -= w[i];
        }
        set(girl, pick);
        girl.hearts(level, 3);
        for (Player p : level.players()) {
            if (girl.getAffection(p) > 0 && p.distanceToSqr(girl) < 64 * 64) {
                p.displayClientMessage(Component.translatable("message.monstergirls.role.chosen", girl.getDisplayName(), Component.translatable(pick.key())), false);
            }
        }
        return pick;
    }

    public static void set(AbstractMonsterGirl girl, Role role) {
        girl.getPersistentData().putString(KEY, role.name());
        apply(girl);
    }

    /** Her role's lasting marks on her: a guard's toughness. On every load, too. */
    public static void apply(AbstractMonsterGirl girl) {
        boolean guard = of(girl) == Role.GUARD;
        modify(girl.getAttribute(Attributes.MAX_HEALTH), GUARD_HEALTH, guard ? 8.0 : 0.0);
        modify(girl.getAttribute(Attributes.ATTACK_DAMAGE), GUARD_ATTACK, guard ? 2.0 : 0.0);
    }

    private static void modify(@Nullable AttributeInstance a, ResourceLocation id, double amount) {
        if (a == null) return;
        a.removeModifier(id);
        if (amount != 0) a.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
    }

    /** How long an expedition takes her. */
    public static long expedition(AbstractMonsterGirl girl, long normal) {
        return of(girl) == Role.SCOUT ? normal / 2 : normal;
    }

    /** Every so often for a cook: something raw from a fridge near her, cooked and put back. What she cooked (empty if nothing). */
    public static ItemStack cook(ServerLevel level, AbstractMonsterGirl girl) {
        if (of(girl) != Role.COOK || !MonsterGirlsConfig.chores()) return ItemStack.EMPTY;
        BlockPos at = girl.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-8, -2, -8), at.offset(8, 2, 8))) {
            if (!(level.getBlockEntity(p) instanceof FridgeBlockEntity f)) continue;
            var store = f.store();
            for (int i = 0; i < store.getContainerSize(); i++) {
                ItemStack raw = store.getItem(i);
                if (raw.isEmpty()) continue;
                var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(raw), level);
                if (recipe.isEmpty()) continue;
                ItemStack cooked = recipe.get().value().getResultItem(level.registryAccess()).copy();
                store.removeItem(i, 1);
                ItemStack left = net.minecraft.world.level.block.entity.HopperBlockEntity.addItem(null, store, cooked.copy(), null);
                if (!left.isEmpty()) girl.spawnAtLocation(left);
                level.sendParticles(ParticleTypes.SMOKE, p.getX() + 0.5, p.getY() + 1.1, p.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.01);
                level.playSound(null, p, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 0.8F, 1.0F);
                return cooked;
            }
        }
        return ItemStack.EMPTY;
    }
}
