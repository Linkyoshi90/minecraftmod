"""The taxonomy of every MonsterGirls mob (TAXONOMY_PLAN.md): which real clade each species belongs to, her true position,
and whether she is a guest (her clade is too small, so she lives with her nearest relative) or a judgement call.

Single source of truth for the clades. `python tools/taxonomy.py` checks that every species in GirlSpecies.java plus the
harpies and anurans has a clade, and writes entity/Clade.java. Re-run it after adding a species.

Her clade is separate from her form (GirlSpecies.Family): the form picks body, rig and behaviour, the clade picks milk,
dairy names, spawn-rate setting and field guide.
"""
import collections
import os
import re

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")

# new family: (display, real clade path, milk effects, old enum ids that fold into it)
FAM = collections.OrderedDict([
    ("FELIFORMIA",     ("Feliforms (cats & kin)", "Mammalia > Carnivora > Feliformia", "Night Vision", "NEKOMATA")),
    ("CANIDAE",        ("Canids (wolves, dogs & foxes)", "Mammalia > Carnivora > Caniformia > Canidae", "Speed + Luck", "CANID, KITSUNE")),
    ("ARCTOIDEA",      ("Arctoids (bears, weasels, raccoons & seals)", "Mammalia > Carnivora > Caniformia > Arctoidea", "Resistance + Water Breathing", "new")),
    ("RODENTIA",       ("Rodents", "Mammalia > Glires > Rodentia", "Haste + Luck", "RODENT")),
    ("LAGOMORPHA",     ("Hares & rabbits", "Mammalia > Glires > Lagomorpha", "Jump Boost", "JACKALOPE")),
    ("PRIMATES",       ("Primates", "Mammalia > Euarchonta > Primates", "Jump Boost + Strength", "new")),
    ("CHIROPTERA",     ("Bats", "Mammalia > Laurasiatheria > Chiroptera", "Night Vision + Slow Falling", "BAT")),
    ("MARSUPIALIA",    ("Marsupials", "Mammalia > Marsupialia", "Jump Boost + Resistance", "MARSUPIAL")),
    ("XENARTHRA",      ("Armadillos, sloths & anteaters", "Mammalia > Xenarthra", "Resistance + Absorption", "ARMOURED")),
    ("PAENUNGULATA",   ("Elephants, sea cows & hyraxes", "Mammalia > Afrotheria > Paenungulata", "Health Boost", "new")),
    ("PERISSODACTYLA", ("Odd-toed hoofed (horses, tapirs, rhinos)", "Mammalia > Laurasiatheria > Perissodactyla", "Speed", "CENTAUR")),
    ("BOVIDAE",        ("Cattle, sheep, goats & antelope", "Mammalia > Artiodactyla > Ruminantia > Bovidae", "Strength", "MINOTAUR")),
    ("CERVIDAE",       ("Deer (and giraffes)", "Mammalia > Artiodactyla > Ruminantia > Cervidae", "Speed + Jump Boost", "new")),
    ("CAMELIDAE",      ("Camels & llamas", "Mammalia > Artiodactyla > Tylopoda > Camelidae", "Fire Resistance", "new")),
    ("SUINA",          ("Pigs & boars", "Mammalia > Artiodactyla > Suina", "Haste + Absorption", "new")),
    ("WHIPPOMORPHA",   ("Whales & hippos", "Mammalia > Artiodactyla > Whippomorpha", "Dolphin's Grace + Water Breathing", "new")),
    ("AVES",           ("Birds (harpies)", "Reptilia > Archosauria > Dinosauria > Aves", "Slow Falling", "HARPY milk")),
    ("ARCHOSAURIA",    ("Crocodiles, dinosaurs & pterosaurs", "Reptilia > Archosauria (birds kept apart)", "Strength + Speed", "THEROPOD")),
    ("TESTUDINES",     ("Turtles & tortoises", "Reptilia > Testudines", "Resistance + Water Breathing", "LIZARDFOLK")),
    ("SQUAMATA",       ("Snakes & lizards", "Reptilia > Lepidosauria > Squamata", "Regeneration", "LAMIA")),
    ("AMPHIBIA",       ("Frogs, toads & salamanders", "Amphibia (Anura + Caudata)", "Jump Boost + Water Breathing", "ANURAN milk")),
    ("ACTINOPTERYGII", ("Fish (and the mermaids of legend)", "Actinopterygii", "Water Breathing", "MERFOLK")),
    ("CHELICERATA",    ("Spiders & scorpions", "Arthropoda > Chelicerata > Arachnida", "Resistance", "ARACHNE")),
    ("HEXAPODA",       ("Insects", "Arthropoda > Pancrustacea > Hexapoda", "Haste", "BUG")),
    ("MALACOSTRACA",   ("Crabs, lobsters & shrimp", "Arthropoda > Pancrustacea > Malacostraca", "Conduit Power", "CRUSTACEAN")),
    ("MOLLUSCA",       ("Octopuses, squid & snails", "Mollusca", "Dolphin's Grace", "MOLLUSK")),
    ("FLORA",          ("Plants & fungi", "Plantae + Fungi (two kingdoms, one guide)", "none", "FLORA")),
    # outside the tree of life
    ("FAE",            ("Fae & folk (elves, fairies, spirits)", "mythic: humanoid folk", "Hero of the Village", "ELF")),
    ("DRAGON",         ("Dragons & chimeras", "mythic: no single real animal", "Fire Resistance + Strength", "DRAGON")),
    ("UNDEAD",         ("Undead", "mythic", "Invisibility", "UNDEAD")),
    ("DEVIL",          ("Devils, oni & giants", "mythic", "Absorption", "DEVIL")),
    ("SLIME",          ("Slimes", "mythic", "Health Boost + Jump Boost", "SLIME")),
    ("CONSTRUCT",      ("Constructs", "made, not born", "none", "CONSTRUCT")),
    ("RIDER",          ("Riders", "mythic", "Strength + Resistance", "RIDER")),
])

D = {  # old family -> default new family
    "NEKOMATA": "FELIFORMIA", "KITSUNE": "CANIDAE", "LAMIA": "SQUAMATA", "ARACHNE": "CHELICERATA", "CENTAUR": "PERISSODACTYLA",
    "MERFOLK": "ACTINOPTERYGII", "SLIME": "SLIME", "JACKALOPE": "LAGOMORPHA", "MINOTAUR": "BOVIDAE", "MOLLUSK": "MOLLUSCA",
    "ELF": "FAE", "UNDEAD": "UNDEAD", "LIZARDFOLK": "TESTUDINES", "DRAGON": "DRAGON", "DEVIL": "DEVIL", "BUG": "HEXAPODA",
    "FLORA": "FLORA", "CANID": "CANIDAE", "RODENT": "RODENTIA", "CRUSTACEAN": "MALACOSTRACA", "MARSUPIAL": "MARSUPIALIA",
    "ARMOURED": "XENARTHRA", "CONSTRUCT": "CONSTRUCT", "BAT": "CHIROPTERA", "THEROPOD": "ARCHOSAURIA", "RIDER": "RIDER",
    "HARPY": "AVES", "ANURAN": "AMPHIBIA",
}

