package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.AnuranRestGoal;
import com.linkyoshi90.monstergirls.entity.ai.SeekWaterGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * The vodyanoy, the Slavic river spirit: a frog-faced old man of the deep pools near villages, the first anuran man
 * (HARMONTIDE_CONCEPTS.md, fifth wave). While his pool stays clean he brings his friends fish (twice as much when a
 * fisherman lives nearby, his old friends). Litter his water and he turns grumpy: he drags swimmers under for a moment
 * and gives nothing until it is clean again. He is never born, so he has no tadpole or froglet stage, and he never
 * carries young.
 */
public class Vodyanoy extends AbstractAnuran {
    /** How far around him a dropped item in the water counts as litter in his pool. */
    public static final int POOL_RADIUS = 8;
    private long nextFish;
    private long nextPull;
    private boolean grumpy;

    public Vodyanoy(EntityType<? extends Vodyanoy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Bullywug.frogAttributes(30.0, 0.24, 4.0, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new SeekWaterGoal(this));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.1, stack -> stack.is(ItemTags.FISHES), false));
        this.goalSelector.addGoal(7, new AnuranRestGoal<>(this, Action.SITTING, 400, 200, 600, v -> waterNearby(v.level(), v.blockPosition(), 4)));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // --- his pool ----------------------------------------------------------------------------------------------------

    /** Litter: anything dropped into the water near him. */
    public boolean poolIsClean() {
        return this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(POOL_RADIUS), ItemEntity::isInWater).isEmpty();
    }

    public boolean isGrumpy() {
        return this.grumpy;
    }

    public void setNextFish(long time) {
        this.nextFish = time;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || this.tickCount % 40 != 0) return;
        boolean clean = this.poolIsClean();
        if (this.grumpy != !clean) {
            this.grumpy = !clean;
            if (this.grumpy) this.startTimedAction(Action.BELLOWING, 40);
        }
        if (this.grumpy) {
            this.pullUnder(level);
            return;
        }
        if (level.getGameTime() >= this.nextFish) this.bringFish(level);
    }

    /** A clean pool: a fish for each friend nearby (two when a fisherman lives close, his old friends). */
    public void bringFish(ServerLevel level) {
        this.nextFish = level.getGameTime() + 2400 + this.random.nextInt(2400);
        boolean fisherman = !level.getEntitiesOfClass(Villager.class, this.getBoundingBox().inflate(24),
                v -> v.getVillagerData().getProfession() == VillagerProfession.FISHERMAN).isEmpty();
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16), p -> this.getAffection(p) >= Orders.AFFECTION)) {
            ItemEntity fish = new ItemEntity(level, p.getX(), p.getY() + 0.5, p.getZ(),
                    new ItemStack(this.random.nextBoolean() ? Items.COD : Items.SALMON, fisherman ? 2 : 1));
            fish.setNoPickUpDelay();
            level.addFreshEntity(fish);
            level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 2, 0.3, 0.2, 0.3, 0);
        }
    }

    /** Grumpy: a swimmer near him is dragged under for a moment (then let go). */
    private void pullUnder(ServerLevel level) {
        if (level.getGameTime() < this.nextPull) return;
        Player p = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 5.0, e -> e instanceof Player pl && pl.isInWater() && !pl.isSpectator()
                && !pl.isCreative());
        if (p == null) return;
        this.nextPull = level.getGameTime() + 200;
        this.startTimedAction(Action.PULLING, 24);
        p.setDeltaMovement(p.getDeltaMovement().add(0.0, -0.8, 0.0));
        p.hurtMarked = true;
        level.sendParticles(ParticleTypes.BUBBLE, p.getX(), p.getY() + 1, p.getZ(), 20, 0.4, 0.4, 0.4, 0.1);
        p.displayClientMessage(Component.translatable("message.monstergirls.vodyanoy_grumpy", this.getDisplayName()), true);
    }

    // --- species data ------------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    @Override
    protected int dryLimitTicks() {
        return 2400;
    }

    /** He is a man: he shares no bed and bears no young. */
    @Override
    public boolean canShareBed() {
        return false;
    }

    /** Never born, never a tadpole: one model only. */
    @Override
    public boolean hasYoungModels() {
        return false;
    }

    @Override
    protected String species() {
        return "vodyanoy";
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("NextFish", this.nextFish);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nextFish = tag.getLong("NextFish");
    }
}
