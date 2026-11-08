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
| `/monstergirls realm rosemoon` | When the next rose moon rises, with a clickable `/time set` to get there. |
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

- About one night in ten (fixed by seed and day) the moon rises rose-violet: a chat line at moonrise and a rose tint to the night fog. That night monsterization runs twice as fast, and possession spreading, summons and the realm's spread four times as often. `/monstergirls realm rosemoon` names the next one and gives a click-to-go `/time set`. Is the tint pretty or too much?

## Options

`demonRealm`, `realmFog`, `monsterization`, `possession`, `possessionSpreads`, `realmSummons`, `roseMoon` in the mod's settings.

## Advancements

Under Adventure: Into the Demon Realm, Spirit, Not Flesh, A Change of Heart, Born of the Realm, Exorcist, Lovestruck, Sabbath, Home Fires, Strong Stuff, The Realm's Gifts.

## The Order (2026-11-08)

The realm's enemy. Sisters of the Order are human women on the vanilla slim player model with their own skins (`tools/gen_order_assets.py`).

| Command | What it does |
|---|---|
| `/monstergirls order` | How the Order sees you (notoriety), and any crusade under way. |
| `/monstergirls order set <0-200>` | (op) Set your notoriety: 40 = marked, 100 = a crusade comes the next night you are near your hearth (13000-19000). |
| `/monstergirls order patrol` | (op) A patrol (two knights, an inquisitrix) sets out for the nearest realm patch, or for you. |
| `/monstergirls order raid` | (op) A crusade sets out for your nearest hearth right now. |

**Look at these in a client**
- The three skins (sister-knight in white and gold over mail, inquisitrix in a navy hood and robe, blonde captain with a sky-blue border). Do the faces read? Does iron armour on the slim model look right?
- The captain wears the Order's banner on her helm (like a raid captain). Does it sit right?
- A kneeling sister crouches: does the pose look like kneeling, or just sneaking?
- An inquisitrix throws splash holy water: the flask flies and breaks; girls in the splash take 4 damage, players only get wet.
- The crusade boss bar ("Crusade of the Order", yellow, notched) counts the sisters still standing.