# id -> (new family or None for default, true position, flag) ; flag: "" | "guest" | "call" (judgement call)
P = {}
def s(fam, ids, pos="", flag=""):
    for i in ids.split():
        P[i] = (fam, pos, flag)

# ---------------------------------------------------------------- old NEKOMATA
s(None, "serval_nekomata", "Felidae: Leptailurus serval")
s(None, "tiger_nekomata", "Felidae: Panthera tigris")
s(None, "lioness_nekomata", "Felidae: Panthera leo")
s(None, "snow_leopard_nekomata", "Felidae: Panthera uncia")
s(None, "cheetah_nekomata", "Felidae: Acinonyx jubatus")
s(None, "lynx_nekomata", "Felidae: Lynx")
s(None, "ocelot_nekomata", "Felidae: Leopardus pardalis")
s(None, "caracal_nekomata", "Felidae: Caracal caracal")
s(None, "black_panther_nekomata", "Felidae: Panthera pardus (melanistic)")
s(None, "smilodon_nekomata", "Felidae: Machairodontinae, Smilodon (extinct)")
s(None, "sabre_nekomata", "Felidae: Machairodontinae (Harmontide)")
s(None, "snowloper_nekomata", "Felidae: Panthera uncia (Harmontide)")
s(None, "jaglare_nekomata", "Felidae: Panthera onca (Harmontide)")
s(None, "censer_lion_nekomata", "Felidae: Panthera leo (Harmontide)")
s(None, "blaze_cat_nekomata waterlil_nekomata", "Felidae (Harmontide)")
s(None, "cheshire_nekomata cait_sith_nekomata bakeneko_nekomata", "Felidae: Felis (folklore cat)")
s(None, "bastet_nekomata", "Felidae: Felis (deity)")
s(None, "rakshasa_nekomata", "Felidae: Panthera tigris (myth)")
s(None, "manticore_nekomata", "Felidae: lion body (myth; the scorpion tail is an ornament)")
s(None, "ammit_nekomata", "Felidae: lion per this design (the myth's Ammit is lion + hippo + crocodile)", "call")
s(None, "nue_nekomata", "Felidae: tiger body (myth; monkey face, snake tail are ornaments)", "call")
s("PERISSODACTYLA", "baku_nekomata", "Tapiridae (myth; this design is a tapir)")
# ---------------------------------------------------------------- old KITSUNE
s(None, "red_kitsune", "Canidae: Vulpes vulpes")
s(None, "arctic_kitsune", "Canidae: Vulpes lagopus")
s(None, "fennec_kitsune", "Canidae: Vulpes zerda")
s(None, "dunefennec_kitsune", "Canidae: Vulpes zerda (Harmontide)")
s(None, "corsac_kitsune", "Canidae: Vulpes corsac")
s(None, "gray_kitsune", "Canidae: Urocyon cinereoargenteus")
s(None, "bat_eared_kitsune", "Canidae: Otocyon megalotis")
s(None, "wattlefox_kitsune vulphea_kitsune", "Canidae: Vulpes (Harmontide)")
s(None, "huli_jing_kitsune obaa_kitsune kumiho_kitsune", "Canidae: Vulpes (folklore fox spirit)")
# ---------------------------------------------------------------- old LAMIA
s(None, "python_lamia", "Serpentes: Pythonidae")
s(None, "boa_lamia", "Serpentes: Boidae")
s(None, "anaconda_lamia", "Serpentes: Boidae, Eunectes")
s(None, "titanoboa_lamia", "Serpentes: Boidae, Titanoboa (extinct)")
s(None, "cobra_lamia", "Serpentes: Elapidae, Ophiophagus hannah")
s(None, "naga_lamia", "Serpentes: Elapidae, Ophiophagus hannah (myth)")
s(None, "wadjet_lamia", "Serpentes: Elapidae, Naja haje (deity)")
s(None, "caraviper_lamia", "Serpentes: Elapidae, Naja (Harmontide)")
s(None, "sea_snake_lamia", "Serpentes: Elapidae, Hydrophiinae")
s(None, "rattlesnake_lamia", "Serpentes: Viperidae, Crotalus")
s(None, "medusa_lamia echidna_lamia xiangliu_lamia sea_serpent_lamia hoop_snake_lamia", "Serpentes (myth; serpent body)")
s(None, "tsuchinoko_lamia", "Serpentes (cryptid)")
s(None, "urchskin_lamia turquoise_serpent_lamia", "Serpentes (Harmontide)")
s(None, "serpopard_lamia", "Serpentes per her rig (myth; the Egyptian serpopard is a leopard with a snake's neck)", "call")
s("MOLLUSCA", "sandworm_lamia", "Annelida (Harmontide sandworm); worms and mollusks are both Lophotrochozoa", "guest")
s("HEXAPODA", "centipede_lamia", "Myriapoda: Chilopoda; the insects' nearest relatives here (Mandibulata)", "guest")
# ---------------------------------------------------------------- old ARACHNE
s(None, "jumping_spider_arachne", "Araneae: Salticidae")
s(None, "peacock_spider_arachne", "Araneae: Salticidae, Maratus")
s(None, "orb_weaver_arachne", "Araneae: Araneidae")
s(None, "tarantula_arachne", "Araneae: Theraphosidae")
s(None, "trapdoor_spider_arachne", "Araneae: Mygalomorphae")
s(None, "wolf_spider_arachne", "Araneae: Lycosidae")
s(None, "black_widow_arachne", "Araneae: Theridiidae, Latrodectus")
s(None, "jorogumo_arachne", "Araneae: Trichonephila clavata (folklore)")
s(None, "ushi_oni_arachne", "Araneae: spider body (folklore; the ox head is an ornament)")
s(None, "spoodr_arachne", "Araneae (Harmontide)")
s(None, "cave_spider_arachne", "Araneae (Minecraft cave spider)")
# ---------------------------------------------------------------- old CENTAUR
s(None, "horse_centaur", "Equidae: Equus caballus")
s(None, "zebra_centaur", "Equidae: Equus quagga")
s(None, "donkey_centaur", "Equidae: Equus asinus")
s(None, "bellmare_centaur", "Equidae: Equus asinus (Harmontide; goat horns are an ornament)")
s(None, "kelpie_centaur unicorn_centaur", "Equidae (folklore horse)")
s(None, "pegasus_centaur", "Equidae (myth; wings are an ornament)")
s(None, "tapir_centaur", "Tapiridae: Tapirus")
s("CERVIDAE", "reindeer_centaur caribou_centaur", "Cervidae: Rangifer tarandus")
s("CERVIDAE", "moose_centaur", "Cervidae: Alces alces")
s("CERVIDAE", "halberelk_centaur", "Cervidae: Cervus canadensis (Harmontide)")
s("CERVIDAE", "momoshika_centaur", "Cervidae: Cervus nippon (Harmontide)")
s("CERVIDAE", "aurorantler_centaur stagel_centaur palmyr_centaur", "Cervidae (Harmontide)")
s("CERVIDAE", "qilin_centaur", "Cervidae (myth; scales are an ornament)")
s("CERVIDAE", "giraffe_centaur", "Giraffidae: Giraffa; too few giraffids for a family of their own", "guest")
s("CERVIDAE", "okapi_centaur", "Giraffidae: Okapia johnstoni", "guest")
s("CERVIDAE", "prairhorn_centaur", "Antilocapridae: Antilocapra americana (Harmontide)", "guest")
s("BOVIDAE", "goat_centaur", "Bovidae: Caprinae, Oreamnos americanus")
s("CAMELIDAE", "camel_centaur", "Camelidae: Camelus bactrianus")
s("CAMELIDAE", "dromedary_centaur", "Camelidae: Camelus dromedarius")
s("CAMELIDAE", "alpaca_centaur", "Camelidae: Vicugna pacos")
s("CAMELIDAE", "vicunero_centaur", "Camelidae: Vicugna vicugna (Harmontide)")
s("CAMELIDAE", "urcuchillay_centaur", "Camelidae: Lama glama (deity)")
s("CAMELIDAE", "ceibler_centaur", "Camelidae: Lama (Harmontide llama-deer)", "call")
s("PAENUNGULATA", "elephant_centaur", "Proboscidea: Loxodonta")
s("PAENUNGULATA", "tusktaur_centaur", "Proboscidea: Mammuthus primigenius (extinct)")
s("FELIFORMIA", "sphinx_centaur ark_seraphim_centaur", "Felidae: lioness body (myth; wings are an ornament)")
s("FELIFORMIA", "lamassu_centaur", "Felidae: lion body per this design (Assyrian lamassu are bull- or lion-bodied)", "call")
s("FELIFORMIA", "anzu_centaur", "Felidae: lioness per this design (the Sumerian Anzu is a lion-headed eagle)", "call")
s("SUINA", "zoglintaur_centaur", "Suidae-like (Minecraft hoglin)")
s("DRAGON", "shedim_centaur", "no single animal (deer-fox spirit)")
s("FLORA", "censhroom_centaur", "Fungi on an unspecified taur; the fungus is the only clear animal-or-not", "call")
# ---------------------------------------------------------------- old MERFOLK
s(None, "koi_mermaid", "Cypriniformes: Cyprinus rubrofuscus")
s(None, "sword_koi_mermaid", "Cypriniformes: Cyprinus (Harmontide)")
s(None, "piranha_mermaid", "Characiformes: Serrasalmidae")
s(None, "pufferfish_mermaid", "Tetraodontiformes: Tetraodontidae")
s(None, "angler_mermaid", "Lophiiformes")
s(None, "seahorse_mermaid", "Syngnathiformes: Hippocampus")
s(None, "eel_mermaid", "Gymnotiformes: Electrophorus (not a true eel)")
s(None, "fishman_mermaid reeffin_mermaid", "Actinopterygii (Harmontide)")
s(None, "siren_mermaid", "generic fish tail (sailors' myth; the Greek sirens were bird-women)")
s(None, "rusalka_mermaid lorelei_mermaid", "generic fish tail (folklore water spirit)")
s(None, "leviathan_mermaid", "Actinopterygii per this design (the Leviathan is also read as a whale or serpent)", "call")
s(None, "shark_mermaid", "Chondrichthyes: Selachimorpha; sharks and rays are too few for their own family", "guest")
s(None, "mantarrow_mermaid", "Chondrichthyes: Mobulidae (Harmontide)", "guest")
s(None, "sawfin_mermaid", "Chondrichthyes: Pristidae (Harmontide)", "guest")
s("WHIPPOMORPHA", "orca_mermaid", "Cetacea: Delphinidae, Orcinus orca")
s("WHIPPOMORPHA", "dolphin_mermaid", "Cetacea: Delphinidae")
s("WHIPPOMORPHA", "beluga_mermaid", "Cetacea: Monodontidae, Delphinapterus leucas")
s("WHIPPOMORPHA", "narwhal_mermaid", "Cetacea: Monodontidae, Monodon monoceros")
s("WHIPPOMORPHA", "cetus_mermaid", "Cetacea (myth; this design is a whale)")
s("ARCTOIDEA", "seal_mermaid", "Pinnipedia: Phocidae")
s("ARCTOIDEA", "selkie_mermaid", "Pinnipedia: Phocidae (folklore)")
s("ARCTOIDEA", "moonseal_mermaid", "Pinnipedia (Harmontide)")
s("ARCTOIDEA", "walrus_mermaid", "Pinnipedia: Odobenus rosmarus")
s("ARCTOIDEA", "rust_otter_mermaid", "Mustelidae: Lutrinae (Harmontide)")
s("ARCTOIDEA", "ahuizotl_mermaid", "Mustelidae: Lontra longicaudis (myth)")
s("PAENUNGULATA", "dugong_mermaid", "Sirenia: Dugong dugon")
s("PAENUNGULATA", "manatee_mermaid", "Sirenia: Trichechus")
s("PAENUNGULATA", "moopie_mermaid", "Sirenia, the real 'sea cows' (Harmontide)")
s("CANIDAE", "doggo_mermaid", "Canis familiaris (Harmontide sea dog; the fish tail is an ornament)", "call")
s("AMPHIBIA", "axolotl_mermaid", "Caudata: Ambystoma mexicanum")
s("AMPHIBIA", "axolume_mermaid", "Caudata: Ambystoma (Harmontide)")
s("AMPHIBIA", "olm_mermaid", "Caudata: Proteus anguinus")
s("SQUAMATA", "mosasaurus_mermaid", "Squamata: Mosasauridae (extinct; mosasaurs were giant sea lizards)")
s("MARSUPIALIA", "platypus_mermaid", "Monotremata: Ornithorhynchus anatinus; mammals that are neither marsupial nor placental", "guest")
s("MALACOSTRACA", "anomalocaris_mermaid", "Radiodonta (extinct stem arthropod)", "guest")
# ---------------------------------------------------------------- old SLIME
s(None, "verdant_slime magma_slime honey_slime frost_slime amethyst_slime mixed_slime mimic_slime ender_slime undine_slime klabuster_slime copper_slime", "outside taxonomy (living jelly)")
# ---------------------------------------------------------------- old JACKALOPE
s(None, "volcano_jackalope", "Leporidae: Romerolagus diazi")
s(None, "jack_jackalope al_miraj_jackalope", "Leporidae: Lepus (legend; horns are an ornament)")
s(None, "wolpertinger_jackalope", "Leporidae (folklore; antlers and wings are ornaments)")
s(None, "inaba_jackalope easter_jackalope moon_rabbit_jackalope", "Leporidae (folklore rabbit)")
s(None, "clockhare_jackalope boghare_jackalope", "Leporidae (Harmontide)")
# ---------------------------------------------------------------- old MINOTAUR
s(None, "cattle_minotaur highland_minotaur holstaur_minotaur", "Bovidae: Bos taurus")
s(None, "minoan_minotaur", "Bovidae: Bos taurus (myth)")
s(None, "bronzejaw_minotaur mooskuh_minotaur", "Bovidae: Bos taurus (Harmontide)")
s(None, "shroomotaur_minotaur", "Bovidae: Bos taurus (Minecraft mooshroom)")
s(None, "aurochs_minotaur", "Bovidae: Bos primigenius (extinct)")
s(None, "yak_minotaur", "Bovidae: Bos grunniens")
s(None, "bison_minotaur", "Bovidae: Bison")
s(None, "dustbison_minotaur", "Bovidae: Bison (Harmontide)")
s(None, "water_buffalo_minotaur", "Bovidae: Bubalus bubalis")
s(None, "reedbull_minotaur", "Bovidae: Bovinae (Harmontide)")
s(None, "sheep_minotaur", "Bovidae: Ovis aries")
s(None, "skjoldram_minotaur brasshorn_minotaur", "Bovidae: Ovis (Harmontide)")
s(None, "oryx_minotaur", "Bovidae: Oryx (Harmontide)")
s("PERISSODACTYLA", "rhino_minotaur", "Rhinocerotidae")
s("PERISSODACTYLA", "rhiwool_minotaur", "Rhinocerotidae: Coelodonta antiquitatis (extinct, Harmontide)")
s("PAENUNGULATA", "tuskin_minotaur", "Proboscidea: Mammuthus (Harmontide)")
s("WHIPPOMORPHA", "hippo_minotaur", "Hippopotamidae: the whales' closest living relatives")
# ---------------------------------------------------------------- old MOLLUSK
s(None, "kraken_mollusk giant_kraken_mollusk", "Cephalopoda (myth)")
s(None, "octopus_mollusk", "Cephalopoda: Octopoda")
s(None, "ika_mollusk", "Cephalopoda: Decapodiformes (squid)")
s(None, "squidkin_mollusk", "Cephalopoda: Decapodiformes (Harmontide)")
s(None, "cuttlefish_mollusk", "Cephalopoda: Sepiida")
s(None, "nautilus_mollusk", "Cephalopoda: Nautilidae")
s(None, "ammonite_mollusk", "Cephalopoda: Ammonoidea (extinct)")
s(None, "scylla_mollusk charybdis_mollusk", "Cephalopoda per her tentacle skirt (myth)")
s(None, "snail_mollusk", "Gastropoda")
s(None, "spewslug_mollusk", "Gastropoda: slug (Harmontide)")
s(None, "sea_slug_mollusk", "Gastropoda: Nudibranchia")
s(None, "jellyfish_mollusk", "Cnidaria: Scyphozoa (her guide entry already says so)", "guest")
s(None, "anemone_mollusk", "Cnidaria: Actiniaria", "guest")
s(None, "min_min_mollusk", "Cnidaria per this design (cap + tentacles); the folklore Min Min is a light", "call")
# ---------------------------------------------------------------- old ELF / UNDEAD / DEVIL / CONSTRUCT / RIDER
for fam, ids in (("ELF", "night_elf high_elf wood_elf blood_elf desert_elf dark_elf brownie_elf valkyrie_elf domovoi_elf mad_hatter_elf angel_elf gandharva_elf apsara_elf knocker_elf kaguya_elf ophanim_elf norn_elf snow_elf gnome_elf dwarf_elf nymph_elf"),):
    s(None, ids, "outside taxonomy (humanoid folk)")
