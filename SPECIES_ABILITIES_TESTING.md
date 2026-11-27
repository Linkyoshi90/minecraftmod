# Each kind her own: what to test

Built overnight on 2026-11-27. Everything below passes the GameTests, but none of it has been seen in a real client.
Travel, flight and swimming are steered by your own game, and the GameTests can't drive that part.
Those items are marked **(client)**: they are the ones to look at first.

## Getting there fast

| Command | What it does |
|---|---|
| `/harmontide befriend` | (op) The girl, harpy or other creature you look at gets friendship 25 with you (enough for everything below). |
| `/harmontide befriend 100` | (op) The same, but devoted (some extras, like longer harpy flights). |
| a stick | Right-click a friend (10+) to cycle her orders; "follow" is what the shoulder riders and road companions need. |

Each part can be switched off in `harmontide-common.toml`: `traversal`, `blockJobs`, `deadCompany`, `instincts`, `workshop`.
Four new journal guides explain all of it in game: *Getting about together*, *Work about the home*, *Little instincts* and *Hands at the workbench*.

Your dev world has `nsfw = true` in `run/config/harmontide-common.toml`. That makes six old GameTests fail (the cow, camel, holstaur and
milk-drip bucket tests, the bed visit and the frog spawn), because a bucket then starts the hand-milking scene. That isn't from tonight's work.

## Getting about together

- **Shoulder riders.** A jumping-spider youngling: crouch + empty hand, or tell her to follow and she hops up herself. On your shoulder: no
  fall damage, and cobwebs don't slow you. Also on the shoulder: gliderunes (slow falling), fairies (a little healing), pixies and
  kesaran-pasaran (Luck), the waterlil cat (water breathing in water), the living doll (night vision in the dark), and harpy hatchlings
  (crouch + empty hand). Crouch two seconds to set one down; she stays down a minute. She sits while she rides. **(client)** Is she on
  the *left shoulder* in third person, and hidden in first person? Does she turn with you? Does the sitting pose fit there?
- **Harpy lift.** Crouch + empty hand on a harpy at friendship 25: she rises and you hang under her. Hold forward to fly where you look.
  Big birds (roc, albatross, swan, owl, garuda...) fly freely for 30 seconds (45 at 100). Most others only glide down. Hummingbirds and
  finches are too small. **(client)** Does it steer well, and do you hang under her rather than sit? When her wings tire she sinks and sets
  you down, and you float down if you crouch off.
- **Harpy catch.** Jump off something high with a bonded harpy (or a gryphon or wyrm you could ride) within 12 blocks: she swoops and catches you.
- **Ducks and swans** float on ponds and paddle about. Swim up to one (friendship 10), right-click: she tows you; she lets go ashore. **(client)**
- **Mermaids** (any but the merchants and the manta): in the water, right-click to take her hand. Look where you want to dive and hold
  forward. You never run out of air. **(client)**
- **Ferries.** In the water, a capybara, turtle, snapping turtle, water buffalo, hippo, beaver, polar bear or spinosaur carries you on her back
  (surface). The octopus, Ika, squidkin, krakens, cuttlefish and nautilus dive with you like a mermaid. **(client)**
- **Sea lamias** (sea snake, sea serpent): ride in their coils as usual; now they dive with you and you keep your air. **(client)**
- **Lamia coils.** Get badly hurt (below 40%) by a mob near a lamia at friendship 25: she lunges and wraps you up (Resistance II, Regeneration,
  attackers switch to her). Crouch to slip out. Once a minute.
- **Slime bounce.** Fall onto (or beside) a slime girl: no damage and you bounce back up. Crouch to just sink in. An unshorn sheep or
  rhiwool minotaur catches you in her fleece the same way (no bounce).
- **Arachne silk.** Crouch + string on an arachne friend while standing at the edge of a gap: a 3-wide silk bridge to the far side (up to 32).
  It dries away after about 5 minutes. **(client)** Is the silk texture readable? An arachne told to follow you swings across on a silk line when
  she can't walk to you, and sometimes for fun between trees. **(client)** Does the white line show from her to the anchor block?
- **Spider mounts.** Give a saddle to a tarantula, wolf spider or ushi-oni at friendship 25, then right-click with an empty hand to ride
  (crouch to pat her instead). Walk into a wall and she climbs it with you. **(client)** Does climbing with a rider work and feel good?
