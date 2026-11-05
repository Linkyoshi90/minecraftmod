# Taxonomy plan: sorting every family by its real clade

Planned 2026-11-05. **Steps 1-5 of section 5 are built** (taxonomy table, `Clade`, clade milks with old-save aliases, config, tests); since then (2026-11-05) `tree_of_life.html` (`tools/build_tree_guide.py`) indexes every species by clade and links her card in the form guides, `MOBS_BY_DATE.md` carries each mob's clade, and the in-game journal has a tree of life. The 34 clade guides (`<clade>_clade_guide.html`, `tools/build_clade_guides.py`) reuse the form guides' cards and link each body's life cycle in place of a per-form appendix; the form guides were kept, not deleted. It covers all 519 mobs: the 458 `GirlSpecies`, the 47 harpies and the 14 anurans.
Result: **34 families** (27 real clades and 7 mythic groups) instead of 28. **114 species change family**, 17 are
"guests" and 15 are judgement calls for you to confirm.

## 1. How each species was placed

1. **Body, not costume.** A species goes where her animal goes. Horns, antlers, wings, a scorpion tail or a fish tail added to
   one animal are ornaments. The jackalope is still a hare, the pegasus a horse, the manticore a lion.
2. **Myths follow their animal.** Where the design is plainly one animal (Bastet is a cat, Anubis a jackal, Naga a king cobra,
   Garuda a bird of prey), the myth joins that animal's clade. Where the design is several animals of equal weight (gryphon,
   chimera, cockatrice, feathered serpents) or none (elves, undead, devils, constructs, slimes, riders, true dragons), she stays in a
   mythic group outside the tree.
3. **The design wins over the legend.** When the original myth and our model disagree (Anzu is a lion-headed eagle in myth, a
   winged lioness here), the model decides. Those cases are listed as judgement calls.
4. **Every family is a real, monophyletic clade**, at the smallest rank that still holds at least 4 of our species. The one
   unavoidable exception: birds are a branch of the dinosaurs, so a reptile family that leaves the harpies out is technically
   incomplete. Birds keep their own family because harpies are their own entity system.
5. **Guests.** A species whose clade has fewer than 4 of ours lives with its nearest real relative, and her guide entry says so
   (the jellyfish already does: "Not a mollusk at all, but she drifts with them"). Her true clade is still stored, so the guide,
   the journal and Jade show the truth.

## 2. The core change: split "family" in two

`GirlSpecies.Family` currently does two jobs. It picks the **body**: entity class, rig, model script, AI and life cycle
(`ModGirls.create`, `Lamia.java`, `gen_lamia.py`). It also picks the **kinship**: milk, the field guide, the spawn-rate setting and the
family name on cheese. The regrouping only concerns the second job.

* Keep today's enum as the **form** (NEKOMATA, LAMIA, CENTAUR... unchanged). As built, it keeps its Java name `GirlSpecies.Family` (renaming it would have touched ~40 files for no behaviour change); read "Form" below as `GirlSpecies.Family`. It keeps choosing the entity class, the rig, the model script
  and every behaviour switch, so no species changes how she moves, looks or acts.
* Add a new **`Clade`** enum (`entity/Clade.java`, the 34 families below), reached by `species.clade()` / `Clade.of(entity)`, generated from `tools/taxonomy.py`.
* A hyena keeps the canid rig and canid behaviour, but drinks, churns, ages and shows up in the guide as a feliform.

This keeps the change from rippling through about 40 behaviour call sites, and it means **no model, texture, animation or
geo file has to move or be regenerated**.

### Which call sites switch to `Clade`

| Uses Clade (taxonomy) | Stays on Form (body / behaviour) |
|---|---|
| `FamilyMilk.of` and the milk buckets | `ModGirls.create / attributes / familyRule` (entity class, spawn rules) |
| Dairy names: cheese, butter, ice cream (`DairyItem.familyName`) | `GirlRenderer` (centaur armour layer, twin tails, slime translucency) |
| `MonsterGirlsConfig` family spawn rates | `SleepGoal`, `Company.TIMID`, `Colds.HARDY`, `Foraging`, `GreenThumb`, `GirlArms`, `Teamwork` |
| Field guides and `MOBS_BY_DATE.md` labels | `Settlement`, `HomeLife`, `Rivalries.HUNTS`, `Market` sea-family checks |
| Jade / `BondInfo` family line, `Scouting` name | `Kinship` defaults (vanilla kin follow the body: cats for cat-shaped girls) |
| `Expeditions` "kin" list (sisters of her own clade) | `Letters.country`, `Stories`, `Treats`, `Favours` (her home region follows her form) |

Afterwards, each Form-side call site can be checked for places where the clade reads better (for example `Colds.HARDY` could
become "furred mammals"). That is optional and not part of this plan.

## 3. The new families at a glance

| New family | Real clade | Species | Folded in from | Milk effects |
|---|---|---|---|---|
| **Feliforms (cats & kin)** `FELIFORMIA` | Mammalia > Carnivora > Feliformia | 32 | NEKOMATA | Night Vision |
| **Canids (wolves, dogs & foxes)** `CANIDAE` | Mammalia > Carnivora > Caniformia > Canidae | 25 | CANID, KITSUNE | Speed + Luck |
| **Arctoids (bears, weasels, raccoons & seals)** `ARCTOIDEA` | Mammalia > Carnivora > Caniformia > Arctoidea | 20 | new | Resistance + Water Breathing |
| **Rodents** `RODENTIA` | Mammalia > Glires > Rodentia | 7 | RODENT | Haste + Luck |
| **Hares & rabbits** `LAGOMORPHA` | Mammalia > Glires > Lagomorpha | 9 | JACKALOPE | Jump Boost |
| **Primates** `PRIMATES` | Mammalia > Euarchonta > Primates | 6 | new | Jump Boost + Strength |
| **Bats** `CHIROPTERA` | Mammalia > Laurasiatheria > Chiroptera | 7 | BAT | Night Vision + Slow Falling |
| **Marsupials** `MARSUPIALIA` | Mammalia > Marsupialia | 5 | MARSUPIAL | Jump Boost + Resistance |
| **Armadillos, sloths & anteaters** `XENARTHRA` | Mammalia > Xenarthra | 4 | ARMOURED | Resistance + Absorption |
| **Elephants, sea cows & hyraxes** `PAENUNGULATA` | Mammalia > Afrotheria > Paenungulata | 7 | new | Health Boost |
| **Odd-toed hoofed (horses, tapirs, rhinos)** `PERISSODACTYLA` | Mammalia > Laurasiatheria > Perissodactyla | 11 | CENTAUR | Speed |
| **Cattle, sheep, goats & antelope** `BOVIDAE` | Mammalia > Artiodactyla > Ruminantia > Bovidae | 17 | MINOTAUR | Strength |
| **Deer (and giraffes)** `CERVIDAE` | Mammalia > Artiodactyla > Ruminantia > Cervidae | 12 | new | Speed + Jump Boost |
| **Camels & llamas** `CAMELIDAE` | Mammalia > Artiodactyla > Tylopoda > Camelidae | 6 | new | Fire Resistance |
| **Pigs & boars** `SUINA` | Mammalia > Artiodactyla > Suina | 5 | new | Haste + Absorption |
| **Whales & hippos** `WHIPPOMORPHA` | Mammalia > Artiodactyla > Whippomorpha | 6 | new | Dolphin's Grace + Water Breathing |
| **Birds (harpies)** `AVES` | Reptilia > Archosauria > Dinosauria > Aves | 47 | HARPY milk | Slow Falling |
| **Crocodiles, dinosaurs & pterosaurs** `ARCHOSAURIA` | Reptilia > Archosauria (birds kept apart) | 9 | THEROPOD | Strength + Speed |
| **Turtles & tortoises** `TESTUDINES` | Reptilia > Testudines | 14 | LIZARDFOLK | Resistance + Water Breathing |
| **Snakes & lizards** `SQUAMATA` | Reptilia > Lepidosauria > Squamata | 30 | LAMIA | Regeneration |
| **Frogs, toads & salamanders** `AMPHIBIA` | Amphibia (Anura + Caudata) | 19 | ANURAN milk | Jump Boost + Water Breathing |
| **Fish (and the mermaids of legend)** `ACTINOPTERYGII` | Actinopterygii | 17 | MERFOLK | Water Breathing |
| **Spiders & scorpions** `CHELICERATA` | Arthropoda > Chelicerata > Arachnida | 13 | ARACHNE | Resistance |
| **Insects** `HEXAPODA` | Arthropoda > Pancrustacea > Hexapoda | 23 | BUG | Haste |
| **Crabs, lobsters & shrimp** `MALACOSTRACA` | Arthropoda > Pancrustacea > Malacostraca | 7 | CRUSTACEAN | Conduit Power |
| **Octopuses, squid & snails** `MOLLUSCA` | Mollusca | 17 | MOLLUSK | Dolphin's Grace |
| **Plants & fungi** `FLORA` | Plantae + Fungi (two kingdoms, one guide) | 14 | FLORA | none |
| **Fae & folk (elves, fairies, spirits)** `FAE` | mythic: humanoid folk | 26 | ELF | Hero of the Village |
| **Dragons & chimeras** `DRAGON` | mythic: no single real animal | 17 | DRAGON | Fire Resistance + Strength |
| **Undead** `UNDEAD` | mythic | 33 | UNDEAD | Invisibility |
| **Devils, oni & giants** `DEVIL` | mythic | 30 | DEVIL | Absorption |
| **Slimes** `SLIME` | mythic | 11 | SLIME | Health Boost + Jump Boost |
| **Constructs** `CONSTRUCT` | made, not born | 8 | CONSTRUCT | none |
| **Riders** `RIDER` | mythic | 5 | RIDER | Strength + Resistance |

Small families: Xenarthra (4), Suina (5), Marsupialia (5, with the platypus as a guest) and Primates (6). They are good places to
add species later. Candidates: a sloth or tamandua, a warthog or babirusa, a wombat or Tasmanian devil, an orangutan or mandrill.

### Guests (nearest relative, flagged in their guide entry)