s(None, "living_doll_undead will_o_wisp_undead yuki_onna_undead phantom_undead vampire_undead zombie_undead frankenstein_undead banshee_undead dullahan_undead lich_undead llorona_undead mummy_undead yurei_undead kasurenijimi_undead draugr_undead ghoul_undead gashadokuro_undead nasnas_undead jiangshi_undead hand_undead koschei_undead mano_peluda_undead gaitress_undead cordyceps_undead lost_soul_undead soul_cluster_undead soul_leech_undead drowned_undead famished_undead skeleton_undead poltergeist_undead sandman_undead candle_spirit_undead", "outside taxonomy (undead)")
s(None, "lesser_devil succubus_devil greater_devil jack_o_lantern_devil jiang_shi_devil oni_devil gremlin_devil karakasa_obake_devil chochin_obake_devil ittan_momen_devil rokurokubi_devil kikimora_devil akaname_devil cyclops_devil genie_devil ogre_devil taotie_devil behemoth_devil fire_giant_devil frost_giant_devil ifrit_devil pazuzu_devil lilith_devil wrath_devil demon_devil fury_devil firesprite_devil goblin_devil troll_devil book_spirit_devil", "outside taxonomy (spirits & demons)")
s(None, "golem_construct gargoyle_construct talosling_construct tin_squirrel_construct tin_jack_construct automaton_construct scarecrow_construct snowgirl_construct", "made, not born")
s(None, "war_rider famine_rider pestilence_rider death_rider charro_negro_rider", "outside taxonomy (riders)")
# ---------------------------------------------------------------- old LIZARDFOLK
s(None, "turtle_lizardfolk", "Testudines: Cheloniidae")
s(None, "conch_turtle_lizardfolk", "Testudines: Cheloniidae (Harmontide)")
s(None, "snapping_turtle_lizardfolk", "Testudines: Chelydridae, Macrochelys temminckii")
s(None, "kappa_lizardfolk", "Testudines: Trionychidae (folklore)")
s(None, "softshell_turtle_lizardfolk", "Testudines: Trionychidae, Apalone spinifera")
s(None, "red_eared_slider_lizardfolk box_turtle_lizardfolk", "Testudines: Emydidae")
s(None, "galapagos_tortoise_lizardfolk", "Testudines: Testudinidae, Chelonoidis niger")
s(None, "black_tortoise_lizardfolk", "Testudines: Testudinidae (myth; the serpent is an ornament)")
s(None, "anvil_tortoise_lizardfolk turtworld_lizardfolk", "Testudines: Testudinidae (Harmontide)")
s(None, "turtfolk_lizardfolk", "Testudines (Harmontide)")
s(None, "matamata_lizardfolk", "Testudines: Chelidae, Chelus fimbriata")
s(None, "archelon_lizardfolk", "Testudines: Protostegidae, Archelon (extinct)")
s("SQUAMATA", "chameleon_lizardfolk", "Squamata: Chamaeleonidae")
s("SQUAMATA", "iguana_lizardfolk", "Squamata: Iguanidae")
s("SQUAMATA", "basilisk_lizardfolk", "Squamata: Corytophanidae, Basiliscus (a real lizard shares her name)")
s("SQUAMATA", "gecko_lizardfolk cave_gekko_lizardfolk", "Squamata: Gekkota")
s("SQUAMATA", "cocoskink_lizardfolk", "Squamata: Scincidae (Harmontide)")
s("SQUAMATA", "komodo_lizardfolk", "Squamata: Varanidae, Varanus komodoensis")
s("SQUAMATA", "chupacabra_lizardfolk", "Squamata (cryptid, spiny reptile)")
s("SQUAMATA", "iron_kobold_lizardfolk", "Squamata (Harmontide reptile kobold)")
s("ARCHOSAURIA", "croc_lizardfolk", "Crocodylia")
s("ARCHOSAURIA", "crocora_lizardfolk", "Crocodylia (Harmontide)")
s("ARCHOSAURIA", "cipactli_lizardfolk", "Crocodylia (myth)")
s("ARCHOSAURIA", "argentinosaurus_lizardfolk", "Dinosauria: Sauropoda (extinct)")
s("AMPHIBIA", "salamander_lizardfolk", "Caudata: Salamandra (legend; salamanders are amphibians, not lizards)")
s("AMPHIBIA", "newt_lizardfolk", "Caudata: Salamandridae")
# ---------------------------------------------------------------- old DRAGON
s(None, "long_dragon wyrm_dragon kannae_dragon wyvern_dragon orochi_dragon bahamut_dragon kun_dragon amphiptere_dragon ender_dragon nenekea_dragon", "outside taxonomy (dragon)")
s(None, "gryphon_dragon chimera_dragon cockatrice_dragon alebrije_dragon", "outside taxonomy (chimera of several animals)")
s(None, "quetzalcoatl_dragon macawtl_dragon", "outside taxonomy (feathered serpent: bird + snake)")
s("SQUAMATA", "jormungandr_dragon", "Serpentes (myth; this design is a python)")
s("ARCHOSAURIA", "moo_dragon", "Dinosauria: Ceratopsidae, Triceratops (extinct, Harmontide)")
s("ARCHOSAURIA", "hairypterix_dragon", "Pterosauria (extinct, Harmontide)")
# ---------------------------------------------------------------- old BUG
s(None, "mosquito_bug", "Insecta: Diptera, Culicidae")
s(None, "moth_bug moth_larva_bug", "Insecta: Lepidoptera (moth)")
s(None, "mothman_bug", "Insecta: Lepidoptera (cryptid)")
s(None, "papillon_bug papillon_larva_bug", "Insecta: Lepidoptera, Papilionoidea")
s(None, "bee_bug queen_bee_bug", "Insecta: Hymenoptera, Apidae")
s(None, "hornet_bug", "Insecta: Hymenoptera, Vespidae")
s(None, "ant_bug queen_ant_bug", "Insecta: Hymenoptera, Formicidae")
s(None, "spider_wasp_bug", "Insecta: Hymenoptera, Pompilidae")
s(None, "firefly_bug", "Insecta: Coleoptera, Lampyridae")
s(None, "ladybug_bug", "Insecta: Coleoptera, Coccinellidae")
s(None, "scarabronze_bug", "Insecta: Coleoptera, Scarabaeidae (Harmontide)")
s(None, "date_beetle_bug", "Insecta: Coleoptera (Harmontide)")
s(None, "mantis_bug", "Insecta: Mantodea")
s(None, "temple_mantis_bug", "Insecta: Mantodea (Harmontide)")
s(None, "dragonfly_bug", "Insecta: Odonata")
s(None, "cicada_bug", "Insecta: Hemiptera, Cicadidae")
s(None, "antlion_bug", "Insecta: Neuroptera, Myrmeleontidae")
s(None, "tardigrade_bug", "Tardigrada: a cousin of the arthropods (Panarthropoda)", "guest")
s("CHELICERATA", "scorpion_bug", "Arachnida: Scorpiones")
s("CHELICERATA", "girtablilu_bug", "Arachnida: Scorpiones (myth)")
s("FAE", "fairy_bug fairy_queen_bug pixi_bug rlff_bug", "outside taxonomy (fairy: insect wings are an ornament)")
# ---------------------------------------------------------------- old FLORA
s(None, "alraune_flora", "Plantae: flowering plant (folklore)")
s(None, "ent_flora dryad_flora", "Plantae: tree (myth)")
s(None, "cactus_flora", "Plantae: Cactaceae")
s(None, "mandragora_flora", "Plantae: Solanaceae, Mandragora")
s(None, "venus_flytrap_flora", "Plantae: Droseraceae, Dionaea muscipula")
s(None, "kesaran_pasaran_flora", "Plantae: Asteraceae (dandelion seed per this design)")
s(None, "rose_flora", "Plantae: Rosaceae, Rosa")
s(None, "lotus_flora", "Plantae: Nelumbonaceae, Nelumbo")
s(None, "sunflower_flora", "Plantae: Asteraceae, Helianthus")
s(None, "rafflesia_flora", "Plantae: Rafflesiaceae")
s(None, "matango_flora", "Fungi: Agaricales (fungi are closer to animals than to plants)")
s(None, "shiitake_flora", "Fungi: Lentinula edodes")
s("FAE", "leshy_flora", "outside taxonomy (forest spirit; moss beard is an ornament)", "call")
# ---------------------------------------------------------------- old CANID
s(None, "wolf_canid amarok_canid", "Canidae: Canis lupus")
s(None, "tarnhelm_canid", "Canidae: Canis lupus (Harmontide)")
s(None, "anubis_canid", "Canidae: Canis lupaster (deity)")
s(None, "kobold_canid", "Canidae: Canis familiaris per this design (folklore mine spirit)")
s(None, "meloncollie_canid obsidian_hound_canid", "Canidae: Canis familiaris (Harmontide)")
s(None, "bloodhound_canid", "Canidae: Canis familiaris")
s(None, "cu_sith_canid hellhound_canid cerberus_canid", "Canidae: Canis (myth hound)")
s("ARCTOIDEA", "polar_bear_canid", "Ursidae: Ursus maritimus")
s("ARCTOIDEA", "grizzly_canid", "Ursidae: Ursus arctos horribilis")
s("ARCTOIDEA", "black_bear_canid", "Ursidae: Ursus americanus")
s("ARCTOIDEA", "panda_canid", "Ursidae: Ailuropoda melanoleuca")
s("ARCTOIDEA", "rheintalensis_canid", "Ursidae: Ursus spelaeus (extinct cave bear, Harmontide)")
s("ARCTOIDEA", "wolverine_canid", "Mustelidae: Gulo gulo")
s("FELIFORMIA", "hyena_canid", "Hyaenidae: Crocuta crocuta (hyenas are feliforms, not dogs)")
s("PRIMATES", "yeti_canid", "Hominidae (cryptid ape)")
s("PRIMATES", "gigantopithecus_canid", "Hominidae: Gigantopithecus blacki (extinct)")
# ---------------------------------------------------------------- old RODENT
s(None, "capybara_rodent", "Rodentia: Caviidae, Hydrochoerus")
s(None, "ratatoskr_rodent", "Rodentia: Sciuridae, Sciurus vulgaris (myth)")
s(None, "marmotaur_rodent", "Rodentia: Sciuridae, Marmota (Harmontide)")
s(None, "squirricerat_rodent pineskrell_rodent", "Rodentia: Sciuridae (Harmontide)")
s(None, "house_mouse_rodent", "Rodentia: Muridae, Mus musculus")
s(None, "beaver_rodent", "Rodentia: Castoridae")
s("CHIROPTERA", "needlehog_rodent", "Eulipotyphla: Erinaceidae (Harmontide hedgehog); closer to bats than to rodents", "guest")
s("CHIROPTERA", "mole_rodent", "Eulipotyphla: Talpidae; closer to bats than to rodents", "guest")
s("ARCTOIDEA", "coatimano_rodent", "Procyonidae: Nasua (Harmontide)")
s("ARCTOIDEA", "silkcoon_rodent", "Procyonidae: Procyon (Harmontide)")
s("ARCTOIDEA", "raccoon_rodent", "Procyonidae: Procyon lotor")
s("ARCTOIDEA", "red_panda_rodent", "Ailuridae: Ailurus fulgens")
s("ARCTOIDEA", "skunk_rodent", "Mephitidae")
s("ARCTOIDEA", "bambadger_rodent", "Mustelidae: badger (Harmontide)")
s("ARCTOIDEA", "kamaitachi_rodent", "Mustelidae: Mustela (folklore weasel)")
s("ARCTOIDEA", "sobolnik_rodent", "Mustelidae: Martes zibellina (Harmontide sable)")
s("CANIDAE", "tanuki_rodent", "Canidae: Nyctereutes procyonoides (a dog, not a raccoon)")
s("FELIFORMIA", "mongoose_rodent", "Herpestidae")
s("FELIFORMIA", "meerkat_rodent", "Herpestidae: Suricata suricatta")
s("PAENUNGULATA", "rockhyrax_rodent", "Hyracoidea: Procaviidae (Harmontide); elephants' cousin")
s("PRIMATES", "gibbon_rodent", "Hylobatidae (Harmontide)")
s("PRIMATES", "sasquatch_rodent", "Hominidae (cryptid)")
s("PRIMATES", "sun_wukong_rodent", "Cercopithecidae: Macaca (myth)")
s("PRIMATES", "lemur_rodent", "Lemuridae: Lemur catta")
# ---------------------------------------------------------------- old CRUSTACEAN / MARSUPIAL / ARMOURED / BAT / THEROPOD
s(None, "crab_crustacean", "Decapoda: Brachyura")
s(None, "floodcrab_crustacean", "Decapoda: Brachyura (Harmontide)")
s(None, "spider_crab_crustacean", "Decapoda: Majoidea")
s(None, "hermit_crab_crustacean", "Decapoda: Paguroidea")
s(None, "lobster_crustacean", "Decapoda: Nephropidae")
s(None, "mantis_shrimp_crustacean", "Stomatopoda")
s("ACTINOPTERYGII", "starfish_crustacean", "Echinodermata: Asteroidea; starfish are closer to fish (and us) than to crabs", "guest")
s(None, "kangaroo_marsupial", "Diprotodontia: Macropodidae")
s(None, "koala_marsupial", "Diprotodontia: Phascolarctidae")
s(None, "gliderune_marsupial", "Diprotodontia: Petaurus breviceps (Harmontide)")
s(None, "opossum_marsupial", "Didelphimorphia: Didelphidae")
s(None, "armadillo_armoured", "Cingulata: Dasypodidae")
s(None, "riverdillo_armoured", "Cingulata (Harmontide)")
s(None, "slothren_armoured", "Pilosa: Folivora (Harmontide sloth)")
s(None, "anteater_armoured", "Pilosa: Vermilingua (Harmontide)")
s("FELIFORMIA", "pangolin_armoured", "Pholidota: Manidae; pangolins are the closest living relatives of the carnivores", "guest")
s("SUINA", "aegis_boar_armoured gribboar_armoured", "Suidae: Sus scrofa (Harmontide)")
s("SUINA", "piglette_armoured", "Suidae-like (Minecraft piglin)")
s("SUINA", "daeodon_armoured", "Entelodontidae (extinct 'hell pig'; some trees put it nearer hippos)", "call")
s(None, "cave_bat", "Chiroptera")
s(None, "date_bat", "Chiroptera: Pteropodidae (Harmontide fruit bat)")
s(None, "frond_bat", "Chiroptera (Harmontide)")
s(None, "camazotz_bat", "Chiroptera: Phyllostomidae (myth; leaf-nosed bat)")
s(None, "vetala_bat", "Chiroptera per this design (Indian spirit)", "call")
s(None, "fernstalker_theropod", "Dinosauria: Theropoda, Dromaeosauridae (Harmontide)")
s(None, "spinosaur_theropod", "Dinosauria: Theropoda, Spinosauridae (extinct)")
s(None, "australovenator_theropod", "Dinosauria: Theropoda, Megaraptora (extinct)")

