package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Life at home (HARMONTIDE_CONCEPTS.md, thirty-fourth wave): grooming, a home-cooked meal, the whistle, picnics, the mirror and the sparring
 * post. It all works on anything Bonded, so harpies and frogs join in; what a girl can't do (the mirror for frogs, training for those with
 * no attack) just leaves her out. The timers live in getPersistentData, so they save without touching any family's save code.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class HomeLife {
    public static final int FRIEND = Orders.AFFECTION;
    public static final int MAX_TRAINING = 20;
    private static final int TOWEL_TICKS = 600;
    private static final long GROOM_WAIT = 6000, MEAL_WAIT = 2400, PICNIC_WAIT = 12000, TRAIN_WAIT = 1200, SOAK_WAIT = 6000;
    private static final ResourceLocation TRAINING = ResourceLocation.fromNamespaceAndPath(HarmontideMod.MODID, "sparring");

    private HomeLife() {}

    /** Everyone Bonded in the box who counts this player a friend. */
    public static List<Mob> friendsOf(ServerLevel level, Player player, AABB box) {
        List<Mob> out = new ArrayList<>();
        for (Mob m : level.getEntitiesOfClass(Mob.class, box, Mob::isAlive)) {
            if (m instanceof Bonded b && !m.isBaby() && b.getAffection(player) >= FRIEND) out.add(m);
        }
        return out;
    }

    private static boolean ready(Mob mob, String timer, long wait) {
        long now = mob.level().getGameTime();
        if (now < mob.getPersistentData().getLong(timer)) return false;
        mob.getPersistentData().putLong(timer, now + wait);
        return true;
    }

    private static void hearts(ServerLevel level, Mob mob) {
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
    }

    // --- the grooming brush ------------------------------------------------------------------------------------------

    /** What the brush brings out of her coat. */
    public static Item shed(Mob mob) {
        if (mob instanceof AbstractHarpy) return Items.FEATHER;
        if (mob instanceof AbstractAnuran) return Items.SLIME_BALL;
        if (mob instanceof AbstractMonsterGirl g) {
            return switch (g.getSpecies().family()) {
                case MERFOLK, MOLLUSK, CRUSTACEAN -> Items.PRISMARINE_SHARD;
                case LAMIA, LIZARDFOLK, DRAGON, THEROPOD -> Items.PHANTOM_MEMBRANE;
                case SLIME -> Items.SLIME_BALL;
                case FLORA -> Items.BONE_MEAL;
                case BUG, ARACHNE -> Items.STRING;
                case UNDEAD -> Items.BONE_MEAL;
                case CONSTRUCT -> Items.IRON_NUGGET;
                default -> Items.RABBIT_HIDE;
            };
        }
        return Items.STRING;
    }

    /** A brush: 2 friendship every five minutes, and now and then what came out of her coat. True if she took it. */
    public static boolean groom(ServerLevel level, Mob mob, Player player) {
        if (!(mob instanceof Bonded b) || b.getAffection(player) <= 0 || !ready(mob, "NextGroom", GROOM_WAIT)) return false;
        b.addAffection(player, 2);
        hearts(level, mob);
        if (mob.getRandom().nextInt(3) == 0) mob.spawnAtLocation(new ItemStack(shed(mob)));
        mob.playSound(SoundEvents.BRUSH_GENERIC, 1.0F, 1.2F);
        return true;
    }

    // --- a home-cooked meal ------------------------------------------------------------------------------------------

    /** A home-cooked meal: anyone will eat it, whatever her diet: 5 friendship, every two minutes. True if she ate. */
    public static boolean eatMeal(ServerLevel level, Mob mob, Player player) {
        if (!(mob instanceof Bonded b) || !ready(mob, "NextMeal", MEAL_WAIT)) return false;
        b.addAffection(player, 5);
        mob.heal(8.0F);
        hearts(level, mob);
        mob.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
        return true;
    }

    // --- the whistle --------------------------------------------------------------------------------------------------

    /** Every friend within 48 comes running. How many answered. */
    public static int whistle(ServerLevel level, Player player) {
        int came = 0;
        for (Mob m : friendsOf(level, player, player.getBoundingBox().inflate(48))) {
            if (m.getNavigation().moveTo(player, 1.3)) came++;
        }
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), player.getSoundSource(), 1.5F, 1.8F);
        return came;
    }

    // --- the picnic blanket -------------------------------------------------------------------------------------------

    /** Food set out on a blanket: friends within 20 come over, 2 friendship each (every ten minutes), and whoever spread it eats well. How many came. */
    public static int picnic(ServerLevel level, BlockPos at, Player player) {
        int came = 0;
        for (Mob m : friendsOf(level, player, new AABB(at).inflate(20))) {
            m.getNavigation().moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 1.0);
            if (ready(m, "NextPicnic", PICNIC_WAIT) && m instanceof Bonded b) {
                b.addAffection(player, 2);
                hearts(level, m);
                came++;
            }
        }
        if (came > 0) player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0));
        return came;
    }

    // --- the vanity mirror ---------------------------------------------------------------------------------------------

    /** The nearest friend within 6 tries on the next of her colours. True if someone did. */
    public static boolean mirror(ServerLevel level, BlockPos at, Player player) {
        Mob best = null;
        for (Mob m : friendsOf(level, player, new AABB(at).inflate(6))) {
            if ((m instanceof AbstractMonsterGirl || m instanceof AbstractHarpy) && (best == null || m.distanceToSqr(player) < best.distanceToSqr(player))) best = m;
        }
        if (best == null) return false;
        if (best instanceof AbstractMonsterGirl g) g.nextVariant();
        else ((AbstractHarpy) best).randomizeVariant();
        level.sendParticles(ParticleTypes.GLOW, best.getX(), best.getY() + best.getBbHeight() * 0.6, best.getZ(), 8, 0.3, 0.5, 0.3, 0.0);
        return true;
    }

    // --- the sparring post ---------------------------------------------------------------------------------------------

    public static int training(Mob mob) {
        return mob.getPersistentData().getInt("Training");
    }

    /** One round at the post: every minute, up to +5 attack at 20 rounds. True if she got better. */
    public static boolean train(Mob mob) {
        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        int level = training(mob);
        if (attack == null || level >= MAX_TRAINING || !ready(mob, "NextTrain", TRAIN_WAIT)) return false;
        mob.getPersistentData().putInt("Training", level + 1);
        attack.removeModifier(TRAINING);
        attack.addPermanentModifier(new AttributeModifier(TRAINING, (level + 1) * 0.25, AttributeModifier.Operation.ADD_VALUE));
        mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        mob.playSound(SoundEvents.PLAYER_ATTACK_WEAK, 0.6F, 1.0F);
        return true;
    }

    // --- the soaking tub -----------------------------------------------------------------------------------------------

    /** Sharing the tub: Regeneration for her, 1 friendship every five minutes. True if she got the friendship. */
    public static boolean soak(ServerLevel level, Mob mob, Player player) {
        mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
        if (!(mob instanceof Bonded b) || !ready(mob, "NextSoak", SOAK_WAIT)) return false;
        b.addAffection(player, 1);
        hearts(level, mob);
        if (mob instanceof AbstractMonsterGirl girl) {
            Flirting.inTub(level, girl, player);
            if (Flirting.canFlirt(girl, player)) {
                girl.towelFor(TOWEL_TICKS);
                girl.blushFor(TOWEL_TICKS);
            }
        }
        return true;
    }

    // --- the bedroll ------------------------------------------------------------------------------------------------------

    /** Making camp: friends within 32 gather round, and it counts as seeing them. How many. */
    public static int camp(ServerLevel level, BlockPos at, Player player) {
        int came = 0;
        for (Mob m : friendsOf(level, player, new AABB(at).inflate(32))) {
            m.getNavigation().moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 1.0);
            Missing.meet(m, player);
            came++;
        }
        return came;
    }

    // --- the friend compass and dancing ------------------------------------------------------------------------------

    /** Every friend in the loaded world, nearest first. */
    public static List<Mob> allFriends(ServerLevel level, Player player) {
        List<Mob> out = new ArrayList<>();
        for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
            if (e instanceof Mob m && m.isAlive() && m instanceof Bonded b && b.getAffection(player) >= FRIEND) out.add(m);
        }
        out.sort(java.util.Comparator.comparingDouble(m -> m.distanceToSqr(player)));
        return out;
    }

    /** With a jukebox playing within 16 she dances, hopping and scattering notes: 1 friendship every five minutes for a friend near. True if she danced. */
    public static boolean dance(ServerLevel level, AbstractMonsterGirl girl, boolean music) {
        if (!music || girl.isBaby() || !girl.onGround() || girl.isOrderedToSit()) return false;
        girl.getJumpControl().jump();
        level.sendParticles(ParticleTypes.NOTE, girl.getX(), girl.getY() + girl.getBbHeight() + 0.3, girl.getZ(), 1, 0.3, 0.1, 0.3, 0.5);
        Player friend = girl.bondedPlayer(FRIEND, 16);
        if (friend != null && ready(girl, "NextDance", GROOM_WAIT)) girl.addAffection(friend, 1);
        return true;
    }

    // --- the sketchbook -----------------------------------------------------------------------------------------------

    /** Draws her: a figurine of her as she is now. Empty for a stranger. */
    public static ItemStack sketch(Mob mob, Player player) {
        if (!(mob instanceof Bonded b) || b.getAffection(player) < FRIEND) return ItemStack.EMPTY;
        return com.linkyoshi90.harmontide.block.FigurineBlockEntity.of(mob);
    }

    // --- the hand -----------------------------------------------------------------------------------------------------

    /** Brushes and meals go first, ahead of what she does with anything else held out to her. */
    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(ModItems.SCOUT_PACK.get()) && event.getTarget() instanceof Mob scout && scout instanceof Bonded) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            if (!event.getLevel().isClientSide && !Scouting.send(scout, event.getEntity(), stack, event.getEntity().isShiftKeyDown())) {
                event.getEntity().displayClientMessage(Component.translatable(Scouting.hasFinds(stack) ? "message.harmontide.scout_full" : "message.harmontide.scout_refused", scout.getDisplayName()), true);
            }
            return;
        }
        if (stack.is(ModItems.SKETCHBOOK.get()) && event.getTarget() instanceof Mob sitter && sitter instanceof Bonded) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            if (event.getLevel().isClientSide) return;
            ItemStack figurine = sketch(sitter, event.getEntity());
            if (figurine.isEmpty()) {
                event.getEntity().displayClientMessage(Component.translatable("message.harmontide.sketch_refused", sitter.getDisplayName()), true);
            } else {
                stack.hurtAndBreak(1, event.getEntity(), net.minecraft.world.entity.LivingEntity.getSlotForHand(event.getHand()));
                event.getEntity().getInventory().placeItemBackInInventory(figurine);
            }
            return;
        }
        boolean brush = stack.is(ModItems.GROOMING_BRUSH.get()), meal = stack.is(ModItems.HOMEMADE_MEAL.get());
        if ((!brush && !meal) || !(event.getTarget() instanceof Mob mob) || !(mob instanceof Bonded)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Player player = event.getEntity();
        if (brush) {
            if (groom(level, mob, player)) stack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(event.getHand()));
            else player.displayClientMessage(Component.translatable("message.harmontide.groomed", mob.getDisplayName()), true);
        } else if (eatMeal(level, mob, player)) {
            Dairy.ate(mob, stack);
            stack.consume(1, player);
            player.getInventory().placeItemBackInInventory(new ItemStack(Items.BOWL));
        } else {
            player.displayClientMessage(Component.translatable("message.harmontide.full", mob.getDisplayName()), true);
        }
    }
}
