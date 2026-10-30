#!/usr/bin/env python3
"""Constructs (the golem family, see construct_field_guide.html): golem.

    python tools/gen_construct.py [species ...] [--previews]

girl_family bipeds shaped from clay: clay skin and hair, a word-tablet on her brow (`shem`) and finger marks pressed
into the clay. Extra animations: "death" (she breaks apart: head, arms and legs fly off and fall, the body crumbles;
the renderer does not tip her over) and "slam" (a two-handed blow).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    # twenty-third wave: the automaton, a pale-blue clockwork girl in armour plates: a round helmet with a disc over each ear,
    # rivets down every plate
    "automaton_construct": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["pauldron", "bracers", "belt"], colors=dict(cloth2="#5a8ad0", leather="#3a5a9a")),
        hair="bob", bangs="swept", ears=None, legs="bare", foot="shoe", tin=True, helm=True,
        pal=dict(SKIN=skin("fair"), HAIR="#7aa8e0", FUR="#8ab4ec", LIGHT="#dceafc", MARK="#3a5a9a", EYE="#2a6ad8", CLOTH="#6a9ae0", TRIM="#dceafc",
                 pattern="solid", extra=dict(boot="#5a8ad0", helm="#8ab4ec", helm_disc="#dceafc", key="#c8d8f0")),
        variants={"silver": dict(HAIR="#a8acb4", FUR="#c8ccd4", LIGHT="#f0f2f6", MARK="#5a5e66", EYE="#3a8ae8", CLOTH="#a8acb4",
                                 extra=dict(boot="#7a7e86", helm="#c8ccd4", helm_disc="#f0f2f6", key="#d8dce4")),
                  "red": dict(HAIR="#d8584a", FUR="#e0705a", LIGHT="#f8d0c0", MARK="#7a2a1e", EYE="#2a8ad8", CLOTH="#c8483a",
                              extra=dict(boot="#8a2e22", helm="#e0705a", helm_disc="#f8e080", key="#f8e080")),
                  "green": dict(HAIR="#5aa86a", FUR="#7ac08a", LIGHT="#d8f0dc", MARK="#2a5a32", EYE="#e8a030", CLOTH="#5aa86a",
                                extra=dict(boot="#3a7a44", helm="#7ac08a", helm_disc="#d8f0dc", key="#d8f0dc")),
                  "gold": dict(HAIR="#d8a840", FUR="#e8c060", LIGHT="#f8ecc0", MARK="#8a6418", EYE="#2a6ad8", CLOTH="#c89030",
                               extra=dict(boot="#8a6418", helm="#e8c060", helm_disc="#f8ecc0", key="#f8ecc0"))}),
    # nineteenth wave: Tin Jack, a scarecrow of riveted blue tin in a tattered cloak and a pointed hat, a lantern at her hip
    "tin_jack_construct": dict(
        build="slim", outfit=dict(top=["corset", "capelet"], bottom="wrap_skirt", extras=["belt", "bracers"],
                                  colors=dict(cloth2="#2a3a6a", leather="#5a4a3a")),
        hair="shaggy", bangs="messy", ears=None, legs="bare", foot="shoe", tin=True, witch_hat=True, glow=["lantern"],
        pal=dict(SKIN=skin("#8aa0c8"), HAIR="#2a3a6a", FUR="#5a7ac0", LIGHT="#c8d8f0", MARK="#1e2a5a", EYE="#5ae8f8", CLOTH="#3a4a8a", TRIM="#d8b060",
                 pattern="solid", extra=dict(boot="#2a3a6a", key="#d8b060", hat="#25305a", hat_band="#d8b060", lantern="#7af0f8")),
        variants={"silver": dict(SKIN=skin("#c8ccd4"), HAIR="#5a5e66", FUR="#a8acb4", LIGHT="#eceef2", MARK="#4a4e56", EYE="#f8c040", CLOTH="#4a4e5a",
                                 extra=dict(boot="#3a3e46", key="#d8b060", hat="#2a2c32", hat_band="#a8323a", lantern="#f8d070")),
                  "copper": dict(SKIN=skin("#d89a6a"), HAIR="#6a2a10", FUR="#c8703a", LIGHT="#f0b890", MARK="#5a2410", EYE="#7af0b0", CLOTH="#2a5a4a",
                                 extra=dict(boot="#4a2410", key="#e8b040", hat="#2a5a3a", hat_band="#e8b040", lantern="#9af0b0")),
                  "green": dict(SKIN=skin("#8ab890"), HAIR="#2a4a2e", FUR="#5a9a62", LIGHT="#c8ecc8", MARK="#1e3a22", EYE="#f8e060", CLOTH="#3a5a3a",
                                extra=dict(boot="#1e3a22", key="#d8b060", hat="#1e2a1e", hat_band="#c8402a", lantern="#f8e070")),
                  "purple": dict(SKIN=skin("#a898c8"), HAIR="#3a2a5a", FUR="#7a62b0", LIGHT="#e0d8f0", MARK="#2a1e4a", EYE="#f8a040", CLOTH="#4a3a7a",
                                 extra=dict(boot="#2a1e4a", key="#d8b060", hat="#e8742a", hat_band="#2a1e4a", lantern="#f8a840"))}),
    "golem_construct": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps"], colors=dict(bandage="#d8cfc0", cloth2="#7a6a58")),
        hair="hime", bangs="parted", ears=None, legs="bare", foot="shoe",
        pal=dict(SKIN=skin("#a89c90"), HAIR="#7a7068", FUR="#a89c90", LIGHT="#c8beb2", MARK="#6a6058", EYE="#e8c868", CLOTH="#d8cfc0", TRIM="#8a6a3a",
                 pattern="solid", extra=dict(boot="#8a8078", shem="#e8c868")),
        variants={"terracotta": dict(SKIN=skin("#c8784a"), HAIR="#8a4a2a", FUR="#c8784a", LIGHT="#e0a070", MARK="#7a3e1e",
                                     extra=dict(boot="#9a5a32", shem="#f8e0a0")),
                  "red_clay": dict(SKIN=skin("#a8503a"), HAIR="#6a2a1e", FUR="#a8503a", MARK="#5a2014", extra=dict(boot="#7a3a28", shem="#e8c868")),
                  "kaolin": dict(SKIN=skin("#ece6dc"), HAIR="#d8d0c4", FUR="#ece6dc", LIGHT="#ffffff", MARK="#b8b0a4", EYE="#5a8ac8",
                                 extra=dict(boot="#c8c0b4", shem="#5a8ac8")),
                  "mud": dict(SKIN=skin("#6a5240"), HAIR="#3e2e22", FUR="#6a5240", MARK="#2e2218", EYE="#a8d878", extra=dict(boot="#4a3a2c", shem="#a8d878"))}),
    # eleventh wave: the talosling, a bronze clockwork hound girl with a wind-up key in her back
    "talosling_construct": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["bracers", "belt"], colors=dict(cloth2="#2a3a6a", leather="#2a3a6a")),
        hair="ponytail", bangs="blunt", ears="fox", legs="bare", foot="paw", tail=[(1.25, 1.25, 3.5), (1, 1, 3)], tail_rest=[(-30, 0, 0), (15, 0, 0)],
        tail_look="fur", talos=True,
        pal=dict(SKIN=skin("#c08a4a"), HAIR="#3a5ab0", FUR="#c08a4a", LIGHT="#e8c080", MARK="#7a4a1e", EYE="#4ae0f0", CLOTH="#3a5ab0", TRIM="#e8c060",
                 pattern="solid", extra=dict(boot="#7a4a1e", key="#d8a840")),
        variants={"copper": dict(SKIN=skin("#c8703a"), FUR="#c8703a", LIGHT="#f0a070", MARK="#7a3a14", CLOTH="#2a6a5a", extra=dict(boot="#7a3a14", key="#c87a3a")),
                  "gold": dict(SKIN=skin("#e0b450"), FUR="#e0b450", LIGHT="#f8e090", MARK="#8a6418", CLOTH="#a82a3a", EYE="#f84a3a",
                               extra=dict(boot="#8a6418", key="#f0d060")),
                  "iron": dict(SKIN=skin("#a8acb4"), FUR="#a8acb4", LIGHT="#d8dce4", MARK="#5a5e66", CLOTH="#3a3a46", extra=dict(boot="#5a5e66", key="#c8ccd4")),
                  "verdigris": dict(SKIN=skin("#5aa890"), FUR="#5aa890", LIGHT="#a0d8c8", MARK="#2a6a58", CLOTH="#7a4a2a", EYE="#f8d040",
                                    extra=dict(boot="#2a6a58", key="#7ac8b0"))}),
    # thirteenth wave: the tin squirrel, a clockwork squirrel of riveted blue tin with a great curled tail
    "tin_squirrel_construct": dict(
        build="petite", outfit=dict(top=["halter", "vest"], bottom="shorts", extras=["satchel"], colors=dict(cloth2="#2a3a6a", leather="#8a6a3a")),
        hair="ponytail", bangs="messy", ears="tuft", legs="bare", foot="paw",
        tail=[(2.5, 2.5, 4), (3, 3, 4), (2.75, 2.75, 4), (2, 2, 3)], tail_rest=[(-70, 0, 0), (55, 0, 0), (45, 0, 0), (40, 0, 0)], tail_look="fur", tin=True,
        pal=dict(SKIN=skin("#a8b8d4"), HAIR="#3a5aa8", FUR="#5a7ac0", LIGHT="#d8e4f8", MARK="#2a3a6a", EYE="#2ad8f8", CLOTH="#2a3a6a", TRIM="#d8b060",
                 pattern="solid", extra=dict(boot="#2a3a6a", key="#d8b060")),
        variants={"silver": dict(SKIN=skin("#c8ccd4"), HAIR="#7a7e88", FUR="#a8acb4", LIGHT="#eceef2", MARK="#5a5e66", EYE="#4a8af8"),
                  "copper": dict(SKIN=skin("#d89a6a"), HAIR="#8a3e1a", FUR="#c8703a", LIGHT="#f0b890", MARK="#6a2a10", CLOTH="#2a5a5a"),
                  "gold": dict(SKIN=skin("#ecd090"), HAIR="#a87a1e", FUR="#e0b450", LIGHT="#f8e8b0", MARK="#7a5410", EYE="#e83a3a", CLOTH="#5a1e2a"),
                  "verdigris": dict(SKIN=skin("#a8d8c8"), HAIR="#2a6a58", FUR="#5aa890", LIGHT="#c8f0e4", MARK="#1e4a3e", EYE="#f8c040", CLOTH="#6a3a1e")}),
    # tenth wave: the gargoyle, carved stone with bat wings, short horns and a spaded tail; "perch" is her statue pose
    "gargoyle_construct": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["armlets"], colors=dict(cloth2="#5a5a62")),
        hair="sidetail", bangs="jagged", ears="elf", legs="bare", foot="claw", horns="devil", wings="bat", wing_scale=0.9, flies=True,
        tail=[(.75, .75, 4), (.5, .5, 4), (2, 2, .5)], tail_look="spade", tail_rest=[(-20, 0, 0), (25, 0, 0), (10, 0, 0)], gargoyle=True,
        pal=dict(SKIN=skin("#8a8a8e"), HAIR="#6a6a70", FUR="#8a8a8e", LIGHT="#b0b0b4", MARK="#4a4a50", EYE="#e8a83a", CLOTH="#5a5a62", TRIM="#9a9aa2",
                 pattern="solid", extra=dict(boot="#6a6a70", horn="#5a5a60", wing="#7a7a80", wing_bone="#4a4a50", tail_col="#7a7a80")),
        variants={"sandstone": dict(SKIN=skin("#d8c49a"), HAIR="#b8a070", FUR="#d8c49a", LIGHT="#ece0c0", MARK="#9a8460",
                                    extra=dict(boot="#b8a070", horn="#9a8460", wing="#c8b088", wing_bone="#8a7450", tail_col="#c8b088")),
                  "blackstone": dict(SKIN=skin("#3a3640"), HAIR="#26222a", FUR="#3a3640", LIGHT="#5a5660", MARK="#1a161e", EYE="#f84a2a",
                                     extra=dict(boot="#26222a", horn="#1a161e", wing="#2e2a34", wing_bone="#16121a", tail_col="#2e2a34")),
                  "moss": dict(SKIN=skin("#7a8a74"), HAIR="#4a6a3a", FUR="#7a8a74", LIGHT="#a0b098", MARK="#3a4a30", EYE="#c8e86a",
                               extra=dict(boot="#5a6a50", horn="#4a5a40", wing="#6a7a62", wing_bone="#3a4a30", tail_col="#6a7a62")),
                  "marble": dict(SKIN=skin("#ecebe8"), HAIR="#d8d6d2", FUR="#ecebe8", LIGHT="#ffffff", MARK="#a8a6a2", EYE="#6a8ac8",
                                 extra=dict(boot="#c8c6c2", horn="#b8b6b2", wing="#e0dfdc", wing_bone="#a8a6a2", tail_col="#e0dfdc"))}),
}


def declare_hook(sp, d):
    if d.get("helm"):                                    # the automaton's round helmet and the discs over her ears
        return [("helm_dome", 11, 3, 11), ("helm_crown", 7, 1.5, 7), ("ear_disc", 1, 4, 4),
                ("shem", 3, 1, .25), ("key_shaft", .75, .75, 3), ("key_bow", 3.5, 3, .5), ("gear", 1.5, 1.5, .25)]
    if d.get("witch_hat"):                               # Tin Jack's pointed hat (bent at the tip) and her lantern
        return [("hat_brim", 13, .5, 13), ("hat_cone", 6, 4, 6), ("hat_mid", 4, 3.5, 4), ("hat_tip", 2, 3, 2), ("hat_band", 6.25, 1, 6.25),
                ("lantern", 2, 2.5, 2), ("lantern_cap", 2.5, .5, 2.5), ("lantern_ring", .25, 1.5, .25),
                ("shem", 3, 1, .25), ("key_shaft", .75, .75, 3), ("key_bow", 3.5, 3, .5), ("gear", 1.5, 1.5, .25)]
    return [("shem", 3, 1, .25), ("key_shaft", .75, .75, 3), ("key_bow", 3.5, 3, .5), ("gear", 1.5, 1.5, .25)]


def paint_hook(pt, pal, d):
    helm, disc = G.shades(pal.extra.get("helm", "#8ab4ec")), G.shades(pal.extra.get("helm_disc", "#dceafc"))
    plated = lambda f, x, y, w, h: pick(helm, 0.5 + 0.2 * (f == "up") + 0.15 * (y == h - 1) - 0.1 * (x % 5 == 0), x, y)
    pt("helm_dome", "all", plated)
    pt("helm_crown", "all", plated)
    pt("ear_disc", "all", lambda f, x, y, w, h: pick(disc, 0.6 + 0.2 * (0 < x < w - 1 and 0 < y < h - 1), x, y) if f in ("east", "west")
       else pick(helm, 0.45, x, y))
    hat, band, lamp = G.shades(pal.extra.get("hat", "#25305a")), G.shades(pal.extra.get("hat_band", "#d8b060")), G.shades(pal.extra.get("lantern", "#7af0f8"))
    patched = lambda f, x, y, w, h: pick(hat, 0.42 + 0.14 * (f == "up") + 0.12 * ((x // 3 + y // 3) % 3 == 0) + (noise(x, y, 97) - 0.5) * 0.1, x, y)
    for p in ("hat_brim", "hat_cone", "hat_mid", "hat_tip"):
        pt(p, "all", patched)
    pt("hat_band", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else pick(band, 0.55 + 0.2 * (x % 4 == 0), x, y))
    pt("lantern", "all", lambda f, x, y, w, h: pick(lamp, 0.7 + 0.15 * (f != "up"), x, y) if 0 < x < w - 1 and f not in ("up", "down")
       else pick(G.shades("#2a2a30"), 0.4, x, y))
    pt("lantern_cap", "all", lambda f, x, y, w, h: pick(G.shades("#2a2a30"), 0.45 + 0.15 * (f == "up"), x, y))
    pt("lantern_ring", "all", lambda f, x, y, w, h: pick(G.shades("#2a2a30"), 0.5, x, y))
    brass = G.shades(pal.extra.get("key", "#d8a840"))
    pt("key_shaft", "all", lambda f, x, y, w, h: pick(brass, 0.55, x, y))
    pt("key_bow", "all", lambda f, x, y, w, h: CLEAR if 0 < x < w - 1 and 0 < y < h - 1 and f in ("north", "south") else pick(brass, 0.6 + 0.2 * (y == 0), x, y))
    pt("gear", "all", lambda f, x, y, w, h: pick(brass, 0.5 + 0.25 * ((x + y) % 2), x, y))
    glow = G.shades(pal.extra.get("shem", "#e8c868"))
    clay = G.shades(pal.extra.get("boot", "#8a8078"))
    pt("shem", "all", lambda f, x, y, w, h: pick(glow, 0.75, x, y) if f == "north" and x in (1, 3, 5, 7, 9) and y in (1, 2) else pick(clay, 0.45, x, y))
    if d.get("tin"):                                     # rivets down every plate, and seams on the tail
        for p in ("thigh", "shin", "arm_up", "forearm", "tail_a", "tail_b", "tail_c", "tail_d"):
            pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(pal.LIGHT, 0.75, x, y) if x in (0, w - 1) and y % 3 == 1
               else pick(pal.MARK, 0.45, x, y) if y % 6 == 0 else None)
        return
    for p in ("thigh", "shin", "arm_up", "forearm"):                # finger marks pressed into the clay
        pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(pal.MARK, 0.5, x, y) if (x * 5 + y * 3) % 41 == 0 else None)


def build_hook(r, g, d):
    if d.get("helm"):                                    # the helmet over the crown of her head, a disc over each ear
        hy, z = g.hy, g.z
        r.add("hair_helm", "head", [0, hy + 8, z], [cube("helm_dome", [-5.5, hy + 7.6, -5.25 + z]), cube("helm_crown", [-3.5, hy + 10.6, -3.25 + z])])
        r.add("hair_discs", "head", [0, hy + 4, z], [cube("ear_disc", [-6.6, hy + 2.0, -2.0 + z]), G.mirror_cube(cube("ear_disc", [-6.6, hy + 2.0, -2.0 + z]))])
    if d.get("witch_hat"):                               # the hat on her head, its tip bent back; the lantern at her right hip
        hy, z = g.hy, g.z
        r.add("hair_hat", "head", [0, hy + 9, z], [G.rot_cube("hat_brim", [-6.5, hy + 8.7, -6.5 + z], [0, hy + 8.7, z], 6, 0, 0),
                                                    cube("hat_band", [-3.125, hy + 9.1, -3.125 + z]), cube("hat_cone", [-3, hy + 9.1, -3 + z]),
                                                    G.rot_cube("hat_mid", [-2, hy + 12.8, -1.6 + z], [0, hy + 13.0, 0.4 + z], -15, 0, 0),
                                                    G.rot_cube("hat_tip", [-1, hy + 15.6, 0.2 + z], [0, hy + 15.8, 1.2 + z], -60, 0, 0)])
        hx = -G.S("hips")[0] / 2 - 1.2
        r.add("lantern", "body", [hx, 11.5, g.z], [cube("lantern_ring", [hx - 0.125, 10.0, -0.125 + g.z]), cube("lantern_cap", [hx - 1.25, 9.6, -1.25 + g.z]),
                                                    cube("lantern", [hx - 1.0, 7.1, -1.0 + g.z])])
    if d.get("talos"):                                   # the wind-up key in her back (bone `key`, it turns while she is wound), gears on her arms
        cd = G.S("chest")[2]
        r.add("key", "body", [0, 17, cd / 2 + g.z], [cube("key_shaft", [-0.375, 16.6, cd / 2 + g.z]), cube("key_bow", [-1.75, 15.75, cd / 2 + 3.0 + g.z])])
        px, py, pz = r.b["arm_r"]["pivot"]
        r.put("arm_r", cube("gear", [px - 0.75, py - 2.5, pz - 1.0]))
    if d.get("tin"):                                     # a little gear on each arm
        px, py, pz = r.b["arm_r"]["pivot"]
        r.put("arm_r", cube("gear", [px - 0.75, py - 2.5, pz - 1.0]))
    if not d.get("gargoyle") and not d.get("talos") and not d.get("tin"):     # the golem's shem; the others have none
        r.put("head", cube("shem", [-1.5, g.hy + 6.3, -4.35 + g.z]))


def anim_extra(an, sp, d):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    fly = lambda dx, dy, dz, rx, rz: {"position": Tk((0, (0, 0, 0)), (0.25, (dx * 0.6, dy + 3, dz * 0.6)), (0.8, (dx, -6, dz)), (1.5, (dx, -6, dz))),
                                      "rotation": Tk((0, (0, 0, 0)), (0.8, (rx, 0, rz)), (1.5, (rx, 0, rz)))}
    an.add("death", 1.5, False, {
        "head": fly(0, 4, -6, -70, 25), "arm_r": fly(-7, 0, -1, -40, 80), "arm_l": fly(7, 0, -1, -40, -80),
        "leg_r": fly(-4, -2, 2, 60, 30), "leg_l": fly(4, -2, 2, 60, -30),
        "body": {"position": Tk((0, (0, 0, 0)), (0.8, (0, -8, 0)), (1.5, (0, -9, 0))), "rotation": Tk((0, (0, 0, 0)), (0.8, (25, 10, 0)), (1.5, (25, 10, 0)))},
        "eyelids": {"position": hold(*H.OUT_)}})
    # the talosling run down: slumped where she sits, head hanging
    an.add("rundown", 2.0, True, merge({"root": {"position": hold(0, -4, 0)}, "body": {"rotation": hold(20, 0, 0)}, "head": {"rotation": hold(35, 0, 10)},
                                        "eyelids": {"position": hold(*H.OUT_)}, "leg_r": {"rotation": hold(-90, 0, 10)}, "leg_l": {"rotation": hold(-90, 0, -10)}},
                                       sym("arm", hold(10, 0, -5))))
    # the gargoyle's statue pose: crouched on her perch, wings folded round her, chin on her fist
    an.add("perch", 2.0, True, merge({"root": {"position": hold(0, -4.5, 0)}, "body": {"rotation": hold(28, 0, 0)}, "head": {"rotation": hold(-18, 0, 0)},
                                      "leg_r": {"rotation": hold(-80, 0, 8)}, "leg_l": {"rotation": hold(-80, 0, -8)},
                                      "shin_r": {"rotation": hold(120, 0, 0)}, "shin_l": {"rotation": hold(120, 0, 0)},
                                      "wing_r": {"rotation": hold(0, 40, 20)}, "wing_l": {"rotation": hold(0, -40, -20)},
                                      "arm_r": {"rotation": hold(-110, 0, -10)}, "forearm_r": {"rotation": hold(-80, 0, 0)}},
                                     {"arm_l": {"rotation": hold(-40, 0, 10)}}))
    an.add("slam", 0.7, False, merge({"body": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-15, 0, 0)), (0.45, (25, 0, 0)), (0.7, (0, 0, 0)))}},
                                     sym("arm", Tk((0, (0, 0, 0)), (0.3, (-170, 0, 10)), (0.45, (-60, 0, 10)), (0.7, (0, 0, 0))))))


if __name__ == "__main__":
    F.run_family("construct", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