| Species | Lives with | Real clade |
|---|---|---|
| Pangolin `pangolin_armoured` | Feliforms (cats & kin) | Pholidota: Manidae; pangolins are the closest living relatives of the carnivores |
| Sandworm `sandworm_lamia` | Octopuses, squid & snails | Annelida (Harmontide sandworm); worms and mollusks are both Lophotrochozoa |
| Jellyfish Girl `jellyfish_mollusk` | Octopuses, squid & snails | Cnidaria: Scyphozoa (her guide entry already says so) |
| Anemone `anemone_mollusk` | Octopuses, squid & snails | Cnidaria: Actiniaria |
| Centipede `centipede_lamia` | Insects | Myriapoda: Chilopoda; the insects' nearest relatives here (Mandibulata) |
| Tardigrade `tardigrade_bug` | Insects | Tardigrada: a cousin of the arthropods (Panarthropoda) |
| Prairhorn `prairhorn_centaur` | Deer (and giraffes) | Antilocapridae: Antilocapra americana (Harmontide) |
| Giraffe `giraffe_centaur` | Deer (and giraffes) | Giraffidae: Giraffa; too few giraffids for a family of their own |
| Okapi `okapi_centaur` | Deer (and giraffes) | Giraffidae: Okapia johnstoni |
| Shark Mermaid `shark_mermaid` | Fish (and the mermaids of legend) | Chondrichthyes: Selachimorpha; sharks and rays are too few for their own family |
| Mantarrow `mantarrow_mermaid` | Fish (and the mermaids of legend) | Chondrichthyes: Mobulidae (Harmontide) |
| Sawfin `sawfin_mermaid` | Fish (and the mermaids of legend) | Chondrichthyes: Pristidae (Harmontide) |
| Starfish `starfish_crustacean` | Fish (and the mermaids of legend) | Echinodermata: Asteroidea; starfish are closer to fish (and us) than to crabs |
| Platypus `platypus_mermaid` | Marsupials | Monotremata: Ornithorhynchus anatinus; mammals that are neither marsupial nor placental |
| Anomalocaris `anomalocaris_mermaid` | Crabs, lobsters & shrimp | Radiodonta (extinct stem arthropod) |
| Needlehog `needlehog_rodent` | Bats | Eulipotyphla: Erinaceidae (Harmontide hedgehog); closer to bats than to rodents |
| Mole `mole_rodent` | Bats | Eulipotyphla: Talpidae; closer to bats than to rodents |

### Judgement calls (please confirm or overrule)