# ---------------------------------------------------------------- harpies & anurans (own entity bases)
H = {
 "sparrow_harpy": "Passeriformes: Passeridae", "finch_harpy": "Passeriformes: Fringillidae",
 "riflebird_harpy": "Passeriformes: Paradisaeidae",
 "crow_harpy": "Passeriformes: Corvidae", "coin_magpie_harpy": "Passeriformes: Corvidae, Pica (Harmontide)",
 "trickster_raven_harpy": "Passeriformes: Corvidae, Corvus corax (Harmontide)", "rookmourn_harpy": "Passeriformes: Corvidae, Corvus frugilegus (Harmontide)",
 "skycrow_harpy": "Passeriformes: Corvidae (Harmontide)", "tengu_harpy": "Passeriformes: Corvidae (folklore crow tengu)",
 "yatagarasu_harpy": "Passeriformes: Corvidae (myth)", "ironbeak_harpy": "Neoaves, courier bird (Harmontide; order unclear)",
 "alkonost_harpy": "Neoaves (myth paradise bird)",
 "owl_harpy": "Strigiformes", "argentowlis_harpy": "Strigiformes (Harmontide)", "stigowl_harpy": "Strigiformes (Harmontide)",
 "lechuza_harpy": "Strigiformes: Tyto alba (folklore)",
 "seagull_harpy": "Charadriiformes: Laridae", "pumpkingull_harpy": "Charadriiformes: Laridae (Harmontide)",
 "albatross_harpy": "Procellariiformes: Diomedeidae", "penguin_harpy": "Sphenisciformes",
 "pelican_harpy": "Pelecaniformes: Pelecanidae", "bennu_harpy": "Pelecaniformes: Ardeidae (myth heron)",
 "ibiset_harpy": "Pelecaniformes: Threskiornithidae (Harmontide)", "ibisking_harpy": "Pelecaniformes: Threskiornithidae (Harmontide)",
 "flamingo_harpy": "Phoenicopteriformes", "mirror_crane_harpy": "Gruiformes: Gruidae (Harmontide)",
 "swan_harpy": "Anseriformes: Anatidae", "duck_harpy": "Anseriformes: Anatidae",
 "poultry_harpy": "Galliformes: Gallus gallus", "peaflare_harpy": "Galliformes: Pavo (Harmontide)",
 "phoenix_harpy": "Neoaves (myth)", "zhuque_harpy": "Neoaves (myth vermilion bird)",
 "reliquary_dove_harpy": "Columbiformes (Harmontide)", "dodobold_harpy": "Columbiformes: Raphus cucullatus (extinct; the dodo was a pigeon)",
 "macaw_harpy": "Psittaciformes", "toucan_harpy": "Piciformes: Ramphastidae", "motmora_harpy": "Coraciiformes: Momotidae (Harmontide)",
 "hummingbird_harpy": "Apodiformes: Trochilidae",
 "condorune_harpy": "Accipitriformes: Cathartidae (Harmontide)", "garuda_harpy": "Accipitriformes (myth eagle-kite)",
 "roc_harpy": "Accipitriformes (myth giant raptor)", "ziz_harpy": "Neoaves (myth giant bird)",
 "ostrich_harpy": "Palaeognathae: Struthionidae", "emu_harpy": "Palaeognathae: Dromaius",
 "emberu_harpy": "Palaeognathae: Dromaius (Harmontide)", "cassowrath_harpy": "Palaeognathae: Casuarius (Harmontide)",
 "landharpy_harpy": "Aves, flightless (Harmontide; order unclear)",
}
A = {
 "bullywug": "Anura (folklore frog-folk)", "bullfrog": "Anura: Ranidae, Lithobates catesbeianus", "tree_frog": "Anura: Hylidae",
 "poison_dart_frog": "Anura: Dendrobatidae", "glass_frog": "Anura: Centrolenidae", "goliath_frog": "Anura: Conraua goliath",
 "desert_rain_frog": "Anura: Breviceps macrops", "mucus_toad": "Anura: Bufonidae (MGE)", "fire_bellied_toad": "Anura: Bombinatoridae",
 "cane_toad": "Anura: Bufonidae, Rhinella marina", "natterjack": "Anura: Bufonidae, Epidalea calamita",
 "surinam_toad": "Anura: Pipidae, Pipa pipa", "vodyanoy": "Anura (Slavic river spirit)", "mailtoad": "Anura: Bufonidae (Harmontide)",
}



