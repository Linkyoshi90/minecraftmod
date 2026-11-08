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

## Hot springs and harpy sky colonies (2026-11-08)

- **Hot springs**: rounded pools (radius 3-4, two deep) floored with geyserite, rimmed with calcite and tuff, in meadows, groves, snowy slopes/plains/taiga, windswept hills, cherry groves, old spruce taiga, ice spikes (1 in 32 chunks, level ground only). Water over geyserite (up to 3 above) steams and never freezes. Standing in it: Regeneration, frost thawed; 60 s of soaking leaves you "well soaked" (Health Boost II, 5 min, once a day). Friends within 12 come and soak (towel and blush for close friends, as in the tub); a wild girl in the same spring warms to you. A girl's cold is cured, and nobody catches a chill in a hot spring. Build your own: 4 calcite + a magma block = 4 geyserite. Option `hotSprings`; advancement Into the Steam. Does the steam look right? Is the pool too small?
- **Harpy aeries** (`/monstergirls locate aerie`): windswept hills/forest/gravelly hills, stony shores, jagged/stony/frozen peaks; about 1 per 36 chunks. 3-4 stone crags, each topped with a hay nest; the tallest (7-9 high) holds the matriarch and a clutch of two eggs. Kinds: gulls/albatrosses on shores, condorunes/tengu on peaks, crows/owls on windswept hills. The matriarch is named "<Kind> Matriarch". Befriend her (affection 25+) and every colony member within 48 becomes your friend. Advancement Welcome to the Flock.
- **Harpy perch** (feather, hay bale, feather over two stripped spruce logs): a passing flock (every 8th day, `entity/Flocks`) within 32 blocks of a perch lands round it and stays for good, the first as matriarch. Advancement Come Home to Roost.

## Merfolk grottoes and fallen chapels (2026-11-08)

- **Merfolk grotto** (`/monstergirls locate grotto`): warm, lukewarm and deep lukewarm oceans with at least ~12 blocks of water; about 1 per 40 chunks. A prismarine dome (radius 6) with dark prismarine flecks, sea lanterns, four arched doorways, a sandy floor with sea pickles, coral round its foot and fans on the inner walls, and a waterlogged chest in the middle (prismarine, nautilus shells, gold, emeralds, fish, rarely a trident, a turtle scute, an enchanted book; 1 in 5 a heart of the sea). A queen (dolphin, reeffin or koi mermaid, named "<Kind> Queen") and three of the reef's girls (seahorse, pufferfish, octopus, cuttlefish, nautilus, anemone) keep to it. Befriend the queen and her court befriends you. Advancements Under the Sea, The Queen's Court. Does the dome sit right on uneven sea floor?
- **Fallen chapel**: at night, with at least two of the realm's own (devil family or realm-changed girls) among your friends within 16, use a heartfruit on an Order chapel's holy font. The bell tolls, an inquisitrix and a knight join the keeper, and your notoriety rises to at least 40 (marked). Make every defender kneel, fall or die before dawn or 3 minutes; stay within 48. The chapel turns: calcite to realm planks, quartz to blackstone and realm logs, glass violet, lanterns soul lanterns, candles purple, the banner purple, the font a realm censer, and realm grass round it. Lose (time, dawn, walked off) and nothing changes. The raid is kept in memory only. Advancement Sabbath at the Altar.

## Market stalls and courier girls (2026-11-08)

