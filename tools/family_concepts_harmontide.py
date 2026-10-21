"""Harmontide species added to existing families (HARMONTIDE_CONCEPTS.md, first batch 2026-10-21).

Imported at the end of family_concepts.py: each entry is appended to its family's species list, and its column is
appended to every row of that family's comparison table (`split`) so the field guides stay aligned.
"""
import family_concepts as C

ADD = {
    "nekomata": [(
        dict(id="ocelot_nekomata", name="Ocelot", group="Jungle & bamboo", tag="Nocturnal · climber", height=1.65,
             biomes=["jungle", "sparse_jungle", "bamboo_jungle"], temperament="Secretive, nocturnal, fiercely private",
             ecology="Leopardus pardalis (Felidae). A small spotted cat of the jungle floor who hunts at dusk and dawn and climbs well. She is cousin to the "
                     "vanilla ocelots and just as shy.",
             interaction="Come at dusk, sneak, and bring fish. Never chase her into the trees.",
             signature=("Tree climber", "She climbs straight up tree trunks and walls like the gray fox and spends the day high in the canopy. Her presents are jungle "
                        "finds: cocoa beans, feathers, the odd melon slice."),
             danger="Low.",
             variants=[("jungle", "Jungle (default)", "Gold coat with chain-like rosettes"), ("grey", "Grey", "Grey-buff coat (a real colour morph)"),
                       ("pale", "Pale", "Cream coat with faded rosettes"), ("melanistic", "Melanistic", "Black on black"),
                       ("golden", "Golden", "Rich orange-gold")]),
        {"Habitat": "Jungle, bamboo", "Social": "Solitary", "Water": "Swims if she must", "Signature": "Climbs trees, jungle presents", "Danger": "Low"})],
    "centaur": [(
        dict(id="tapir_centaur", name="Tapir", group="Jungle & swamp", tag="Gentle · dream-eater", height=2.1,
             biomes=["jungle", "sparse_jungle", "bamboo_jungle", "swamp", "mangrove_swamp"], temperament="Gentle, sleepy, quietly protective",
             ecology="Tapirus (Perissodactyla: Tapiridae), the same order as horses, so she belongs with the centaurs. A shy browser of the jungle and swamp "
                     "who eats fruit and leaves at night. In Japan the tapir became the baku, the eater of nightmares.",
             interaction="Bring melons and apples. Brush her.",
             signature=("Baku", "Phantoms never spawn for a player sleeping within 32 blocks of a tapir, and the phantom girl stays away too. She eats the "
                        "bad dreams."),
             danger="Low.",
             variants=[("malayan", "Malayan (default)", "Black with a white saddle"), ("lowland", "Lowland", "Even dark brown"),
                       ("mountain", "Mountain", "Woolly black-brown"), ("bairds", "Baird's", "Grey-brown"),
                       ("calf", "Calf coat", "Brown with pale stripes and spots, like a baby tapir")]),
        {"Habitat": "Jungle, swamp", "Ridden": "Yes: slow and steady", "Signature": "Baku: no phantoms", "Temperament": "Gentle, sleepy", "Danger": "Low"})],
    "lizardfolk": [(
        dict(id="snapping_turtle_lizardfolk", name="Alligator Snapping Turtle", group="Swamps & mangroves", tag="Patient · ancient", height=1.8,
             biomes=["swamp", "mangrove_swamp"], temperament="Patient, grumpy, very old",
             ecology="Macrochelys temminckii (Testudines: Chelydridae), the largest freshwater turtle in North America. She lies still on the riverbed "
                     "with her mouth open and wiggles a pink, worm-like lure on her tongue until a fish swims in.",
             interaction="Leave her be in the water and she will feed you. Bring her fish.",
             signature=("Tongue lure", "While she sits still in water she catches a fish now and then and drops it at her feet (cod or salmon, at most "
                        "three lying around at a time). Hit her and she hides in her shell. Her bite is the hardest in the family after the crocodile's."),
             danger="Moderate if provoked (a hard bite).",
             variants=[("common", "Common (default)", "Dark brown shell, olive skin"), ("mossy", "Mossy", "Algae on her shell and hair"),
                       ("dark", "Dark", "Almost black"), ("suwannee", "Suwannee", "The Suwannee River species, paler and greyer"),
                       ("leucistic", "Leucistic", "Pale cream, very rare")]),
        {"Habitat": "Swamps, mangroves", "Signature": "Tongue lure, shell", "Danger": "Moderate if provoked"})],
    "undead": [(
        dict(id="banshee_undead", name="Banshee", group="Birch forests, at night", tag="Mournful · watchful", height=1.75,
             biomes=["birch_forest", "old_growth_birch_forest", "meadow"], temperament="Mournful, gentle, protective",
             ecology="The bean sí, the woman of the fairy mound in Irish folklore. Her keening warns of a coming death. Here it is a warning you can act on, "
                     "not a curse. She combs her long hair with a silver comb.",
             interaction="Pumpkin pie, and listen when she cries.",
             signature=("Keening", "When a player within 24 blocks drops to three hearts or fewer, she keens. Every hostile mob within 16 blocks of that player "
                        "glows for ten seconds, as a bell shows raiders. She keens at most once a minute for each player."),
             danger="None.",
             variants=[("keening", "Keening (default)", "Grey cloak, silver hair"), ("green", "Green", "Green dress, auburn hair"),
                       ("red", "Red", "Red-haired, dark cloak"), ("mist", "Mist", "All pale, like fog"), ("raven", "Raven", "Black hair and cloak")]),
        {"Haunts": "Birch forests, at night", "Signature": "Keening reveals danger", "Danger": "None"})],
}

for fid, entries in ADD.items():
    fam = C.family(fid)
    for species, cols in entries:
        if any(s["id"] == species["id"] for s in fam["species"]):
            continue
        fam["species"].append(species)
        for label, values in fam.get("split", []):
            values.append(cols.get(label, "-"))