# short name of each clade, for "Feliform Milk" and "Firm Feliform Cheese"; milk item prefix (None: gives no milk) and colour
SHORT = collections.OrderedDict([
    ("FELIFORMIA", ("Feliform", "feliform", "#f6ecd8")), ("CANIDAE", ("Canid", "canid", "#ece4d8")),
    ("ARCTOIDEA", ("Arctoid", "arctoid", "#eef0f4")), ("RODENTIA", ("Rodent", "rodent", "#f0e6d4")),
    ("LAGOMORPHA", ("Lagomorph", "lagomorph", "#f8ecec")), ("PRIMATES", ("Primate", "primate", "#f2e8dc")),
    ("CHIROPTERA", ("Bat", "bat", "#dcd4e4")), ("MARSUPIALIA", ("Marsupial", "marsupial", "#f0e4cc")),
    ("XENARTHRA", ("Xenarthran", "xenarthran", "#e0dccc")), ("PAENUNGULATA", ("Paenungulate", "paenungulate", "#e6e8ea")),
    ("PERISSODACTYLA", ("Perissodactyl", "perissodactyl", "#f6efe0")), ("BOVIDAE", ("Bovid", "bovid", "#fbf8f0")),
    ("CERVIDAE", ("Cervid", "cervid", "#f4ead8")), ("CAMELIDAE", ("Camelid", "camelid", "#f6ead0")),
    ("SUINA", ("Suine", "suine", "#f8e4e0")), ("WHIPPOMORPHA", ("Whippomorph", "whippomorph", "#dce8f4")),
    ("AVES", ("Bird", "bird", "#f4efe2")), ("ARCHOSAURIA", ("Archosaur", "archosaur", "#e8ecd0")),
    ("TESTUDINES", ("Testudine", "testudine", "#ecf0c8")), ("SQUAMATA", ("Squamate", "squamate", "#e4efd0")),
    ("AMPHIBIA", ("Amphibian", "amphibian", "#dcecc8")), ("ACTINOPTERYGII", ("Fish", "fish", "#d4ecf2")),
    ("CHELICERATA", ("Chelicerate", "chelicerate", "#e8e0ee")), ("HEXAPODA", ("Insect", "insect", "#f4eab0")),
    ("MALACOSTRACA", ("Malacostracan", "malacostracan", "#f4d8cc")), ("MOLLUSCA", ("Mollusk", "mollusk", "#d8dcf0")),
    ("FLORA", ("Flora", None, None)), ("FAE", ("Fae", "fae", "#eef6e4")), ("DRAGON", ("Dragon", "dragon", "#f8d8b8")),
    ("UNDEAD", ("Undead", "undead", "#d8dcdc")), ("DEVIL", ("Devil", "devil", "#f4d0d4")), ("SLIME", ("Slime", "slime", "#c8f0d0")),
    ("CONSTRUCT", ("Construct", None, None)), ("RIDER", ("Rider", "rider", "#e8e0d0")),
])
assert list(SHORT) == list(FAM)