**Behaviour worth watching**
- Patrols: by day, one try every 30 s per player, 1 in 6, only with realm grass within 40 blocks (or anywhere once you're marked), never with sisters already within 96. They walk to the realm, turn it back, free the possessed, fight devils and realm-changed girls. Ordinary wild girls are left alone. Too frequent? Too rare?
- Notoriety: each minute +1 in the realm's air, +1 per devil or realm-changed friend within 32 (up to 3); -1 a minute with no ties. +2 per heartfruit given, +5 per sister killed, +10 per sister fallen; -20 sparing one, -5 freeing the possessed with splash holy water. Is 100 the right distance to a crusade?
- Crusade: captain + 2 knights + inquisitrix, +1 per 3 residents (max 7). Won: residents +3 friendship, notoriety back to 0. Dawn with sisters standing: they vanish, bless 5 blocks round the hearth, residents -3, notoriety 50.
- Realm silver sword: each hit takes a point of faith (knight 6, inquisitrix 5, captain 10), no damage. The realm's air in the dark also wears 1 every 10 s. Kneeling: heartfruit = she falls (succubus; captain = demon) devoted to you (60 friendship); empty hand = spared, she walks off. Left kneeling in the realm's dark for a minute, she falls by herself.

Options: `orderPatrols`, `orderRaids`. Advancements: Holy Orders, Heretic, Crisis of Faith, Mercy, Fallen, Crusade Repelled.

## Realm-touched players and Order chapels (2026-11-08)

| Command | What it does |
|---|---|
| `/monstergirls realm touch` | How far the realm has touched you (0-100). |
| `/monstergirls realm touch set <n>` | (op) 25 touched, 50 kindred, 75 realm-born. |
| `/monstergirls locate chapel` | The nearest chapel of the Order (new chunks only). |

- **Touch** grows +1 a minute in the realm's air, +4 per heartfruit you eat, +3 waking beside a devil or realm-changed girl, +5 per sister you make fall. Splash holy water breaking within 4 blocks of you: -15. Each minute near a holy font: -1. Is the pace right?
- **Touched**: night vision in the realm's air at night, and its haze rises from you. **Kindred**: wild devils and realm-changed girls don't pick a fight with you (unless you hit them first), +0.15 bed odds with them, and the Order counts it as a tie. **Realm-born**: no hunger in its air, Strength I at its night.
- **Chapels**: a 7x9 calcite hall with quartz pillars, sky-blue windows, gold trim, a bell over the door, spruce pews, lanterns, a holy font (wards 12 blocks from the realm), the Order's banner on the back wall, a chest (holy water, splash holy water, bread, gold, candles, a rare golden apple), and one sister-knight who keeps it. Plains, meadows, forests, cherry groves, savannas; about one per 36 chunks. Does it sit well on uneven ground (the foundation goes down up to 6 blocks)? Does the banner face the right way?

Options: `realmTouched` (chapels follow `orderPatrols`). Advancements: Kindred, Of the Realm.

## A Lilith's manor; tithes (2026-11-08)

- `/monstergirls locate manor` (or `/locate structure monstergirls:realm_manor`): dark forests only, about one per 48 chunks, new chunks only. A 13x11 hall of realm planks on polished blackstone, violet windows, a stepped roof, a purple runner to a realm-stair throne before crying obsidian, purple candles, two unlit realm censers (a heartfruit lights them), a purple bed and a chest (heartfruit, weepcaps, realm silver, amethyst, gold, rarely a censer, a realm silver sword or an enchanted book). Realm grass round its walls. Its lady is a wild grown Lilith (who spreads the realm, as wild ones do) with one or two succubi, kept within 14 blocks. Look: does the roof read well? Does the door face out? Do the girls stay home after a reload? (They should keep within 14 blocks until one befriends you; then she's free to follow.)
- Tithe: right-click a holy font with a gold ingot: -10 notoriety per ingot; refused (gold kept) at 0. Advancements: An Audience, Indulgence.

## Hamlets, villages and passing flocks (2026-11-08)

Not realm features, but new since the last playtest.

- **Hamlet** (3 residents at your hearth): next morning it takes a name (e.g. Willowmere) and raises a patterned banner beside the hearth (mobGriefing on). Walking within 32 blocks shows "Willowmere, a hamlet of 4" in the action bar; right-clicking the hearth shows it too. A renamed name tag used on the hearth (owner only) renames the place.
- **Village** (6 residents): every 5th day (day % 5 == 2), on a morning you're within 64 blocks, a grown girl of a family not living there yet arrives, already a friend, and settles (up to 12). Too often? Too rare?
- **Passing flocks**: every 8th day (day % 8 == 3), in the first 3000 ticks of the morning, if you're under open sky and not in the realm: 3-5 wild harpies (swan/duck/pelican/flamingo if there's water within 24 blocks, else sparrow/finch/crow) land 20-36 blocks away. A day later they vanish in a puff of feathers, except any who has made a friend. `/time set` to a flock morning to try it.

Options: `hamlets`, `passingFlocks`. Advancements: A Name on the Map, Word Gets Around.

## Villager sweethearts and grandchildren (2026-11-08)

- A grown daughter of yours (born to you, full size, not a construct) on a morning near free villagers has a 1 in 4 chance to take a shine to one. Or introduce them: right-click a villager with any small flower while your unattached daughter is within 16 blocks of him (the flower is used up, no trade screen).
- By day (2000-11000) she walks over to see him from up to 24 blocks; close by they look at each other, a heart now and then, and she blushes. Does this fight her other jobs (farmhand, guard) too much?
- Three mornings within 32 blocks of each other and they're wed: a bell, a burst of hearts, and the villager gives you major positive gossip (kinder prices).
- Wed, every 4th day (day % 4 == 0) near him, if she can conceive: a little one on the way (two at most per daughter). The baby is born devoted to you, like a daughter (never in your bed, no lucky moments, no flirting). You get a chat line when she's born (or the eggs are laid).
- If her villager dies she grieves (chat line) and may court again later. `/monstergirls bonds` shows who's courting or wed and how many little ones.

Option: `sweethearts`. Advancements: Sweet on Someone, Wedding Bells, Grandparent.

## Dates (2026-11-08)

- Sneak and right-click a close friend (affection 25+, grown, full size, not your daughter, not a construct) with any small flower. She takes it, blushes and names her kind of place, always the same one for her: lots of flowers (6+ within 6 blocks), the water's edge (water within 4, open sky), a high place (40+ above sea level, open sky) or the night sky (13000-23000, clear, overworld).
- She keeps within 4 blocks of you. 30 seconds together at her kind of place: +8 affection, Regeneration 30 s, Luck 5 min, a closing line. After 10 minutes without it she sighs and calls it a day. One date a day each.
- Does following you fight her sit order or her job too much? Is half a minute too short?

Option: `dates`. Advancement: It's a Date.

## Weather spirits (2026-11-08)

New girls: raiju (canid), ame-onna (undead), sylph (elf). Spawn eggs work any time; natural spawns only in their weather (`/weather thunder`, `/weather rain`).

- **Raiju**: plains, savanna, meadows, thunderstorms only. Lightning never touches her. Befriended (affection 25+), in a storm every 10 s a visual bolt hits the nearest monster within 12 blocks of you (8 damage, fire). Does the yellow-and-black barring read as lightning?
- **Ame-onna**: woods, swamps, rivers, cherry groves, rain only. Befriended, in the rain the crops within 6 blocks of her grow a stage every 30 s. Right-click her with a water bottle: a 5-minute shower (once a day; refused if it's already raining).
- **Sylph**: windswept hills and meadows, any rain or storm. She flies. Befriended, you near her under the open sky get Speed I and, falling more than 3 blocks, Slow Falling. Are her fairy wings visible enough?
- A wild one that spawned naturally leaves in a puff of cloud when her weather clears; befriend her first and she stays.

## The Demon's spire (2026-11-08)

- `/monstergirls locate spire` (or `/locate structure monstergirls:demon_spire`): badlands, wooded badlands, eroded badlands; about one per 56 chunks; new chunks only.
- A 15x15 tower of polished blackstone, realm log corners, violet windows, four storeys of 7 and a battlemented roof (about 32 blocks tall). A ladder climbs the back wall (east of the middle) through every floor.
  - Hall: realm door, purple runner, candles, two succubi kept to the spire.
  - Library: bookshelves on every wall, an enchanting table, a lectern, a chest (enchanted books, XP bottles, lapis, heartfruit).
  - Bedchamber: a purple bed, two unlit censers, crying obsidian.
  - Treasury: gold blocks, amethyst, a chest (gold, diamonds, emeralds, realm silver, golden apples, rarely an enchanted golden apple or netherite scrap).
  - Roof: a throne before crying obsidian, and a grown wild Demon on it.
- Advancements: The Spire (step in), Before the Throne (reach the roof). Look: does the foundation reach down far enough on badlands slopes (8 blocks)? Can you get off the ladder onto each floor? Does the Demon stay on her roof after a reload?

## Realm variants (2026-11-08)

The realm takes on the land it spreads into (spread, the worldgen realm patches, and a wild Lilith's growth all use it):
- **Frost Realm**: on grass or dirt where it's cold enough to snow. Pale lilac turf rimed with frost. Weepcaps, no heartfruit.
- **Bog Realm**: on mud, or any ground in a swamp or mangrove swamp. Dark wet violet over mud. Weepcaps sprout twice as often, no heartfruit. Drops mud.
- **Realm Sand**: on sand and red sand. Rose-violet with pink glints; falls like sand; cacti and sugar cane still grow on it (it's in the sand tag). Heartfruit, no weepcaps or crimson roots. Purified it's sand again (red sand in the badlands).
- Everything else about the realm (realm air, torchlight, holy water, holy fonts, the Order's purifying, depth limit, summons) works on all four.

Look: is the frost turf readable as "realm" and not just snow? Is the bog too dark at night? Try a wild Lilith egg in a snowy plains, a swamp and a desert.
