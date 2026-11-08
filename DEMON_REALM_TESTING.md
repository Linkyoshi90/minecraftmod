# Demon Realm and monsterization: what to test

Everything below passes the GameTests, but none of it has been seen in a real client. These are the things only a playtest can judge.

## Getting there fast

| Command | What it does |
|---|---|
| `/monstergirls locate realm` | Nearest wild realm patch, with clickable coordinates. Only finds patches in chunks generated after this update. |
| `/locate structure monstergirls:realm_patch` | The same, the vanilla way. |
| `/place feature monstergirls:realm_patch` | A patch right where you stand. |
| `/monstergirls realm list` | The possessed and the realm-changed creatures within 48 blocks. |
| `/monstergirls realm possess` | (op) Possesses the villager, wandering trader, pig, cat or chicken you look at. |
| `/monstergirls realm turn` | (op) Changes the creature you look at into her girl form on the spot. |
| `/monstergirls realm summon` | (op) A succubus or demon steps out of the haze at your feet. |
| `/gamerule randomTickSpeed 60` | Fast-forwards the realm's spread, trees turning, weepcaps (set back to 3 after). |
| `/time set 18000` | Night: possession, summons, spreading villages and the sabbath only happen in the dark. |

## Look at these in a client

**Possessed mobs (villager, wandering trader, pig, cat, chicken)**
- Glowing violet eyes: do they sit exactly on the vanilla eyes, or one pixel off? Do a villager's profession hat or a cat's collar hide them?
- Two small dark horns on the head: right size, right place, do they follow the head as it turns and nods? (Pig, cat and chicken heads are found by their vanilla field name; if a horn layer is missing, that lookup failed.)
- A low, changed call now and then, with haze.

**Holstaur** (a cow on realm grass in the dark, or `/monstergirls realm turn` on a cow)
- The new "plush" build: full chest, soft belly, wide hips, thick thighs. Does it read the way you wanted?
- Glass bottle on her: three holstaur milk. Drinking it: Strength.

**Realm wood**
- Planks, stairs, slab, fence, fence gate, door, trapdoor, button, pressure plate in dark purple. Doors and trapdoors open/close and look right from both sides.

**Realm censer**
- Fed a heartfruit it lights (violet, light 10, haze). Girls within 8 blocks flirt more. It burns for a day and goes out.
- Note: its light (10) turns realm grass back nearby, so it is for homes, not the realm itself.

**Shrines and residents in wild patches**
- One patch in four has a blackstone shrine: chest, four candle pillars. One in three has two possessed pigs or chickens.

**Splash holy water** (holy water + gunpowder)
- Thrown, it shows as a bottle in flight, breaks with a splash, frees the possessed within 4 blocks and turns the realm back.

**Holy font** (calcite, holy water, gold)
- A calcite basin of pale ice-blue water with rising sparks. Within 12 blocks: no realm grass takes hold, nobody is possessed, passes it on or turns.

**Realm silver** (iron ingot, 4 amethyst shards, heartfruit; then a sword like an iron one)
- Hit a monster girl, harpy or frog girl: no damage, she stops attacking, blushes, +1 friendship (once per 5 seconds). Anything else takes iron-sword damage. Try it on a wild succubus.
- Realm silver armour (ingots, the usual shapes): does it look right worn? Two pieces: girls' blows hurt half. All four: wild girls who aren't your friends won't target you.

## Behaviour worth watching over a few nights

- A possessed villager passes it to a villager standing next to her at night (about one night in four), little patches of realm take root under them, and an iron golem among three of them turns into a golem girl. Is the pace right, too slow, too fast? (Option `possessionSpreads`.)
- Possessed animals (pig, cat, chicken) breed with their own kind every half day; the young are girls (piglettes, nekomata, poultry harpies). Heartfruit lures them and readies them at once.
- Possessed villagers and wandering traders no longer breed with each other: when you sleep, the nearest one within 24 blocks sets out for your bed (her own brain may still pull her home; she counts if she ends the night within 12 blocks). The morning message says she slipped into your bed; a day later she has a little succubus or demon, devoted to you. She walks over if she can; if she is still not there after you have lain down three seconds, she slips in through the haze beside the bed.
- Give a possessed villager a heartfruit and she seeks your bed that night from up to 48 blocks, ahead of nearer ones. After she bears your young her prices for you drop (village gossip).
- Either way it stops while 6 girls live within 32 blocks.
- Under a full moon the realm's own (devils and every girl it changed) dance in it, and the realm spreads fast.
- Deep in a realm at night a wild succubus or demon now and then steps out (never two near each other). (Option `realmSummons`.)

## Wandering traders

- One in eight arrives already possessed (glowing eyes, horns), selling heartfruit, weepcaps, a realm censer and a **Realm Explorer Map** to the nearest wild realm patch (8 emeralds and a compass).

## Rose moon

- About one night in ten (fixed by seed and day) the moon rises rose-violet: a chat line at moonrise and a rose tint to the night fog. That night monsterization runs twice as fast, and possession spreading, summons and the realm's spread four times as often. Hard to wait for: `/time set` to nights on different days until the message appears. Is the tint pretty or too much?

## Options

`demonRealm`, `realmFog`, `monsterization`, `possession`, `possessionSpreads`, `realmSummons`, `roseMoon` in the mod's settings.

## Advancements

Under Adventure: Into the Demon Realm, Spirit, Not Flesh, A Change of Heart, Born of the Realm, Exorcist, Lovestruck, Sabbath, Home Fires, Strong Stuff, The Realm's Gifts.