# the families before the taxonomy (2026-11-05): their lang keys go
OLD_FAMILIES = ["harpy", "anuran", "nekomata", "kitsune", "lamia", "arachne", "centaur", "merfolk", "slime", "jackalope", "minotaur",
                "mollusk", "elf", "undead", "lizardfolk", "dragon", "devil", "bug", "flora", "canid", "rodent", "crustacean", "marsupial",
                "armoured", "construct", "bat", "theropod", "rider"]


def milks():
    """prefix -> (name, colour) for every clade with milk, in FamilyMilk order."""
    return collections.OrderedDict((pre, (name, col)) for k, (name, pre, col) in SHORT.items() if pre)


def lang_entries():
    out = collections.OrderedDict()
    for k, (name, pre, col) in SHORT.items():
        out["family.monstergirls." + k.lower()] = name
        out["monstergirls.configuration." + k.lower()] = FAM[k][0] + " (%)"
        out["clade.monstergirls." + k.lower()] = FAM[k][0]          # the journal's page line and tree of life
    for pre, (name, col) in milks().items():
        out["item.monstergirls." + pre + "_milk"] = name + " Milk"
    return out


def write_lang():
    """Drops the old family keys and adds the clade keys, editing en_us.json line by line (keeps its order and anyone else's edits)."""
    import json
    path = os.path.join(ROOT, "src/main/resources/assets/monstergirls/lang/en_us.json")
    text = open(path, encoding="utf-8").read()
    want = lang_entries()
    stale = set()
    for f in OLD_FAMILIES:
        stale |= {"family.monstergirls." + f, "monstergirls.configuration." + f, "item.monstergirls." + f + "_milk"}
    stale -= set(want)
    lines = [l for l in text.rstrip().split("\n") if not any(l.strip().startswith(json.dumps(k) + ":") for k in stale)]
    have = json.loads("\n".join(lines).rstrip().rstrip("}").rstrip().rstrip(",") + "\n}")
    for i, l in enumerate(lines):                       # change values that differ
        for k, v in want.items():
            if l.strip().startswith(json.dumps(k) + ":") and have.get(k) != v:
                lines[i] = "  %s: %s%s" % (json.dumps(k), json.dumps(v, ensure_ascii=False), "," if l.rstrip().endswith(",") else "")
    new = ["  %s: %s" % (json.dumps(k), json.dumps(v, ensure_ascii=False)) for k, v in want.items() if k not in have]
    assert lines[-1].strip() == "}"
    body = lines[:-1]
    if body and body[-1].rstrip().endswith(","):
        body[-1] = body[-1].rstrip()[:-1]
    if new:
        body[-1] = body[-1] + ","
        body += [n + "," for n in new[:-1]] + [new[-1]]
    out = "\n".join(body + ["}"]) + "\n"
    json.loads(out)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(out)
    return len(stale & set(json.loads(text))), len(new)