| Species | Placed in | Reasoning |
|---|---|---|
| Ammit `ammit_nekomata` | Feliforms (cats & kin) | Felidae: lion per this design (the myth's Ammit is lion + hippo + crocodile) |
| Nue `nue_nekomata` | Feliforms (cats & kin) | Felidae: tiger body (myth; monkey face, snake tail are ornaments) |
| Lamassu `lamassu_centaur` | Feliforms (cats & kin) | Felidae: lion body per this design (Assyrian lamassu are bull- or lion-bodied) |
| Anzu `anzu_centaur` | Feliforms (cats & kin) | Felidae: lioness per this design (the Sumerian Anzu is a lion-headed eagle) |
| Doggo `doggo_mermaid` | Canids (wolves, dogs & foxes) | Canis familiaris (Harmontide sea dog; the fish tail is an ornament) |
| Serpopard `serpopard_lamia` | Snakes & lizards | Serpentes per her rig (myth; the Egyptian serpopard is a leopard with a snake's neck) |
| Min Min Light `min_min_mollusk` | Octopuses, squid & snails | Cnidaria per this design (cap + tentacles); the folklore Min Min is a light |
| Ceibler `ceibler_centaur` | Camels & llamas | Camelidae: Lama (Harmontide llama-deer) |
| Censhroom `censhroom_centaur` | Plants & fungi | Fungi on an unspecified taur; the fungus is the only clear animal-or-not |
| Daeodon `daeodon_armoured` | Pigs & boars | Entelodontidae (extinct 'hell pig'; some trees put it nearer hippos) |
| Leviathan `leviathan_mermaid` | Fish (and the mermaids of legend) | Actinopterygii per this design (the Leviathan is also read as a whale or serpent) |
| Leshy `leshy_flora` | Fae & folk (elves, fairies, spirits) | outside taxonomy (forest spirit; moss beard is an ornament) |
| Vetala `vetala_bat` | Bats | Chiroptera per this design (Indian spirit) |
| Ironbeak Harpy `ironbeak_harpy` | Birds (harpies) | Neoaves, courier bird (Harmontide; order unclear) |
| Landharpy `landharpy_harpy` | Birds (harpies) | Aves, flightless (Harmontide; order unclear) |

## 4. What each kind of game content needs

### Milk buckets (`item/FamilyMilk`, `ModItems.MILK`)
* `FamilyMilk` becomes one constant per milk-giving clade: 32 of them (every clade except FLORA and CONSTRUCT), up from 26 + harpy + anuran.
  `FamilyMilk.of` reads `species.clade()`. Harpies give `AVES`, anurans `AMPHIBIA`.
* New buckets: `feliform_milk`, `canid_milk` (kept), `arctoid_milk`, `primate_milk`, `paenungulate_milk`, `cervid_milk`,
  `camelid_milk`, `suine_milk`, `whippomorph_milk`, `testudine_milk`, `squamate_milk`, `amphibian_milk`, `fish_milk`,
  `fae_milk`... (item id = short English adjective of the clade; the summary table proposes effects).
* Retired buckets (`nekomata_milk`, `kitsune_milk`, `lamia_milk`, `lizardfolk_milk`, `merfolk_milk`, `jackalope_milk`,
  `minotaur_milk`, `centaur_milk`, `theropod_milk`, `armoured_milk`, `bug_milk`, `arachne_milk`, `elf_milk`, `harpy_milk`,
  `anuran_milk`...) become **registry aliases** pointing at their successor (`DeferredRegister.addAlias`), so buckets in
  chests, tanks and fridges survive. One successor each: the clade that kept most of the old family (kitsune and canid → canid,
  lizardfolk → testudine, merfolk → fish, armoured → xenarthran, harpy → bird, anuran → amphibian).
* Assets: `tools/gen_milk_assets.py` `MILKS` dict gets the new keys + colours. It writes the bucket textures, item models,
  the `c:buckets/milk` tag and `cake_from_family_milk.json`. Lang `item.monstergirls.<x>_milk`.
* Effects: kept wherever a family survives. New picks for the new clades are in the summary table. Lizardfolk's Fire Resistance
  + Speed has no direct successor (turtles get Resistance + Water Breathing, like the turtle helmet).

### Cheese, butter, ice cream, cooked dishes (`entity/Dairy`, `ModComponents.MILK_FAMILY`)
* No new items: these are single items that carry a `milk_family` component and take their name from `family.monstergirls.<clade>`
  ("Firm Feliform Cheese"). Only lang keys are added.
* **Save migration:** the component is stored as the enum name (`Codec.STRING.xmap(FamilyMilk::valueOf, ...)`). Old cheese
  says `"NEKOMATA"`, so `valueOf` would throw. The codec gets an alias map (old name → successor) before `valueOf`. The
  network codec is ordinal-based and needs no migration (client and server always run the same build).
* Milk tank / fridge / cheese shelf block entities store stacks, so the item alias and the component alias cover them.

### Models, textures, animations, rigs
* **Nothing moves.** Models belong to the Form, and each `gen_<form>.py` keeps every species it builds today. Entity ids
  (`hyena_canid`, `orca_mermaid`, `baku_nekomata`) stay as they are. Renaming them would break every saved entity, spawn egg,
  egg block, texture path and test for no visible gain. Players never see the suffix.
* New previews only: a `tools/clade_roster.py` that stitches the existing per-species preview PNGs into one roster per clade
  (`previews/<clade>_roster.png`) for the guides.
* Milk bucket textures are the only new textures (above).

### Field guides (`tools/family_concepts*.py` → `tools/build_field_guides.py` → `<family>_field_guide.html`)
* One source of truth: a new `tools/taxonomy.py` holding the table below (`id → clade, true position, guest/call flag`), plus a
  `CLADES` dict (display name, clade path, theme colours, lede). `gen_girl_data.py` reads it and emits `Clade` into `GirlSpecies.java`
  (and a small `HarpyClades`/anuran map for the other two bases).
* Guides are rebuilt **per clade**: `feliformia_field_guide.html` and so on. Species cards are reused as they are. Two new boxes per card:
  "True position" (the clade path) and "Body" (which form's life cycle, mating and bond rules she follows, linking to that form's
  section). Form-level prose (lifecycle, systems, friendship tiers) moves to a per-form appendix shared by every guide, since a
  feliform guide now covers cat-, taur-, canid- and rodent-bodied girls.
* Each clade needs a new lede/hero text written. The 7 mythic groups and the families that barely change (Bovidae, Squamata, Hexapoda,
  Chelicerata, Mollusca) can mostly keep theirs.
* Old guide files are deleted and the volume numbering is redone. `update_old_guides.py` and the volumes nav follow automatically.

### Lang (`en_us.json`)
* `family.monstergirls.<clade>` for all 34 (old keys removed), `item.monstergirls.<x>_milk` for the new buckets, the config
  screen labels for the new spawn-rate keys, and any guest-note strings shown in game.
* Species names do not change.

### Config (`MonsterGirlsConfig.FAMILY_RATES`)
* Spawn-rate sliders become one per clade (the 34 keys). Old keys (`nekomata`, `lizardfolk`...) disappear from the file and
  NeoForge resets those values to 100%. Recommend accepting that and noting it in the changelog. Copying old values over to the
  successor clade is possible but adds a one-off migration for a rarely changed setting.
* The separate `harpy` / `anuran` rate keys become `aves` / `amphibia` through the same loop.

### Other places that mention families
* Spawn eggs, egg blocks (`GirlEggBlock`), harpy eggs, frog spawn: per species, no change.
* Creative tab: optionally sort by clade, then species. Cosmetic.
* Advancements: none are per family (`naturalist` counts species). No change.
* `MOBS_BY_DATE.md`, `HARMONTIDE_ROSTER.md`, `PAINTS.md`: regenerate or relabel the family column.
* GameTests: the 5 suites that name `Family.X` (`HarmontideLifeTests`, `MarketTests`, `SeventeenthWaveTests`,
  `TheropodTests`, `MilkTests`) keep `Family.X` where they test behaviour. `MilkTests`/`DairyTests` test the new clade milks. Add one
  `TaxonomyTests`: every species has a clade, every milk-giving clade has a bucket, old component names and old bucket ids resolve.

## 5. Order of work

1. **`tools/taxonomy.py`** with the table below, plus a coverage check (fails if a species has no clade, the same check this
   plan was generated with).
2. **Java split:** rename `Family` → `Form` (mechanical), add `Clade` + `species.clade()` via `gen_girl_data.py`, harpy/anuran clade lookup.
3. **Milk:** rewrite `FamilyMilk` per clade, item aliases, component codec aliases, `gen_milk_assets.py` (new buckets + colours), lang.
4. **Switch the taxonomy call sites** in the left column of section 2. Config keys.
5. **Tests:** `TaxonomyTests` + updated milk/dairy tests. Run the full suite (`--no-configuration-cache`, about 16 min, in the background).
6. **Guides:** clade guides, per-form appendix, guest notes, clade rosters, new ledes.
7. **Docs:** MOBS_BY_DATE etc.

Steps 1-5 are one change (a working build with new milks and the old guides). Step 6 is the big writing job and can follow
separately.

## 6. Open questions

* **Kitsune into Canidae.** Foxes are canids, and the gray fox and bat-eared fox are not even "true foxes", so a fox-only family
  cannot be monophyletic. The plan merges them and keeps "Kitsune" as a chapter title inside the canid guide. Alternative: keep
  KITSUNE as a separate *form* (it already is) and accept one milk for both.
* **Milk for non-mammals.** The plan keeps the current fantasy rule (every family but plants and constructs gives milk). A stricter
  version (only mammals lactate; birds give "crop milk", which pigeons and flamingos really make; everyone else gives none) would cut
  13 buckets and change existing gameplay, so it is not proposed.
* **Pecora grain.** Deer and cattle could be one family (Pecora, 29 species, the giraffes and pronghorn included properly instead of
  as guests). The plan keeps Bovidae separate because the minotaur dairy features are built around it.

## 7. The full table

"(moved)" = she leaves her current family. *guest* and *judgement call* as above.


### Feliforms (cats & kin) `FELIFORMIA` (32)

*Mammalia > Carnivora > Feliformia*

| Species | id | From | True position | |
|---|---|---|---|---|
| Blaze Cat | `blaze_cat_nekomata` | nekomata | Felidae (Harmontide) |  |
| Waterlil | `waterlil_nekomata` | nekomata | Felidae (Harmontide) |  |
| Cheetah Nekomata | `cheetah_nekomata` | nekomata | Felidae: Acinonyx jubatus |  |
| Caracal | `caracal_nekomata` | nekomata | Felidae: Caracal caracal |  |
| Bastet | `bastet_nekomata` | nekomata | Felidae: Felis (deity) |  |
| Cheshire Cat | `cheshire_nekomata` | nekomata | Felidae: Felis (folklore cat) |  |
| Cait Sìth | `cait_sith_nekomata` | nekomata | Felidae: Felis (folklore cat) |  |
| Bakeneko | `bakeneko_nekomata` | nekomata | Felidae: Felis (folklore cat) |  |
| Ocelot Nekomata | `ocelot_nekomata` | nekomata | Felidae: Leopardus pardalis |  |
| Serval Nekomata | `serval_nekomata` | nekomata | Felidae: Leptailurus serval |  |
| Lynx Nekomata | `lynx_nekomata` | nekomata | Felidae: Lynx |  |
| Sabre | `sabre_nekomata` | nekomata | Felidae: Machairodontinae (Harmontide) |  |
| Smilodon | `smilodon_nekomata` | nekomata | Felidae: Machairodontinae, Smilodon (extinct) |  |
| Lioness Nekomata | `lioness_nekomata` | nekomata | Felidae: Panthera leo |  |
| Censer Lion | `censer_lion_nekomata` | nekomata | Felidae: Panthera leo (Harmontide) |  |
| Jaglare | `jaglare_nekomata` | nekomata | Felidae: Panthera onca (Harmontide) |  |
| Black Panther | `black_panther_nekomata` | nekomata | Felidae: Panthera pardus (melanistic) |  |
| Tiger Nekomata | `tiger_nekomata` | nekomata | Felidae: Panthera tigris |  |
| Rakshasa | `rakshasa_nekomata` | nekomata | Felidae: Panthera tigris (myth) |  |
| Snow Leopard Nekomata | `snow_leopard_nekomata` | nekomata | Felidae: Panthera uncia |  |
| Snowloper | `snowloper_nekomata` | nekomata | Felidae: Panthera uncia (Harmontide) |  |
| Manticore | `manticore_nekomata` | nekomata | Felidae: lion body (myth; the scorpion tail is an ornament) |  |
| Ammit | `ammit_nekomata` | nekomata | Felidae: lion per this design (the myth's Ammit is lion + hippo + crocodile) | judgement call |
| Nue | `nue_nekomata` | nekomata | Felidae: tiger body (myth; monkey face, snake tail are ornaments) | judgement call |
| Lamassu | `lamassu_centaur` | centaur (moved) | Felidae: lion body per this design (Assyrian lamassu are bull- or lion-bodied) | judgement call |
| Sphinx | `sphinx_centaur` | centaur (moved) | Felidae: lioness body (myth; wings are an ornament) |  |
| Ark Seraphim | `ark_seraphim_centaur` | centaur (moved) | Felidae: lioness body (myth; wings are an ornament) |  |
| Anzu | `anzu_centaur` | centaur (moved) | Felidae: lioness per this design (the Sumerian Anzu is a lion-headed eagle) | judgement call |
| Mongoose | `mongoose_rodent` | rodent (moved) | Herpestidae |  |
| Meerkat | `meerkat_rodent` | rodent (moved) | Herpestidae: Suricata suricatta |  |
| Hyena | `hyena_canid` | canid (moved) | Hyaenidae: Crocuta crocuta (hyenas are feliforms, not dogs) |  |
| Pangolin | `pangolin_armoured` | armoured (moved) | Pholidota: Manidae; pangolins are the closest living relatives of the carnivores | guest |

### Canids (wolves, dogs & foxes) `CANIDAE` (25)

*Mammalia > Carnivora > Caniformia > Canidae*

| Species | id | From | True position | |
|---|---|---|---|---|
| Cù-sìth | `cu_sith_canid` | canid | Canidae: Canis (myth hound) |  |
| Hellhound | `hellhound_canid` | canid | Canidae: Canis (myth hound) |  |
| Cerberus | `cerberus_canid` | canid | Canidae: Canis (myth hound) |  |
| Bloodhound | `bloodhound_canid` | canid | Canidae: Canis familiaris |  |
| Meloncollie | `meloncollie_canid` | canid | Canidae: Canis familiaris (Harmontide) |  |
| Obsidian Hound | `obsidian_hound_canid` | canid | Canidae: Canis familiaris (Harmontide) |  |
| Kobold | `kobold_canid` | canid | Canidae: Canis familiaris per this design (folklore mine spirit) |  |
| Anubis | `anubis_canid` | canid | Canidae: Canis lupaster (deity) |  |
| Wolf Girl | `wolf_canid` | canid | Canidae: Canis lupus |  |
| Amarok | `amarok_canid` | canid | Canidae: Canis lupus |  |
| Tarnhelm | `tarnhelm_canid` | canid | Canidae: Canis lupus (Harmontide) |  |
| Bat-Eared Fox | `bat_eared_kitsune` | kitsune | Canidae: Otocyon megalotis |  |
| Gray Fox Kitsune | `gray_kitsune` | kitsune | Canidae: Urocyon cinereoargenteus |  |
| Wattlefox | `wattlefox_kitsune` | kitsune | Canidae: Vulpes (Harmontide) |  |
| Vulphea | `vulphea_kitsune` | kitsune | Canidae: Vulpes (Harmontide) |  |
| Huli Jing | `huli_jing_kitsune` | kitsune | Canidae: Vulpes (folklore fox spirit) |  |
| Kitsune Obaa | `obaa_kitsune` | kitsune | Canidae: Vulpes (folklore fox spirit) |  |
| Kumiho | `kumiho_kitsune` | kitsune | Canidae: Vulpes (folklore fox spirit) |  |
| Corsac Fox Kitsune | `corsac_kitsune` | kitsune | Canidae: Vulpes corsac |  |
| Arctic Fox Kitsune | `arctic_kitsune` | kitsune | Canidae: Vulpes lagopus |  |
| Red Fox Kitsune | `red_kitsune` | kitsune | Canidae: Vulpes vulpes |  |
| Fennec Kitsune | `fennec_kitsune` | kitsune | Canidae: Vulpes zerda |  |
| Dunefennec | `dunefennec_kitsune` | kitsune | Canidae: Vulpes zerda (Harmontide) |  |
| Tanuki | `tanuki_rodent` | rodent (moved) | Canidae: Nyctereutes procyonoides (a dog, not a raccoon) |  |
| Doggo | `doggo_mermaid` | merfolk (moved) | Canis familiaris (Harmontide sea dog; the fish tail is an ornament) | judgement call |

### Arctoids (bears, weasels, raccoons & seals) `ARCTOIDEA` (20)

*Mammalia > Carnivora > Caniformia > Arctoidea*

| Species | id | From | True position | |
|---|---|---|---|---|
| Red Panda | `red_panda_rodent` | rodent (moved) | Ailuridae: Ailurus fulgens |  |
| Skunk | `skunk_rodent` | rodent (moved) | Mephitidae |  |
| Wolverine | `wolverine_canid` | canid (moved) | Mustelidae: Gulo gulo |  |
| Ahuizotl | `ahuizotl_mermaid` | merfolk (moved) | Mustelidae: Lontra longicaudis (myth) |  |
| Rust Otter | `rust_otter_mermaid` | merfolk (moved) | Mustelidae: Lutrinae (Harmontide) |  |
| Sobolnik | `sobolnik_rodent` | rodent (moved) | Mustelidae: Martes zibellina (Harmontide sable) |  |
| Kamaitachi | `kamaitachi_rodent` | rodent (moved) | Mustelidae: Mustela (folklore weasel) |  |
| Bambadger | `bambadger_rodent` | rodent (moved) | Mustelidae: badger (Harmontide) |  |
| Moonseal | `moonseal_mermaid` | merfolk (moved) | Pinnipedia (Harmontide) |  |
| Walrus | `walrus_mermaid` | merfolk (moved) | Pinnipedia: Odobenus rosmarus |  |
| Seal Mermaid | `seal_mermaid` | merfolk (moved) | Pinnipedia: Phocidae |  |
| Selkie | `selkie_mermaid` | merfolk (moved) | Pinnipedia: Phocidae (folklore) |  |
| Coatimano | `coatimano_rodent` | rodent (moved) | Procyonidae: Nasua (Harmontide) |  |
| Silkcoon | `silkcoon_rodent` | rodent (moved) | Procyonidae: Procyon (Harmontide) |  |
| Raccoon | `raccoon_rodent` | rodent (moved) | Procyonidae: Procyon lotor |  |
| Panda Girl | `panda_canid` | canid (moved) | Ursidae: Ailuropoda melanoleuca |  |
| Black Bear Girl | `black_bear_canid` | canid (moved) | Ursidae: Ursus americanus |  |
| Grizzly Girl | `grizzly_canid` | canid (moved) | Ursidae: Ursus arctos horribilis |  |
| Polar Bear Girl | `polar_bear_canid` | canid (moved) | Ursidae: Ursus maritimus |  |
| Rheintalensis | `rheintalensis_canid` | canid (moved) | Ursidae: Ursus spelaeus (extinct cave bear, Harmontide) |  |

### Rodents `RODENTIA` (7)

*Mammalia > Glires > Rodentia*

| Species | id | From | True position | |
|---|---|---|---|---|
| Beaver | `beaver_rodent` | rodent | Rodentia: Castoridae |  |
| Capybara Girl | `capybara_rodent` | rodent | Rodentia: Caviidae, Hydrochoerus |  |
| House Mouse | `house_mouse_rodent` | rodent | Rodentia: Muridae, Mus musculus |  |
| Squirricerat | `squirricerat_rodent` | rodent | Rodentia: Sciuridae (Harmontide) |  |
| Pineskrell | `pineskrell_rodent` | rodent | Rodentia: Sciuridae (Harmontide) |  |
| Marmotaur | `marmotaur_rodent` | rodent | Rodentia: Sciuridae, Marmota (Harmontide) |  |
| Ratatoskr | `ratatoskr_rodent` | rodent | Rodentia: Sciuridae, Sciurus vulgaris (myth) |  |

### Hares & rabbits `LAGOMORPHA` (9)

*Mammalia > Glires > Lagomorpha*

| Species | id | From | True position | |
|---|---|---|---|---|
| Clockhare | `clockhare_jackalope` | jackalope | Leporidae (Harmontide) |  |
| Boghare | `boghare_jackalope` | jackalope | Leporidae (Harmontide) |  |
| Inaba | `inaba_jackalope` | jackalope | Leporidae (folklore rabbit) |  |
| Easter Bunny | `easter_jackalope` | jackalope | Leporidae (folklore rabbit) |  |
| Moon Rabbit | `moon_rabbit_jackalope` | jackalope | Leporidae (folklore rabbit) |  |
| Wolpertinger | `wolpertinger_jackalope` | jackalope | Leporidae (folklore; antlers and wings are ornaments) |  |
| Jack Jackalope | `jack_jackalope` | jackalope | Leporidae: Lepus (legend; horns are an ornament) |  |
| Al-mi'raj | `al_miraj_jackalope` | jackalope | Leporidae: Lepus (legend; horns are an ornament) |  |
| Volcano Rabbit | `volcano_jackalope` | jackalope | Leporidae: Romerolagus diazi |  |

### Primates `PRIMATES` (6)

*Mammalia > Euarchonta > Primates*

| Species | id | From | True position | |
|---|---|---|---|---|
| Sun Wukong | `sun_wukong_rodent` | rodent (moved) | Cercopithecidae: Macaca (myth) |  |
| Yeti | `yeti_canid` | canid (moved) | Hominidae (cryptid ape) |  |
| Sasquatch | `sasquatch_rodent` | rodent (moved) | Hominidae (cryptid) |  |
| Gigantopithecus | `gigantopithecus_canid` | canid (moved) | Hominidae: Gigantopithecus blacki (extinct) |  |
| Gibbon | `gibbon_rodent` | rodent (moved) | Hylobatidae (Harmontide) |  |
| Lemur | `lemur_rodent` | rodent (moved) | Lemuridae: Lemur catta |  |

### Bats `CHIROPTERA` (7)

*Mammalia > Laurasiatheria > Chiroptera*

| Species | id | From | True position | |
|---|---|---|---|---|
| Bat Girl | `cave_bat` | bat | Chiroptera |  |
| Frondbat | `frond_bat` | bat | Chiroptera (Harmontide) |  |
| Vetala | `vetala_bat` | bat | Chiroptera per this design (Indian spirit) | judgement call |
| Camazotz | `camazotz_bat` | bat | Chiroptera: Phyllostomidae (myth; leaf-nosed bat) |  |
| Datebat | `date_bat` | bat | Chiroptera: Pteropodidae (Harmontide fruit bat) |  |
| Needlehog | `needlehog_rodent` | rodent (moved) | Eulipotyphla: Erinaceidae (Harmontide hedgehog); closer to bats than to rodents | guest |
| Mole | `mole_rodent` | rodent (moved) | Eulipotyphla: Talpidae; closer to bats than to rodents | guest |

### Marsupials `MARSUPIALIA` (5)

*Mammalia > Marsupialia*

| Species | id | From | True position | |
|---|---|---|---|---|
| Opossum | `opossum_marsupial` | marsupial | Didelphimorphia: Didelphidae |  |
| Kangaroo Girl | `kangaroo_marsupial` | marsupial | Diprotodontia: Macropodidae |  |
| Gliderune | `gliderune_marsupial` | marsupial | Diprotodontia: Petaurus breviceps (Harmontide) |  |
| Koala Girl | `koala_marsupial` | marsupial | Diprotodontia: Phascolarctidae |  |
| Platypus | `platypus_mermaid` | merfolk (moved) | Monotremata: Ornithorhynchus anatinus; mammals that are neither marsupial nor placental | guest |

### Armadillos, sloths & anteaters `XENARTHRA` (4)

*Mammalia > Xenarthra*

| Species | id | From | True position | |
|---|---|---|---|---|
| Riverdillo | `riverdillo_armoured` | armoured | Cingulata (Harmontide) |  |
| Armadillo Girl | `armadillo_armoured` | armoured | Cingulata: Dasypodidae |  |
| Slothren | `slothren_armoured` | armoured | Pilosa: Folivora (Harmontide sloth) |  |
| Anteater | `anteater_armoured` | armoured | Pilosa: Vermilingua (Harmontide) |  |

### Elephants, sea cows & hyraxes `PAENUNGULATA` (7)

*Mammalia > Afrotheria > Paenungulata*

| Species | id | From | True position | |
|---|---|---|---|---|
| Rockhyrax | `rockhyrax_rodent` | rodent (moved) | Hyracoidea: Procaviidae (Harmontide); elephants' cousin |  |
| Elephant | `elephant_centaur` | centaur (moved) | Proboscidea: Loxodonta |  |
| Tuskin | `tuskin_minotaur` | minotaur (moved) | Proboscidea: Mammuthus (Harmontide) |  |
| Tusktaur | `tusktaur_centaur` | centaur (moved) | Proboscidea: Mammuthus primigenius (extinct) |  |
| Moopie | `moopie_mermaid` | merfolk (moved) | Sirenia, the real 'sea cows' (Harmontide) |  |
| Dugong Mermaid | `dugong_mermaid` | merfolk (moved) | Sirenia: Dugong dugon |  |
| Manatee | `manatee_mermaid` | merfolk (moved) | Sirenia: Trichechus |  |

### Odd-toed hoofed (horses, tapirs, rhinos) `PERISSODACTYLA` (11)

*Mammalia > Laurasiatheria > Perissodactyla*

| Species | id | From | True position | |
|---|---|---|---|---|
| Kelpie Centaur | `kelpie_centaur` | centaur | Equidae (folklore horse) |  |
| Unicorn | `unicorn_centaur` | centaur | Equidae (folklore horse) |  |
| Pegasus Centaur | `pegasus_centaur` | centaur | Equidae (myth; wings are an ornament) |  |
| Donkey | `donkey_centaur` | centaur | Equidae: Equus asinus |  |
| Bellmare | `bellmare_centaur` | centaur | Equidae: Equus asinus (Harmontide; goat horns are an ornament) |  |
| Horse Centaur | `horse_centaur` | centaur | Equidae: Equus caballus |  |
| Zebra Centaur | `zebra_centaur` | centaur | Equidae: Equus quagga |  |
| Tapir Centaur | `tapir_centaur` | centaur | Tapiridae: Tapirus |  |
| Rhino | `rhino_minotaur` | minotaur (moved) | Rhinocerotidae |  |
| Rhiwool | `rhiwool_minotaur` | minotaur (moved) | Rhinocerotidae: Coelodonta antiquitatis (extinct, Harmontide) |  |
| Baku | `baku_nekomata` | nekomata (moved) | Tapiridae (myth; this design is a tapir) |  |

### Cattle, sheep, goats & antelope `BOVIDAE` (17)

*Mammalia > Artiodactyla > Ruminantia > Bovidae*

| Species | id | From | True position | |
|---|---|---|---|---|
| Bison Minotaur | `bison_minotaur` | minotaur | Bovidae: Bison |  |
| Dustbison | `dustbison_minotaur` | minotaur | Bovidae: Bison (Harmontide) |  |
| Yak Minotaur | `yak_minotaur` | minotaur | Bovidae: Bos grunniens |  |
| Aurochs Minotaur | `aurochs_minotaur` | minotaur | Bovidae: Bos primigenius (extinct) |  |
| Cattle Minotaur | `cattle_minotaur` | minotaur | Bovidae: Bos taurus |  |
| Highland Minotaur | `highland_minotaur` | minotaur | Bovidae: Bos taurus |  |
| Bronzejaw Ox | `bronzejaw_minotaur` | minotaur | Bovidae: Bos taurus (Harmontide) |  |
| Mooskuh | `mooskuh_minotaur` | minotaur | Bovidae: Bos taurus (Harmontide) |  |
| Shroomotaur | `shroomotaur_minotaur` | minotaur | Bovidae: Bos taurus (Minecraft mooshroom) |  |
| Minoan Minotaur | `minoan_minotaur` | minotaur | Bovidae: Bos taurus (myth) |  |
| Reedbull | `reedbull_minotaur` | minotaur | Bovidae: Bovinae (Harmontide) |  |
| Water Buffalo Minotaur | `water_buffalo_minotaur` | minotaur | Bovidae: Bubalus bubalis |  |
| Oryx | `oryx_minotaur` | minotaur | Bovidae: Oryx (Harmontide) |  |
| Skjoldram | `skjoldram_minotaur` | minotaur | Bovidae: Ovis (Harmontide) |  |
| Brasshorn Ram | `brasshorn_minotaur` | minotaur | Bovidae: Ovis (Harmontide) |  |
| Sheep Girl | `sheep_minotaur` | minotaur | Bovidae: Ovis aries |  |
| Mountain Goat Centaur | `goat_centaur` | centaur (moved) | Bovidae: Caprinae, Oreamnos americanus |  |

### Deer (and giraffes) `CERVIDAE` (12)

*Mammalia > Artiodactyla > Ruminantia > Cervidae*

| Species | id | From | True position | |
|---|---|---|---|---|
| Aurorantler | `aurorantler_centaur` | centaur (moved) | Cervidae (Harmontide) |  |
| Stagel | `stagel_centaur` | centaur (moved) | Cervidae (Harmontide) |  |
| Palmyr | `palmyr_centaur` | centaur (moved) | Cervidae (Harmontide) |  |
| Qilin | `qilin_centaur` | centaur (moved) | Cervidae (myth; scales are an ornament) |  |
| Moose | `moose_centaur` | centaur (moved) | Cervidae: Alces alces |  |
| Halberelk | `halberelk_centaur` | centaur (moved) | Cervidae: Cervus canadensis (Harmontide) |  |
| Momoshika | `momoshika_centaur` | centaur (moved) | Cervidae: Cervus nippon (Harmontide) |  |
| Reindeer Centaur | `reindeer_centaur` | centaur (moved) | Cervidae: Rangifer tarandus |  |
| Caribou | `caribou_centaur` | centaur (moved) | Cervidae: Rangifer tarandus |  |
| Prairhorn | `prairhorn_centaur` | centaur (moved) | Antilocapridae: Antilocapra americana (Harmontide) | guest |
| Giraffe | `giraffe_centaur` | centaur (moved) | Giraffidae: Giraffa; too few giraffids for a family of their own | guest |
| Okapi | `okapi_centaur` | centaur (moved) | Giraffidae: Okapia johnstoni | guest |

### Camels & llamas `CAMELIDAE` (6)

*Mammalia > Artiodactyla > Tylopoda > Camelidae*

| Species | id | From | True position | |
|---|---|---|---|---|
| Camel Centaur | `camel_centaur` | centaur (moved) | Camelidae: Camelus bactrianus |  |
| Dromedary Centaur | `dromedary_centaur` | centaur (moved) | Camelidae: Camelus dromedarius |  |
| Ceibler | `ceibler_centaur` | centaur (moved) | Camelidae: Lama (Harmontide llama-deer) | judgement call |
| Urcuchillay | `urcuchillay_centaur` | centaur (moved) | Camelidae: Lama glama (deity) |  |
| Alpaca Centaur | `alpaca_centaur` | centaur (moved) | Camelidae: Vicugna pacos |  |
| Vicunero | `vicunero_centaur` | centaur (moved) | Camelidae: Vicugna vicugna (Harmontide) |  |

### Pigs & boars `SUINA` (5)

*Mammalia > Artiodactyla > Suina*

| Species | id | From | True position | |
|---|---|---|---|---|
| Daeodon | `daeodon_armoured` | armoured (moved) | Entelodontidae (extinct 'hell pig'; some trees put it nearer hippos) | judgement call |
| Zoglintaur | `zoglintaur_centaur` | centaur (moved) | Suidae-like (Minecraft hoglin) |  |
| Piglette | `piglette_armoured` | armoured (moved) | Suidae-like (Minecraft piglin) |  |
| Aegis Boar | `aegis_boar_armoured` | armoured (moved) | Suidae: Sus scrofa (Harmontide) |  |
| Gribboar | `gribboar_armoured` | armoured (moved) | Suidae: Sus scrofa (Harmontide) |  |

### Whales & hippos `WHIPPOMORPHA` (6)

*Mammalia > Artiodactyla > Whippomorpha*

| Species | id | From | True position | |
|---|---|---|---|---|
| Cetus | `cetus_mermaid` | merfolk (moved) | Cetacea (myth; this design is a whale) |  |
| Dolphin Mermaid | `dolphin_mermaid` | merfolk (moved) | Cetacea: Delphinidae |  |
| Orca Mermaid | `orca_mermaid` | merfolk (moved) | Cetacea: Delphinidae, Orcinus orca |  |
| Beluga | `beluga_mermaid` | merfolk (moved) | Cetacea: Monodontidae, Delphinapterus leucas |  |
| Narwhal | `narwhal_mermaid` | merfolk (moved) | Cetacea: Monodontidae, Monodon monoceros |  |
| Hippo | `hippo_minotaur` | minotaur (moved) | Hippopotamidae: the whales' closest living relatives |  |

### Birds (harpies) `AVES` (47)

*Reptilia > Archosauria > Dinosauria > Aves*

| Species | id | From | True position | |
|---|---|---|---|---|
| Garuda Harpy | `garuda_harpy` | harpy | Accipitriformes (myth eagle-kite) |  |
| Roc Harpy | `roc_harpy` | harpy | Accipitriformes (myth giant raptor) |  |
| Condorune Harpy | `condorune_harpy` | harpy | Accipitriformes: Cathartidae (Harmontide) |  |
| Swan | `swan_harpy` | harpy | Anseriformes: Anatidae |  |
| Duck | `duck_harpy` | harpy | Anseriformes: Anatidae |  |
| Hummingbird | `hummingbird_harpy` | harpy | Apodiformes: Trochilidae |  |
| Landharpy | `landharpy_harpy` | harpy | Aves, flightless (Harmontide; order unclear) | judgement call |
| Seagull Harpy | `seagull_harpy` | harpy | Charadriiformes: Laridae |  |
| Pumpkingull Harpy | `pumpkingull_harpy` | harpy | Charadriiformes: Laridae (Harmontide) |  |
| Reliquary Dove | `reliquary_dove_harpy` | harpy | Columbiformes (Harmontide) |  |
| Dodobold Harpy | `dodobold_harpy` | harpy | Columbiformes: Raphus cucullatus (extinct; the dodo was a pigeon) |  |
| Motmora | `motmora_harpy` | harpy | Coraciiformes: Momotidae (Harmontide) |  |
| Poultry Harpy | `poultry_harpy` | harpy | Galliformes: Gallus gallus |  |
| Peaflare Harpy | `peaflare_harpy` | harpy | Galliformes: Pavo (Harmontide) |  |
| Mirror Crane Harpy | `mirror_crane_harpy` | harpy | Gruiformes: Gruidae (Harmontide) |  |
| Ziz Harpy | `ziz_harpy` | harpy | Neoaves (myth giant bird) |  |
| Alkonost Harpy | `alkonost_harpy` | harpy | Neoaves (myth paradise bird) |  |
| Zhuque Harpy | `zhuque_harpy` | harpy | Neoaves (myth vermilion bird) |  |
| Phoenix Harpy | `phoenix_harpy` | harpy | Neoaves (myth) |  |
| Ironbeak Harpy | `ironbeak_harpy` | harpy | Neoaves, courier bird (Harmontide; order unclear) | judgement call |
| Cassowrath Harpy | `cassowrath_harpy` | harpy | Palaeognathae: Casuarius (Harmontide) |  |
| Emu Harpy | `emu_harpy` | harpy | Palaeognathae: Dromaius |  |
| Emberu Harpy | `emberu_harpy` | harpy | Palaeognathae: Dromaius (Harmontide) |  |
| Ostrich Harpy | `ostrich_harpy` | harpy | Palaeognathae: Struthionidae |  |
| Crow Harpy | `crow_harpy` | harpy | Passeriformes: Corvidae |  |
| Skycrow Harpy | `skycrow_harpy` | harpy | Passeriformes: Corvidae (Harmontide) |  |
| Tengu Harpy | `tengu_harpy` | harpy | Passeriformes: Corvidae (folklore crow tengu) |  |
| Yatagarasu Harpy | `yatagarasu_harpy` | harpy | Passeriformes: Corvidae (myth) |  |
| Trickster Raven Harpy | `trickster_raven_harpy` | harpy | Passeriformes: Corvidae, Corvus corax (Harmontide) |  |
| Rookmourn Harpy | `rookmourn_harpy` | harpy | Passeriformes: Corvidae, Corvus frugilegus (Harmontide) |  |
| Coin Magpie Harpy | `coin_magpie_harpy` | harpy | Passeriformes: Corvidae, Pica (Harmontide) |  |
| Finch Harpy | `finch_harpy` | harpy | Passeriformes: Fringillidae |  |
| Riflebird Harpy | `riflebird_harpy` | harpy | Passeriformes: Paradisaeidae |  |
| Sparrow Harpy | `sparrow_harpy` | harpy | Passeriformes: Passeridae |  |
| Bennu Harpy | `bennu_harpy` | harpy | Pelecaniformes: Ardeidae (myth heron) |  |
| Pelican | `pelican_harpy` | harpy | Pelecaniformes: Pelecanidae |  |
| Ibiset Harpy | `ibiset_harpy` | harpy | Pelecaniformes: Threskiornithidae (Harmontide) |  |
| Ibisking | `ibisking_harpy` | harpy | Pelecaniformes: Threskiornithidae (Harmontide) |  |
| Flamingo Harpy | `flamingo_harpy` | harpy | Phoenicopteriformes |  |
| Toucan Harpy | `toucan_harpy` | harpy | Piciformes: Ramphastidae |  |
| Albatross Harpy | `albatross_harpy` | harpy | Procellariiformes: Diomedeidae |  |
| Macaw Harpy | `macaw_harpy` | harpy | Psittaciformes |  |
| Penguin Harpy | `penguin_harpy` | harpy | Sphenisciformes |  |
| Owl Harpy | `owl_harpy` | harpy | Strigiformes |  |
| Argentowlis | `argentowlis_harpy` | harpy | Strigiformes (Harmontide) |  |
| Stigowl Harpy | `stigowl_harpy` | harpy | Strigiformes (Harmontide) |  |
| La Lechuza | `lechuza_harpy` | harpy | Strigiformes: Tyto alba (folklore) |  |

### Crocodiles, dinosaurs & pterosaurs `ARCHOSAURIA` (9)

*Reptilia > Archosauria (birds kept apart)*

| Species | id | From | True position | |
|---|---|---|---|---|
| Fernstalker | `fernstalker_theropod` | theropod | Dinosauria: Theropoda, Dromaeosauridae (Harmontide) |  |
| Australovenator | `australovenator_theropod` | theropod | Dinosauria: Theropoda, Megaraptora (extinct) |  |
| Spinosaur | `spinosaur_theropod` | theropod | Dinosauria: Theropoda, Spinosauridae (extinct) |  |
| Crocodile | `croc_lizardfolk` | lizardfolk (moved) | Crocodylia |  |
| Crocora | `crocora_lizardfolk` | lizardfolk (moved) | Crocodylia (Harmontide) |  |
| Cipactli | `cipactli_lizardfolk` | lizardfolk (moved) | Crocodylia (myth) |  |
| Moo | `moo_dragon` | dragon (moved) | Dinosauria: Ceratopsidae, Triceratops (extinct, Harmontide) |  |
| Argentinosaurus | `argentinosaurus_lizardfolk` | lizardfolk (moved) | Dinosauria: Sauropoda (extinct) |  |
| Hairypterix | `hairypterix_dragon` | dragon (moved) | Pterosauria (extinct, Harmontide) |  |

### Turtles & tortoises `TESTUDINES` (14)

*Reptilia > Testudines*

| Species | id | From | True position | |
|---|---|---|---|---|
| Turtfolk | `turtfolk_lizardfolk` | lizardfolk | Testudines (Harmontide) |  |
| Matamata | `matamata_lizardfolk` | lizardfolk | Testudines: Chelidae, Chelus fimbriata |  |
| Turtle | `turtle_lizardfolk` | lizardfolk | Testudines: Cheloniidae |  |
| Conch Mail Turtle | `conch_turtle_lizardfolk` | lizardfolk | Testudines: Cheloniidae (Harmontide) |  |
| Alligator Snapping Turtle | `snapping_turtle_lizardfolk` | lizardfolk | Testudines: Chelydridae, Macrochelys temminckii |  |
| Red-Eared Slider | `red_eared_slider_lizardfolk` | lizardfolk | Testudines: Emydidae |  |
| Box Turtle | `box_turtle_lizardfolk` | lizardfolk | Testudines: Emydidae |  |
| Archelon | `archelon_lizardfolk` | lizardfolk | Testudines: Protostegidae, Archelon (extinct) |  |
| Anvil Tortoise | `anvil_tortoise_lizardfolk` | lizardfolk | Testudines: Testudinidae (Harmontide) |  |
| Turtworld | `turtworld_lizardfolk` | lizardfolk | Testudines: Testudinidae (Harmontide) |  |
| Black Tortoise | `black_tortoise_lizardfolk` | lizardfolk | Testudines: Testudinidae (myth; the serpent is an ornament) |  |
| Galapagos Tortoise | `galapagos_tortoise_lizardfolk` | lizardfolk | Testudines: Testudinidae, Chelonoidis niger |  |
| Kappa | `kappa_lizardfolk` | lizardfolk | Testudines: Trionychidae (folklore) |  |
| Softshell Turtle | `softshell_turtle_lizardfolk` | lizardfolk | Testudines: Trionychidae, Apalone spinifera |  |

### Snakes & lizards `SQUAMATA` (30)

*Reptilia > Lepidosauria > Squamata*

| Species | id | From | True position | |
|---|---|---|---|---|
| Urchskin | `urchskin_lamia` | lamia | Serpentes (Harmontide) |  |
| Turquoise Serpent | `turquoise_serpent_lamia` | lamia | Serpentes (Harmontide) |  |
| Tsuchinoko | `tsuchinoko_lamia` | lamia | Serpentes (cryptid) |  |
| Medusa | `medusa_lamia` | lamia | Serpentes (myth; serpent body) |  |
| Hoop Snake | `hoop_snake_lamia` | lamia | Serpentes (myth; serpent body) |  |
| Xiangliu | `xiangliu_lamia` | lamia | Serpentes (myth; serpent body) |  |
| Sea Serpent | `sea_serpent_lamia` | lamia | Serpentes (myth; serpent body) |  |
| Echidna | `echidna_lamia` | lamia | Serpentes (myth; serpent body) |  |
| Serpopard | `serpopard_lamia` | lamia | Serpentes per her rig (myth; the Egyptian serpopard is a leopard with a snake's neck) | judgement call |
| Boa Lamia | `boa_lamia` | lamia | Serpentes: Boidae |  |
| Anaconda | `anaconda_lamia` | lamia | Serpentes: Boidae, Eunectes |  |
| Titanoboa | `titanoboa_lamia` | lamia | Serpentes: Boidae, Titanoboa (extinct) |  |
| Sea Snake Lamia | `sea_snake_lamia` | lamia | Serpentes: Elapidae, Hydrophiinae |  |
| Caraviper | `caraviper_lamia` | lamia | Serpentes: Elapidae, Naja (Harmontide) |  |
| Wadjet Lamia | `wadjet_lamia` | lamia | Serpentes: Elapidae, Naja haje (deity) |  |
| King Cobra Lamia | `cobra_lamia` | lamia | Serpentes: Elapidae, Ophiophagus hannah |  |
| Naga Lamia | `naga_lamia` | lamia | Serpentes: Elapidae, Ophiophagus hannah (myth) |  |
| Python Lamia | `python_lamia` | lamia | Serpentes: Pythonidae |  |
| Rattlesnake Lamia | `rattlesnake_lamia` | lamia | Serpentes: Viperidae, Crotalus |  |
| Jormungandr | `jormungandr_dragon` | dragon (moved) | Serpentes (myth; this design is a python) |  |
| Iron Kobold | `iron_kobold_lizardfolk` | lizardfolk (moved) | Squamata (Harmontide reptile kobold) |  |
| Chupacabra | `chupacabra_lizardfolk` | lizardfolk (moved) | Squamata (cryptid, spiny reptile) |  |
| Chameleon | `chameleon_lizardfolk` | lizardfolk (moved) | Squamata: Chamaeleonidae |  |
| Basilisk | `basilisk_lizardfolk` | lizardfolk (moved) | Squamata: Corytophanidae, Basiliscus (a real lizard shares her name) |  |
| Gecko Girl | `gecko_lizardfolk` | lizardfolk (moved) | Squamata: Gekkota |  |
| Cave Gekko | `cave_gekko_lizardfolk` | lizardfolk (moved) | Squamata: Gekkota |  |
| Iguana | `iguana_lizardfolk` | lizardfolk (moved) | Squamata: Iguanidae |  |
| Mosasaurus | `mosasaurus_mermaid` | merfolk (moved) | Squamata: Mosasauridae (extinct; mosasaurs were giant sea lizards) |  |
| Cocoskink | `cocoskink_lizardfolk` | lizardfolk (moved) | Squamata: Scincidae (Harmontide) |  |
| Komodo | `komodo_lizardfolk` | lizardfolk (moved) | Squamata: Varanidae, Varanus komodoensis |  |

### Frogs, toads & salamanders `AMPHIBIA` (19)

*Amphibia (Anura + Caudata)*

| Species | id | From | True position | |
|---|---|---|---|---|
| Vodyanoy | `vodyanoy` | anuran | Anura (Slavic river spirit) |  |
| Bullywug | `bullywug` | anuran | Anura (folklore frog-folk) |  |
| Fire-Bellied Toad | `fire_bellied_toad` | anuran | Anura: Bombinatoridae |  |
| Desert Rain Frog | `desert_rain_frog` | anuran | Anura: Breviceps macrops |  |
| Mailtoad | `mailtoad` | anuran | Anura: Bufonidae (Harmontide) |  |
| Mucus Toad | `mucus_toad` | anuran | Anura: Bufonidae (MGE) |  |
| Natterjack | `natterjack` | anuran | Anura: Bufonidae, Epidalea calamita |  |
| Cane Toad | `cane_toad` | anuran | Anura: Bufonidae, Rhinella marina |  |
| Glass Frog | `glass_frog` | anuran | Anura: Centrolenidae |  |
| Goliath Frog | `goliath_frog` | anuran | Anura: Conraua goliath |  |
| Poison Dart Frog | `poison_dart_frog` | anuran | Anura: Dendrobatidae |  |
| Tree Frog | `tree_frog` | anuran | Anura: Hylidae |  |
| Surinam Toad | `surinam_toad` | anuran | Anura: Pipidae, Pipa pipa |  |
| Bullfrog | `bullfrog` | anuran | Anura: Ranidae, Lithobates catesbeianus |  |
| Axolume | `axolume_mermaid` | merfolk (moved) | Caudata: Ambystoma (Harmontide) |  |
| Axolotl Mermaid | `axolotl_mermaid` | merfolk (moved) | Caudata: Ambystoma mexicanum |  |
| Olm | `olm_mermaid` | merfolk (moved) | Caudata: Proteus anguinus |  |
| Salamander | `salamander_lizardfolk` | lizardfolk (moved) | Caudata: Salamandra (legend; salamanders are amphibians, not lizards) |  |
| Newt | `newt_lizardfolk` | lizardfolk (moved) | Caudata: Salamandridae |  |

### Fish (and the mermaids of legend) `ACTINOPTERYGII` (17)

*Actinopterygii*

| Species | id | From | True position | |
|---|---|---|---|---|
| Reeffin | `reeffin_mermaid` | merfolk | Actinopterygii (Harmontide) |  |
| Fishman | `fishman_mermaid` | merfolk | Actinopterygii (Harmontide) |  |
| Leviathan | `leviathan_mermaid` | merfolk | Actinopterygii per this design (the Leviathan is also read as a whale or serpent) | judgement call |
| Piranha | `piranha_mermaid` | merfolk | Characiformes: Serrasalmidae |  |
| Sword Koi | `sword_koi_mermaid` | merfolk | Cypriniformes: Cyprinus (Harmontide) |  |
| Koi Mermaid | `koi_mermaid` | merfolk | Cypriniformes: Cyprinus rubrofuscus |  |
| Electric Eel | `eel_mermaid` | merfolk | Gymnotiformes: Electrophorus (not a true eel) |  |
| Angler | `angler_mermaid` | merfolk | Lophiiformes |  |
| Seahorse | `seahorse_mermaid` | merfolk | Syngnathiformes: Hippocampus |  |
| Pufferfish | `pufferfish_mermaid` | merfolk | Tetraodontiformes: Tetraodontidae |  |
| Rusalka | `rusalka_mermaid` | merfolk | generic fish tail (folklore water spirit) |  |
| Lorelei | `lorelei_mermaid` | merfolk | generic fish tail (folklore water spirit) |  |
| Siren | `siren_mermaid` | merfolk | generic fish tail (sailors' myth; the Greek sirens were bird-women) |  |
| Mantarrow | `mantarrow_mermaid` | merfolk | Chondrichthyes: Mobulidae (Harmontide) | guest |
| Sawfin | `sawfin_mermaid` | merfolk | Chondrichthyes: Pristidae (Harmontide) | guest |
| Shark Mermaid | `shark_mermaid` | merfolk | Chondrichthyes: Selachimorpha; sharks and rays are too few for their own family | guest |
| Starfish | `starfish_crustacean` | crustacean (moved) | Echinodermata: Asteroidea; starfish are closer to fish (and us) than to crabs | guest |

### Spiders & scorpions `CHELICERATA` (13)

*Arthropoda > Chelicerata > Arachnida*

| Species | id | From | True position | |
|---|---|---|---|---|
| Spoodr | `spoodr_arachne` | arachne | Araneae (Harmontide) |  |
| Cave Spider | `cave_spider_arachne` | arachne | Araneae (Minecraft cave spider) |  |
| Orb Weaver Arachne | `orb_weaver_arachne` | arachne | Araneae: Araneidae |  |
| Wolf Spider Arachne | `wolf_spider_arachne` | arachne | Araneae: Lycosidae |  |
| Trapdoor Spider Arachne | `trapdoor_spider_arachne` | arachne | Araneae: Mygalomorphae |  |
| Jumping Spider Arachne | `jumping_spider_arachne` | arachne | Araneae: Salticidae |  |
| Peacock Spider | `peacock_spider_arachne` | arachne | Araneae: Salticidae, Maratus |  |
| Tarantula Arachne | `tarantula_arachne` | arachne | Araneae: Theraphosidae |  |
| Black Widow | `black_widow_arachne` | arachne | Araneae: Theridiidae, Latrodectus |  |
| Jorogumo | `jorogumo_arachne` | arachne | Araneae: Trichonephila clavata (folklore) |  |
| Ushi-oni | `ushi_oni_arachne` | arachne | Araneae: spider body (folklore; the ox head is an ornament) |  |
| Scorpion | `scorpion_bug` | bug (moved) | Arachnida: Scorpiones |  |
| Girtablilu | `girtablilu_bug` | bug (moved) | Arachnida: Scorpiones (myth) |  |

### Insects `HEXAPODA` (23)

*Arthropoda > Pancrustacea > Hexapoda*

| Species | id | From | True position | |
|---|---|---|---|---|
| Date Palm Beetle | `date_beetle_bug` | bug | Insecta: Coleoptera (Harmontide) |  |
| Ladybug | `ladybug_bug` | bug | Insecta: Coleoptera, Coccinellidae |  |
| Firefly | `firefly_bug` | bug | Insecta: Coleoptera, Lampyridae |  |
| Scarabronze | `scarabronze_bug` | bug | Insecta: Coleoptera, Scarabaeidae (Harmontide) |  |
| Mosquito | `mosquito_bug` | bug | Insecta: Diptera, Culicidae |  |
| Cicada | `cicada_bug` | bug | Insecta: Hemiptera, Cicadidae |  |
| Bee | `bee_bug` | bug | Insecta: Hymenoptera, Apidae |  |
| Queen Bee | `queen_bee_bug` | bug | Insecta: Hymenoptera, Apidae |  |
| Ant | `ant_bug` | bug | Insecta: Hymenoptera, Formicidae |  |
| Queen Ant | `queen_ant_bug` | bug | Insecta: Hymenoptera, Formicidae |  |
| Spider-Wasp | `spider_wasp_bug` | bug | Insecta: Hymenoptera, Pompilidae |  |
| Hornet | `hornet_bug` | bug | Insecta: Hymenoptera, Vespidae |  |
| Mothman | `mothman_bug` | bug | Insecta: Lepidoptera (cryptid) |  |
| Moth | `moth_bug` | bug | Insecta: Lepidoptera (moth) |  |
| Moth Caterpillar | `moth_larva_bug` | bug | Insecta: Lepidoptera (moth) |  |
| Papillon | `papillon_bug` | bug | Insecta: Lepidoptera, Papilionoidea |  |
| Papillon Caterpillar | `papillon_larva_bug` | bug | Insecta: Lepidoptera, Papilionoidea |  |
| Mantis Girl | `mantis_bug` | bug | Insecta: Mantodea |  |
| Temple Mantis | `temple_mantis_bug` | bug | Insecta: Mantodea (Harmontide) |  |
| Antlion | `antlion_bug` | bug | Insecta: Neuroptera, Myrmeleontidae |  |
| Dragonfly | `dragonfly_bug` | bug | Insecta: Odonata |  |
| Tardigrade | `tardigrade_bug` | bug | Tardigrada: a cousin of the arthropods (Panarthropoda) | guest |
| Centipede | `centipede_lamia` | lamia (moved) | Myriapoda: Chilopoda; the insects' nearest relatives here (Mandibulata) | guest |

### Crabs, lobsters & shrimp `MALACOSTRACA` (7)

*Arthropoda > Pancrustacea > Malacostraca*

| Species | id | From | True position | |
|---|---|---|---|---|
| Crab Girl | `crab_crustacean` | crustacean | Decapoda: Brachyura |  |
| Floodcrab | `floodcrab_crustacean` | crustacean | Decapoda: Brachyura (Harmontide) |  |
| Spider Crab | `spider_crab_crustacean` | crustacean | Decapoda: Majoidea |  |
| Lobster | `lobster_crustacean` | crustacean | Decapoda: Nephropidae |  |
| Hermit Crab Girl | `hermit_crab_crustacean` | crustacean | Decapoda: Paguroidea |  |
| Mantis Shrimp | `mantis_shrimp_crustacean` | crustacean | Stomatopoda |  |
| Anomalocaris | `anomalocaris_mermaid` | merfolk (moved) | Radiodonta (extinct stem arthropod) | guest |

### Octopuses, squid & snails `MOLLUSCA` (17)

*Mollusca*

| Species | id | From | True position | |
|---|---|---|---|---|
| Kraken | `kraken_mollusk` | mollusk | Cephalopoda (myth) |  |
| Giant Kraken | `giant_kraken_mollusk` | mollusk | Cephalopoda (myth) |  |
| Scylla | `scylla_mollusk` | mollusk | Cephalopoda per her tentacle skirt (myth) |  |
| Charybdis | `charybdis_mollusk` | mollusk | Cephalopoda per her tentacle skirt (myth) |  |
| Ammonite | `ammonite_mollusk` | mollusk | Cephalopoda: Ammonoidea (extinct) |  |
| Squidkin | `squidkin_mollusk` | mollusk | Cephalopoda: Decapodiformes (Harmontide) |  |
| Ika | `ika_mollusk` | mollusk | Cephalopoda: Decapodiformes (squid) |  |
| Nautilus | `nautilus_mollusk` | mollusk | Cephalopoda: Nautilidae |  |
| Octopus Girl | `octopus_mollusk` | mollusk | Cephalopoda: Octopoda |  |
| Cuttlefish | `cuttlefish_mollusk` | mollusk | Cephalopoda: Sepiida |  |
| Min Min Light | `min_min_mollusk` | mollusk | Cnidaria per this design (cap + tentacles); the folklore Min Min is a light | judgement call |
| Snail Girl | `snail_mollusk` | mollusk | Gastropoda |  |
| Sea Slug | `sea_slug_mollusk` | mollusk | Gastropoda: Nudibranchia |  |
| Spewslug | `spewslug_mollusk` | mollusk | Gastropoda: slug (Harmontide) |  |
| Anemone | `anemone_mollusk` | mollusk | Cnidaria: Actiniaria | guest |
| Jellyfish Girl | `jellyfish_mollusk` | mollusk | Cnidaria: Scyphozoa (her guide entry already says so) | guest |
| Sandworm | `sandworm_lamia` | lamia (moved) | Annelida (Harmontide sandworm); worms and mollusks are both Lophotrochozoa | guest |

### Plants & fungi `FLORA` (14)

*Plantae + Fungi (two kingdoms, one guide)*

| Species | id | From | True position | |
|---|---|---|---|---|
| Matango | `matango_flora` | flora | Fungi: Agaricales (fungi are closer to animals than to plants) |  |
| Shiitake | `shiitake_flora` | flora | Fungi: Lentinula edodes |  |
| Kesaran Pasaran | `kesaran_pasaran_flora` | flora | Plantae: Asteraceae (dandelion seed per this design) |  |
| Sunflower | `sunflower_flora` | flora | Plantae: Asteraceae, Helianthus |  |
| Cactus Girl | `cactus_flora` | flora | Plantae: Cactaceae |  |
| Venus Flytrap | `venus_flytrap_flora` | flora | Plantae: Droseraceae, Dionaea muscipula |  |
| Lotus | `lotus_flora` | flora | Plantae: Nelumbonaceae, Nelumbo |  |
| Rafflesia | `rafflesia_flora` | flora | Plantae: Rafflesiaceae |  |
| Rose | `rose_flora` | flora | Plantae: Rosaceae, Rosa |  |
| Mandragora | `mandragora_flora` | flora | Plantae: Solanaceae, Mandragora |  |
| Alraune | `alraune_flora` | flora | Plantae: flowering plant (folklore) |  |
| Ent | `ent_flora` | flora | Plantae: tree (myth) |  |
| Dryad | `dryad_flora` | flora | Plantae: tree (myth) |  |
| Censhroom | `censhroom_centaur` | centaur (moved) | Fungi on an unspecified taur; the fungus is the only clear animal-or-not | judgement call |

### Fae & folk (elves, fairies, spirits) `FAE` (26)

*mythic: humanoid folk*

| Species | id | From | True position | |
|---|---|---|---|---|
| Night Elf | `night_elf` | elf | outside taxonomy (humanoid folk) |  |
| High Elf | `high_elf` | elf | outside taxonomy (humanoid folk) |  |
| Wood Elf | `wood_elf` | elf | outside taxonomy (humanoid folk) |  |
| Blood Elf | `blood_elf` | elf | outside taxonomy (humanoid folk) |  |
| Desert Elf | `desert_elf` | elf | outside taxonomy (humanoid folk) |  |
| Dark Elf | `dark_elf` | elf | outside taxonomy (humanoid folk) |  |
| Brownie | `brownie_elf` | elf | outside taxonomy (humanoid folk) |  |
| Valkyrie | `valkyrie_elf` | elf | outside taxonomy (humanoid folk) |  |
| Domovoi | `domovoi_elf` | elf | outside taxonomy (humanoid folk) |  |
| Mad Hatter | `mad_hatter_elf` | elf | outside taxonomy (humanoid folk) |  |
| Angel | `angel_elf` | elf | outside taxonomy (humanoid folk) |  |
| Gandharva | `gandharva_elf` | elf | outside taxonomy (humanoid folk) |  |
| Apsara | `apsara_elf` | elf | outside taxonomy (humanoid folk) |  |
| Knocker | `knocker_elf` | elf | outside taxonomy (humanoid folk) |  |
| Kaguya | `kaguya_elf` | elf | outside taxonomy (humanoid folk) |  |
| Ophanim | `ophanim_elf` | elf | outside taxonomy (humanoid folk) |  |
| Norn | `norn_elf` | elf | outside taxonomy (humanoid folk) |  |
| Snow Elf | `snow_elf` | elf | outside taxonomy (humanoid folk) |  |
| Gnome | `gnome_elf` | elf | outside taxonomy (humanoid folk) |  |
| Dwarf | `dwarf_elf` | elf | outside taxonomy (humanoid folk) |  |
| Nymph | `nymph_elf` | elf | outside taxonomy (humanoid folk) |  |
| Fairy | `fairy_bug` | bug (moved) | outside taxonomy (fairy: insect wings are an ornament) |  |
| Fairy Queen | `fairy_queen_bug` | bug (moved) | outside taxonomy (fairy: insect wings are an ornament) |  |
| Pixi | `pixi_bug` | bug (moved) | outside taxonomy (fairy: insect wings are an ornament) |  |
| Rlff | `rlff_bug` | bug (moved) | outside taxonomy (fairy: insect wings are an ornament) |  |
| Leshy | `leshy_flora` | flora (moved) | outside taxonomy (forest spirit; moss beard is an ornament) | judgement call |

### Dragons & chimeras `DRAGON` (17)

*mythic: no single real animal*

| Species | id | From | True position | |
|---|---|---|---|---|
| Gryphon | `gryphon_dragon` | dragon | outside taxonomy (chimera of several animals) |  |
| Alebrije | `alebrije_dragon` | dragon | outside taxonomy (chimera of several animals) |  |
| Cockatrice | `cockatrice_dragon` | dragon | outside taxonomy (chimera of several animals) |  |
| Chimera | `chimera_dragon` | dragon | outside taxonomy (chimera of several animals) |  |
| Long Dragon | `long_dragon` | dragon | outside taxonomy (dragon) |  |
| Wyrm | `wyrm_dragon` | dragon | outside taxonomy (dragon) |  |
| Kannae | `kannae_dragon` | dragon | outside taxonomy (dragon) |  |
| Wyvern | `wyvern_dragon` | dragon | outside taxonomy (dragon) |  |
| Yamata no Orochi | `orochi_dragon` | dragon | outside taxonomy (dragon) |  |
| Nenekea | `nenekea_dragon` | dragon | outside taxonomy (dragon) |  |
| Bahamut | `bahamut_dragon` | dragon | outside taxonomy (dragon) |  |
| Kun | `kun_dragon` | dragon | outside taxonomy (dragon) |  |
| Amphiptere | `amphiptere_dragon` | dragon | outside taxonomy (dragon) |  |
| Ender Dragon | `ender_dragon` | dragon | outside taxonomy (dragon) |  |
| Macawtl | `macawtl_dragon` | dragon | outside taxonomy (feathered serpent: bird + snake) |  |
| Quetzalcoatl | `quetzalcoatl_dragon` | dragon | outside taxonomy (feathered serpent: bird + snake) |  |
| Shedim | `shedim_centaur` | centaur (moved) | no single animal (deer-fox spirit) |  |

### Undead `UNDEAD` (33)

*mythic*

| Species | id | From | True position | |
|---|---|---|---|---|
| Living Doll | `living_doll_undead` | undead | outside taxonomy (undead) |  |
| Will-o'-wisp | `will_o_wisp_undead` | undead | outside taxonomy (undead) |  |
| Yuki-onna | `yuki_onna_undead` | undead | outside taxonomy (undead) |  |
| Phantom Girl | `phantom_undead` | undead | outside taxonomy (undead) |  |
| Vampire | `vampire_undead` | undead | outside taxonomy (undead) |  |
| Zombie Girl | `zombie_undead` | undead | outside taxonomy (undead) |  |
| Frankenstein's Monster | `frankenstein_undead` | undead | outside taxonomy (undead) |  |
| Banshee | `banshee_undead` | undead | outside taxonomy (undead) |  |
| Dullahan | `dullahan_undead` | undead | outside taxonomy (undead) |  |
| Lich | `lich_undead` | undead | outside taxonomy (undead) |  |
| La Llorona | `llorona_undead` | undead | outside taxonomy (undead) |  |
| Mummy | `mummy_undead` | undead | outside taxonomy (undead) |  |
| Yurei | `yurei_undead` | undead | outside taxonomy (undead) |  |
| Kasurenijimi | `kasurenijimi_undead` | undead | outside taxonomy (undead) |  |
| Draugr | `draugr_undead` | undead | outside taxonomy (undead) |  |
| Ghoul | `ghoul_undead` | undead | outside taxonomy (undead) |  |
| Gashadokuro | `gashadokuro_undead` | undead | outside taxonomy (undead) |  |
| Nasnas | `nasnas_undead` | undead | outside taxonomy (undead) |  |
| Jiangshi | `jiangshi_undead` | undead | outside taxonomy (undead) |  |
| Hand | `hand_undead` | undead | outside taxonomy (undead) |  |
| Koschei | `koschei_undead` | undead | outside taxonomy (undead) |  |
| La Mano Peluda | `mano_peluda_undead` | undead | outside taxonomy (undead) |  |
| Gaitress | `gaitress_undead` | undead | outside taxonomy (undead) |  |
| Cordyceps Infected | `cordyceps_undead` | undead | outside taxonomy (undead) |  |
| Lost Soul | `lost_soul_undead` | undead | outside taxonomy (undead) |  |
| Soul Cluster | `soul_cluster_undead` | undead | outside taxonomy (undead) |  |
| Soul Leech | `soul_leech_undead` | undead | outside taxonomy (undead) |  |
| Drowned | `drowned_undead` | undead | outside taxonomy (undead) |  |
| Famished | `famished_undead` | undead | outside taxonomy (undead) |  |
| Skeleton | `skeleton_undead` | undead | outside taxonomy (undead) |  |
| Poltergeist | `poltergeist_undead` | undead | outside taxonomy (undead) |  |
| Sandman | `sandman_undead` | undead | outside taxonomy (undead) |  |
| Candle Spirit | `candle_spirit_undead` | undead | outside taxonomy (undead) |  |

### Devils, oni & giants `DEVIL` (30)

*mythic*

| Species | id | From | True position | |
|---|---|---|---|---|
| Lesser Devil | `lesser_devil` | devil | outside taxonomy (spirits & demons) |  |
| Succubus | `succubus_devil` | devil | outside taxonomy (spirits & demons) |  |
| Greater Devil | `greater_devil` | devil | outside taxonomy (spirits & demons) |  |
| Jack o'Lantern | `jack_o_lantern_devil` | devil | outside taxonomy (spirits & demons) |  |
| Jiang Shi | `jiang_shi_devil` | devil | outside taxonomy (spirits & demons) |  |
| Oni | `oni_devil` | devil | outside taxonomy (spirits & demons) |  |
| Gremlin | `gremlin_devil` | devil | outside taxonomy (spirits & demons) |  |
| Karakasa Obake | `karakasa_obake_devil` | devil | outside taxonomy (spirits & demons) |  |
| Chochin-obake | `chochin_obake_devil` | devil | outside taxonomy (spirits & demons) |  |
| Ittan-momen | `ittan_momen_devil` | devil | outside taxonomy (spirits & demons) |  |
| Rokurokubi | `rokurokubi_devil` | devil | outside taxonomy (spirits & demons) |  |
| Kikimora | `kikimora_devil` | devil | outside taxonomy (spirits & demons) |  |
| Akaname | `akaname_devil` | devil | outside taxonomy (spirits & demons) |  |
| Cyclops | `cyclops_devil` | devil | outside taxonomy (spirits & demons) |  |
| Genie | `genie_devil` | devil | outside taxonomy (spirits & demons) |  |
| Ogre | `ogre_devil` | devil | outside taxonomy (spirits & demons) |  |
| Taotie | `taotie_devil` | devil | outside taxonomy (spirits & demons) |  |
| Behemoth | `behemoth_devil` | devil | outside taxonomy (spirits & demons) |  |
| Fire Giant | `fire_giant_devil` | devil | outside taxonomy (spirits & demons) |  |
| Frost Giant | `frost_giant_devil` | devil | outside taxonomy (spirits & demons) |  |
| Ifrit | `ifrit_devil` | devil | outside taxonomy (spirits & demons) |  |
| Pazuzu | `pazuzu_devil` | devil | outside taxonomy (spirits & demons) |  |
| Lilith | `lilith_devil` | devil | outside taxonomy (spirits & demons) |  |
| Wrath | `wrath_devil` | devil | outside taxonomy (spirits & demons) |  |
| Demon | `demon_devil` | devil | outside taxonomy (spirits & demons) |  |
| Fury | `fury_devil` | devil | outside taxonomy (spirits & demons) |  |
| Firesprite | `firesprite_devil` | devil | outside taxonomy (spirits & demons) |  |
| Goblin | `goblin_devil` | devil | outside taxonomy (spirits & demons) |  |
| Troll | `troll_devil` | devil | outside taxonomy (spirits & demons) |  |
| Book Spirit | `book_spirit_devil` | devil | outside taxonomy (spirits & demons) |  |

### Slimes `SLIME` (11)

*mythic*

| Species | id | From | True position | |
|---|---|---|---|---|
| Verdant Slime | `verdant_slime` | slime | outside taxonomy (living jelly) |  |
| Magma Slime | `magma_slime` | slime | outside taxonomy (living jelly) |  |
| Honey Slime | `honey_slime` | slime | outside taxonomy (living jelly) |  |
| Frost Slime | `frost_slime` | slime | outside taxonomy (living jelly) |  |
| Amethyst Slime | `amethyst_slime` | slime | outside taxonomy (living jelly) |  |
| Mixed Slime | `mixed_slime` | slime | outside taxonomy (living jelly) |  |
| Mimic | `mimic_slime` | slime | outside taxonomy (living jelly) |  |
| Ender Slime | `ender_slime` | slime | outside taxonomy (living jelly) |  |
| Undine | `undine_slime` | slime | outside taxonomy (living jelly) |  |
| Klabuster | `klabuster_slime` | slime | outside taxonomy (living jelly) |  |
| Copper Slime | `copper_slime` | slime | outside taxonomy (living jelly) |  |

### Constructs `CONSTRUCT` (8)

*made, not born*

| Species | id | From | True position | |
|---|---|---|---|---|
| Golem | `golem_construct` | construct | made, not born |  |
| Gargoyle | `gargoyle_construct` | construct | made, not born |  |
| Talosling | `talosling_construct` | construct | made, not born |  |
| Tin Squirrel | `tin_squirrel_construct` | construct | made, not born |  |
| Tin Jack | `tin_jack_construct` | construct | made, not born |  |
| Automaton | `automaton_construct` | construct | made, not born |  |
| Scarecrow | `scarecrow_construct` | construct | made, not born |  |
| Snowgirl | `snowgirl_construct` | construct | made, not born |  |

### Riders `RIDER` (5)

*mythic*

| Species | id | From | True position | |
|---|---|---|---|---|
| War | `war_rider` | rider | outside taxonomy (riders) |  |
| Famine | `famine_rider` | rider | outside taxonomy (riders) |  |
| Pestilence | `pestilence_rider` | rider | outside taxonomy (riders) |  |
| Death | `death_rider` | rider | outside taxonomy (riders) |  |
| Charro Negro | `charro_negro_rider` | rider | outside taxonomy (riders) |  |