- **Mermaids push your boat.** A mermaid at friendship 25 swims ahead of the boat you're in and pushes it the way you look. **(client)**
- **Air step.** Press jump again in mid-air: with a kitsune following you, a spirit leap; with a sylph, angel, valkyrie, gandharva
  or ophanim, a gust and a slow fall; with a wolpertinger, a second hop; with a fairy or pixie on your shoulder, a little flutter.
  Once per jump. **(client)** Does the second press feel responsive (it waits for the server's answer)?
- **Silk ladder.** Crouch + string on an arachne friend while *looking up* at a wall: a climbable silk ladder up it (dries after ~5 min).
- **Vine ladder.** Crouch + bone meal on a plant girl friend while looking up at a wall: real vines up it (they stay).
- **Dragon mounts.** Besides the gryphon and wyrm, a devoted (100) wyvern, long dragon, amphiptere, quetzalcoatl, bahamut, chimera,
  ender dragon, kun or alebrije lets you ride (empty hand) and fly where you look, and catches you when you fall. **(client)**
- **Beast mounts.** Give a saddle to a polar bear, grizzly, bison, yak, water buffalo, aurochs, hippo, rhino or capybara at friendship 25,
  then an empty hand rides her on land (crouch pats). The swimmers among them keep you aboard when they wade into water. **(client)**
- **Ostrich leap.** A saddled ostrich you ride takes the same charged jump as a horse. **(client)**
- **Centaur leaps.** Riding any centaur, hold jump and release: the jump bar fills and she leaps with you (goat highest, camels,
  elephant and giraffe lowest; not while winded). **(client)** Does the jump bar show and feel right?
- **Ender slime blink.** Crouch + empty hand on an ender slime friend: you're sent to the spot you look at (16 blocks max, every 10 s).
- **Centaurs pull minecarts.** Sit in a minecart on rails with a centaur ordered to follow: she runs ahead and the cart rolls.
- **Golem toss.** Crouch + empty hand on a golem, talosling, automaton or gargoyle friend: thrown up and forward, no fall damage.
- **Company on the road.** Followers lend you: cat girls halve fall damage, jackalopes/kangaroos give Jump Boost, wolves/hellhounds give Speed when you sprint,
  snow kinds (snow leopard, arctic fox, polar bear, yak, highland, reindeer, caribou) stop powder snow freezing you, fire kinds put you out
  when you burn, mermaids give Conduit Power under water, a chameleon makes you invisible while you crouch and keep still.
- **Eyes behind you.** With anyone on your shoulder, a monster coming at you from behind glows and she tugs your ear (action bar).

## Work about the home

These run for girls who are fond of *anyone*, while free or on patrol (not while following or sitting).

- **Dragons** walk to a furnace/blast furnace/smoker with something to cook and breathe on it: no fuel needed, twice as fast. They also light
  unlit campfires. (Not the long dragon, Jormungandr or kun.)
- **Liches and Koschei** keep a brewing stand with potions and an ingredient going without blaze powder, twice as fast.
- **Cattle minotaurs and holstaurs** carry milk buckets from a chest to a cheese shelf with room, put the empty bucket back, and take hard
  cheeses off the shelves into a chest that already holds cheese.
- **Holstaurs** bake a cake (sugar ×2, egg, wheat ×3 in a chest within 6 of a crafting table). **Brownies and domovoi** bake bread from wheat there.
  Note: they will use *all* the wheat in a chest beside a crafting table.
- **Beavers**: hand one an axe (friendship 10) and she fells wild trees (logs touching natural leaves, never builds) around that spot,
  one every couple of minutes, logs into the nearest chest. The axe wears out. Crouch + empty hand takes it back.
- **The mooskuh** turns bowls in a nearby chest into mushroom stew (one every ~2 minutes).
- **Bug girls** (not fairies or pixies) take honey from full hives without angering bees, and calm angry bees.
- **Rodents**: hand one a stack of saplings (friendship 10): she plants them around that spot, 3 apart. You're told when she's done.
- **Arachne** weave string from a chest beside a loom into white wool (4 to 1). **Wood and desert elves** fletch arrows from flint, a stick
  and a feather in a chest beside a fletching table.
- **Slime girls fill cauldrons**: undine with water, frost slime with powder snow, magma slime with lava (once per 5 minutes each).
- **Crabs** (befriended) shed a *Crab Armour Piece* every 2 to 3 days. In a smithing table, with no template, a worn item plus a piece mends half its wear.
- **Zombie girls** walking with you (following, or within 10 blocks): skeletons, phantoms, creepers and the other undead leave you alone until you hit one.

## Little instincts and the workbench

- Show a **minotaur** friend a **compass**: which way home (your bed, else spawn) and how far.
- Show a **bat** friend a **torch** underground: which way the open sky is. Show a **kitsune** friend a **lantern**: the way to the nearest
  village; a **dwarf**: the nearest mineshaft (a world search, so expect a small hitch).
- **Boar girls** (piglette, aegis boar, gribboar, daeodon) root up finds in soft ground every few minutes, now and then torchflower seeds or a pitcher pod.
- An **anteater** friend eats silverfish near her and puffs dust over infested stone.
- A **giraffe** friend browses the leaves above her head and now and then shakes down an apple (oak), cocoa beans (jungle) or a stick.
- A **dinner bell** keeps calling a friend for half a minute, so she gets there even if something distracts her.
- **Cat girls** get the zoomies at dusk now and then. **Owl harpies** give you night vision in the dark. **Penguins** belly-slide on ice
  (3× speed, the "lie" pose). **(client)** Does the slide look right?
- A **kitsune** at friendship 25 veils you when you're badly hurt (invisible for 4 seconds, attackers lose you). A **mummy** friend bandages you at half health.
- An **elf** fond of anyone near an enchanting table makes it offer as if 15 bookshelves stood around it.
- A **dwarf, cyclops, iron kobold or anvil tortoise** friend gives back half the levels of an anvil job.
- **Ring a bell**: free friends nearby come to it.
- **Winged friends** (valkyrie, angel, gandharva, ophanim, sylph, phantom, moths, mothman, papillon, dragonfly) catch your fall.
- A **yuki-onna** friend freezes water under your feet. **Fire kinds** (salamander, blaze cat, magma slime, ifrit, fire giant, hellhound)
  cool lava under your feet into a crust that melts back after about 6 seconds. Careful.
- Tooltips on string, compass, saplings, bells and the crab piece say what they're for.
- Five new advancements: *A Friend on Your Shoulder*, *Up, Up and Away*, *Caught!*, *Ferry Crossing*, *Bridge of Silk*.

## Also changed

- About 20 harpy species (duck, swan, roc, garuda, macaw, poultry...) had no wandering at all and just stood still. They now panic,
  wander or fly about, and look at people.
- A nursing baby and a kangaroo's joey always sat on the *south* side of their mother, whatever way she faced. They now turn with her.
