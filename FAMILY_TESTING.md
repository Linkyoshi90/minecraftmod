# Many spouses, one family: what to test

Built on 2026-11-09 while you were at work. Everything below passes the GameTests (963), but none of it has been seen in a real client. Polygamy is jealousy-free by design: the wives are glad of each other and everything is between you and each of them.

## Getting there fast

| Command | What it does |
|---|---|
| `/monstergirls family` | Your wives (where they are, days to each anniversary) and your little ones (little, grown or their calling). |
| `/monstergirls family wed` | (op) The girl you look at adores you, says yes and is wed: by a hearth within 8 blocks if there is one (the full ceremony), else on the spot. |
| `/monstergirls family due` | (op) The girl you look at is expecting (by you) and gives birth within two seconds. |
| `/monstergirls family grow` | (op) The little one you look at is a quarter of the way more grown (try it three times to see the growing-up moments, a fourth to see her come of age). |
| `/time add 40d` | Forty days on: the next wedding anniversary (and three days for the end of a honeymoon: `/time add 3d`). |

Note: your dev world's config still had `maxSpouses = 1`; I set `run/config/monstergirls-common.toml` to 0 (any number). Other worlds keep whatever their toml says.

## Marriage, many times over

- **Proposing again**: a wedding ring to a second girl at full friendship. A wife nearby gives her blessing in chat.
- **The perfect proposal**: propose at her kind of place (the same one she names for a date: flowers, the water's edge, somewhere high, or the open night sky) for a bigger moment, Luck for a day and an advancement.
- **Someone's daughter**: when another player proposes to your grown daughter, you hear of it.
- **She asks first**: after a lovely date (sneak + flower, go to her kind of place), a girl at full friendship who has never asked hands you a wedding ring herself. Give it back to say yes.
- **The wedding** (both by a hearth): bell, fireworks, friends gather. New: your other wives come as bridesmaids (cherry petals), guests leave presents by the hearth, the bride tosses her bouquet (a grown daughter catches it and takes a shine to a villager; otherwise a flirty guest catches it and blushes at you), and you get a little figurine of her in her veil. Does the figurine keep the veil?
- **Group wedding**: two or more fiancées standing by the hearth together are all wed at once (one bouquet, one set of presents).
- **By kind**: a kitsune's wedding brings a sunshower (rain from a clear day sky for a minute), harpies shed feathers, mermaids bubbles, dragons a plume of flame, the undead soul fire, slimes a wobble.
- **Realm wedding**: wed by a *lit realm censer* instead of a hearth: violet smoke, and +15 realm touch.
- **Three wives**: the advancement *One Big Happy Family* and a 4x3 painting, *The Whole Family*. Does it hang right?

## Married life

- **Hearts add up**: +2 hearts per wife within 32 blocks, up to three wives (+6).
- **Warmth, not jealousy**: pet one wife and the others within 8 smile (green sparkles) and get +1 friendship, once a minute each.
- **Family bed**: two or more wives in your bed at once. The morning message, and Absorption. Where do three of them lie? (Placement was never seen.)
- **Shared dreams**: the morning after a wife shared your bed, one time in three she dreamt of a real landmark nearby (oasis, sky island, shrine, inn, grotto, aerie, camp, labyrinth, lair, den) and says which way and roughly how far. Go there with her (within 40 blocks of it, her within 16 of you) and the dream comes true: +10, Luck for a day, an advancement. Is it too often, too rare?
- **Packed lunch**: the first time each morning you pass within 6 blocks of a wife, she hands you a named lunch.
- **Welcome home**: log in near a wife and she greets you after a few seconds.
- **Notes from home**: come back after a while away; each wife who missed you leaves a note on a journal page.
- **Honeymoon**: three days after the wedding every pat flirts and she is keener for bed. Each anniversary (every 40 days) is a one-day second honeymoon, with a present; the fifth brings two diamonds, the tenth an enchanted golden apple (and advancements).
- **Portrait day**: use a plain painting on your hearth with two or more of your family within 8 blocks: it becomes *The Whole Family* with their names and the day.
- **Happy news**: a girl who conceives tells you the next morning.
- **Wives' own words**: when a wife flirts at a pat she has her own lines. Logging in on an anniversary reminds you she's waiting.
- **Birth**: you hear when a girl's time comes (wherever you are), and wives near the mother gather round.
- **Loss**: if a wife dies, the others near you hold you close (Regeneration) and her ring drops, with her name on it.
- Jade shows "Your wife" (and honeymoon), "Engaged to you", how long until an expecting girl is due, and what she craves once she has said.
- Journal: a **Family** guide page (all the little-ones features in one place), a **Family** page (wives, wedding day, days to each anniversary, the departed), **Our little ones** (each by name, kind, birth day and mother), **Notes from home**.

## The little ones

- **Cravings**: an expecting girl tells a friend nearby the one food she craves. Feed it to her before her time: twins (a second litter or clutch).
- **Names**: little ones born to you come with a name (option `nameNewborns`, on). Name tags still rename them.
- **Cradle** (planks around wool, on two sticks): at night little ones within 6 blocks nap beside it and grow twice as fast. Right-click it: a lullaby. Does the model face the right way?
- **Toys**, one a day: wooden hoe (farmhand), bowl (cook), wooden sword (guard), compass (scout) tip the calling she picks when she grows up.
- **Story time**: show a little one a written book, and all the little ones within 6 sit and listen.
- **Growing up**: be near as she grows: her first word (your name), a flower she picked, a drawing of you both. When she grows up near you she brings you the first thing of her calling (hay, stew, a shield or a spyglass).
- **Hobbies run in the family**: half of daughters take up their mother's hobby, and half take after her colouring.
- **Thunder**: in a thunderstorm, your little ones run to you and hide their face in your side.
- **Harpy chicks and frog little ones** get the story time, growing-up moments (and chicks the cradle) too. Harpies crave as well: fed, a full clutch of four.
- **Renaming** a wife or little one with a name tag updates the journal pages.
- Advancements: *A Little One of Your Own*, *A Whole Family Tree* (ten), and your daughter's little ones show as grandchildren.

## Family reunions (2026-11-10)

- **Reunion Day** is the 12th day of every 24 (the journal's Family guide says how many days to go; that morning you are told).
- At dusk, standing by your own hearth: every grown child of yours who lives further than 32 blocks away comes home, with her villager sweetheart and her little ones (your grandchildren). Daughters out of loaded range are fetched from where they were last seen (her chunk is held open for a few seconds by a self-expiring ticket). Same dimension only.
- Fireworks, hearts, +6 friendship each, the little ones run to you; advancement *All Together Again*.
- The next morning everyone who came from afar goes back where they were.
- Try it: `/time set` to a day where `day % 24 == 11` (e.g. `/time set 11d`, then `/time add 11500` for dusk) with a grown daughter living away (or `/monstergirls family grow` a little one four times and walk her off).
- Things to check in a client: does she arrive on solid ground near the hearth, and does she really end up back in her village the next morning (and her sweetheart with her)?
- Option: `reunions`.

## Daughters move out (2026-11-10)

- A grown daughter of yours who settles at your hearth (free-roam near it, a day later) builds a **house of her own** 12 to 20 blocks away instead of the little hut: seven by seven, in her family's materials, two beds against the back wall, a lantern between them, a cradle and a chest in the back corners. You're told when she starts and when it's done.
- Mornings after, she adds her sign (her name), a candle, her flower in a pot and rugs, like any hut.
- She sleeps at her house at night instead of beside the hearth (the hearth's bell still calls her), and comes to the hearth by day as before.
- Your first visit to the finished house is the **housewarming**: hearts, +4 friendship, advancement *A House of Her Own*. The journal's children page marks her "her own house".
- Once she's wed to a villager sweetheart, the next morning near him he **moves in**: the second bed becomes his home (his old village bed is left free).
- Daughters who already built a hut keep it. Harpy and frog daughters nest as before.
- Try it: `/monstergirls family grow` a little one four times, set her to free roam near your hearth, wait a day (or `/time add 24000`).
- Things to check in a client: the spot she picks, that she walks into her house at night, and that the villager really sleeps there.
- Option: `daughtersMoveOut`.

## Family tree (2026-11-10)

- The journal has **Family Tree** pages after the Family page: you at the top, each wife (♥, the departed in grey) with the little ones she bore under her, their own little ones under them (· in green), and on down the generations. Little ones of a mother who isn't your wife come under "with her name"; those whose mother isn't known (harpy chicks, frog young) hang straight from you.
- It's drawn from the journal's own lists, so it remembers those far away or gone. From today each child also remembers her mother's id; older entries are linked by the mother's name.
- Check in a client: does a big tree read well in the book (indentation, the ♥ and · marks)?

## Vow renewal (2026-11-10)

- On a wife's anniversary (every 40 days after the wedding), give her the **wedding ring** while you both stand by a hearth, or a lit realm censer: the vows are renewed. Your friends gather, the other wives stand beside her with petals, bells and fireworks (violet smoke at a censer), she tosses the bouquet, wears her veil, and you get a second honeymoon as long as the first (three days). +4 friendship. The ring stays yours.
- Once a year with each wife. On another day, the ring by a hearth tells you how many days to wait. The anniversary message reminds you.
- The family page shows "vows renewed Nx"; advancement *I Still Do*.
- Try it: `/monstergirls family wed` her, then `/time add` 40 days (`/time add 960000`) and stand by the hearth with her.

## Honeymoon trip (2026-11-10)

- At the wedding (and at a vow renewal, if she has no wish yet) a wife names a place she has always wanted to see: one of 14 lovely overworld biomes (cherry grove, flower forest, sunflower plains, meadow, beach, warm ocean, ice spikes, mushroom fields, badlands, jungle, bamboo jungle, desert, savanna, lush caves), never the one she's standing in.
- Go there with her: you standing in that biome, she within 16 blocks. You get a **souvenir** (an item of that place, named "Souvenir of the ...", with her name and the day on it) and a **scout pack** full of that biome's finds; +6 friendship, the honeymoon runs a day longer, advancement *Honeymoon Trip*.
- The family page shows "(dreams of the ...)" until you go, then "(been to: ...)". At the next renewal she wishes for somewhere new.
- Try it: `/monstergirls family wed` her, read the wish in chat, `/locate biome` it and travel there with her following.

## Server safety (2026-11-10)

- `./gradlew checkServerSafe` (also part of `check` and of every GameTest run) fails the build if code a dedicated server loads names a client-only class, the usual reason a mod works in single player and crashes a server.
- A real dedicated server (`./gradlew runServer`, run/eula.txt in place) boots cleanly as of 2026-11-10. Gradle doesn't pass typed commands to it, so stop it with Ctrl+C. Joining it from a client (`./gradlew runClient`, Multiplayer, localhost) works too (checked by the user, 2026-11-10).

## Performance (2026-11-10)

- `PerformanceTests` times a lived-in home (41 creatures) every GameTest run and writes `run/perf_report.txt`, dearest kind first. First run: 4.4 ms a tick in all (a server tick has 50). If the game feels slow somewhere, that file is the first thing to look at.

## The Order's chaplain (2026-11-10)

- A sister of the Order you spared comes back to your hearth as a chaplain for three days and blesses any wedding or vow renewal there (see DEMON_REALM_TESTING.md, The Order).

## Friendly zombies and the fletching table (2026-11-10)

- Zombies, husks, drowned and zombie villagers a zombie girl introduced you to now leave villagers and wandering traders alone. Wild ones still don't.
- A **fletching table** within 4 blocks of a gull's or sparrow's roost hopper makes her shed feathers, flint and sticks half as often again.

## Mining shafts (2026-11-10)

- A digger sent mining (pickaxe, then a sample) now leaves a **real shaft** beside her spot: two rungs of ladder right away, deeper while she's down (whenever the spot is loaded), about 10 blocks for stone or coal and nearly 30 for diamonds.
- She only digs natural ground (dirt, sand, gravel, stone, ores) and stops before anything built, water or lava beside the shaft, a cave under it, or a wall that won't hold a ladder. In a built base (plank floor) no shaft opens and she goes down as before.
- Ore she strikes on the way comes up with her haul. The shaft stays afterwards.
- Config `minersDigShafts` (on by default) turns it off.
- Check in a client: climb the shaft, and that it never opens into a cave or floods.

## Harpy and frog wedding looks (2026-11-10)

- Harpies and frogs/toads now dress for the wedding too, for the same day the girls wear the veil (and on anniversaries and vow renewals): a harpy wears a **crown of white feathers** with a short sheer veil behind; a frog or toad a **lily pad** for a hat with a pink blossom on it and the veil falling from under it.
- Her wedding figurine wears it as well (the look is saved with her).
- Rough proportions on four real head bones: `previews/wedding_looks.png`. Check in a client that the crown doesn't sink into big crests (owls, cassowaries) and that the pad sits right on the wide frog heads. Texture: `tools/gen_wedding_looks.py`.

## Wardrobe outfits, redone (2026-11-10)

The raincoat, winter coat, yukata and maid apron are now painted onto each girl's own model (`previews/wardrobe_outfits.png`), drawn as her model again with that texture, so they follow her pose. Check in a client that they don't flicker against her skin (they're drawn the same way as dyed sheep wool).

## Wandering merchants (2026-11-11)

- One wild grown girl in 25 (not merfolk, elves or legends) comes as a **wandering merchant**: green sparkles over her, and she greets you when you come within 8. An empty hand opens a real trading screen: 4 wares of her clade (e.g. spinners: spider silk, honey, cobwebs, exoskeletons; cattle: milk, leather, hay, wool, a saddle; devils: heartfruit, weepcaps, blaze powder, nether wart; dragons: gold, a diamond, blaze rods), 6 uses each, and she buys 6 bread for an emerald. Each trade +1 friendship.
- She never picks a fight while trading. After 2 days she vanishes down the road, unless someone is her friend (10+) or she has a name tag: then she stays as an ordinary girl.
- While she's a merchant, an empty hand trades instead of petting (crouch-patting still works where her family has it). Config `wanderingMerchants`, guide "Wandering merchants". To try one: `/summon` doesn't make merchants (natural spawns only); look for green sparkles.

## The doppelganger (2026-11-11)

- A new elf, the **Doppelganger** (`/summon monstergirls:doppelganger_elf`, or her spawn egg): dark forests, birch woods and taiga.
- Every 30 s she shifts: wild, to the look of the nearest girl she can copy; befriended (10+), to the look of whichever companion near you you're fondest of. Use a **glass pane** on her for her true face; again to let her borrow.
- She copies elves, devils, undead (not the hand, gaitress, souls or will-o-wisp), kitsune, nekomata, canids, jackalopes, rodents, minotaurs, lizardfolk, marsupials, slimes and flora.
- **Never seen in a client**: the borrowed model is drawn with HER animations (the bones the two share move; a tail or wings of the borrowed kind may hang still). Check that nothing looks broken, that the swap isn't a flicker, and whether the borrowed look should keep the original's size instead of hers.

## Headgear (2026-11-11)

A grown friend devoted to you (60+) within 8 blocks gives you her kind's headgear once (per player and piece, whoever gives it). Worn on the head:

- **Fox Mask** (kitsune): Luck I.
- **Bunny Ears** (jackalope): monsters within 16 that are after you glow.
- **Witch Hat** (devil): +50% from experience orbs.
- **Flower Crown** (flora): bees never turn on you; outdoors by day you heal half a heart every 10 s.
- One point of armour each. Models and textures from `tools/gen_headgear_assets.py` (`previews/headgear.png` shows icons and texture sheets). **Never seen on a head in a client**: check that the mask sits at the side of the head, the ears stand up, the hat isn't buried in the hair, the crown circles the brow. Guide "Headgear", config `headgear`.

## Spirit pacts (2026-11-11)

Crouch and use an **amethyst shard** on a devoted (60+) grown salamander, undine, sylph or gnome: she binds herself to you and her element goes with you everywhere (her own gifts near her are unchanged). One spirit per element; the same again ends it; her death ends it (if you're online then).

- **Fire** (salamander): your melee blows set what they hit alight for 4 s (never a friend).
- **Water** (undine): Regeneration I while you're in water or rain.
- **Wind** (sylph): Jump Boost I always, Speed I under open sky.
- **Earth** (gnome): below sea level with no sky above, Haste I and Resistance I.
- All four: advancement Elementalist. Guide "Spirit pacts", config `spiritPacts`. Check in a client: is Jump Boost all the time annoying when building? (Easy to change to outdoors only.)

## Legendary trials (2026-11-11)

A wild legend sets anyone who comes within 8 blocks (and isn't her friend yet) a trial, for 3 days. Pass it: +40 friendship and her relic (`previews/relics.png`). Each player once per legend. Advancement Worthy, guide "Legendary trials", config `legendTrials`.

| Legends | Trial | Relic |
|---|---|---|
| Great birds (phoenix, zhuque, yatagarasu, roc, ziz, bennu, garuda, alkonost, anzu, ratatoskr) | Hand her an offering she names: golden apple, phantom membrane or blaze rod | **Sky Plume**: carried, you drift down when falling |
| The deep (leviathan, cetus, scylla, charybdis, kraken, kun, turtworld, black tortoise) | Offering: nautilus shell, prismarine crystals or glow ink sac | **Tide Pearl**: carried, water breathing and Dolphin's Grace in water |
| Great serpents (xiangliu, echidna, medusa, wadjet, basilisk, jormungandr, orochi, quetzalcoatl, cipactli) | Offering: emerald, golden apple or ender pearl | **Serpent's Eye**: use on a creature you look at within 16: near-stone for 5 s (not your friends), 30 s cooldown |
| Dragons, hounds, giants, riders, Wukong (long, bahamut, ender, cerberus, amarok, behemoth, taotie, pazuzu, the five riders, sun wukong) | Beat her in melee to half health: she yields | **Dragon Scale**: carried, Fire Resistance while burning |
| The night's own, oracles, sacred beasts, the deathless (kaguya, norn, ophanim, bastet, ammit, nue, lilith, sphinx, lamassu, qilin, urcuchillay, ark seraphim, koschei, gashadokuro) | Vigil: stay within 24 of her through a night (8000 ticks); leaving or a short night starts over | **Moon Mirror**: carried, night vision at night; use: everything within 32 glows 10 s, 60 s cooldown |

- Cerberus, amarok and bahamut are no longer in the old-customs duel (their trial replaces it).
- Check in a client: is the trial message too pushy (it comes as soon as you're within 8)? Does a flying legend wander off during a vigil? Do the icons read well?

## Care needs (2026-11-11)

- **A pool for the merfolk**: a merfolk living at your hearth with 6+ blocks of still water within 8 of the fire treats the pool as her place at home (she waits in it, so she never dries out at home). Without one she tells you once a day (-2 friendship).
- **Lamias feel the cold**: in a cold biome (where the wardrobe's winter coat comes out) a lamia gets Slowness II unless warmed: winter coat on, a fire, lit campfire, lava or magma within 5, or a hot spring.
- **Harpies moult**: every 4-7 days a grown harpy with a friend (10+) gives her favourite within 32 blocks 3-6 feathers. Sparrows and gulls are left out (they already shed into their roost hopper).
- Already in the mod, so not added: lamias shedding skin, merfolk drying out ashore, slimes (water stings them here).
- Config `careNeeds`, journal guide "Care needs". Check in a client: does a merfolk resident actually swim to the pool at night, and does the lamia's crawl in snow feel too harsh?

## Liaison inspections (2026-11-11)

Carry a **homestay permit** and own a hearth with residents: about 3 days later, and then every 7, an **inspector** from the exchange office (a named liaison villager) walks up to your hearth by day while you're within 64 blocks of it. At the fire she checks, within 16 blocks: a bed for each resident there, 8+ food in fridges/chests/barrels, nobody hurt or under half health, residents fond of you (average 40+), no monsters within 24. Two points each.

- 6 or more: an emerald a point. 10: also Hero of the Village for a day, and the advancement Model Home.
- Under 6: a warning, no pay. Either way you get her written report (what passed, what to fix).
- Then she walks off and vanishes after about 20 seconds. Journal guide "Inspections", config `inspections`.
- Check in a client: does she actually reach the fire (villager pathing), and does she leave tidily rather than joining a village?

## Old customs (2026-11-10)

Some wild kinds take to a stranger in their own way, once each, and only before they're fond of you (journal guide "Old customs", advancement "The Old Ways", config `courtship`).

- **A fair duel**: lizard warriors (salamander, croc, komodo), the old bull breeds (Minoan, aurochs, bison, rhino), oni, ogres, trolls, fire and frost giants, hellhounds, wolves, wyrms, wyverns, valkyries, dark elves and lionesses. Hit one with a sword (not arrows) until a blow would take her below half health: she yields instead, her pack stands down too, and she respects you (+30).
- **Her snare**: every arachne, and the tigress. One who sees you within 12 blocks now and then snares you (a web at your feet; the tigress just runs you down) and comes to claim you. Don't strike her and she claims you (+30, web cut, the tigress pins you a moment). Struck, she gives up until tomorrow.
- **Keeping your house**: a wild kikimora, brownie or domovoi within 32 blocks of your hearth while you're home walks over and keeps it every 30 seconds: loose things into nearby chests, cobwebs down, +2. After 6 visits she stays fond of the house (+30).
- Check in a client: how often a spider snares you (about once a minute while one is near and hasn't courted you yet; is it too often?), and whether a dueling oni's damage feels fair.

## Elsewhere

- **Birthday candles**: on a girl's birthday (Jade shows it), light a candle on a cake within 8 blocks of her: she comes over, blows it out and makes a wish (+8 for each friend there).
- **The dragon egg**: crouch and use a blaze rod on the dragon egg with a dragon-girl friend within 8 blocks: it hatches into a little ender dragon girl, devoted to you. (Without the dragon friend it only grows warm.)
- **Spyglass**: whoever is in the crosshair (up to 64 blocks) is studied twice as fast for the bestiary.
- **Musicians**: a girl with the music hobby plays a note block within 4 blocks during her hobby hour (its tuning is left alone).
- **Hobbies at home**: with nobody about when she finishes, a girl who lives at a hearth puts what she made in a chest (food in a fridge) near it.
- **A hero's welcome**: earn a great vanilla advancement (kill the dragon, summon the Wither, hero of the village, cure a zombie villager, full beacon, totem of undying, elytra, adventuring time, monster hunter) and the next time you are within 12 blocks of your hearth with friends about, they throw a party: fireworks, +5 each, a present each.
- **Compliments**: put on something new (armour, trims, dyed leather) and the first friend who sees you says so (+1). Is it naggy when you swap armour a lot? (Each outfit only once.)
- **Notices** (behind `friendNotices`): a friend warns when the tool in your hand is nearly broken, tells you to sleep after three sleepless days (before phantoms), and shouts when a creeper starts hissing at you, with the direction.
- **Grandchildren**: your daughter's little ones show on the children page as your grandchildren.

- **Portrait paintings**: 29 2x2 busts of the girls, so any painting you hang may come up as one. A girl whose hobby is the new **painting** hobby sometimes leaves you her own kind's portrait.
- **Archery**: crouch and show a friend a bow with a target block within 24 blocks of her. Three arrows each; she really shoots. Does she hit at 20 blocks? (A centaur or elf should; a slime shouldn't.) Note: crouch + bow no longer arms a fighter; a plain right-click still does.
- **The Day of Hearts**: a fourth festival, day 20 of every 24. Set a cake out by the hearth; at dusk each resident takes a slice and gives you a sweetheart cookie. (`every_festival` now needs all four.)
- **Anubis atonement**: a condemned heart lightens by one for each zombie villager you cure and each iron golem you build. Then show her a feather.
- **Kin slimes and phantoms** now follow their girl (slimes hop after her, phantoms circle overhead). Never seen moving in a client.

## Options

All on by default, in the mod's config screen: `maxSpouses` (0 = any number), `nameNewborns`, `compliments`, `herosWelcome`, `cravings`, `sharedDreams`, `packedLunch`; the notices use the existing `friendNotices`.

## Known flaky tests seen tonight (unrelated to these features)

`riflebirdDancesForAFan`, `orbWeaverSpinsAHuntingWeb`, `caneToadSwallowsItemsAndSmallMobs`, `murdersRaidFields`, `wrecksBoatsAtTheRock`, `begsAtTheFarm`, `boneMealOnTheRealm`, `rootedFloraGrowsCrops`, `enderSlimeBlinksAndCarries`, `heartfruitMakesHerFall` (now prints who else was near), `broodsOnTheNestAtNight`: random AI rolls, each passing on the next run. Note: GameTest batches run side by side, so a test that sets the time of day can upset a neighbour.
