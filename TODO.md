# Harmontide to-do

## 0. Tonight: survival server playtest (2026-10-21)

Write down numbers and coordinates, not impressions. They turn into the next to-dos.

- [ ] Before you join: run `./gradlew build` and start a **dedicated server** once (`runServer`). Client-only classes leaking into server code only crash there.
- [ ] **Generation:** which species did you meet, in which biome, and how many in the first hour of exploring? Note any biome that feels empty or crowded.
- [ ] **Spawning:** do girls crowd out vanilla animals or the hostile mob cap? Compare animal counts with an area that has no girls in it.
- [ ] **Behaviour:** check for griefing (blocks broken, crops trampled, chests touched) with `mobGriefing` both true and false. Do they get stuck in caves or doors, or drown?
- [ ] **Multiplayer:** do the follow, bond, gift and bed-sharing features behave with two players? Does whoever bonded first win?
- [ ] **Performance:** check tick time with a crowd of girls (for example a harpy roost or an anuran pond). Use `/neoforge tps`, or spark if it is installed.
- [ ] **Arrow farm:** the real-client check from section 2.
- [ ] Note every crash or log spam with a timestamp, and copy `logs/latest.log` afterwards.

## 0b. Harmontide rename

- [x] Display name and creative tab now say **Harmontide** (`gradle.properties` `mod_name`, `itemGroup`).
- [ ] Decide whether to rename `mod_id` `monstergirls` to `harmontide` as well. That **breaks existing worlds**: every entity, block and item id changes. Do it once, before any public release, and **not** before tonight's server test. It touches the package `com.linkyoshi90.monstergirls`, the `assets/monstergirls/` and `data/monstergirls/` namespaces, `MonsterGirlsMod.MODID`, the tools scripts and the gametest structures.
- [ ] Retitle the `*_field_guide.html` pages ("A field guide to an inhabited world") under Harmontide and link them from the website.

## 0c. New species concepts

See [HARMONTIDE_CONCEPTS.md](HARMONTIDE_CONCEPTS.md) for the next wave of 39 species: their families, classifications, vanilla niches and a suggested first batch.

- [x] First four are in the game (2026-10-21), in existing families with GameTests (`HarmontideTests`):
  - **Ocelot** nekomata: jungle and bamboo, climbs walls and trunks, brings jungle presents.
  - **Tapir** centaur: jungle and swamp, eats melon. The baku: no vanilla phantoms or phantom girls within 32 blocks.
  - **Alligator snapping turtle** lizardfolk: swamps, tongue lure (fish at her feet, at most 3 at a time), shell retreat.
  - **Banshee** undead: birch forests and meadows at night. Keens when a player nearby drops to 3 hearts, and every hostile mob near that player glows.
- [ ] Playtest them tonight: do they spawn often enough? Is the turtle's fish rate (about 1 a minute) too generous? Is the banshee's ghast-scream keen too loud? Does the ocelot get stuck climbing?
- [x] Second batch (2026-10-23), tested in `HarmontideTests`:
  - **Kappa** (lizardfolk): rivers and swamps. Shell and water dish. Strong when wet, weak and slow after two minutes dry. Bow to her (crouch, facing her) and she bows back and spills her dish (weak, and she stops fighting you), +1 friendship once every 5 minutes. Melon slices and kelp stand in for cucumbers.
  - **Naga** (lamia): jungles. King cobra hood and a gold crown. Nobody she is not fond of opens or breaks an unopened structure chest within 10 blocks of her. Player chests are never affected.
  - **Dullahan** (undead): plains and meadows at night. She carries her head in the crook of her arm, with a blue spirit flame at the neck. Half of them ride a skeleton horse. She backs away from anyone holding gold.
- [x] Fourth wave (2026-10-24), the design notes in HARMONTIDE_CONCEPTS.md "Fourth wave", tested in `FourthWaveTests`:
  - **Camel** and **Dromedary** (centaur): anyone may ride them, with no friendship needed. Cactus puts one in the mood, and next to a vanilla camel in love she conceives (half the young are camel calves). A bucket milks her once every 5 minutes. Kin of vanilla camels.
  - **Kelpie** (centaur): rivers. She is ridden only under a saddle, put on at fond (10). In water she carries you faster than a boat (0.42 a tick, 0.5 sprinting). She attacks players she is not fond of between 13000 and 23000 on the clock.
  - **Wadjet** (lamia, on two legs with a cobra hood): desert. Every 2 s she gives Regeneration and Resistance to players within 8 blocks and to the girls (any family) fond of them. She hunts every hostile mob and often brings emeralds. Named **Nekhbet**: level II effects and +4 attack damage.
  - **Hornet** (bug): forests. She stings players within 5 blocks unless fond of them. Until befriended she hunts bees and bee girls. On patrol she goes for hostile mobs. Eats meat and fruit, gives paper and clay.
  - **Dugong** (merfolk): warm and lukewarm ocean, always spawns in pairs. Seagrass (3 friendship) wins her over, and she gives sea treasure.
  - **Tengu harpy:** peaks and cherry groves. Neutral. Her fan does 2 damage and throws the target away from her territory. On patrol she attacks hostile mobs (creepers and endermen too) within 18 blocks of her spot. Wild, she soars 28 blocks up and comes down to drink at water.
  - **Toucan harpy:** jungles. Eats fruit and clacks her beak (her ambient sound). Two toucans within 8 blocks now and then hold a 6-second clack-off.
  - **Penguin harpy:** snowy beaches, ice and frozen ocean, in colonies of 4 to 8. Fast underwater. She hunts fish, and when befriended she brings her player one for every fish she catches. One in 12 is a wanderer who walks off alone in a straight line until befriended. Orca mermaids hunt penguins nobody has befriended, and wild penguins go for koi mermaids.
  - **The injured girl:** 1 wild girl in 50 (1 hornet in 5) spawns at 30% health, half speed, unable to fight. A flier cannot fly, and a broken wing hangs limp. A healing balm mends her even when she is wild: +40 friendship, and a hornet goes straight to full.
- [ ] Playtest the fourth wave: does the broken wing droop the right way (`GirlRenderer`, the angle was never seen in a client)? Is the kelpie's water speed fun and does it keep the rider dry? Do tengu ever come down to drink often enough to meet? Does the penguin wanderer get stuck on the first cliff? Is the wadjet's blessing too strong for a desert you just walk through?
- [ ] Not done for the fourth wave: injured harpies and anurans (only the girl families get injured), the harpy field guide entries for the tengu, toucan and penguin (that guide is hand-written), and sounds of their own (they borrow the crow's, the parrot's and a pitched-up donkey bray).
- [x] Playtest fixes (2026-10-24):
  - The penguin swims face down; her swim pose had her on her back.
  - The wadjet cannot be ridden; she walks on legs and has no coils.
  - `/monstergirls bonds` says "monster girls", not "harpies".
  - Swimmers (merfolk, sea snake, mollusks, penguin) turn before they go and accept a waypoint within 1.5 blocks, so they stop circling a target overhead (`AmphibiousMoveControl`, `SwimmerNavigation`).
  - Ika and the octopus swim instead of bobbing on the surface.
  - Dolphin, orca, kraken, ammonite, ika and octopus are on `can_breathe_under_water`. Every merfolk still rises for a breath every 1 to 3 minutes, to sell it.
  - A girl climbing into your bed says so in chat.
- [ ] Playtest: do the orca and the seal still circle in open water? The tests only reproduced the sea snake's orbit, around a target mostly overhead.
- [ ] Flaky test: `nekomataPatienceAndSwat` failed once, `toadEatsPests` once; `strandedTadpoleFindsThePond` fails about one run in three now.
- [x] Riders steer lamias and unsaddled centaurs again (2026-10-24). Who steers was gated on friendship, which the client never knows, so nobody steered. The friendship check stays where you climb on. Test: `ridersSteerWithoutKnowingFriendship`.
- [x] Fifth wave (2026-10-24), the design in HARMONTIDE_CONCEPTS.md "Fifth wave", tested in `FifthWaveTests`:
  - **New family, the canids** (`Canid.java`, `tools/gen_canid.py`, field guide volume XX):
    - **Wolf:** packs led by an alpha. Wild, she hunts sheep and rabbits. Bones win her over, she howls at night, and her player's tamed wolves join her fights.
    - **Polar bear:** wild, she hunts seal mermaids and penguin harpies that nobody has befriended. She fishes at open water and attacks strangers within 8 blocks of her cubs.
    - **Panda:** born with a personality. Lazy pandas lie about and walk slower, worried ones avoid strangers, playful ones roll, aggressive ones guard their friends, and weak ones have 20 health and sneeze often. Every panda sneezes up slimeballs.
    - **Anubis:** nocturnal. She weighs every player within 10 blocks: anyone with villager or iron golem kills (a stat) is condemned and hunted. Unopened loot chests within 12 blocks are closed to anyone she has not weighed light.
  - **Pegasus** (centaur): ridden only under a golden bridle (gold horse armour, put on at fond). Golden apples are worth 10 friendship. Hold jump: she leaps (the horse jump bar) and glides down at 0.12 a tick. No fall damage.
  - **Lich** (undead, deep dark): she places a phylactery block near her. While it stands, a killing blow sends her back beside it at half health. Remove it (break or blast) and she is freed and peaceful. She casts Slowness II and Weakness every 4 s. Freed and fond, she trades books for XP (10, 20 for a written book, 30 for an enchanted book).
  - **Mimic** (slime): one unopened loot chest in eight, when a player opens it. Never a chest without a loot table. She takes the loot into her 27-slot hoard and bites once. Gold wins her over; then she follows her friend, and crouch-right-click opens her.
  - **Vodyanoy** (anuran, the first man): rivers. While no dropped item floats within 8 blocks, his friends get a fish every 2 to 4 minutes (2 with a fisherman villager within 24 blocks). With litter in the water he is grumpy: he drags swimmers under once every 10 s. He has no bed sharing, no young and no tadpole stage.
  - **Frog kin:** vanilla frogs within 16 blocks walk to any anuran girl. A slimeball puts her in the mood; next to a vanilla frog in love she conceives. A third of that spawn hatches as vanilla tadpoles, and the rest are hers (devoted to nobody).