- **Market stall** (red wool, white wool, red wool / stick _ stick / spruce planks, chest, spruce planks): within 16 blocks of a hearth with 6+ residents. Each morning (first 2000 ticks): half the goods on it (every other stack) sell, 4 items to the emerald, at least 1 a stack; then each grown resident within 48 of the hearth adds something: farmhand 6 wheat/carrots/potatoes, cook 3 bread/baked potatoes/pumpkin pies, guard 8 arrows or iron nuggets, anyone else her usual present (only while the hearth's owner is online). Open it (3x3) to take the emeralds. Advancement Open for Business. Too generous? Too slow?
- **Mailbox** (spruce planks, feather, spruce planks / planks, chest, planks / _ stick _): put things in; a harpy or centaur friend (25+, grown, not sitting) living within 32 picks them up every 5 s and delivers them after a second per 12 blocks (at least 5 s). Feather on mailbox A, then on mailbox B: linked both ways (mail goes to the other box). Feather on the same box twice: unlinked (mail goes to the owner's inventory, wherever they are in the dimension). Parcels wait for an unloaded box or an offline owner. Advancement Special Delivery. The courier doesn't visibly fly off: is that okay, or should she?

## Aurora nights (2026-11-08)

- One night in six (scattered, not every 6th), clear weather only, overworld, for each player standing where it's cold enough to snow under the open sky: curtains of green/teal/violet dust particles 40-56 blocks overhead, every half second. `/time set 18000` on a cold, clear night and try a few nights (`/time add 24000`). Does it look like an aurora from the ground? Too many particles?
- The first time that night: a chat line, a chime, two frost-touched wild girls (yuki-onna, snow elf, frost slime, snow leopard, arctic fox, polar bear) appear 16-32 blocks away, and three glowing aurora shards drop 4-20 blocks away. At dawn the frost-touched leave in a puff of snow, unless they've made a friend.
- An aurora shard given (right-click) to any grown girl, harpy or frog: +6 affection and a blush. Option `auroraNights`; advancement Northern Lights.

## Scout maps and wayside inns (2026-11-08)

- **Scout maps**: a girl back from an expedition brings a filled map one time in four (a scout: one in two), with a red X over the nearest unexplored chapel, manor, spire, aerie or grotto within 50 chunks (merfolk, mollusks and crustaceans: grottoes only; devils and realm-changed girls: manors and spires). Named "Map to a ...". Advancement X Marks the Spot. Watch for a lag spike when one is drawn (it searches for structures).
- **Wayside inn** (`/monstergirls locate inn`): plains, sunflower plains, meadows, savannas, forests, flower forests; about 1 per 44 chunks. An 11x9 oak and cobblestone common room under a pitched spruce roof: a barrel counter (one barrel holds the larder), two tables with stools, a campfire in a cobblestone hearth, two red beds, lanterns. A holstaur innkeeper keeps to it (befriended or not). Every morning one or two travelling girls of families not living nearby arrive; a day later they move on, unless one has made a friend. An emerald to the innkeeper: rabbit stew, two bread, Regeneration; one time in three a traveller's map too. Advancement A Hot Meal. Is the roof right?

## Messages in bottles (2026-11-08)

- Standing on a beach (biome tag is_beach) by day, each player gets one roll a day (checked every 30 s, the first check of the day counts): 1 in 3 a glowing Message in a Bottle washes up within 12 blocks on sand or gravel next to water.
- Use it: the glass bottle is kept, and inside is a map to an unexplored structure (half the time; falls back to treasure if there's none in reach), up to three shipwreck-treasure items (3 in 10), or a letter (2 in 10) from a selkie, dolphin, seal, lorelei, koi or cuttlefish girl, who appears at the water's edge with 10 affection. Advancement Washed Ashore.

## Couriers in flight, kitsune shrines, labyrinths (2026-11-08)

- **Couriers** now take off: when one picks up a parcel she disappears in a puff of cloud (invisible, still, unhurtable where she stood) for the round trip (twice the one-way time), then lands again on the spot. She can't be picked for another parcel while she's away.
- **Kitsune shrine** (`/monstergirls locate shrine`): cherry groves, bamboo jungles, birch and old birch forests; about 1 per 36 chunks. A gravel path under three red-terracotta torii with blackstone lintels, stone lanterns, a raised dark-oak hall with a bell, two lanterns and an offering box; a red kitsune shrine maiden keeps it until befriended. Any food in the offering box draws a fortune once a day: great blessing (15 %: Luck II 20 min, Regeneration), blessing (35 %: Luck 10 min), small blessing (35 %: Speed 5 min) or curse (15 %: Bad Luck 5 min), and a named Fortune Slip (paper). The offering box is craftable (red wool over dark oak planks and a trapdoor). Advancements Fortune Slip, Great Blessing. Does the roof read right? The hall has no overhang at the back (the chunk ends there).
- **Labyrinth** (`/monstergirls locate labyrinth`): savannas, plains, deserts; about 1 per 48 chunks. A ring of mossy and chiseled stone at the surface over a ladder shaft 12 down into a 15x15 maze (7x7 cells, a new layout each time), stone/mossy/cracked brick walls 3 high, a few soul lanterns; at the heart a 3x3 room with a chest (gold, iron, string, saddle, golden horse armour, diamonds, golden apple, enchanted book, wheat, milk) and a Minoan minotaur guard, who attacks strangers on her ground. Advancements Into the Maze, Thread the Maze. Is it too dark? Is the ladder easy to get off at the top?

## Oases, sky islands, silk dens (2026-11-08)

All three are small code-built places sharing one piece type (`world/OneChunkStructure`); each awards an advancement when you step into it.
- **Oasis** (`/monstergirls locate oasis`, deserts, ~1 per 40 chunks): a pool (radius 3, 2 deep in the middle) ringed by grass, sugar cane and two or three palms (jungle logs and leaves, cocoa), and the broken sandstone pillars of a shrine with an altar chest (gold, lapis, emeralds, melon, sugar cane, clock, compass, rarely a diamond or golden apple). A sphinx keeps it: her riddles work as before (right-click, answer with the item). Advancement A Drink in the Desert.
- **Sky island** (`/monstergirls locate island`, windswept hills/forest, meadows, cherry groves, sunflower plains; ~1 per 48 chunks): about 45 blocks above the ground, a 13-wide island of grass over a stone cone (some coal and iron ore), a birch, flowers, a woolly cloud at its edge, vines trailing from the rim, a chest (feathers, phantom membrane, fireworks, amethyst, rarely an elytra). A sylph keeps to it. Getting up is up to you. Advancement Head in the Clouds.
- **Silk den** (`/monstergirls locate den`, dark forests, ~1 per 36 chunks): a podzol hollow ringed by six dead trees (stripped dark oak) with cobwebs strung between them high up, cobwebs underfoot, white-wool cocoon pillars, a bone block, and a chest of four silk cocoons (each holding a diamond, gold, emeralds, an ender pearl, a name tag or spider eyes) and string. A jorogumo keeps it. Advancement Into the Web.

## Dragon lairs, centaur camps, graveyards (2026-11-08)

- **Dragon's lair** (`/monstergirls locate lair`, windswept hills/gravelly hills, stony and jagged peaks, groves; ~1 per 48 chunks): a mound of stone, basalt and tuff (radius 7), hollow inside (radius 5), a tunnel in from the north, a smooth basalt floor scattered with gold and raw gold blocks, amethyst on the walls, and a chest of treasure (gold, raw gold, emeralds, diamonds, golden gear, rarely an enchanted golden apple). A wyvern or wyrm lives there: she takes that chest as her hoard and keeps to the lair until befriended. Advancement Here Be Dragons. Does the mound sit well on mountain slopes?
- **Centaur camp** (`/monstergirls locate camp`, plains, sunflower plains, savannas; ~1 per 40 chunks): three wool A-frame tents, a campfire with log benches, hay bales and a chest of supplies (bread, carrots, arrows, a bow, leather, saddle, leads, golden carrots). Three horse centaurs (on the savanna: one horse, two zebras) keep to it. Advancement Round the Campfire.
- **Old graveyard** (`/monstergirls locate graveyard`, forests, taigas, plains, dark forests; ~1 per 40 chunks): a 13x13 plot fenced in dark oak with a gate gap, eight mossy headstones over coarse-dirt mounds (some with lit candles), a gravel path, a dead tree, and a small stone crypt with a chest (bones, candles, gold nuggets, an enchanted book, a soul lantern, rarely music disc 13). A yurei and a will-o'-wisp keep to it. Advancement Here Lies...

## Races and treasure hunts (2026-11-08)

- **Race**: right-click a friend (25+, grown, not sitting) holding any banner while looking at a block 8-48 blocks away: green sparkles mark the finish, a 3-second count shows on the action bar, then she runs for it (speed 1.6) and so do you. First within 2 blocks (horizontally) wins; 60 seconds' limit. You win: she drops one of her presents. She wins: a victory hop. Either way +3 affection. Advancements On Your Marks, Photo Finish. Do girls get distracted by their own goals mid-race (she re-aims every second)?
- **Treasure hunt**: right-click a friend with any shovel (not sneaking; sneaking an alraune still digs her up). She buries one of her presents (1 in 4: an emerald or two) 6-12 blocks away under suspicious sand (in sand) or suspicious gravel (anywhere else). Every second: "Warmer!" / "Colder..." / "Hot!" on the action bar. Brush it out: +3 affection. After 5 minutes she shows you the spot. Advancement Warmer, Warmer...

## Hobbies (2026-11-08)

- Every grown girl (harpies and frogs too) has a hobby, always the same for her: gardening, fishing, reading, music, baking, collecting or knitting. Her friends see it in Jade ("Hobby: baking").
- Each has her own hour of the day for it, starting somewhere between day time 1000 and 9000 and lasting 1000 ticks. During it she gives off her hobby's particles and an occasional quiet sound wherever she is. A gardener also grows the crops within 3 blocks a stage now and then.
- After 30 seconds of her hour, if a friend is within 32 she leaves what she made at her feet, once a day:
  - gardening: flowers or seeds
  - fishing: cod, salmon or tropical fish, rarely a nautilus shell (only with water within 6)
  - baking: bread, cookies, pumpkin pie or now and then a cake
  - collecting: flint, amethyst, quartz, clay, prismarine, feathers, glow ink, rarely an emerald
  - knitting: wool, or 1 in 4 a dyed leather cap named "Cap knitted by ..."
  - reading: 12 XP
  - music: Haste for 2 minutes to everyone within 16
- Stay within 4 blocks of a friend for 30 seconds of her hour: she shows you how (+4 affection, and the same kind of thing for you), once a day. Advancement Shared Interests.
- Things to watch: is it too busy with many girls about (particles, sounds)? Does she wander off mid-hobby? She doesn't stand still for it.