# ---------------------------------------------------------------- lookups
def species_rows():
    """(id, old family) for every mob in the game: the GirlSpecies table, then the harpies and anurans."""
    src = open(os.path.join(ROOT, "src/main/java/com/linkyoshi90/monstergirls/entity/girl/GirlSpecies.java"), encoding="utf-8").read()
    rows = re.findall(r'^\s+[A-Z_]+\("([a-z_]+)", Family\.([A-Z_]+)', src, re.M)
    return rows + [(i, "HARPY") for i in H] + [(i, "ANURAN") for i in A]


def place(sp, old):
    """(clade, true position, flag) of one species."""
    if sp in H:
        return "AVES", H[sp], "call" if "unclear" in H[sp] else ""
    if sp in A:
        return "AMPHIBIA", A[sp], ""
    fam, pos, flag = P[sp]
    return fam or D[old], pos, flag


def check(rows):
    ids = dict(rows)
    missing = [i for i, f in rows if f not in ("HARPY", "ANURAN") and i not in P]
    extra = [i for i in P if i not in ids]
    assert not missing, "species without a clade: %s" % missing
    assert not extra, "clades for species not in the game: %s" % extra


JAVA = '''package com.linkyoshi90.monstergirls.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Where each mob really sits in the tree of life (TAXONOMY_PLAN.md). GENERATED by tools/taxonomy.py; do not edit by hand.
 * Her body, rig and behaviour follow GirlSpecies.Family (her form); her clade decides her milk, the family name on dairy,
 * the spawn-rate setting and the field guide she belongs in. The last seven are mythic: they sit outside the tree.
 */
public enum Clade {
%(consts)s;

    private final String path;
    private final boolean mythic;

    Clade(String path, boolean mythic) {
        this.path = path;
        this.mythic = mythic;
    }

    /** The clade's place in the tree, e.g. "Mammalia > Carnivora > Feliformia". */
    public String path() {
        return this.path;
    }

    /** Outside the tree of life: fae, dragons, undead, devils, slimes, constructs, riders. */
    public boolean mythic() {
        return this.mythic;
    }

    /** "feliformia": the lang key family.monstergirls.&lt;key&gt; and the config key. */
    public String key() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /** Her clade, by entity id path ("hyena_canid", "sparrow_harpy"), or null for anything that is not one of ours. */
    @Nullable
    public static Clade of(String id) {
        return switch (id) {
%(cases)s
            default -> null;
        };
    }

    @Nullable
    public static Clade of(Entity entity) {
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return "monstergirls".equals(key.getNamespace()) ? of(key.getPath()) : null;
    }

    /** A guest: her own clade has too few of us, so she lives with her nearest relative (the guide says so). */
    public static boolean guest(String id) {
        return switch (id) {
            case %(guests)s -> true;
            default -> false;
        };
    }
}
'''