- [ ] Playtest the fifth wave: the pegasus leap and glide feel (the jump bar is client-side, never seen), how often you meet a mimic, whether Anubis condemning on lifetime stats is too harsh (there is no atonement yet), and whether the lich respawning at her phylactery is findable.
- [ ] Not done: frog kin introductions (Kinship's introduce-with-food covers girl families only), and harpy guide entries.
- [x] Sixth wave (2026-10-24), the design in HARMONTIDE_CONCEPTS.md "Sixth wave", tested in `SixthWaveTests`:
  - **Kobold** (canid, caves and mountains): befriended, every 10 s she points out the nearest diamond, gold or emerald ore within 10 blocks (once per ore, on the action bar with the distance). She yelps when the block her friend looks at holds up sand or gravel. Every 5 s she growls at a mimic chest within 6 blocks, which rattles. Mimic chests are now fixed by the world seed (`SlimeGirl.isMimicChest`), so a kobold can smell one before it is opened. Kin: wolves.
  - **Grizzly** (canid, taiga): she fishes salmon at open water and raids full beehives within 8 blocks (the honey goes to 0, the bees go for her, and her friend gets 3 honeycomb). She hibernates (lying, no hostility) while it snows where she stands; a hit or a hand wakes her. She guards her cubs like the polar bear. Kin: polar bears.
  - **New family, the rodents** (`Rodent.java`, `tools/gen_rodent.py`, field guide volume XXI):
    - **Capybara:** wolves, foxes, cats and ocelots within 8 blocks drop their targets. Idle girls within 6 blocks sit down beside her (1 in 3, every 5 s). She soaks in water. Devoted friends near her get Regeneration.
    - **Ratatoskr:** she gathers dropped seeds and saplings into a 9-slot cache, and every minute or so she forgets a sapling, which takes root where she stands. Hand her any item (fond, not food) and she runs it to the nearest other player she is fond of. One delivery in 8 is a prank (a stick, seeds, a dead bush or a poisonous potato), and the real item goes into her cache to come back later as a present.
  - **Leshy** (flora, male, old-growth taiga, no bed sharing): he keeps a tally per player within 24 blocks. Every 6 saplings planted earn 2 friendship, 2 spruce saplings and 2 apples. At 12 logs cut (net) the cutter gets Blindness, Nausea and Slowness ("lost in his fog") and loses 5 friendship. He bellows at a vodyanoy within 12 blocks.
  - **Black Tortoise / Xuanwu** (lizardfolk, frozen rivers): projectiles do nothing. She hides in her shell like the turtles. Kin: turtles.
  - **La Llorona** (undead, rivers and swamps): at night she weeps and looks for a baby animal or village child with no grown one of its kind within 12 blocks, then leads it to the nearest grown one within 48 blocks. Once it is home she stops weeping for the rest of the night, and every player within 16 blocks gains 3 friendship.
  - **Zhuque, the Vermilion Bird** (`ZhuqueHarpy`, the phoenix's model in her own colours, savanna and badlands): she is fireproof, sets whatever she strikes alight, and once befriended ripens crops within 6 blocks every 3 s. She has her own egg (Vermilion Egg), eggs and hatchling.
  - **The Four Symbols** (advancement, challenge): befriend (10+) a long dragon (east), a white tiger nekomata (west), Zhuque (south) and the Black Tortoise (north).
- [ ] Playtest the sixth wave: how chatty the kobold is underground, whether the Ratatoskr prank is funny or annoying, how the leshy's tally feels for a normal tree farm (12 logs is about two trees), and whether La Llorona finds lost young often enough.
- [ ] Not done for the sixth wave: Zhuque has no harpy field guide entry (nor have the tengu, toucan and penguin). The grizzly cannot hibernate in a den (she sleeps where the snow catches her).
- [x] **Turtles** (2026-10-25, lizardfolk), tested in `TurtleAndKinTests`:
  - **Red-eared slider** (rivers, swamps, in groups of 2 to 4): when a stranger comes within 6 blocks, every slider within 8 slides into the nearest water. Her food is worth double.
  - **Galapagos tortoise** (beaches, stony shores, savanna): 70 health, armour 12, speed 0.12. Once she is fond of you, an empty hand climbs onto her shell (crouch to pat her, as with centaurs). She keeps going where she was going, and cactus does not hurt her.
  - **Box turtle** (forests): shut in her shell she takes no damage at all. A stranger sprinting within 4 blocks makes her shut, and she stays within 16 blocks of home until someone befriends her.
  - **Softshell turtle** (rivers): no armour, quick. Still in water on sand, mud, gravel, dirt or clay, she is buried (invisible) and bites any stranger within 1.5 blocks for 6 damage. She can breathe underwater.
  - **Matamata** (swamps, mangroves): still in water she is invisible (dead leaves). Fish within 4 blocks get sucked in; her friend gets the fish. She can breathe underwater.
  - All of them count vanilla turtles as kin, and all but the softshell hide in their shells.
- [x] **Kinship for every vanilla counterpart** (2026-10-25). New: bat to vampire, snow golem to yuki-onna, iron golem to Frankenstein's monster, skeleton horse to dullahan, skeleton and stray to lich, allay to the fairy and fairy queen, vex to lesser devil, tropical fish to koi, and mooshroom to matango. Horses go to the pegasus (not the kelpie), and wolves stay with the wolf girl and the kobold (not Anubis). Harpies now have kin too: parrots, or chickens for the ostrich and the penguin. So do the anurans: frogs. Still none (no vanilla animal): shark, seal, dugong, reindeer, tapir, snail, ammonite, capybara, Ratatoskr, the dragons and most plants.
- [x] **Finch harpy** (2026-10-25, model by Kimi: `tools/gen_finch_harpy.py`, checked by `tools/validate_finch_harpy.py`), wired as `FinchHarpy`:
  - She flocks in groups of 3 to 5 on meadows, flower forests, sunflower plains and cherry groves, and eats seeds and sweet berries.
  - At dawn one finch starts the chorus, every finch within 16 blocks joins in (at most once every 2 minutes), and her friends nearby get a minute of Luck.
  - She has her own pale blue, speckled Finch Egg, eggs, hatchling, belly, sit and lie poses, paints and spawn egg. Her kin are parrots.
  - No colour variants yet, because the model has none.
- [x] **Alex's Mobs (optional)** (`compat/AlexsMobs.java`). Its animals are looked up by id, so the mod is not a dependency, and without it nothing changes.
  - With it, these become kin: tiger, snow leopard, grizzly bear, seal, orca, sharks, crocodile and caiman, alligator snapping turtle, terrapin (the slider), rattlesnake, anaconda, giant squid, mimic octopus, banana slug, mimicube, bison, moose (reindeer), crimson mosquito, bald eagle (gryphon), crow, seagull, toucan, emu (ostrich) and rain frog.
  - Polar bears and orcas hunt its seals.
  - The capybara's calm now comes from the tag `monstergirls:calmed_by_capybara`, which has Alex's Mobs predators as optional entries.
- [ ] Alex's Mobs is a 1.20.1 Forge mod. I could not check that a 1.21.1 NeoForge build exists, or that its entity ids are unchanged there. Confirm the ids in a pack that has it.
- [ ] Playtest: whether parrots and chickens trailing every harpy (and iron golems following Frankenstein's monster out of the village) is charming or a nuisance.
- [x] **Seventh wave** (2026-10-25), see HARMONTIDE_CONCEPTS.md "Seventh wave", tested in `SeventhWaveTests`:
  - **Crustaceans** (`Crustacean.java`, `tools/gen_crustacean.py`, volume XXII):
    - Crab and hermit crab. They walk sideways (the walk and run animations turn the body), and are neutral: whoever hits one first gets pinched (Slowness II for 2 s). Devoted, she pinches whatever hurts her friend. They lay eggs in sand.
    - The hermit crab moves into a nautilus or shulker shell you hand her, and you get her old shell back if it is one of those two.
  - **Marsupials** (`Marsupial.java`, `tools/gen_marsupial.py`, volume XXIII):
    - Kangaroo. Her joey climbs into her pouch (she carries it as a passenger) and hops out once grown. Her kicks carry knockback 2.5, her mob joins in when one is hit, and devoted she kicks whatever hurts her friend.
  - **Armoured** (`Armoured.java`, `tools/gen_armoured.py`, volume XXIV):
    - Armadillo. A hit or a sprinting stranger curls her up for 4 s, with no damage at all. Befriended, she charges monsters near her friend as a rolling ball (speed and knockback).
    - Kin of vanilla armadillos. She gives scutes, and one litter in four is quadruplets.
  - **Constructs** (`Construct.java`, `tools/gen_construct.py`, volume XXV):
    - Golem: 120 health, 12 damage, full knockback resistance. She fights monsters near villagers (never creepers), and near her friend once devoted.
    - Iron golems are now her kin (no longer Frankenstein's monster's). She has no bed and no young, and clay heals her.
    - When she dies she plays a break-apart animation without tipping over, sheds clay particles and drops 4 to 8 clay balls.
  - **Jellyfish** (mollusk family): she glows at night (light blocks now work under water), stings whatever touches her with Poison (never her friends), and friends swimming near her get Night Vision.
  - **Ahuizotl** (merfolk): a wild one within 12 blocks of your hook steals the catch (`ItemFishedEvent` is cancelled) unless you hold a lantern, which also keeps her away. Befriended, she fishes for you.
  - **Homage variants**, rolled rarely in the wild (one roll in 8 keeps them):
    - Cactus `gunman` and `blossom`, the living doll's `lost`, the ocelot nekomata's `stitched`, the salamander's `nurse`, and the wolf's and Anubis's `mummy`, redone from the user's references to match Gregory Horror Show closely (HARMONTIDE_CONCEPTS.md has the details).
    - Their props are extra cubes painted transparent on every other variant. That covers the sombreros, braids, ponchos, bandolier, mustache, lasso, Catherine's dress, snout and syringe, Katie's face, the muzzles, the machete and the cleaver.
    - The two mummies have the show's blood, on those variants only (the user asked for it as part of the homage). There is no lettering on Neko Zombie's shirt.
    - The Dillon look is the armadillo's `ranger`; the Kangaroo Jack look is the kangaroo's `outback`.
    - Generic names and no logos, so they can ship. Keep it that way for a public release.
  - **Paints** (27 in all):
    - Copper weathers (copper, exposed, weathered, oxidized), about one stage every 40 minutes unless waxed with honeycomb; an axe takes the wax off, then scrapes back a stage (`Painted`).
    - Glow and magma glow in the dark (`PaintGlowLayer`, with their own glowmasks). Ink is manga line art.
    - Pumpkin rolls only in October, candy cane only in December.
- [x] Flaky tests fixed (2026-10-25). `dustBathesOnSand` now sets daytime, since at night the sparrow sleeps instead. `fullMoonEveryoneTakesHerTurn` failed because a random rest goal (digging in, burrowing) could pull an anuran away from the bed after her turn; `AnuranRestGoal` now leaves her there (`AbstractAnuran.isInBed`).
- [x] **Eighth wave** (2026-10-25), tested in `EighthWaveTests`:
  - **Unicorn** (centaur, flower forests, cherry groves, meadows):
    - A player with villager or iron golem kills (`Canid.heartWeight`) cannot raise her affection, and she keeps 10 blocks away from them.
    - Every 2 s her horn clears Poison and Wither from friends within 8 blocks, and takes 10 s off the cure of each zombie villager being cured near her. This uses reflection on `villagerConversionTime`.
    - She is kin to horses, and friends can ride her like the other centaurs. She has a rainbow-mane variant.
  - **Black bear** (canid, forests):
    - She climbs trees (`onClimbable` while bumping into something) and eats ripe sweet berry bushes back to bare.
    - A sprinting stranger, a hit, an explosion, a bell or a goat horn (`VanillaGameEvent`) makes her drop what she holds and run for 5 s. Once she is befriended, only explosions still do.
  - **Koala** (marsupial, forests):
    - She is awake only from 22500 to 1500 and from 11500 to 13500. The rest of the time she sleeps, clinging two logs up the side of a trunk with no gravity, or where she stands if there is no tree.
    - She eats leaves only, and her joey rides on her back.
    - The drop bear variant drops onto a player walking underneath: 2 damage and Nausea, or a hug for a friend. Then she waits a minute.
  - **Gecko** (lizardfolk): speed 0.38, and she climbs walls.
    - She trails the nearest player and inspects items they drop: she picks one up, turns it over for 2 s and puts it back, once per item.
    - A hit makes her shed her tail (the bone is hidden by `FLAG_TAILLESS`), and it grows back after 3 minutes. She eats fruit.
  - **Chupacabra** (lizardfolk, nocturnal):
    - She hunts goats that are not named, leashed, befriended or within 2 blocks of a fence or wall.
    - She turns invisible while someone looks straight at her, and ignores sticks (`takesOrders`).
    - Fed mutton, she lingers 6 to 12 blocks from her friend. Her gifts are mostly 32 mutton.
  - **Firefly** (bug): she lights the night, and her lantern glowmask blinks on the world clock, so every firefly blinks in time.
    - A glass bottle catches her or a fairy in a jar (`GirlJarBlock`, light 12), as an item that keeps her data. Opening or breaking the jar lets her out and leaves the bottle.
    - Catching one awards the "Why?" advancement.
  - **Mummy** (undead, deserts):
    - Hits, or a crouching empty-hand pull once befriended, take off one bandage layer at a time (`FLAG_UNWRAP_A`/`_B` hide the layer bones), dropping string and paper. The lowest layer is skimpy wraps.
    - From there she spins back up within 3.5 s and won't unravel again for a minute.
    - She keeps strangers from unopened tomb chests, as Anubis does.
  - **Flamingo harpy** (`FlamingoHarpy`, `tools/gen_flamingo_harpy.py`, mangroves, swamps and beaches, flocks of 3 to 6):
    - Each day without fish fades her a shade (pink, pale, white), and each fish she eats brings her back a shade.
    - She stands on one leg (the new harpy action `PERCHING`), and now and then a flock of three or more marches together.
    - She has her own eggs, Flamingo Egg, hatchling and spawn egg.
  - **Paints:** frost, sakura, stained_glass (translucent) and sepia, so 31 in all. `gen_paints.py` now tells paint names from model names properly; underscores used to confuse it.
  - The jellyfish swims at a quarter of her old speed now (user report).
- [x] Playtest fixes (2026-10-25):
  - **Catfish:** the penguin harpy and the matamata also hunt Alex's Mobs catfish (`AlexsMobs.isFish`; it gives the mod's raw catfish if there is one, else a cod).
  - **Climbing:** the bear and the gecko now climb on purpose (`ClimbGoal`).
    - They head into a trunk or wall at least 3 blocks tall within 8 blocks and stay up for 8 to 10 s. Neither takes fall damage.
    - The gecko does it about once every 15 s.
    - The black bear does it now and then, and straight away when scared and a tree is near.
  - **Drop bear:** she looks for a trunk within 8 blocks (was 4) and drops on anyone within 3.5 blocks under her (was 2.5).
- [x] **Ninth wave** (2026-10-25), tested in `NinthWaveTests`:
  - **Sheep** (minotaur): shears (`FLAG_SHORN` hides the bones `wool`, `wool_hips`, `wool_head`), wool back after grazing (`ate`), any dye (synced; `DyedWoolLayer` tints `<model>_wool.png`). Vanilla sheep are her kin, so they follow her.
  - **Highland** and **yak** (minotaur): `canFreeze` false (so is the cattle's highland colouring); the yak takes a friend's chest (`FLAG_CHEST`, the `pack` bone) and opens two rows of cargo on a crouching empty hand. The cargo drops when she dies.
  - **Bat** (new family `bat`, `BatGirl`, `tools/gen_bat.py`): she flies; by day `RoostGoal` hangs her from the nearest ceiling within 8 blocks ("roost" turns the model over inside its own height). Befriended, she pings every 3 s: monsters within 16 of her friend glow. Kin of vanilla bats.
  - **Dark elf** (elf, deep dark): `dampensVibrations`; the Warden is her kin, but `Kinship.ignores` spares only her friends and Wardens cannot be introduced; she clears a Warden's anger at her friends every second. Echo shard and sculk trades.
  - **Medusa** (lamia): strangers who meet her eyes (the enderman's test) and the monsters she sees get Slowness VI; `FLAG_BLINDFOLD` (shown, and harmless) once anyone is her friend.
  - **Oni** (devil, nocturnal): hostile at night, a honey bottle calms her, cocoa beans make her flee the thrower for a minute; befriended, she walks to the door nearest her friend at night and fights monsters there.
  - **Mantis** (bug, walks): the new actions `PRAY` and `STRIKE`; she targets silverfish, endermites and spiders and lunges when within 4 blocks. The orchid variant turns invisible next to flowers.
  - **Ender slime** (slime, the End): teleports when hurt or wet; while wild she picks up `ENDERMAN_HOLDABLE` blocks (mob griefing on) and puts them down later. Kin of endermites.
  - **Wolpertinger** (jackalope): a second jump at the top of the first, glides at -0.15 (the new `isGliding` hook plays "fly"), no fall damage; once a day she swaps a nearby player's boots for leather ones, and gives them back when that player feeds her.
  - **Venus flytrap** (flora, swamps): `SNAP` holds a mob or stranger within 1.6 blocks for 3 s and bites every second; befriended, only monsters (never girls, villagers, golems or pets).
  - **Siren** (merfolk): strangers within 24 blocks are pulled toward her; friends swimming within 16 get Conduit Power.
  - **Paints:** opal, chrome, aurora and camo (35 in all). The firefly's blink is now a smooth pulse on the world clock (`GirlRenderer.fireflyPulse`); her light block is unchanged.
  - **Roster:** `tools/harmontide_roster.py` writes `HARMONTIDE_ROSTER.md` (the reference creatures, done and left).
- [ ] Dev client for the ninth wave: the bat's roost pose under a real ceiling (is she flush, does she clip?), the sheep's dyed wool layer and shorn look, the yak's pack, the medusa's blindfold, the flytrap's snap, the wolpertinger's glide, the dark elf's glow, and the four new paints.
- [x] **Tenth wave** (2026-10-25), Harmontide's own creatures, tested in `TenthWaveTests`:
  - **Tusktaur** (centaur): her hits also `sweep` everything in a cone in front of her (4 damage, knockback; never friends or girls); never freezes.
  - **Aurorantler** (centaur): a light block at her feet at night and Night Vision for a friend within 8; glowing antlers (glow masks).
  - **Rhiwool** (minotaur): `startCharge` at a target 4 to 14 blocks off runs her straight at it for 25 ticks, 8 damage and a big knockback to the first thing she hits; shears and dye as for the sheep (`isWoolly`).
  - **Meloncollie** (canid): every 2 s, befriended, `herd` drives the nearest stray cow, sheep, pig or chicken to the fence gate nearest her; every 2 minutes she plants a melon stem in empty farmland.
  - **Needlehog** (rodent): `curlUp` for 3 s after a hit or near a sprinting stranger, immune and pricking attackers (thorns, 2); targets silverfish; paper, books and maps go into her notes (her presents, dropped on death).
  - **Clockhare** (jackalope): chimes at dawn and dusk, warns her friend when `TIME_SINCE_REST` passes three nights, Haste within 8 blocks.
  - **Axolume** (merfolk, lush caves): a light where she is and Regeneration for her friend within 8; glowing gills, crest and fluke.
  - **Aegis boar** (armoured): no rolling up; `Armoured.shield` (on `LivingIncomingDamageEvent`) moves a third of any blow on her friend within 8 blocks to her and sets her on the attacker.
  - **Blaze cat** (nekomata, the Nether): `fireImmune`, sets what she hits alight, `kindle` lights campfires and candles within 4, Fire Resistance for her friend. Blazes and hoglins, like the Warden, spare only their girl's friends (`Kinship.ignores`).
  - **Gargoyle** (construct): flies; by day with no target she takes the `PERCH` pose and cannot be hurt; at night she fights monsters within 16 of her friend. Stone, not clay, when she breaks.
  - **Coin magpie** and **trickster raven** (`CoinMagpieHarpy`, `TricksterRavenHarpy`, the crow's rig in `tools/gen_coin_magpie_harpy.py` and `tools/gen_trickster_raven_harpy.py`): the magpie hoards the shinies she carries (9 slots) and pays a gold nugget with one; the raven plays a monster's voice beside strangers and, every 5 s, calls monsters that target her friend 14 blocks away. Each has her own egg, hatchling and spawn egg.
  - **Paints:** plush, pixel, origami and gummy (translucent), so 39 in all.
- [ ] Dev client for the tenth wave: the tusk sweep and rhiwool charge in play, the gargoyle's perch pose and flight, the aurorantler's and axolume's glow, the blaze cat's flames, the needlehog's ball, and the corvids' new colours. Not done: the harpy field guide has no entries for the coin magpie and the trickster raven (that guide is hand-written).
- [x] **Eleventh wave** (2026-10-26), Harmontide's helpers, tested in `EleventhWaveTests`:
  - **Anvil tortoise** (lizardfolk): a damaged tool or armour piece in hand gets a quarter of its durability back for one repair material from your inventory (`Lizardfolk.repair`, `isValidRepairItem`), no experience; not pushable, knockback resistant.
  - **Mailtoad** (`Mailtoad`, the cane toad's rig in `tools/gen_mailtoad.py`): a friend (10+) gives her an item, then a paper renamed in an anvil to a player's name. She walks to them within 48 blocks and hops straight to them beyond it, in the same dimension; offline or elsewhere, she waits and tells the sender every 5 minutes. A crouching empty hand from the sender takes it back; she drops it on death and never despawns while carrying.
  - **Gliderune** (marsupial): glides (falls at most 0.12 a tick, no fall damage); a friend's crouching empty hand puts her on their shoulder (Slow Falling for them; two seconds of crouching sets her down).
  - **Slothren** (armoured, nocturnal): by day `hangFrom` the nearest leaves or log within 8 above her (`HANG`); every 20 s `tend`: a flower on grass or moss on stone under her, and befriended, bone meal on crops and saplings within 4.
  - **Coatimano** (rodent): `findDig` suspicious sand or gravel within 12, `brushAt` it until it gives up its find, which goes to her friend's inventory.
  - **Waterlil** (nekomata): lily pads under and ahead of her and of her friend within 6, gone 10 s later unless someone is standing on them (and when she goes).
  - **Rookmourn** (`RookmournHarpy`, the crow's rig): every 2 s clears Poison, Nausea, Hunger and Wither from players and friends within 8; a golden apple has her start curing the nearest zombie villager.
  - **Cassowrath** (`CassowrathHarpy`, the ostrich's rig): the ostrich's behaviour with 40 health and 9 damage kicks; green cassowary eggs.
  - **Talosling** (construct): `windUp` with redstone (+5 minutes, 20 at most); wound, she fights monsters within 16 of her friend; run down (`RUNDOWN`), she stops where she is. Her spring is saved.
  - **Mantarrow** (merfolk): a friend's empty hand mounts her; she swims at boat-plus speed under her rider's steering, keeps them at the surface with Water Breathing, and leaps now and then.
  - **Sphinx** (centaur, never ridden): a hand asks a riddle (nine, `riddle.monstergirls.N`); the item held out next answers it: right gives an enchanted book, gold or emeralds (+5), wrong gives a pounce (4 damage, -2).
  - **Brownie** (elf, nocturnal, no trading): every 10 s at night, befriended, `tidy` merges part stacks in the chests within 8 and puts loose items into them.
  - **Paints:** synthwave, cookie, cloud and valentine (February only, like pumpkin in October), so 43 in all.
- [x] **Twelfth wave** (2026-10-26), tested in `TwelfthWaveTests`:
  - **Cheshire cat** (nekomata): `GirlRenderer.cheshireAlpha` fades her out unless the viewer looks straight at her; her grin is a glow layer drawn whole. Befriended, `blinkToFriend` puts her back beside a friend 14 blocks away.
  - **Kesaran pasaran** (flora): no gravity; `tickDrift` follows `Flora.wind` (the same for every puff, turning over the day) 2 to 5 blocks up, or her friend; Luck within 8; sugar `split`s her (`MAX_PUFFS` 3 within 16).
  - **Tsuchinoko** (lamia): `tickRoll` curls her up (`ROLL`) and speeds her when the ground ahead lies lower; honey; golden gifts.
  - **Platypus** (merfolk): `electrosense` makes monsters within 16 of her friend glow while she is in water; her spur poisons strangers who hit her.
  - **Moonseal** (merfolk): glowing crescents (glow mask); `moonSong` gives Dolphin's Grace to her swimming friend, and Regeneration on full-moon nights.
  - **Marmotaur** (rodent): `keepWatch` makes monsters within 16 of her friend glow and whistles at new ones (every 10 s at most).
  - **Bambadger** (rodent): every 30 s, befriended, `playFlute` grows the bamboo tips and sugar cane within 6.
  - **Dodobold** (`DodoboldHarpy`, `tools/gen_dodobold_harpy.py`, the penguin's rig made small and fat): flightless; a friend's crouching empty hand opens her 18-slot saddlebags (dropped on death; she never despawns while carrying); dodo eggs. Chickens are her kin.
  - **Boghare** (jackalope): Jump Boost II within 8 of her friend; lily pads are a treat; rabbits and frogs are kin.
  - **Gremlin** (devil, nocturnal): wild, every 15 s at night `tinker` flips a lever or swings a door within 6 (a cookie stops her for a day); befriended, furnaces within 6 tick twice while her friend is within 16.
  - **Karakasa obake** (devil): walks by hopping on one leg; `Devil.shelter` (on `ProjectileImpactEvent`) cancels projectiles about to hit a player within 4 blocks of her when she is their friend.
  - **Alebrije** (dragon): flies; befriended, every minute `paintSheep` dyes a sheep within 8 a new colour; dye presents.
  - **Paints:** porcelain, denim, moss and easter (April only, like valentine in February), so 47 in all.
  - **Waterlil:** only her head shows, resting on the lily pad (the pad's own bone); her body hangs under it.
- [x] **Thirteenth wave** (2026-10-26), tested in `ThirteenthWaveTests`:
  - **Rust otter** (merfolk): every minute, befriended, `scrape` takes one stage of weathering off the most weathered copper block within 6 of her friend (`WeatheringCopper.getPrevious`).
  - **Tin squirrel** (construct): `scrap` turns a damaged iron or gold tool or armour piece into 1 to 9 nuggets (more the less worn); she pulls nuggets within 1.5 blocks into her 9-slot hoard (saved; her presents come from it).
  - **Peaflare** (`PeaflareHarpy`, `tools/gen_peaflare_harpy.py`, the crow's rig with a crest and a nine-feather train): `dazzle` gives Slowness II and Weakness to monsters within 8 of her friend, at most every 10 s, with her "display". Peafowl eggs; chickens are her kin.
  - **Wattlefox** (kitsune): fireproof; `douse` puts out fire blocks within 6 of her friend (and the friend); every 2 minutes, befriended, `plantAcacia`.
  - **Scarabronze** (bug): armour 10; rotten flesh in her hand becomes 2 bone meal; `sunrise` gives her friend Haste for 5 minutes once a day in the first half minute of morning.
  - **Datebat** (bat): every 5 s at night, befriended, `pollinate` grows the crops within 6 of her a stage.
  - **Snowloper** (nekomata): on the powder-snow walkers list; never freezes; `snowStride` gives Speed II on snow.
  - **Jaglare** (nekomata): no fall damage; `climbTree` up onto leaves when wild and idle; `dropOnPrey` pounces on monsters and animals 2+ blocks below; `roar` weakens monsters near her friend.
  - **Squirricerat** (rodent): fights monsters near her friend (not creepers); every 2 minutes `buryAcorn` plants a sapling on open grass.
  - **Kamaitachi** (rodent): fast; `gust` cuts up to 6 grass, fern, cobweb and natural (not player-placed) leaf blocks within 3 of her friend each second.
  - **Momoshika** (centaur): `blossom` gives Regeneration by day within 8; pink petals, apples, cherry saplings, rarely a golden apple.
  - **Chochin-obake** (devil): her lantern glows (glow mask) and lights the night where she stands; befriended, she walks to her friend at night.
  - **Paints:** tartan, watercolour, hologram (translucent) and fireworks (January only), so 51 in all.
- [x] **Fixes from the thirteenth-wave playtest** (2026-10-27): the brownie now works every 2 s at night or while her friend sleeps (sleeping skips the night, so she used to miss it), tidying the chests near her friend as well as near herself; milk gives the bucket back. The chochin-obake's lantern and the moonseal's crescents glow brighter (`glow_boost` on the glow masks; light level 15 for the lantern; bigger crescents and a soft light at night for the moonseal). Befriended girls, harpies and frogs now send their death message to every online player with 10+ friendship (`GirlEvents.onFriendDeath`, honours `showDeathMessages`).
- [x] **Fourteenth wave** (2026-10-27), tested in `FourteenthWaveTests`:
  - **Gribboar** (armoured, never rolls up): every 30 s `root` digs up a find from dirt, grass, podzol or mycelium under her.
  - **Silkcoon** (rodent): within 3 blocks of water, `washed` takes the dye out of the stack in her friend's hand (wool, carpet, bed, terracotta, stained glass and panes, dyed leather).
  - **Bronzejaw ox** (minotaur, bulls one in three): `ploughOnLead` tills the grass, dirt or path under her into farmland while a player leads her.
  - **Cocoskink** (lizardfolk): every 30 s, befriended, `growCocoa` puts a cocoa pod on a bare side of a jungle log within 6 of her friend.
  - **Prairhorn** (centaur): speed 0.42, the fastest ride.
  - **Halberelk** (centaur): `Centaur.fell` (on `BlockEvent.BreakEvent`) fells up to 96 connected logs of the same kind above the one her friend cut within 8 of her, only if natural leaves touch them (no log cabins); crouching cuts one log.
  - **Vulphea** (kitsune): fireproof, her bite burns 3 s; once a game day, a killing blow `toAshes` instead (invisible, no AI, unhurtable) for a minute, then she rises at full health. Saved.
  - **Cait Sìth** (nekomata): `warnOffPhantoms` drops phantoms' aim at her friend and pushes them off; a milk bucket gives her friend Luck for 5 minutes (the bucket comes back).
  - **Cù-sìth** (canid): `bark` every 2 s while monsters are within 10 of her friend; the third sends them fleeing 14 blocks with Weakness.
  - **Yeti** (canid): `throwSnowball` every 1.5 s at monsters within 12 of her friend; every 5 minutes on snow `buildSnowGolem` (2 at most near her); never freezes; powder-snow walker.
  - **Undine** (slime): every 5 s, befriended, `water` sets farmland within 6 to full moisture and fills cauldrons; Water Breathing for her friend in the water.
  - **Ittan-momen** (devil): no legs, a four-bone cotton ribbon; she flies; a friend's empty hand mounts her and she flies where they look (the dragons' ride).
  - **Paints:** mosaic, chalk, leopard and autumn (November only), so 55 in all.
- [x] **Fifteenth wave** (2026-10-27), tested in `FifteenthWaveTests`:
  - **Crocora** (lizardfolk): every 0.5 s `rescue` checks her friend within 16; with 1 bubble of air left or less, eyes under water, she hauls them up to open air above the water and fills their air.
  - **Iron kobold** (lizardfolk): `isIronGear` stacks (anything an iron ingot repairs) are mended a quarter by the anvil tortoise's `repair`, for an iron ingot.
  - **Mooskuh** (minotaur, bulls one in three): every 20 s, standing still, `growMoss` turns the stone or cobblestone under her to moss. Milked like the cows.
  - **Bellmare** (centaur): every 2 s, befriended, `leadHerd` walks cows, sheep, goats and pigs within 12 (and over 4 away, not leashed) over to her.
  - **Vicunero** (centaur): every 1.5 s, `spitAtMonster` fires a llama spit at a monster within 12 of her and 10 of her friend.
  - **Rockhyrax** (rodent): every 1 s, `sentinel` makes the creepers within 12 of her friend glow for 10 s, with a flute whistle.
  - **Riverdillo** (armoured): every 30 s in the water, `angle` drops a cod, salmon or tropical fish at her friend's feet.
  - **Reeffin** (merfolk): every 10 s in the water, `tendReef` puts a coral, coral fan or sea pickle on bare sand or gravel under water within 4 of her.
  - **Caraviper** (lamia, the cobra's hood display too): every 2 s, `spitVenom` blinds and poisons a monster within 10 of her and her friend.
  - **Bakeneko** (nekomata): `ghostLights` gives light 7 at night, 13 beside her friend; her wisps, paws and tail tip glow (glow masks).
  - **Rokurokubi** (devil): `nightWatch` at night starts the "watch" action (a new `Action.WATCH`: the neck bone, shrunk away in every other animation, stretches 11.5 px) and makes the monsters within 24 of her friend glow.
  - **Huli jing** (kitsune): always nine tails (`rollTails`); every 2 s, `charm` gives her friend within 8 Hero of the Village in a village or with a villager within 16.
  - **Paints:** checkered, sunset, thunder (bolts glow: `PaintGlowLayer`, `gen_paints.GLOWING`) and burlap, so 59 in all.
  - The preview renderer (`harpy_lib.render`) now applies bone scale, so shrunk-away bones stay hidden in previews.
- [x] **Sixteenth wave** (2026-10-27), tested in `SixteenthWaveTests`:
  - **Jorogumo** (arachne, on two legs: `gen_arachne.py` legged_*, bones `bleg1..4_r/l` with knee `b` and claw `c` joints, `hair_bow`): every 2 s at night, befriended, `bindingWeb` drops a cobweb at the feet of a monster within 8 of her friend; `meltSnares` takes it away after 10 s (saved; all of them go when she does). The kimono's web print is `outfits._webbed` (`kimono_web`, `kimono_flower`).
  - **Dunefennec** (kitsune): every second, `desertGuide` gives her friend within 8 Speed II on sand and clears husks' aim at her friend.
  - **Floodcrab** (crustacean): every 20 s, befriended, `puddle` turns dirt or grass beside water within 4 of her into mud (mobGriefing).
  - **Temple mantis** (bug): every second, `cutTripwire` cuts (disarmed, like shears) a tripwire within 8 of her friend that is inside a structure.
  - **Sword koi** (merfolk): her hits set a drowned's hand drop chances to 2, so it always drops what it held.
  - **Squidkin** (mollusk, the octopus' rig without its cap, `tent_hair` locks): when her friend within 12 is hurt, `inkCloud` blinds the monsters within 6 and gives her friend 3 s of Invisibility (every 10 s at most).
  - **Reedbull** (minotaur, bulls one in three; `regalia`: `hair_hbands`, `blanket`): every 30 s, `plantReeds` puts sugar cane where it can grow within 6 of her friend (mobGriefing).
  - **Kikimora** (devil, `kerchief`): every 2 s at night, `nightLatch` shuts wooden doors, trapdoors and fence gates open within 8 of her friend (not the one they stand in).
  - **Cockatrice** (dragon, flies, `hair_comb`): every 3 s, `glare` gives Slowness IV for 5 s to a monster within 12 of her and her friend.
  - **Valkyrie** (elf, flies, `helm`): `GirlEvents.onPlayerDrops` lets a befriended valkyrie within 32 take a dead friend's drops (`gatherFallen`, saved); `returnFallen` gives them back once the friend is within 6. She does not despawn while she holds them, and drops them if she dies.
  - **Pumpkingull** (`PumpkingullHarpy`, `tools/gen_pumpkingull_harpy.py` on the gull's rig, dressed in a pumpkin, `pumpkin_hat`): every second, `calmEndermen` clears the aim of endermen within 16 of her at her friend. Gull eggs.
  - **Condorune** (`CondoruneHarpy`, `tools/gen_condorune_harpy.py` on the crow's rig): `catchFall` gives her friend within 16 Slow Falling and resets the fall once they have fallen over 6 blocks. Crow eggs.
  - **Paints:** neon (glows: `PaintGlowLayer`, `gen_paints.GLOWING`), zebra, terrazzo and clover (March only, `Painted.roll`), so 63 in all. `gen_paints.py` needs Pillow: `uv run --no-project --with pillow python tools/gen_paints.py`.
  - `add_harpy_extras.py` was re-run: the coin magpie, trickster raven, rookmourn, cassowrath, dodobold and peaflare had no belly bone or sit/lie poses until now.
- [x] **Life with the girls** (2026-10-27), tested in `HarmontideLifeTests`:
  - **Weapons** (`GirlArms`): the girls who fight for their friend on their own (not only when hurt: `GirlArms.FIGHTERS` plus the armoured, construct, crustacean and marsupial families; the elves carry their own bows) take a bow, crossbow or, with TaCZ, a gun from their friend (friendship 10+): right-click her with it, or throw it within 10 blocks of her and she fetches it (`FetchGoal`, even mid-fight; a thrown weapon replaces hers, which drops at her feet). Ammunition for her gun works the same way. Sneak + empty hand takes the weapon and her ammo pouch back. `RangedGoal` keeps her 4 to 14 blocks off and shoots what she targets (arrows never run out). Girls now render their main-hand item (`GirlRenderer.HeldItemLayer`, bone `hand_r`). Aiming poses (`client/AimPose`, over any animation on the shared arm bones, following her head's pitch): bow drawn across the chest and released on each shot, crossbow and gun held in both hands with a recoil kick; synced by `DATA_AIM` and the `EVENT_SHOT` entity event.
  - **TaCZ** (optional; the unofficial NeoForge port, `compileOnly maven.modrinth:tacz-1.21.1`): `compat/TaczCompat` is the only code naming TaCZ classes and does nothing without the mod. She fires through `IGunOperator` and reloads from her ammo pouch (9 slots, her `Capabilities.ItemHandler.ENTITY`); right-click her with ammunition for her gun to fill it.
  - **Water breathing**: `gen_girl_data.py` writes `minecraft:can_breathe_under_water` (every mermaid and mollusk but the snail, plus the frogs and a few others); ten mermaids and the squidkin had been drowning.
  - **Harmontide journal** (book + feather; `JournalItem`, `Journal`): every kind of our creatures a player has seen within 12 blocks, and the ones befriended, kept on the player (attachment `journal`, survives death); opened, it is written afresh as a book: a contents page, then a page per kind (her home and her signature, lang `journal.monstergirls.<id>.*` from the concepts).
  - **Settlements** (`HearthBlock`, `HearthBlockEntity`, `Settlement`): a hearth (campfire ringed with stone bricks). A befriended girl roaming free within 16 of her friend's hearth settles there, goes home when she strays (24 blocks by day, 6 at night), and a day later builds a 5x5 hut in her family's materials 6 to 14 blocks away (a block every half second; mobGriefing). With three residents or more, every morning each puts a present into the hearth's store.
  - **Expeditions** (`Expeditions`): an empty map sends a befriended grown girl off for a day (saved on her friend, attachment `expeditions`); she comes back to her hearth or her friend with three presents, finds of her family's country, now and then an emerald or a diamond, and one time in five a new girl of her family who is already her friend's friend.
- [x] Journal and hearth discoverable (2026-10-28): the journal opens again (its pages are synced to the client before the book screen opens), it starts with four guide pages (making friends, the hearth, expeditions, weapons), both recipes are unlocked at login, and every player gets a journal on their first visit (not on the GameTest server).
- [x] Homage variants from POP_CULTURE_VARIANTS.md: `jewel_thief` (cave bat), `ink_kid` (ika), `pink_dairy` (cattle minotaur), `sun_goddess` (wolf girl); palettes only, rare in the wild (`HOMAGES`). The rest of that list wants a prop each.
- [ ] Dev client for the life features: a held bow and a TaCZ gun in a girl's hand (position on `hand_r`; the item may need vanilla's extra 180-degree turn once the arm is raised), the aiming poses and recoil, TaCZ shooting and reloading from the pouch, the journal's pages, a hut going up and the market's presents, an expedition's return.
- [x] **Twentieth wave** (2026-10-28), tested in `TwentiethWaveTests`:
  - **Brasshorn ram** (minotaur, ram horns, wool, regalia; rams too): every second, `mountainFooting` gives her friend within 10 Jump Boost I.
  - **Ceibler** (centaur, fleece, blanket, bells, horns): every second, `trailGuide` gives her friend within 10 Speed I while they stand on dirt, grass or a path.
  - **Sobolnik** (rodent, pendant): every second, `burrowEyes` gives her friend within 10 Night Vision while they cannot see the sky.
  - **House mouse** (rodent, `mouse` ears in girl_lib, `bare_tail`, `whisker_nose`, `ghs_kit` props by variant): every 30 s, `crumbs` gives her hungry friend within 8 one food.
  - **Turtworld** (lizardfolk, dome shell, `island`): every 5 s, `livingIsland` bonemeals the saplings and crops within 4 of her friend.
  - **Fernstalker** (lizardfolk, `ferns`: fronds and `hair_crest`; a monster): every 2 s, `ambush` hits the nearest monster within 8 of her friend for 4.
  - **Basilisk** (lizardfolk, spines, `crown`: `hair_crown`): every 2 s, `stoneGaze` gives the nearest monster within 8 of her friend Slowness IV.
  - **Moo** (dragon, `trifrill`: `hair_frill`, brow and nose horns): every second, `herdStand` gives her friend within 8 Strength I when two or more monsters are within 8 of them.
  - **Hairypterix** (dragon, membrane wings, flies, `ptcrest`: `hair_ptcrest`): every half second, `updraft` gives her friend within 12 falling more than 3 blocks Slow Falling.
  - **Doggo** (merfolk, big ears, a water creature): every second, `lifeguard` refills the air of her friend within 10 when it is down to a third.
  - **Ghoul** (undead, tattered): every second, `carrion` eats one rotten flesh item within 8 of her friend and heals her by 2.
  - **Manticore** (nekomata, `stinger` on the tail tip): every 3 s, `tailSpike` hits the nearest monster within 10 of her friend for 3 with Slowness II.
  - **Mothman** (bug, moth wings, red eyes): every second at night, `omen` makes the monsters within 16 of her friend glow.
  - **Paints:** tie_dye, jade, lace and bluebell (May only, `Painted.roll`), so 79 in all.
  - **Homage variants** (`AbstractMonsterGirl.HOMAGES`): innkeeper, prankster, soul_keeper (house mouse), sage_princess (high elf), green_hero (wood elf, `hero_cap`: `hair_hcap`), tree_guard (ent), snow_wraith (yuki-onna), headless_rider and death_envoy (dullahan).
- [x] Own models for two homage looks (2026-10-28): `axolotl_mermaid_mud_fin` (Mudkip) and `shark_mermaid_trident_hood` (Gawr Gura), in `gen_merfolk.HOMAGE_MODELS`; `AbstractMonsterGirl.OWN_MODELS` makes `GirlRenderer` draw them; painted like any model. Tested in `TwentiethWaveTests.homagesWithTheirOwnModels`.
- [ ] Dev client: the Mudkip and Gura models (the trident in her hand while she swims and sits, the hood's fin, the head fan).
- [ ] Dev client for the twentieth wave: the turtworld's island, the fernstalker's fronds, the moo's frill, the manticore's sting, the mouse's ears and the Gregory props at game scale; the four paints; the nine homage looks.
- [ ] Not done for the twentieth wave: Celty's cat-eared helmet and Lala's scythe (both are palette-only for now); a dullahan-only prop each would do it.
- [x] **Nineteenth wave** (2026-10-28), tested in `NineteenthWaveTests`:
  - **Tin Jack** (construct, `witch_hat`: `hair_hat`, a lantern that glows): every second, `scarePhantoms` gives the phantoms within 24 of her friend Weakness II, clears their target and sends them off.
  - **Turquoise serpent** (lamia): every second, `scaleWard` gives her friend within 10, below half health, Resistance I.
  - **Hoop snake** (lamia): every 5 s, `sting` poisons the nearest monster within 8 of her friend.
  - **Moopie** (merfolk, `cowhorns`: `hair_horns`, cow ears, patches; a water creature): every 5 s, `seaMilk` clears Poison, Hunger and Nausea from her friend within 8.
  - **Tuskin** (minotaur, `tusks`, wool; bulls too): every second, `snowplough` packs the powder snow within 3 of her friend into snow blocks and thaws them.
  - **Sabre** (nekomata, `sabre` fangs): every 2 s, `finishingBite` kills a monster within 8 of her friend that has a quarter of its health or less.
  - **Obsidian hound** (canid, `club`: a macuahuitl on her back): every second, `coolLava` turns still lava within 2 of her friend's feet to obsidian.
  - **Kasurenijimi** (undead, `smudge` over her face): every second, `smudge` gives her sneaking friend within 12 Invisibility.
  - **Draugr** (undead, torn hem): every second, `layDead` makes the undead within 16 that hunt her friend forget them.
  - **Camazotz** (bat, `leafnose`): every 2 s at night, `nightBite` hurts the nearest monster within 8 of her friend for 2 and heals her friend by 2.
  - **Akaname** (devil, `tongue`): every 2 s, `scrub` turns one mossy cobblestone or stone brick block (stairs, slabs, walls too) within 6 of her friend clean.
  - **Emberu** (`EmberuHarpy`, `tools/gen_emberu_harpy.py`: the ostrich's rig recoloured; runs, cannot fly; hatchling, eggs, spawns in savannas and badlands, chickens are kin): every second, `kickAway` kicks the monsters within 3 of her friend away.
  - **Paints:** comic, blueprint, koi and lemonade (June only, `Painted.roll`), so 75 in all.
  - **Homage variants** (`AbstractMonsterGirl.HOMAGES`): octo_agent, ninja_chameleon, shell_trooper, town_clerk, scythe_green, leaf_sprout, mud_fin, trident_hood.
- [ ] Dev client for the nineteenth wave: Tin Jack's hat and lantern, the tuskin's tusks and the sabre's fangs at game scale, the kasurenijimi's smudged face, the emberu running and kicking, the four paints, the eight homage looks.
- [ ] Not done for the nineteenth wave: a harpy field guide entry for the emberu (that guide is hand-written).
- [x] **Eighteenth wave** (2026-10-28), tested in `EighteenthWaveTests`:
  - **Spoodr** (arachne, `tufts`: `hair_tufts`): every 2 s, `sweepWebs` clears a cobweb within 6 of her friend and gives them its string.
  - **Ushi-oni** (arachne, `ox_horns`: `hair_horns`): `bellow`, at most every 20 s: the monsters within 10 of her friend drop their target, get Slowness II and run.
  - **Urchskin** (lamia, `spines` on every tail segment, `hair_spines`; breathes water): `Lamia.onFriendHurt` (LivingDamageEvent.Post): a monster that hurts her friend within 8 of her takes 3 thorns damage (`prick`).
  - **Pineskrell** (rodent, `cones` by her ear, glow mask): every 5 s, `oreSense` sparkles up to 16 ores (`c:ores`) within 6 of her friend.
  - **Turtfolk** (lizardfolk, shell, `flower`): `shellGuard`, at most every 30 s: Absorption I for 30 s for her friend within 10 who has none.
  - **Tarnhelm** (canid, `tarnhelm` helm, its gem glows): every 5 s, `sentinel` gives raiders within 48 of her friend Glowing.
  - **Palmyr** (centaur, `fronds` on the antlers, a fan tail, `diamonds` at scale 2.4): every second, `fleetFoot` gives her sprinting friend within 12 Speed I.
  - **Skjoldram** (minotaur, ram horns, wool, `browplate`; bulls too): every half second, `ramCharge` butts the creepers within 6 of her friend away and resets their fuse.
  - **Bastet** (nekomata): every half second, `wardCreepers` clears creepers within 12 of her friend off them, resets their fuse and sends them away.
  - **Rusalka** (merfolk): every second, `riverLuck` gives her friend within 16 Luck while their hook is out.
  - **Domovoi** (elf, `kerchief`, soot on her nose): `keepHearth` gives the furnaces within 6 of her friend (found every 2 s) a second tick every tick.
  - **Mirror crane** (`MirrorCraneHarpy`, `tools/gen_mirror_crane_harpy.py`: the flamingo's rig recoloured; hatchling, eggs, spawns in rivers, swamps, cherry groves and mangroves): `onProjectileImpact`: one projectile in three that would hit her friend within 16 of her is turned back (`deflect`), not her friend's own.
  - **Paints:** cow, rust, lava lamp (blobs glow: `PaintGlowLayer`, `gen_paints.GLOWING`) and sunflower (August only, `Painted.roll`), so 71 in all.
  - `tools/add_harpy_extras.py` also gave the condorune and pumpkingull their belly, sit and lie, which the sixteenth wave had missed.
- [ ] Dev client for the eighteenth wave: the urchskin's spines and the ushi-oni's horns at game scale, the pineskrell's cones and the tarnhelm's gem at night, the mirror crane turning a skeleton's arrows, the domovoi at a furnace row, the four paints (lava lamp at night).
- [ ] Not done for the eighteenth wave: a harpy field guide entry for the mirror crane (that guide is hand-written).
- [x] **Seventeenth wave** (2026-10-27), tested in `SeventeenthWaveTests`:
  - **Censer lion** (nekomata, `censer` at her hip): every second, `incense` resets her friend's time since rest (within 16, so no phantoms spawn for them) and clears the aim of phantoms within 32 of her at her friend.
  - **Conch mail turtle** (lizardfolk, dome shell with a `conch` spire): every second, `conchCall` gives her friend within 8, eyes underwater, Conduit Power.
  - **Date palm beetle** (bug, `elytra`, `dates`): every 10 s, `shareDate` feeds her friend within 6 three hunger points when they have 14 or fewer.
  - **Dustbison** (minotaur, bulls one in three): every half second, `dustBath` puts out her burning friend within 8 and gives 5 s of Fire Resistance.
  - **Frondbat** (bat, flies, `hair_crown` of flowers): every 30 s, `shakeLeaves` drops what a leaf block within 6 of her friend drops when broken (a stick at least), and an apple one time in four.
  - **Sawfin** (merfolk, `saw` rostrum and fleur-de-lis shield): every second, `sapperAid` gives her friend within 8, eyes underwater, Haste II.
  - **Stagel** and **Lamassu** (centaurs; the lamassu on the sphinx's body with goat horns): every second, `blessFriend` gives her friend within 8 Jump Boost II (stagel) or Resistance I (lamassu).
  - **Klabuster** (slime, `swirl`: the swirl on her head, `hair_strand` with the tuft; the art's dingleberry, reworked 2026-10-27 from a misread water drop): every 30 s, `manure` bone-meals the crops within 6 of her friend.
  - **Dryad** (flora, `plant="dryad"`: antlers with leaves, `hair_leaves`): every 30 s, `nurture` bone-meals the saplings within 8 of her friend.
  - **Amarok** (canid): every second, `packMother` gives her friend's tamed wolves within 12 of her Strength I.
  - **Yurei** (undead, floats on the will-o'-wisp's wisp in white, no glow; a night monster spawn): every second at night, `haunt` gives the monsters within 8 of her friend Weakness I.
  - **Paints:** polka, circuit (traces glow: `PaintGlowLayer`, `gen_paints.GLOWING`), giraffe and watermelon (July only, `Painted.roll`), so 67 in all.
- [ ] Dev client for the seventeenth wave: the conch spire and the sawfin's saw at game scale, the yurei floating (and her satchel), the frondbat's crown, the four paints (circuit at night).
- [ ] Dev client for the sixteenth wave: the jorogumo's back legs at game scale (do they clip walls or doorways?), her four eyes, the kikimora at a real house at night, the valkyrie holding and returning a real death's drops (with keepInventory off and on), the condorune catching a real fall, the pumpkin hat, and the four paints (neon at night).
- [x] The eight sixteenth-wave originals reworked against `harmontide-creature-images/` (their blue looks): the floodcrab's stone fan shell (`hair_fan`), the temple mantis' ring arch (`halo`, `hair_arch`), the sword koi's katana (`katana`), the reedbull's regalia, the squidkin on the octopus body, the pumpkingull in a pumpkin (old look: the `orange` variant), the dunefennec and condorune recoloured. `tools/harmontide_roster.py` now reads that folder.
- [ ] Not done for the sixteenth wave: harpy field guide entries for the pumpkingull and condorune (that guide is hand-written).
- [ ] Dev client for the fifteenth wave: the rokurokubi's neck at night (does the head ride up smoothly, and come back down at dawn?), the crocora rescue in deep water, the bellmare walking a herd home, the vicunero's spit, the reeffin's coral, and the four paints (thunder at night).
- [ ] Dev client for the fourteenth wave: the ittan-momen ride (does the rider sit well on the ribbon, can you land?), the halberelk felling a big spruce and leaving a log cabin alone, the vulphea's ashes and rising, the bronzejaw ox ploughing on a lead, the silkcoon washing, and the four paints.
- [ ] Dev client for the thirteenth wave: the peaflare's display fan in play, the jaglare in real jungle trees, the kamaitachi's gusts near a player's leaf builds (persistent leaves should be spared), the chochin-obake's light, and the hologram paint.
- [ ] Dev client for the twelfth wave: the cheshire fade and grin in first and third person, the kesaran's drift (does she get stuck under trees?), the tsuchinoko's roll, the dodobold's saddlebags screen, the karakasa catching skeleton arrows, the gremlin's furnace speed, and the waterlil's body under the water.
- [ ] Server playtest for the mailtoad and the brownie (they need heavy testing on a server): delivery to a player who logs off mid-walk, to another dimension, to a name with different case, two mailtoads to one player; the brownie with modded containers, double chests, shulker boxes inside chests, and item frames.
- [ ] Dev client for the eleventh wave: the slothren's hang pose under a real branch, the gliderune on a shoulder, the mantarrow ride and leap, the sphinx's wings and pounce, the talosling run down, the waterlil's pads under a sprinting friend, and the four new paints.
- [x] **Jade** (optional, `compat/JadePlugin`): every girl, harpy and frog shows "Pregnant: yes/no" (not on bulls, the leshy, the vodyanoy, constructs or young) and "Friendship: 12/25" (the looking player's affection out of the next milestone, 10/25/60/100; `BondInfo`). The server sends both, so a server without Jade shows neither. Jade is in the dev client but kept off the game test server (its join packet breaks the tests' fake players). `dustBathesOnSand` failed once more (2026-10-25), unrelated.
- [ ] Dev client: check the two Jade lines look right under a girl's name, and can be toggled in Jade's plugin settings.
- [ ] Dev client: the drop bear with the wider trigger (`/summon monstergirls:koala_marsupial ~ ~ ~ {Variant:"drop_bear"}` by day near a tree, then walk under her), and the bear and gecko climbing.
- [ ] Dev client for the eighth wave:
  - the black bear and the gecko climbing
  - the koala on her trunk, and where the joey sits on her back
  - the drop bear's drop
  - the firefly blink and the jar model
  - the mummy's layers and spin
  - the flamingo's one-leg pose and march
  - stained glass in game
- [ ] Dev client for the seventh wave: the crabs' sideways walk, the joey's seat in the pouch, the golem's break-apart (and that she does not tip over), the jellyfish's translucency and glow, and the glow and magma paints in the dark.
- [ ] Flaky tests: `strandedTadpoleFindsThePond`, `wrecksBoatsAtTheRock` and `orbWeaverSpinsAHuntingWeb` each failed one full run (2026-10-25) and passed the others. On 2026-10-26 `trustedMobComesToTheBed`, `strandedTadpoleFindsThePond`, `dustBathesOnSand` and `rootedFloraGrowsCrops` each failed one of two runs. `oniFleesBeansAndTakesADrink` failed once because `isDay` lags `setDayTime` by a tick; it now waits a tick. `loneBullywugBacksDownIntoWater` failed in the thirteenth- and fourteenth-wave runs (2026-10-26/27): the flee went for the nearest water as the crow flies, which was water from a neighbouring test below the platform edge, and she stood at the edge. Fixed: fleeing and dry frogs now take the nearest water they have a path to (`AbstractAnuran.findReachableWater`).
- [ ] Flaky tests (2026-10-24): `mermaidFindsSeashells` failed twice in a row, then passed. `pythonLaysAClutchThatHatches` failed once: she can wander more than 5 blocks before laying, out of the area the test searches.
- [x] **Sleep schedules** (`SleepGoal`): day girls lie down and sleep at night, nocturnal ones by day. The nocturnal ones are the undead, foxes, moths and mosquitoes, the night elf, the succubus, the jiang shi, the jack o'lantern, the tapir and the ocelot. A hit or a hand wakes her for 30 s. Bed-sharing comes first, a follow order keeps her up, and nekomata (their own naps), merfolk, harpies and anurans keep their own rest.
- [ ] Third wave of creatures: see HARMONTIDE_CONCEPTS.md "Third wave", with reference art in `D:\wamp64\www\images\creatures\`.
- [x] **Harmontide paints:** a girl who spawns (naturally or from a spawn egg) is painted 1 time in 64, in blue, red, green, yellow, purple, black or inverted. Daughters inherit it, whether born or hatched from her clutch, and it is saved. Textures come from `tools/gen_paints.py` (run it after the model scripts): her body takes the paint's colour and clothes shift with it, like the references. Harpies and anurans too (since 2026-10-24): a painted mother's eggs and frogspawn hatch painted young, and hatchlings, tadpoles and froglets have painted textures as well. Limit: a harpy egg picked up as an item loses its paint, as it already loses the father.
- [x] **Treat paints** (2026-10-24), five more that are not palette swaps. Each one is drawn per cube face from the geo file, so drips run down from each face's top edge:
  - **Chocolate:** a milk-chocolate girl with glossy dark chocolate dripping down every part (only a rim on her face).
  - **Ice cream:** strawberry ice cream with sprinkles melting off her hair.
  - **Strawberry:** strawberry hair with seeds and a leafy crown.
  - **Jello:** glossy and see-through, drawn translucent.
  - **Milk:** creamy porcelain white, keeping her eyes and outlines.
  They join the 1-in-64 roll, so there are 12 paints in all.
- [x] **Six more paints** (2026-10-24), whole-body finishes: **gold** (a gilded statue with glints), **marble** (white stone with grey veins across every part), **galaxy** (a deep violet night sky with a drifting nebula and stars), **ghost** (pale blue and see-through, drawn translucent like jello), **honey** (amber honey dripping from her hair and pooled on every top), **rainbow** (each part its own colour, softly graded). Eyes and outlines stay. 18 paints in all. `python tools/gen_paints.py gold marble` now regenerates only the paints named.
- [ ] Dev client: ghost's translucency (on top of jello's), and whether rainbow reads as fun or as noise at game scale.
- [ ] Dev client: how the drips read at game scale, and jello's translucency on the harpies' wings.
- [x] **Expecting bellies** no longer stretch abs over the bump: the belly bone has its own smooth skin texture (`belly_skin`). All 17 girl families were regenerated.
- [x] Decided: no Kachina and no Rainbow Serpent. Bears share a family with kobolds and the other canids ("canid girls").
- Not doing: the full-moon "phantom pregnancy" of every befriended girl. It is still the whole household getting pregnant by the player in one night. Normal single bed-sharing stays as it is.
- Not doing: the full-moon event where every befriended girl queues at the bed to mate (a group sex scene; the mod is low ecchi). A non-sexual full moon is fine: a moonlit vigil, or moods.
- [ ] Decide on the sensitive ones: Kachina (recommendation: drop it), Uktena, Wayob. Decided (2026-10-26): no Thunderbird.
- [ ] Decide which new families are worth their own class: Giants, Celestials, Horsemen, Fury, Crustaceans.

## 0d. Kinship (girls and their vanilla counterparts)

In since 2026-10-21: `entity/girl/Kinship.java`, tested in `KinshipTests`.
- A girl draws her vanilla kin to her: they follow her within 16 blocks. While she does not see a player as prey, her kin neither flee from that player nor attack them. So a calmed zombie girl's zombies leave you alone, and an ocelot nekomata's ocelots stop running.
- Feed her and one of her kin her family's food within 30 seconds, close together: she **introduces** you. Cats, horses, donkeys, mules and llamas are tamed, ocelots and foxes trust you, and everything else becomes a persistent friend that follows you and never targets a player.
- Kin: nekomata and cats (ocelot nekomata and ocelots), kitsune and foxes, arachne and spiders, the horse and zebra centaur and horses, donkeys and mules, the alpaca and llamas, the goat centaur and goats, dolphin and orca mermaids and dolphins, the axolotl mermaid and axolotls, slimes and slimes (magma: magma cubes), jackalopes and rabbits, minotaurs and cows and mooshrooms, kraken, ika and octopus and squid, the zombie girl and zombies, husks, drowned and zombie villagers, the phantom girl and phantoms, the turtles and turtles, bees and bees. Everyone else has no kin and bonds and breeds with players only, as before. Minotaur cows already breed with bulls.
- [ ] Playtest: does a befriended zombie still go for villagers? (It does today. Decide whether friends should leave villagers alone too.) Befriended zombies still burn in daylight.
- [ ] Harpies and anurans are separate classes, not covered yet: parrots and chickens, frogs.
- [ ] Slimes and phantoms have no pathfinding, so they do not visibly follow. They only stop attacking.

## 0e. Stick orders and stances

In since 2026-10-21: `entity/Orders.java`, used by harpies, anurans and every girl family. Tested in `OrdersTests`.
- **Right-click with a stick** cycles **Sit → Patrol → Follow → Free roam**. Patrol: she roams within 10 blocks of the spot where you gave the order. Follow: she stays within 7 blocks of you, and catches up at once if more than 40 blocks behind. Anyone can make her sit and get up again; patrol and follow need her to be fond of you (10+).
- **Sneak + right-click with a stick** cycles **Hostile → Neutral → Self-defence → Peaceful** (fond only). Until a stance is set she follows her instincts, as before.
  - Hostile attacks hostile mobs, wild girls that are hostile to you, and anyone who hurts her or you.
  - Neutral defends herself and you.
  - Self-defence only hits back.
  - Peaceful never fights.
- **No friendly fire:** two companions (both fond of the same player) never hurt or target each other, nor each other's befriended vanilla mobs. That is the phoenix and ostrich crossfire fix. Stray shots never hit her player either; deliberate pokes (a swat, a clumsy kick) still do.
- [ ] **Next command:** even a sitting girl should run for water when she is drying out (anurans, merfolk) or on fire. She should get up, save herself and then go back to her spot.
- [ ] Playtest: is patrol at 10 blocks and follow at 7 the right size? Does the follow teleport feel right for flying harpies?
- [ ] Befriended vanilla mobs (Kinship) do not take stick orders yet.
- [ ] `RelationshipTests.eggHatchesIntoAGrowingHatchling` failed once and then passed on the next run. It is flaky and needs looking at.

## 0f. Answers to the field guides' open questions (2026-10-22)

The answers given on 2026-10-22, as a work list. Ticked = in game.

**All families**
- [x] Befriended vanilla mobs (Kinship) take stick orders and stances too.
- [ ] Dev-client test: harpy hatchlings growing up (`eggHatchesIntoAGrowingHatchling` was flaky once).
- [x] Crash fix (2026-10-22, at random tick speed 100): `BuildNestGoal` ticked once more after losing its nest spot. Not growth-related. Regression test: `AnswersTests.nestGoalSurvivesALostSpot`.

**Harpy**
- [x] The female/male colouring split (grey-brown against black-and-white) becomes two variants of the same mob. Already the case: the ostrich's `hen` variant.
- [x] Ostrich kick: 3 hearts on normal (vanilla difficulty scaling for the rest), high knockback. It should hurt.
- [x] Territory-edge offering: any herbivore food (new item tag `monstergirls:herbivore_food`).
- [x] Winged cap (leather + feathers): slows the fall (capped fall speed) and cuts fall damage to a fifth; sneaking folds the wings.
- [x] Winged cap: a GeckoLib armour model (`geo/item/armor/winged_cap.geo.json`) whose wings beat while falling and fold otherwise. Check it in the dev client: the server loads it, but nobody has seen it render yet.

**Anuran**
- [x] Skirt and cattails become a removable outfit (the bullywug). At high friendship shears take them off: you get a lily pad and three sugar cane. A lily pad puts them back. Woven reed shorts are painted under the skirt, so she stays dressed.

**Nekomata**
- [x] A true nekomata's spare life stays hers alone (no player totem). Answer: no.
- [x] Kittens can be put on a lead.

**Kitsune**
- [x] The nine-tailed elder spawns naturally, very rarely, in cherry groves.
- [x] The stash game works on other mobs' drops too.

**Lamia**
- [x] Shed Skin crafts a light armour piece (a snakeskin cloak).
- [x] She carries her player in her coils: a slow mount off sand, fast on sand.

**Arachne**
- [x] The silk cocoon present becomes an item you open.
- [x] The spider truce covers cave spiders (it already did: `GirlEvents.onTarget`).
- [x] Spider-silk trousers and shoes: light, iron-level protection, but they wear out faster.

**Centaur**
- [x] Ridden centaurs have stamina. A 15-second gallop, then a slow walk until a third is back.
- [x] The zebra lets only her father ride her.

**Merfolk**
- [x] Young merfolk can be carried in a water bucket.
- [x] The dragon koi can fly (slow levitation). Out of water she floats about a block above the ground, without gravity.
- [x] All merfolk fetch items the player dropped. Items that fell into the water.
- [x] At high friendship they share air with a drowning player, then surface themselves so they don't drown. Once every 30 seconds. Water breathing at high friendship was replaced by this; devoted still gives conduit power.
- [x] Merfolk merchants: neutral wild merfolk who trade ocean loot (from the loot pools) like villagers. One wild grown merfolk in five. Empty hand opens her trades. Wares are rolled from the ruin, shipwreck and buried treasure loot tables; she buys cod and gold.
- [x] Merfolk scales drop now and then, even from wild ones. They craft into flippers (faster swimming).

**Slime**
- [x] Two slime species can merge into a mixed slime. At high friendship with both, a slime ball on one while a slime of another kind is within 3 blocks: they flow into one marbled slime (ten looks, one per pair). She keeps both girls' data. A killing blow, or sneak + slime ball, separates them again.
- [x] A slime's inventory spills out when she splits.

**Jackalope**
- [x] The Inaba brings rice cakes (a new food) at full devotion.
- [x] Rice cakes craft into mochi with other foods (apple mochi, melon mochi...).
- Not doing: several jackalopes breeding with one sleeping player at once. It turns the abstract bed-sharing into a group scene, and the mod is low ecchi.

**Minotaur**
- [x] Cheese becomes its own item.
- [x] Only cows (female minotaurs) can be milked (already the case).
- Not doing: the embarrassed-but-gives-milk-anyway reaction, and milk dripping from the chest. Both sexualise the milking.

**Mollusk**
- [x] The kraken can pull a boat (a ride). High friendship: in the water next to her player's boat she tows it the way the rider looks.
- [x] Every squid type (kraken, ika, octopus) drops an ink sac now and then.

**Elves**
- [ ] High elf settlements grow beyond one pavilion, but only if worldgen and performance stay unaffected.

**Undead**
- [x] The living doll sits on bookshelves (any block that powers an enchanting table). With nobody to stalk she hops onto a free bookshelf top within 10 blocks and sits there for one to two minutes, watching the room.

**Lizardfolk**
- [x] The chameleon's colours follow the biome.

**Dragon**
- [x] The wyrm's fire breath sets fire on top of blocks (normal fire ticks do the rest, no burnt block variants). Obeys `mobGriefing`.

**Devil**
- [x] The lesser devil steals from chests too.

**Bug**
- [x] The moth eats wool. Wool and carpet lying about (she heals on it). She never eats wool built into something.
- [x] Bee girls collect nectar from flora girls. A bee girl carrying flora nectar near a vanilla flower has a small chance to turn it into a flora girl.

**Flora**
- [x] The alraune can be planted as a flower block. At high friendship, sneak with a shovel: she becomes an Alraune Bloom item. Plant it on dirt; right-click or break it and she steps out with her bonds intact.
- [x] Nectar from flora girls at high friendship, collected with a glass bottle (built as gathering, not "milking").
- [x] Nectar waters and feeds a field: pour a bottle on farmland and it soaks every block within 2 and gives every crop a growth spurt.
- [x] Nectar as a placeable fluid (Bucket of Nectar: three nectar bottles and a bucket). It hydrates farmland like water, and crops within 4 blocks grow on half of their growth ticks. It is slow and honey-like, makes no infinite source, and renders as golden-tinted water.

## 0g. Round of 2026-10-22 (evening)

- [x] **Cowbell:** a minotaur cow's bell rings once (a note over her head) when her milk is ready again. This replaces the milk-drip idea, which stays out (it sexualises her).
- [x] **Orders outrank following:** under sit or patrol a girl no longer follows her player, flocks, flies home, brings presents, begs or joins your bed. Under follow only her own following gives way (presents and bed stay). This covers harpies (leader, flock, home, owl night-stalking), anurans and girls.
- [x] **Healing balm** (nectar bottle, honey bottle and a small flower make 3, or a present from a healer): use it on a girl who is fond of you, or on a befriended vanilla mob, for 10 health and Regeneration II. Healers are the flora, the verdant and honey slimes, the snail, the fairies and the axolotl. At high friendship one present in five is a balm, and every 30 seconds they tend your most-hurt companion nearby (4 health and a little regeneration).
- [x] **Farmhands:** a girl patrolling (stick) for a player she is fond of at high friendship harvests ripe crops in her patrol area, replants them from the harvest, and stores the rest in a chest or barrel there (or drops it). Flora girls also nudge growing crops along. Obeys mobGriefing. Merfolk don't farm.
- [x] **Slimes in any RGB colour:** a dye mixes its colour into hers, a water bottle washes it out, and merging mixes the two colours. Mixing averages the hues round the colour wheel and keeps brightness and most of the saturation (red + blue = magenta, red + green = yellow), so repeated merges never turn white. Dyed slimes use `<species>_tint.png` (light greys) with the colour applied by the renderer.
- [ ] Dev client: how the tint looks on each slime (the core and eyes take the colour too), the cowbell, and how harpies settle on patrol.
- [x] Farming, round 2: harpies and anurans farm too, and fond (10) is enough, the same as for patrolling. The patrol message says "tends the fields" when farmland is near. Only works while her player is in the world.
- [x] Slime merging tells you why it didn't happen (both slimes need high friendship, 25).
- [x] The Harmontide creative tab holds everything (spawn eggs also stay in vanilla's Spawn Eggs tab).
- [x] Friendship Charm (creative only, no recipe): use it on any girl to fill her friendship to the full.
- [x] Papi (the rare sparrow variant) looks like her anime self: sky-blue hair and wings, orange-amber eyes, a white sleeveless shirt and denim shorts (plus the ahoge and yellow bird legs she already had).
- [x] Easter bunny egg-hiding: when no spot fits she retries after 10 s, not after 2 to 4 minutes (also fixed a flaky test).

## 1. More species ideas

Ideas to pick from. Each new species needs a gen module, a field-guide entry, tests and its own favourite food.

- **Nekomata:** caracal, black panther, ocelot
- **Kitsune:** kumiho (nine tails from birth), bat-eared fox
- **Lamia:** viper, anaconda, medusa (a petrifying gaze)
- **Arachne:** black widow, peacock spider, cave spider
- **Centaur:** donkey, unicorn, camel, elk
- **Merfolk:** pufferfish, angler, manatee, siren (a luring song)
- **Slime:** ender slime (teleports), copper slime (oxidises), crystal slime
- **Harpy:** penguin, flamingo, hummingbird, vulture, parrot, duck (mallard, mandarin, white farm duck)
- **Anuran:** axolotl-like newt girls (a salamander sub-family?)
- **Jackalope:** wolpertinger, snowshoe hare
- **Minotaur:** yak, highland cow
- **Mollusk:** jellyfish, nautilus, sea slug (nudibranch)
- **Elf:** snow elf, dark elf of the deep dark
- **Undead:** mummy, banshee, ghoul, skeleton
- **Lizardfolk:** gecko (climbs walls), komodo (venomous bite), axolotl
- **Dragon:** amphiptere, sea serpent, ender dragon girl
- **Devil:** imp, oni, hellhound
- **Bug:** ant with queen, mantis, firefly, beetle, spider-wasp
- **Flora:** mushroom-cap dryad, venus flytrap, rose, lotus (water)
- **Whole new families:** wolf girls, bats, kobolds, golems, cyclops, sheep girls, sphinx, gorgons

## 2. Seagull/sparrow arrow farm

- [x] Gulls shed feathers and sometimes flint while roosting in a hopper; sparrows shed feathers and sticks. Done in the 2026-10-20 batch: `AbstractHarpy.roostDrop`, `RoostInHopperGoal`. Covered by GameTests.
- [ ] Check in a real client that an arrow farm built from it works (hopper → chest, drop rate, roost stays put).
- [ ] Decide whether the flint and stick rates feel right. Should a fletching table nearby give a bonus?

## 3. Custom sounds for every species

At the moment **every** sound is a vanilla sound.
- Harpies and anurans already have their own sound events in `sounds.json`, but those still point at vanilla files. To replace them, drop `.ogg` files into `assets/monstergirls/sounds/` and edit `sounds.json`; no code change is needed.
- The girl families (`entity/girl/*`) call `SoundEvents.*` directly. They first need a sound event per species, either registered in `ModSounds` or generated by a script, and then the `getAmbientSound`/`getHurtSound`/`getDeathSound` and `playSound` calls switched over.
- Base set per species: **ambient, hurt, death, eat**. Below, "+" marks the extra action sounds and "currently" gives the vanilla sound in use.

### Harpies (event exists, file is vanilla)
| Species | Sounds to replace (currently) |
|---|---|
| sparrow | ambient, hurt, death, eat (parrot); + sing (flute note) |
| crow | caw, giggle, hurt, death, eat (parrot); + laugh (witch celebrate) |
| owl | hoot (flute note); hurt, death, eat (parrot) |
| seagull | ambient, hurt, death, eat (parrot); + sing (harp note) |
| albatross | ambient, hurt, death, eat (parrot); + bill clack (wooden button) |
| ostrich | ambient (sniffer), hurt/death/eat (camel); + hiss (goat pre-ram), kick (goat impact), step (camel step) |
| riflebird | ambient, hurt, death, eat (parrot); + sing (pling), bass, hat (note blocks), lay (chicken plop) |
| phoenix | ambient (blaze breathe), hurt (blaze hit), death (parrot); + shoot (blaze shoot), cry (icechime), rebirth (fire charge) |
| all harpies | + egg laid (chicken egg) |

### Anurans (event exists, file is vanilla frog)
All 12 species have **ambient, hurt, death, eat, tongue, bellow**, all on the frog sounds: bullywug, mucus toad, bullfrog, tree frog, poison dart frog, glass frog, goliath frog, desert rain frog, fire-bellied toad, cane toad, natterjack, Surinam toad. Bullywug also has a mucus squish (slime squish small).

### Girl families (need new events)
| Family / species | ambient | hurt | death | extras |
|---|---|---|---|---|
| **Nekomata** (serval, tiger, lioness, snow leopard, cheetah, lynx) | cat ambient / purr | cat hurt | cat death | hiss, roar (ravager roar), purreow, second life (totem) |
| **Kitsune** (red, arctic, fennec, corsac, gray) | fox sleep by day / fox ambient | fox hurt | fox death | screech, bite |
| **Lamia** (python, cobra, sea snake, rattlesnake, boa) | axolotl idle | cat hiss | axolotl death | rattle (chain armour equip), spit (llama spit), hiss |
| **Arachne** (jumping spider, orb weaver, tarantula, trapdoor, wolf spider) | spider ambient | spider hurt | spider death | ambush burst, brushing |
| **Centaur:** horse, reindeer | horse ambient | horse hurt | horse death | step, saddle, chestplate, brush |
| zebra | donkey ambient | horse hurt | horse death | angry bray (donkey angry) |
| goat | goat ambient | goat hurt | horse death | |
| alpaca | llama ambient | llama hurt | horse death | spit, chest (donkey chest), shear |
| **Merfolk:** orca, shark, seal, dolphin | dolphin ambient (water/air) | dolphin hurt | dolphin death | tease/play (dolphin play), shell found (brush sand) |
| koi | fish swim | dolphin hurt | dolphin death | |
| axolotl | axolotl idle (water/air) | axolotl hurt | axolotl death | |
| **Slime:** verdant, magma, honey, frost | slime squish small | slime hurt small | slime death small | bounce (slime jump), split/merge squish, magma crackle (furnace), honey bottle fill |
| amethyst | amethyst chime | slime hurt small | slime death small | resonance chime |
| **Jackalope** (Inaba, Jack, Easter, Al-mi'raj, volcano) | rabbit ambient | rabbit hurt | rabbit death | hop, egg hide (grass place), volcano pant |
| **Minotaur** (cattle, Minoan, bison, aurochs, water buffalo; cow and bull voices) | cow ambient | cow hurt | cow death | milk, bellow (ravager roar), charge hit (ravager attack), plough (hoe till), step, brush |
| **Mollusk:** kraken, Ika, octopus, ammonite | squid ambient (water) | squid hurt | squid death | ink (squid squirt), grab/lash |
| snail | turtle ambient (land) | squid hurt | squid death | retract into shell |
| **Elf** (night, high, wood, blood, desert) | *none* | player hurt | player death | trade yes (villager yes), bow (skeleton shoot), crossbow, pavilion build |
| **Undead:** zombie | zombie ambient | zombie hurt | zombie death | |
| phantom | phantom ambient | player hurt | soul escape | |
| will-o'-wisp | soul escape | player hurt | soul escape | |
| living doll | *none* | player hurt | soul escape | tug (leather armour equip) |
| yuki-onna, vampire, Frankenstein | allay ambient | player hurt | soul escape | vampire drink (honey drink), Frankenstein lightning |
| **Lizardfolk:** salamander, chameleon, iguana, croc | *none* | player hurt | player death | tongue (frog tongue), egg crack, death roll |
| turtle | turtle ambient (land) | turtle hurt | turtle death | shell retreat |
| **Dragon:** long, Jormungandr, wyrm, gryphon, wyvern, Orochi | ender dragon ambient | ender dragon hurt | player death | fire breath (blaze shoot), hoard (item pickup), wing flap, Orochi multi-head bite |
| Kannae | *none* | ender dragon hurt | player death | lightning call |
| **Devil:** lesser devil | vex ambient | player hurt | player death | prank giggle (vex), sweet thank-you (witch celebrate) |
| succubus, greater devil, jack o'lantern, jiang shi | allay ambient (none while disguised) | player hurt | player death | fireball (blaze shoot), jiang shi hop, pumpkin reveal |
| **Bug:** bee, queen bee | bee loop | bee hurt | bee death | hive work, pollinate |
| mosquito | bee loop aggressive | bee hurt | bee death | drink (honey drink) |
| fairy, fairy queen | amethyst chime | bee hurt | bee death | fountain finished (amethyst resonate), heal |
| moth, moth larva, scorpion, papillon, papillon larva | *none* | bee hurt | bee death | sting, larva chew, cocoon/emerge |
| **Flora** (alraune, matango, ent, cactus, mandragora) | azalea leaves step | azalea leaves break | azalea break | mandragora scream (ghast scream), bone meal, pollen/spore puff, matango sink |
| **All girls** | | | | eat (generic eat), chirp/happy, egg laid (chicken egg), pet |

**Full register:** [SOUNDS.md](SOUNDS.md) lists every sound with its file and line (regenerate it with `python tools/sound_register.py`).

### Plan for the sound work
- [ ] Pick a naming scheme: `entity.<species>.<slot>`. Generate the `sounds.json` entries, the `ModSounds` registration and the subtitles from `GirlSpecies`, the same way `gen_girl_data.py` works.
- [ ] As a first step, point every new event at the vanilla sound it replaces, so nothing changes until real files arrive.
- [ ] Switch the family classes from `SoundEvents.*` to the per-species events.
- [ ] Record or source the `.ogg` files (CC0/licensed), one folder per species.
- [ ] Give the girls without an ambient sound one: elves, the living doll, Kannae, most lizardfolk and the moths/scorpions/papillons.
