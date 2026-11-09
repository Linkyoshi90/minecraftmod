package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.UUID;

/**
 * Hobbies (2026-11-08). Every girl has a pastime of her own, always the same for her (from her UUID): gardening, fishing,
 * reading, music, baking, collecting pretty things, knitting or painting, and an hour of the day that is hers for it. During her hour she
 * potters away at it where she is (a gardener tends the crops round her as she goes). Once she has put in {@link #WORK} ticks of it,
 * if a friend is about, she leaves what she made at her feet: flowers and seeds, a fish or two, bread and cookies, a shiny find,
 * wool or a knitted cap, now and then a portrait (a painting; her own kind's if there is one); a reader shares what she learned (experience), a musician plays everyone near a merry tune (Haste).
 * Keep her company through it (within {@link #NEAR} for {@link #COMPANY} ticks) and she shows you how she does it: affection and a
 * share for you, once a day. Her friends see her hobby in Jade. Half of all daughters born take up their mother's hobby.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Hobbies {
    public static final int HOUR = 1000, WORK = 600, COMPANY = 600, NEAR = 4, SEEN = 32, AFFECTION = 4, TEND = 3;
    private static final String KEY = "Hobby", WORK_TIME = "HobbyWork", WORK_DAY = "HobbyWorkDay", DONE_DAY = "HobbyDone",
            WITH = "HobbyWith", WITH_TIME = "HobbyWithTime", SHARED_DAY = "HobbyShared";

    private Hobbies() {}

    public enum Hobby {
        GARDENING(ParticleTypes.HAPPY_VILLAGER, SoundEvents.CROP_PLANTED),
        FISHING(ParticleTypes.FISHING, SoundEvents.FISHING_BOBBER_SPLASH),
        READING(ParticleTypes.ENCHANT, SoundEvents.BOOK_PAGE_TURN),
        MUSIC(ParticleTypes.NOTE, SoundEvents.NOTE_BLOCK_FLUTE.value()),
        BAKING(ParticleTypes.SMOKE, SoundEvents.SMOKER_SMOKE),
        COLLECTING(ParticleTypes.WAX_OFF, SoundEvents.AMETHYST_BLOCK_CHIME),
        KNITTING(ParticleTypes.CLOUD, SoundEvents.WOOL_PLACE),
        PAINTING(ParticleTypes.WAX_ON, SoundEvents.PAINTING_PLACE);

        public final ParticleOptions particle;
        public final SoundEvent sound;

        Hobby(ParticleOptions particle, SoundEvent sound) {
            this.particle = particle;
            this.sound = sound;
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** Her hobby: always the same for her. */
    public static Hobby hobby(Mob girl) {
        String set = girl.getPersistentData().getString(KEY);
        if (!set.isEmpty()) {
            try {
                return Hobby.valueOf(set);
            } catch (IllegalArgumentException ignored) {
                // fall through to the one she was born with
            }
        }
        UUID id = girl.getUUID();
        long mix = Long.rotateLeft(id.getMostSignificantBits(), 17) ^ (id.getLeastSignificantBits() * 31L);
        if (Math.floorMod(mix, 8L) == 7L) return Hobby.PAINTING;               // painting came later: one in eight, and the rest keep the hobby they had
        return Hobby.values()[(int) Math.floorMod(mix, 7L)];
    }

    /** Sets her hobby (tests, and anything that ever wants to change it). */
    public static void setHobby(Mob girl, Hobby hobby) {
        girl.getPersistentData().putString(KEY, hobby.name());
    }

    /** When her hour starts (day time, 1000..9000). */
    public static int hourStart(Mob girl) {
        return 1000 + (int) Math.floorMod(girl.getUUID().getMostSignificantBits() >>> 7, 9L) * 1000;
    }

    /** Whether, at this time of day, it is her hour. */
    public static boolean herHour(Mob girl, long dayTime) {
        long t = Math.floorMod(dayTime, 24000L), start = hourStart(girl);
        return t >= start && t < start + HOUR;
    }

    public static boolean canPractise(Mob girl) {
        return girl instanceof Bonded && girl.isAlive() && !girl.isBaby() && !girl.isSleeping() && girl.getTarget() == null
                && !Games.racing(girl) && Dates.dateWith(girl) == null;
    }

    /** The nearest player within {@code range} who counts her a friend. */
    @Nullable
    public static Player friendNear(ServerLevel level, Mob girl, double range) {
        if (!(girl instanceof Bonded b)) return null;
        Player best = null;
        double bestD = range * range;
        for (Player p : level.players()) {
            if (p.isSpectator() || b.getAffection(p) < HomeLife.FRIEND) continue;
            double d = p.distanceToSqr(girl);
            if (d <= bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    private static long dayMark(ServerLevel level) {
        return level.getDayTime() / 24000L + 1;                         // stored +1 so 0 means never
    }

    public static boolean doneToday(ServerLevel level, Mob girl) {
        return girl.getPersistentData().getLong(DONE_DAY) == dayMark(level);
    }

    public static boolean sharedToday(ServerLevel level, Mob girl) {
        return girl.getPersistentData().getLong(SHARED_DAY) == dayMark(level);
    }

    public static int work(Mob girl) {
        return girl.getPersistentData().getInt(WORK_TIME);
    }

    /** Whether there is water close enough to fish in. */
    public static boolean waterNear(ServerLevel level, BlockPos at) {
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-6, -3, -6), at.offset(6, 1, 6))) {
            if (level.getFluidState(p).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    /** What her hobby made this time. Empty if nothing came of it (no water to fish in; music and reading make nothing to hold). */
    public static ItemStack make(ServerLevel level, Mob girl, RandomSource r) {
        return switch (hobby(girl)) {
            case GARDENING -> r.nextInt(3) == 0
                    ? new ItemStack(pick(r, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS), 2 + r.nextInt(3))
                    : new ItemStack(pick(r, Items.POPPY, Items.DANDELION, Items.CORNFLOWER, Items.ALLIUM, Items.OXEYE_DAISY, Items.AZURE_BLUET, Items.LILY_OF_THE_VALLEY), 2 + r.nextInt(3));
            case FISHING -> !waterNear(level, girl.blockPosition()) ? ItemStack.EMPTY
                    : r.nextInt(40) == 0 ? new ItemStack(Items.NAUTILUS_SHELL)
                    : new ItemStack(pick(r, Items.COD, Items.COD, Items.SALMON, Items.TROPICAL_FISH), 1 + r.nextInt(2));
            case READING, MUSIC -> ItemStack.EMPTY;
            case BAKING -> switch (r.nextInt(8)) {
                case 0 -> new ItemStack(Items.CAKE);
                case 1, 2 -> new ItemStack(Items.PUMPKIN_PIE, 2);
                case 3, 4 -> new ItemStack(Items.COOKIE, 4 + r.nextInt(5));
                default -> new ItemStack(Items.BREAD, 2 + r.nextInt(3));
            };
            case COLLECTING -> r.nextInt(30) == 0 ? new ItemStack(Items.EMERALD)
                    : new ItemStack(pick(r, Items.FLINT, Items.AMETHYST_SHARD, Items.QUARTZ, Items.CLAY_BALL, Items.PRISMARINE_SHARD, Items.FEATHER, Items.GLOW_INK_SAC), 1 + r.nextInt(3));
            case PAINTING -> r.nextInt(3) == 0 ? portrait(level, girl, r) : new ItemStack(pick(r, Items.PAPER, Items.YELLOW_DYE, Items.CYAN_DYE, Items.PINK_DYE), 1 + r.nextInt(2));
            case KNITTING -> {
                if (r.nextInt(4) == 0) {                                      // a knitted cap, in a colour she picked
                    ItemStack cap = new ItemStack(Items.LEATHER_HELMET);
                    cap.set(DataComponents.DYED_COLOR, new DyedItemColor(DyeColor.values()[r.nextInt(16)].getTextureDiffuseColor() & 0xFFFFFF, true));
                    cap.set(DataComponents.CUSTOM_NAME, Component.translatable("item.monstergirls.knitted_cap", girl.getDisplayName()));
                    yield cap;
                }
                yield new ItemStack(pick(r, Items.WHITE_WOOL, Items.PINK_WOOL, Items.LIGHT_BLUE_WOOL, Items.YELLOW_WOOL, Items.LIME_WOOL, Items.MAGENTA_WOOL), 2 + r.nextInt(2));
            }
        };
    }

    /** A painting she made: a portrait of her own kind if there is one (tools/gen_portraits.py), else of one of the others. */
    public static ItemStack portrait(ServerLevel level, Mob girl, RandomSource r) {
        var reg = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.PAINTING_VARIANT);
        net.minecraft.resources.ResourceLocation own = com.linkyoshi90.monstergirls.MonsterGirlsMod.id("portrait_"
                + net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(girl.getType()).getPath());
        net.minecraft.resources.ResourceLocation pick = reg.containsKey(own) ? own : null;
        if (pick == null) {
            java.util.List<net.minecraft.resources.ResourceLocation> ours = reg.keySet().stream()
                    .filter(k -> k.getNamespace().equals(com.linkyoshi90.monstergirls.MonsterGirlsMod.MODID)).sorted().toList();
            if (ours.isEmpty()) return new ItemStack(Items.PAINTING);
            pick = ours.get(r.nextInt(ours.size()));
        }
        ItemStack painting = new ItemStack(Items.PAINTING);
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putString("id", "minecraft:painting");
        data.putString("variant", pick.toString());
        painting.set(DataComponents.ENTITY_DATA, net.minecraft.world.item.component.CustomData.of(data));
        return painting;
    }

    private static Item pick(RandomSource r, Item... items) {
        return items[r.nextInt(items.length)];
    }

    /** A gardener tends a crop near her: the first unripe one within {@link #TEND} grows a stage. True if one did. */
    public static boolean tend(ServerLevel level, Mob girl) {
        BlockPos at = girl.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-TEND, -1, -TEND), at.offset(TEND, 1, TEND))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof CropBlock crop && !crop.isMaxAge(s)) {
                level.setBlock(p, crop.getStateForAge(Math.min(crop.getMaxAge(), crop.getAge(s) + 1)), 2);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 3, 0.25, 0.2, 0.25, 0.0);
                return true;
            }
        }
        return false;
    }

    /** What the hobby gives someone besides anything she made: a reader shares what she learned, a musician's tune lifts everyone. */
    private static void extras(ServerLevel level, Mob girl, @Nullable Player to) {
        switch (hobby(girl)) {
            case READING -> {
                if (to != null) ExperienceOrb.award(level, to.position(), 12);
                else ExperienceOrb.award(level, girl.position(), 12);
            }
            case MUSIC -> {
                for (Player p : level.getEntitiesOfClass(Player.class, girl.getBoundingBox().inflate(16))) {
                    p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2400, 0));
                }
                level.sendParticles(ParticleTypes.NOTE, girl.getX(), girl.getEyeY() + 0.5, girl.getZ(), 10, 0.6, 0.3, 0.6, 1.0);
            }
            default -> {
            }
        }
    }

    /** She's done for today: if a friend is about, she leaves what she made at her feet. What she made (perhaps empty). */
    public static ItemStack finish(ServerLevel level, Mob girl) {
        girl.getPersistentData().putLong(DONE_DAY, dayMark(level));
        Player friend = friendNear(level, girl, SEEN);
        if (friend == null) return ItemStack.EMPTY;
        ItemStack made = make(level, girl, level.random);
        if (!made.isEmpty()) {
            ItemEntity drop = new ItemEntity(level, girl.getX(), girl.getY() + 0.5, girl.getZ(), made.copy());
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }
        extras(level, girl, null);
        level.playSound(null, girl.blockPosition(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.NEUTRAL, 0.6F, 1.3F);
        friend.displayClientMessage(Component.translatable("message.monstergirls.hobby.done", girl.getDisplayName(),
                Component.translatable("hobby.monstergirls." + hobby(girl).key())).withStyle(ChatFormatting.GREEN), true);
        return made;
    }

    /** Kept her company long enough: she shows them how she does it. True if she did (once a day). */
    public static boolean share(ServerLevel level, Mob girl, Player player) {
        if (!(girl instanceof Bonded b) || sharedToday(level, girl) || b.getAffection(player) < HomeLife.FRIEND) return false;
        girl.getPersistentData().putLong(SHARED_DAY, dayMark(level));
        b.addAffection(player, AFFECTION);
        ItemStack made = make(level, girl, level.random);
        if (!made.isEmpty() && !player.addItem(made)) player.drop(made, false);
        extras(level, girl, player);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.hobby.shared." + hobby(girl).key(), girl.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        Dairy.award(player, "shared_interests");
        return true;
    }

    /** {@code ticks} more of her hour: she works at it, a gardener tends what grows round her, and company adds up. */
    public static void practise(ServerLevel level, Mob girl, int ticks) {
        CompoundTag t = girl.getPersistentData();
        long day = dayMark(level);
        if (t.getLong(WORK_DAY) != day) {
            t.putLong(WORK_DAY, day);
            t.putInt(WORK_TIME, 0);
            t.remove(WITH);
            t.putInt(WITH_TIME, 0);
        }
        Hobby h = hobby(girl);
        if (level.random.nextInt(2) == 0) level.sendParticles(h.particle, girl.getX(), girl.getY() + girl.getBbHeight() * 0.7, girl.getZ(), 2, 0.3, 0.2, 0.3, 0.02);
        if (level.random.nextInt(4) == 0) level.playSound(null, girl.blockPosition(), h.sound, SoundSource.NEUTRAL, 0.4F, 0.9F + level.random.nextFloat() * 0.3F);
        if (h == Hobby.GARDENING && level.random.nextInt(3) == 0) tend(level, girl);
        if (!doneToday(level, girl)) {
            t.putInt(WORK_TIME, work(girl) + ticks);
            if (work(girl) >= WORK) finish(level, girl);
        }
        if (sharedToday(level, girl)) return;
        Player near = friendNear(level, girl, NEAR);
        if (near == null) return;
        if (!t.hasUUID(WITH) || !t.getUUID(WITH).equals(near.getUUID())) {
            t.putUUID(WITH, near.getUUID());
            t.putInt(WITH_TIME, 0);
            near.displayClientMessage(Component.translatable("message.monstergirls.hobby.invite", girl.getDisplayName(),
                    Component.translatable("hobby.monstergirls." + h.key())).withStyle(ChatFormatting.GRAY), true);
        }
        t.putInt(WITH_TIME, t.getInt(WITH_TIME) + ticks);
        girl.getLookControl().setLookAt(near);
        if (t.getInt(WITH_TIME) >= COMPANY) share(level, girl, near);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob girl && girl.tickCount % 20 == 11 && girl.level() instanceof ServerLevel level && girl instanceof Bonded
                && herHour(girl, level.getDayTime()) && canPractise(girl)) {
            practise(level, girl, 20);
        }
    }
}