def write_java(rows):
    by = collections.OrderedDict((k, []) for k in FAM)
    guests = []
    for sp, old in rows:
        clade, pos, flag = place(sp, old)
        by[clade].append(sp)
        if flag == "guest":
            guests.append(sp)
    mythic = {"FAE", "DRAGON", "UNDEAD", "DEVIL", "SLIME", "CONSTRUCT", "RIDER"}
    consts = ",\n".join('    %s("%s", %s)' % (k, FAM[k][1].replace('"', ''), "true" if k in mythic else "false") for k in FAM)
    cases = []
    for k, ids in by.items():
        if not ids:
            continue
        chunk, line = [], "            case "
        for i, sp in enumerate(ids):
            piece = '"%s"' % sp + (", " if i < len(ids) - 1 else "")
            if len(line) + len(piece) > 150:
                chunk.append(line.rstrip())
                line = "                 "
            line += piece
        chunk.append(line + " -> " + k + ";")
        cases.append("\n".join(chunk))
    java = JAVA % dict(consts=consts, cases="\n".join(cases), guests=", ".join('"%s"' % g for g in guests))
    path = os.path.join(ROOT, "src/main/java/com/linkyoshi90/monstergirls/entity/Clade.java")
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(java)
    return by


def write_mobs_by_date(rows):
    """Labels each line of MOBS_BY_DATE.md with her clade ("**Wolverine Canid** · *Arctoid*"; the id suffix is her form). Can be run twice."""
    path = os.path.join(ROOT, "MOBS_BY_DATE.md")
    if not os.path.exists(path):
        return 0
    old = dict(rows)
    out, n = [], 0
    for line in open(path, encoding="utf-8").read().split(chr(10)):
        m = re.match(r"^(\d+\. \*\*([^*]+)\*\*)(?: · \*[^*]+\*)?(.*)$", line)
        if m:
            sp = m.group(2).lower().replace(" ", "_").replace("-", "_").replace("'", "")
            if sp in old:
                line = "%s · *%s*%s" % (m.group(1), SHORT[place(sp, old[sp])[0]][0], m.group(3))
                n += 1
        out.append(line)
    text = chr(10).join(out)
    if "*Feliform*" in text and "clade (her real kin" not in text:
        text = text.replace("(git history).", "(git history). Each name is followed by her clade (her real kin, TAXONOMY_PLAN.md); the family word in her name is her form.", 1)
    with open(path, "w", encoding="utf-8", newline=chr(10)) as fh:
        fh.write(text)
    return n


def main():
    rows = species_rows()
    check(rows)
    by = write_java(rows)
    gone, added = write_lang()
    print("lang: %d old keys removed, %d added" % (gone, added))
    print("MOBS_BY_DATE.md: %d lines labelled" % write_mobs_by_date(rows))
    moved = sum(1 for sp, old in rows if place(sp, old)[0] != D[old])
    print("%d species in %d clades, %d moved" % (len(rows), len(FAM), moved))
    for k, ids in by.items():
        print("  %-15s %3d" % (k, len(ids)))


if __name__ == "__main__":
    main()
