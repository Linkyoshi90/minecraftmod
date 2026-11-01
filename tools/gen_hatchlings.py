#!/usr/bin/env python3
"""Harpy hatchlings: a tiny chick-girl for each of the eight harpy species. Shared engine: chibi_lib.py on top of harpy_lib.py.

    python tools/gen_hatchlings.py                   # write <species>_hatchling geo / animation / texture
    python tools/gen_hatchlings.py --preview x.png   # also render a contact sheet of all eight

Concept: a fluffy hatchling, all head and fluff, about half the height of her mother (0.7 to 1.2 blocks). A big round
head with the humanoid chibi face (huge shiny eyes, blush, tiny smile) under a cap of down, a plump round body with a
pale belly, stubby wing buds, a pom-pom of tail fluff and little legs with three-toed feet. Each species keeps the
signature of her mother: the owl her ear tufts and face disc, the ostrich her long legs and striped back, the phoenix
her flame tufts (with a glow mask), the riflebird her black velvet with a teal sheen. Animations: idle, walk, fly.
The game uses these for a hatchling harpy (AbstractHarpy#isBaby) and falls back to idle/walk/fly only.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import chibi_lib as C
from harpy_lib import rgb, ramp, pick, noise, cube, mirror_cube, hang, track, sym, merge, hold

D = H.D


def R(*cols):
    return ramp([rgb(*c) for c in cols])


SKIN = ramp([rgb(206, 150, 124), rgb(230, 178, 150), rgb(244, 202, 174), rgb(252, 224, 198)], nodither=True)
SKIN_PALE = ramp([rgb(214, 170, 156), rgb(236, 198, 182), rgb(248, 220, 204), rgb(255, 238, 224)], nodither=True)

# k scales the base model (about 14.5 units tall); legh is the leg length in base units
SPECIES = {
    "sparrow_harpy": dict(k=0.80, legh=2.5, eye=1.0,
                          down=R((90, 58, 36), (128, 86, 52), (166, 118, 74), (202, 160, 108), (228, 196, 150)),
                          belly=R((214, 188, 150), (232, 212, 176), (246, 232, 204), (255, 246, 226)),
                          wing=R((70, 44, 28), (108, 70, 42), (150, 104, 64), (196, 150, 100)),
                          leg=R((176, 112, 88), (210, 144, 112), (232, 172, 140)),
                          iris=R((96, 56, 20), (156, 100, 30), (214, 156, 56), (244, 204, 100)), skin=SKIN,
                          deco="streaks", tufts=3),
    "crow_harpy": dict(k=0.94, legh=2.8, eye=1.0,
                       down=R((22, 20, 34), (38, 34, 58), (58, 52, 86), (84, 76, 118), (118, 108, 154)),
                       belly=R((46, 42, 66), (66, 60, 92), (88, 80, 120), (112, 104, 146)),
                       wing=R((16, 14, 26), (30, 26, 48), (48, 42, 74), (80, 70, 118)),
                       leg=R((44, 42, 52), (70, 68, 80), (100, 98, 112)),
                       iris=R((60, 40, 90), (104, 70, 140), (160, 120, 190), (206, 176, 230)), skin=SKIN,
                       deco="sheen", sheen=(110, 100, 170), tufts=3),
    "ostrich_harpy": dict(k=1.0, legh=6.5, eye=0.9,
                          down=R((120, 96, 66), (170, 142, 100), (208, 184, 142), (232, 212, 172), (246, 232, 200)),
                          belly=R((220, 204, 168), (238, 226, 192), (250, 242, 216), (255, 252, 234)),
                          wing=R((96, 74, 50), (140, 110, 74), (184, 152, 106), (222, 196, 150)),
                          leg=R((178, 140, 128), (208, 170, 154), (232, 200, 184)),
                          iris=R((70, 44, 20), (120, 78, 28), (176, 122, 44), (222, 176, 84)), skin=SKIN,
                          deco="stripes", dark=(84, 62, 42), tufts=1),
    "owl_harpy": dict(k=0.97, legh=2.0, eye=1.35,
                      down=R((150, 118, 80), (196, 164, 120), (226, 204, 164), (242, 228, 196), (252, 244, 224)),
                      belly=R((226, 210, 176), (242, 232, 204), (252, 246, 226), (255, 252, 240)),
                      wing=R((110, 80, 52), (158, 120, 80), (204, 170, 124), (236, 212, 172)),
                      leg=R((214, 196, 160), (232, 218, 188), (246, 236, 214)),
                      iris=R((120, 56, 8), (190, 100, 14), (240, 160, 30), (255, 212, 90)), skin=SKIN_PALE,
                      deco="speckle", dark=(104, 74, 46), tufts=0, ears=True, disc=True),
    "seagull_harpy": dict(k=0.80, legh=2.8, eye=1.0,
                          down=R((110, 110, 106), (156, 156, 150), (196, 196, 188), (226, 226, 220), (246, 246, 242)),
                          belly=R((218, 218, 214), (236, 236, 232), (248, 248, 246), (255, 255, 255)),
                          wing=R((96, 100, 108), (136, 142, 152), (178, 184, 194), (214, 220, 228)),
                          leg=R((200, 100, 80), (230, 130, 100), (246, 164, 128)),
                          iris=R((140, 100, 20), (200, 154, 30), (240, 204, 70), (255, 236, 130)), skin=SKIN,
                          deco="speckle", dark=(84, 76, 72), tufts=2),
    "albatross_harpy": dict(k=1.02, legh=3.0, eye=1.0,
                            down=R((190, 190, 196), (214, 214, 220), (232, 232, 236), (246, 246, 248), (255, 255, 255)),
                            belly=R((232, 232, 238), (244, 244, 248), (252, 252, 254), (255, 255, 255)),
                            wing=R((120, 122, 134), (156, 158, 170), (196, 198, 208), (232, 234, 242)),
                            leg=R((214, 150, 150), (234, 180, 176), (248, 208, 204)),
                            iris=R((30, 40, 70), (52, 70, 110), (84, 108, 156), (140, 164, 210)), skin=SKIN_PALE,
                            deco="plain", tufts=2, bigfeet=True),
    "riflebird_harpy": dict(k=0.83, legh=2.6, eye=1.0,
                            down=R((10, 10, 18), (22, 22, 34), (36, 36, 54), (54, 54, 76), (80, 80, 106)),
                            belly=R((30, 30, 44), (46, 46, 64), (64, 64, 86), (88, 88, 112)),
                            wing=R((8, 8, 14), (18, 22, 32), (28, 40, 52), (44, 90, 96)),
                            leg=R((40, 40, 48), (66, 66, 78), (96, 96, 110)),
                            iris=R((10, 70, 66), (24, 124, 112), (56, 184, 164), (130, 232, 212)), skin=SKIN_PALE,
                            deco="sheen", sheen=(40, 190, 170), tufts=3),
    "phoenix_harpy": dict(k=0.97, legh=2.5, eye=1.0,
                          down=R((150, 28, 14), (210, 64, 18), (246, 112, 28), (255, 164, 52), (255, 214, 104)),
                          belly=R((246, 148, 40), (255, 188, 64), (255, 220, 112), (255, 242, 170)),
                          wing=R((120, 20, 12), (190, 48, 16), (240, 98, 24), (255, 170, 60)),
                          leg=R((200, 120, 24), (240, 160, 40), (255, 200, 80)),
                          iris=R((140, 40, 8), (214, 84, 16), (255, 146, 30), (255, 214, 90)), skin=SKIN,
                          deco="flame", tufts=5, glow=True),
    "zhuque_harpy": dict(k=0.97, legh=2.5, eye=1.0,
                         down=R((140, 18, 18), (200, 44, 24), (240, 90, 34), (255, 150, 56), (255, 210, 110)),
                         belly=R((240, 150, 60), (252, 190, 90), (255, 224, 140), (255, 244, 190)),
                         wing=R((110, 14, 14), (176, 34, 20), (230, 80, 30), (255, 160, 60)),
                         leg=R((200, 120, 24), (240, 160, 40), (255, 200, 80)),
                         iris=R((150, 80, 10), (210, 130, 20), (246, 184, 44), (255, 222, 120)), skin=SKIN,
                         deco="flame", tufts=5, glow=True),
    "tengu_harpy": dict(k=0.94, legh=2.8, eye=1.0,
                        down=R((22, 20, 34), (38, 34, 58), (58, 52, 86), (84, 76, 118), (118, 108, 154)),
                        belly=R((200, 192, 170), (222, 214, 192), (240, 234, 214), (252, 248, 236)),
                        wing=R((16, 14, 26), (30, 26, 48), (48, 42, 74), (80, 70, 118)),
                        leg=R((44, 42, 52), (70, 68, 80), (100, 98, 112)),
                        iris=R((110, 20, 20), (160, 34, 30), (204, 64, 44), (236, 110, 70)), skin=SKIN,
                        deco="sheen", sheen=(110, 100, 170), tufts=3),
    "finch_harpy": dict(k=0.78, legh=2.4, eye=1.0,
                        down=R((68, 43, 32), (96, 61, 42), (132, 91, 62), (177, 137, 98), (222, 202, 169)),
                        belly=R((207, 194, 169), (235, 225, 202), (247, 237, 216), (255, 250, 236)),
                        wing=R((55, 30, 22), (78, 42, 28), (105, 57, 35), (137, 79, 47)),
                        leg=R((166, 116, 35), (215, 161, 52), (239, 195, 85)),
                        iris=R((105, 57, 15), (151, 91, 20), (202, 139, 35), (235, 183, 65)), skin=SKIN,
                        deco="streaks", tufts=2),
    "coin_magpie_harpy": dict(k=0.9, legh=2.8, eye=1.0,
                              down=R((16, 26, 30), (28, 46, 52), (44, 70, 78), (230, 232, 236), (246, 248, 250)),
                              belly=R((214, 216, 222), (232, 234, 238), (244, 246, 248), (255, 255, 255)),
                              wing=R((12, 26, 34), (18, 52, 62), (24, 92, 104), (40, 138, 148)),
                              leg=R((44, 42, 52), (70, 68, 80), (100, 98, 112)),
                              iris=R((30, 20, 14), (52, 36, 26), (80, 58, 40), (112, 82, 58)), skin=SKIN,
                              deco="sheen", sheen=(60, 150, 160), tufts=2),
    "trickster_raven_harpy": dict(k=1.0, legh=3.2, eye=1.0,
                                  down=R((22, 20, 34), (38, 34, 58), (58, 52, 86), (84, 76, 118), (118, 108, 154)),
                                  belly=R((46, 42, 66), (66, 60, 92), (88, 80, 120), (112, 104, 146)),
                                  wing=R((16, 14, 26), (30, 26, 48), (48, 42, 74), (80, 70, 118)),
                                  leg=R((44, 42, 52), (70, 68, 80), (100, 98, 112)),
                                  iris=R((60, 40, 90), (104, 70, 140), (160, 120, 190), (206, 176, 230)), skin=SKIN,
                                  deco="sheen", sheen=(110, 100, 170), tufts=4),
    "rookmourn_harpy": dict(k=0.94, legh=2.8, eye=1.0,
                            down=R((22, 20, 34), (38, 34, 58), (58, 52, 86), (84, 76, 118), (118, 108, 154)),
                            belly=R((46, 42, 66), (66, 60, 92), (88, 80, 120), (112, 104, 146)),
                            wing=R((16, 14, 26), (30, 26, 48), (48, 42, 74), (80, 70, 118)),
                            leg=R((44, 42, 52), (70, 68, 80), (100, 98, 112)),
                            iris=R((60, 40, 90), (104, 70, 140), (160, 120, 190), (206, 176, 230)), skin=SKIN,
                            deco="sheen", sheen=(110, 100, 170), tufts=3),
    "cassowrath_harpy": dict(k=1.0, legh=6.5, eye=0.9,
                             down=R((96, 76, 50), (140, 112, 74), (184, 152, 106), (214, 188, 140), (236, 218, 180)),
                             belly=R((210, 194, 160), (230, 218, 186), (246, 238, 212), (255, 250, 232)),
                             wing=R((70, 54, 36), (110, 86, 58), (150, 120, 84), (190, 160, 120)),
                             leg=R((150, 140, 110), (186, 176, 140), (214, 206, 170)),
                             iris=R((60, 20, 12), (110, 36, 16), (160, 60, 24), (204, 96, 40)), skin=SKIN,
                             deco="stripes", dark=(60, 44, 28), tufts=1),
    "dodobold_harpy": dict(k=1.05, legh=2.4, eye=1.05,
                           down=R((40, 56, 104), (60, 82, 140), (86, 112, 176), (120, 146, 206), (164, 186, 232)),
                           belly=R((214, 206, 184), (232, 226, 206), (246, 242, 226), (255, 252, 242)),
                           wing=R((26, 38, 78), (42, 60, 116), (62, 88, 156), (92, 122, 192)),
                           leg=R((196, 150, 40), (226, 186, 70), (244, 214, 120)),
                           iris=R((20, 40, 80), (34, 70, 130), (60, 110, 180), (110, 160, 220)), skin=SKIN,
                           deco="none", tufts=1),
    "peaflare_harpy": dict(k=1.0, legh=3.2, eye=1.0,
                           down=R((120, 100, 60), (160, 136, 90), (196, 172, 124), (222, 204, 160), (240, 228, 196)),
                           belly=R((210, 196, 160), (230, 220, 190), (244, 238, 216), (255, 250, 236)),
                           wing=R((90, 72, 40), (130, 106, 64), (170, 142, 94), (206, 180, 130)),
                           leg=R((96, 96, 104), (132, 132, 140), (168, 168, 176)),
                           iris=R((90, 50, 10), (140, 86, 20), (196, 134, 40), (230, 182, 80)), skin=SKIN,
                           deco="none", tufts=3),
    "pumpkingull_harpy": dict(k=0.80, legh=2.8, eye=1.0,
                              down=R((48, 96, 186), (70, 124, 214), (104, 156, 232), (150, 192, 244), (206, 228, 252)),
                              belly=R((220, 226, 236), (236, 240, 246), (248, 250, 252), (255, 255, 255)),
                              wing=R((34, 66, 140), (60, 110, 200), (96, 150, 228), (170, 200, 240)),
                              leg=R((206, 120, 20), (236, 160, 40), (248, 196, 80)),
                              iris=R((14, 40, 100), (30, 76, 160), (60, 120, 210), (120, 170, 240)), skin=SKIN,
                              deco="none", tufts=1),
    "condorune_harpy": dict(k=0.94, legh=2.8, eye=1.0,
                            down=R((24, 50, 140), (36, 76, 190), (60, 104, 220), (100, 144, 240), (150, 186, 250)),
                            belly=R((196, 182, 150), (222, 210, 182), (238, 230, 208), (248, 244, 230)),
                            wing=R((8, 16, 50), (16, 34, 100), (28, 60, 160), (44, 90, 210)),
                            leg=R((26, 44, 100), (44, 70, 150), (70, 104, 196)),
                            iris=R((20, 60, 110), (40, 110, 170), (80, 170, 220), (150, 214, 240)), skin=SKIN,
                            deco="none", tufts=2),
    "ibiset_harpy": dict(k=1.0, legh=6.0, eye=0.9,
                         down=R((28, 54, 140), (46, 86, 186), (84, 130, 220), (140, 180, 240), (210, 228, 252)),
                         belly=R((200, 220, 248), (226, 236, 252), (246, 250, 255), (255, 255, 255)),
                         wing=R((24, 48, 130), (40, 78, 176), (70, 120, 214), (120, 166, 236)),
                         leg=R((30, 32, 40), (50, 54, 64), (74, 78, 90)),
                         iris=R((120, 80, 10), (180, 130, 20), (230, 184, 50), (250, 222, 130)), skin=SKIN,
                         deco="sheen", sheen=(236, 200, 90), tufts=2),
    "stigowl_harpy": dict(k=0.9, legh=2.8, eye=1.15,
                          down=R((26, 30, 46), (44, 50, 74), (76, 84, 112), (130, 138, 170), (200, 206, 228)),
                          belly=R((130, 138, 170), (170, 176, 204), (206, 210, 230), (232, 234, 246)),
                          wing=R((24, 28, 44), (40, 46, 70), (66, 74, 104), (104, 112, 146)),
                          leg=R((20, 20, 26), (40, 40, 50), (66, 66, 80)),
                          iris=R((20, 50, 110), (34, 80, 160), (60, 120, 210), (120, 176, 240)), skin=SKIN,
                          deco="sheen", sheen=(160, 168, 200), tufts=2),
    "emu_harpy": dict(k=1.0, legh=6.5, eye=0.9, down=R((70, 54, 40), (104, 82, 60), (140, 114, 86), (178, 152, 120), (216, 196, 168)), belly=R((140, 116, 88), (176, 150, 118), (206, 186, 156), (232, 218, 196)), wing=R((54, 42, 30), (84, 66, 48), (118, 96, 72), (154, 130, 102)), leg=R((60, 56, 58), (88, 84, 84), (120, 116, 114)), iris=R((90, 50, 14), (140, 80, 20), (190, 116, 30), (226, 160, 60)), skin=SKIN, deco="sheen", sheen=(178, 152, 120), tufts=2),
    "macaw_harpy": dict(k=0.9, legh=2.8, eye=1.0, down=R((24, 96, 180), (40, 136, 220), (90, 180, 240), (160, 216, 250), (220, 242, 255)), belly=R((226, 160, 20), (244, 190, 40), (252, 216, 90), (255, 238, 160)), wing=R((18, 76, 156), (30, 112, 200), (56, 150, 232), (110, 190, 246)), leg=R((50, 54, 64), (74, 78, 90), (100, 104, 118)), iris=R((70, 60, 30), (120, 106, 60), (176, 160, 100), (220, 210, 150)), skin=SKIN, deco="sheen", sheen=(250, 206, 70), tufts=2),
    "roc_harpy": dict(k=1.2, legh=3.4, eye=1.0, down=R((44, 74, 136), (70, 110, 176), (108, 150, 206), (156, 192, 232), (214, 232, 248)), belly=R((150, 178, 208), (186, 208, 230), (214, 228, 242), (250, 252, 255)), wing=R((36, 60, 118), (58, 94, 160), (90, 132, 196), (130, 170, 220)), leg=R((70, 120, 116), (110, 170, 160), (160, 214, 200)), iris=R((30, 60, 150), (50, 100, 200), (80, 140, 230), (120, 180, 246)), skin=SKIN, deco="sheen", sheen=(156, 192, 232), tufts=2),
    "ziz_harpy": dict(k=0.9, legh=2.8, eye=1.0, down=R((36, 110, 200), (60, 150, 230), (110, 190, 244), (176, 224, 252), (232, 246, 255)), belly=R((230, 234, 244), (240, 244, 250), (248, 250, 254), (255, 255, 255)), wing=R((214, 50, 50), (244, 130, 50), (246, 214, 80), (90, 200, 120)), leg=R((200, 130, 30), (236, 170, 50), (250, 206, 100)), iris=R((20, 40, 100), (34, 70, 150), (60, 110, 200), (120, 170, 236)), skin=SKIN, deco="sheen", sheen=(246, 214, 80), tufts=2),
    "bennu_harpy": dict(k=0.95, legh=4.0, eye=1.0, down=R((50, 90, 150), (76, 124, 186), (116, 164, 216), (170, 206, 240), (226, 240, 252)), belly=R((216, 224, 236), (232, 238, 246), (244, 248, 252), (255, 255, 255)), wing=R((42, 76, 134), (62, 106, 170), (92, 142, 204), (136, 182, 230)), leg=R((214, 160, 140), (232, 190, 170), (244, 214, 198)), iris=R((120, 80, 10), (180, 130, 20), (230, 184, 50), (250, 222, 130)), skin=SKIN, deco="sheen", sheen=(170, 206, 240), tufts=2),
    "garuda_harpy": dict(k=0.9, legh=2.8, eye=1.0, down=R((40, 90, 150), (60, 124, 190), (96, 160, 220), (150, 200, 240), (214, 236, 252)), belly=R((216, 150, 30), (244, 190, 60), (250, 216, 110), (255, 238, 170)), wing=R((34, 72, 130), (52, 104, 170), (80, 140, 206), (120, 176, 232)), leg=R((200, 140, 30), (236, 184, 50), (250, 216, 100)), iris=R((70, 36, 10), (120, 66, 20), (170, 100, 30), (216, 140, 50)), skin=SKIN, deco="sheen", sheen=(244, 190, 60), tufts=2),
    "lechuza_harpy": dict(k=0.9, legh=2.8, eye=1.15, down=R((92, 50, 24), (132, 76, 38), (170, 106, 58), (206, 146, 94), (236, 196, 150)), belly=R((188, 130, 80), (214, 170, 124), (232, 204, 168), (246, 226, 196)), wing=R((76, 42, 20), (112, 64, 32), (150, 92, 48), (188, 130, 80)), leg=R((120, 80, 40), (166, 116, 62), (204, 156, 96)), iris=R((20, 60, 110), (34, 96, 160), (60, 140, 200), (120, 190, 232)), skin=SKIN, deco="sheen", sheen=(206, 146, 94), tufts=2),
    "alkonost_harpy": dict(k=0.9, legh=2.8, eye=1.0, down=R((208, 202, 188), (230, 226, 214), (242, 240, 230), (250, 248, 242), (255, 254, 250)), belly=R((222, 218, 206), (238, 236, 226), (248, 246, 240), (254, 252, 248)), wing=R((204, 198, 184), (226, 222, 210), (240, 238, 228), (248, 246, 240)), leg=R((210, 160, 60), (236, 196, 90), (250, 224, 140)), iris=R((50, 34, 26), (84, 60, 44), (120, 90, 66), (156, 124, 94)), skin=SKIN, deco="sheen", sheen=(250, 248, 242), tufts=2),
    "poultry_harpy": dict(k=1.0, legh=2.8, eye=1.0, down=R((85, 78, 39), (151, 139, 69), (217, 199, 99), (247, 233, 154), (252, 248, 226)), belly=R((88, 84, 61), (156, 149, 109), (224, 214, 156), (252, 244, 199), (254, 252, 239)), wing=R((81, 70, 28), (144, 124, 49), (206, 178, 71), (238, 216, 132), (250, 244, 220)), leg=R((78, 56, 16), (139, 99, 29), (199, 142, 42), (233, 188, 110), (248, 236, 213)), iris=R((14, 14, 18), (26, 26, 32), (37, 37, 46), (105, 105, 112), (212, 212, 214)), skin=SKIN, deco="sheen", sheen=(200, 210, 230), tufts=2),
    "landharpy_harpy": dict(k=1.0, legh=6.0, eye=0.9, down=R((64, 64, 68), (114, 114, 121), (164, 164, 174), (205, 205, 213), (240, 240, 243)), belly=R((81, 81, 84), (144, 144, 149), (206, 206, 214), (238, 238, 244), (250, 250, 252)), wing=R((48, 48, 53), (85, 85, 94), (123, 123, 135), (173, 173, 182), (231, 231, 234)), leg=R((20, 20, 23), (36, 36, 42), (51, 51, 60), (117, 117, 124), (215, 215, 217)), iris=R((31, 20, 14), (55, 36, 26), (80, 51, 37), (139, 117, 105), (222, 215, 212)), skin=SKIN, deco="sheen", sheen=(200, 210, 230), tufts=2),
    "argentowlis_harpy": dict(k=1.1, legh=2.8, eye=1.2, down=R((20, 28, 51), (36, 49, 91), (51, 71, 131), (117, 132, 180), (215, 220, 233)), belly=R((77, 79, 85), (136, 141, 151), (196, 203, 217), (230, 236, 247), (248, 249, 252)), wing=R((14, 21, 42), (26, 38, 75), (37, 55, 108), (105, 119, 161), (212, 216, 228)), leg=R((20, 20, 25), (36, 36, 45), (51, 51, 65), (117, 117, 128), (215, 215, 218)), iris=R((70, 28, 37), (124, 49, 65), (178, 71, 94), (216, 132, 150), (244, 220, 225)), skin=SKIN, deco="sheen", sheen=(200, 210, 230), tufts=2),
    "skycrow_harpy": dict(k=0.9, legh=2.8, eye=1.0, down=R((10, 12, 19), (18, 22, 34), (26, 32, 49), (97, 101, 115), (210, 211, 215)), belly=R((20, 23, 33), (36, 42, 59), (51, 60, 85), (117, 124, 143), (215, 217, 223)), wing=R((7, 9, 16), (12, 16, 29), (17, 23, 42), (90, 94, 110), (208, 209, 213)), leg=R((14, 16, 22), (26, 29, 39), (37, 42, 57), (105, 110, 121), (212, 213, 216)), iris=R((31, 64, 86), (55, 114, 153), (80, 164, 221), (139, 205, 250), (222, 240, 253)), skin=SKIN, deco="sheen", sheen=(200, 210, 230), tufts=2),
    "ibisking_harpy": dict(k=0.95, legh=4.0, eye=1.0, down=R((14, 25, 53), (26, 45, 95), (37, 65, 137), (105, 128, 184), (212, 218, 234)), belly=R((37, 48, 70), (65, 85, 124), (94, 123, 178), (150, 173, 216), (225, 231, 244)), wing=R((10, 20, 48), (18, 36, 85), (26, 51, 123), (97, 117, 173), (210, 215, 231)), leg=R((14, 14, 18), (26, 26, 32), (37, 37, 46), (105, 105, 112), (212, 212, 214)), iris=R((70, 56, 22), (124, 99, 39), (178, 142, 57), (216, 188, 121), (244, 236, 216)), skin=SKIN, deco="sheen", sheen=(200, 210, 230), tufts=2),
    "motmora_harpy": dict(k=0.9, legh=2.8, eye=1.0, down=R((14, 31, 75), (26, 55, 134), (37, 80, 192), (105, 139, 227), (212, 222, 247)), belly=R((58, 70, 85), (104, 124, 151), (149, 178, 217), (194, 216, 247), (237, 244, 252)), wing=R((10, 25, 70), (18, 45, 124), (26, 65, 178), (97, 128, 216), (210, 218, 244)), leg=R((20, 20, 31), (36, 36, 55), (51, 51, 80), (117, 117, 139), (215, 215, 222)), iris=R((14, 25, 53), (26, 45, 95), (37, 65, 137), (105, 128, 184), (212, 218, 234)), skin=SKIN, deco="sheen", sheen=(200, 210, 230), tufts=2),
    "reliquary_dove_harpy": dict(k=0.9, legh=2.8, eye=1.0, down=R((42, 53, 70), (75, 95, 124), (108, 137, 178), (161, 184, 216), (228, 234, 244)), belly=R((77, 81, 85), (136, 144, 151), (196, 206, 217), (230, 238, 247), (248, 250, 252)), wing=R((37, 48, 67), (65, 85, 119), (94, 123, 171), (150, 173, 210), (225, 231, 242)), leg=R((64, 37, 37), (114, 65, 65), (164, 94, 94), (205, 150, 150), (240, 225, 225)), iris=R((14, 20, 37), (26, 36, 65), (37, 51, 94), (105, 117, 150), (212, 215, 225)), skin=SKIN, deco="sheen", sheen=(200, 210, 230), tufts=2),
    "yatagarasu_harpy": dict(k=0.9, legh=2.8, eye=1.0,
                             down=R((20, 20, 26), (34, 34, 42), (52, 52, 62), (80, 80, 92), (120, 120, 134)),
                             belly=R((120, 160, 210), (150, 186, 228), (180, 210, 240), (210, 230, 250)),
                             wing=R((16, 16, 22), (28, 28, 36), (44, 44, 54), (68, 68, 80)),
                             leg=R((14, 14, 18), (30, 30, 36), (50, 50, 58)),
                             iris=R((20, 50, 110), (34, 80, 160), (60, 120, 210), (120, 176, 240)), skin=SKIN,
                             deco="sheen", sheen=(180, 210, 240), tufts=2),
    "ironbeak_harpy": dict(k=0.9, legh=2.8, eye=1.0,
                           down=R((36, 70, 160), (60, 110, 210), (100, 150, 236), (150, 190, 248), (210, 230, 255)),
                           belly=R((170, 200, 240), (200, 220, 248), (226, 236, 252), (246, 250, 255)),
                           wing=R((26, 48, 120), (44, 80, 180), (70, 120, 224), (110, 160, 244)),
                           leg=R((110, 118, 134), (150, 158, 176), (190, 198, 214)),
                           iris=R((60, 30, 10), (110, 60, 20), (170, 100, 30), (220, 150, 60)), skin=SKIN,
                           deco="sheen", sheen=(150, 190, 248), tufts=2),
    "emberu_harpy": dict(k=1.0, legh=6.5, eye=0.9,
                         down=R((30, 56, 140), (50, 90, 190), (90, 136, 220), (150, 186, 240), (210, 228, 250)),
                         belly=R((150, 170, 210), (190, 206, 236), (224, 234, 250), (248, 250, 255)),
                         wing=R((20, 40, 100), (34, 64, 150), (56, 96, 196), (100, 140, 224)),
                         leg=R((50, 58, 90), (74, 84, 120), (104, 114, 150)),
                         iris=R((20, 60, 110), (40, 100, 170), (70, 150, 210), (120, 200, 240)), skin=SKIN,
                         deco="stripes", dark=(20, 34, 90), tufts=1),
    "mirror_crane_harpy": dict(k=0.9, legh=4.0, eye=1.0,
                               down=R((60, 90, 170), (90, 126, 206), (130, 164, 230), (180, 204, 246), (226, 236, 255)),
                               belly=R((226, 232, 244), (240, 244, 252), (248, 250, 255), (255, 255, 255)),
                               wing=R((20, 34, 90), (34, 56, 130), (56, 88, 176), (96, 130, 210)),
                               leg=R((50, 54, 64), (74, 78, 90), (100, 104, 118)),
                               iris=R((120, 80, 10), (180, 130, 20), (230, 184, 50), (250, 222, 130)), skin=SKIN,
                               deco="plain", tufts=2),
    "flamingo_harpy": dict(k=0.9, legh=4.0, eye=1.0,
                           down=R((214, 150, 166), (232, 176, 190), (244, 200, 210), (250, 220, 228), (255, 238, 242)),
                           belly=R((240, 210, 218), (248, 226, 232), (254, 240, 244), (255, 250, 252)),
                           wing=R((200, 120, 140), (226, 150, 168), (242, 180, 194), (250, 206, 216)),
                           leg=R((190, 120, 130), (220, 150, 160), (240, 180, 188)),
                           iris=R((150, 110, 10), (200, 160, 20), (236, 200, 50), (250, 230, 120)), skin=SKIN,
                           deco="plain", tufts=2),
    "toucan_harpy": dict(k=0.86, legh=2.6, eye=1.0,
                         down=R((14, 14, 18), (28, 28, 34), (46, 46, 54), (70, 70, 80), (104, 104, 116)),
                         belly=R((214, 214, 208), (236, 236, 230), (248, 248, 244), (255, 255, 255)),
                         wing=R((8, 8, 12), (20, 20, 26), (36, 36, 44), (60, 60, 70)),
                         leg=R((60, 86, 150), (86, 116, 186), (120, 150, 214)),
                         iris=R((16, 60, 110), (30, 100, 170), (60, 150, 220), (120, 200, 245)), skin=SKIN,
                         deco="plain", tufts=2),
    "penguin_harpy": dict(k=0.84, legh=1.6, eye=1.1,
                          down=R((110, 112, 120), (140, 142, 150), (168, 170, 178), (196, 198, 204), (222, 224, 228)),
                          belly=R((214, 216, 220), (232, 234, 238), (246, 246, 248), (255, 255, 255)),
                          wing=R((70, 72, 80), (98, 100, 108), (128, 130, 138), (160, 162, 170)),
                          leg=R((150, 80, 70), (196, 112, 96), (226, 146, 126)),
                          iris=R((60, 30, 14), (100, 54, 24), (150, 90, 40), (200, 140, 70)), skin=SKIN_PALE,
                          deco="plain", tufts=1, bigfeet=True),
}


def sizes(sp):
    e = sp["eye"]
    s = {
        "head": (8, 7, 7), "cap": (8, 2.25, 7, ("down",)), "tuft": (1.5, 2, 1.5), "tuft_tall": (1.5, 3, 1.5), "ear": (1.5, 3, 1.5),
        "eyelids": (6.4 * e if e < 1.2 else 7.4, 2.5 * e, .25),
        "body": (6, 5, 5), "belly": (4, 3.5, .5), "bellyfluff": (4.5, 1.5, 1),
        "wing": (1.25, 4, 3.5), "wingtip": (1, 2.25, 3),
        "tail": (3, 1.5, 2), "tailf": (1, 2.5, .5),
        "leg": (1.5, sp["legh"], 1.5), "foot": (3 if sp.get("bigfeet") else 2.5, .5, 3 if sp.get("bigfeet") else 2.5), "toe": (.5, .5, 1),
    }
    return s


# ----------------------------------------------------------------------------- painting
def paint(sp, name):
    pt = H.Painter()
    glow = H.Painter() if sp.get("glow") else None
    down, belly, wing, leg = sp["down"], sp["belly"], sp["wing"], sp["leg"]
    dark = sp.get("dark", (60, 40, 30))
    deco = sp["deco"]

    def body_deco(f, x, y, w, h, t):
        if deco == "streaks" and f in ("north", "up", "east", "west", "south") and (x % 5 == 1) and y % 4 < 2 and noise(x, y, 9) < 0.7:
            return pick(down, t - 0.30, x, y)
        if deco == "speckle" and noise(x, y, 21) > 0.90:
            return rgb(*dark)
        if deco == "stripes" and f in ("up", "east", "west", "south") and (x // 3) % 2 == 0:
            return rgb(*dark) if noise(x, y, 4) > 0.15 else pick(down, t - 0.2, x, y)
        if deco == "sheen" and noise(x, y, 17) > 0.86:
            r, g, b = sp["sheen"]
            return rgb(r, g, b)
        if deco == "flame" and (f == "up" or y < h * 0.35) and noise(x, y, 31) > 0.55:
            return pick(down, min(1.0, t + 0.28), x, y)
        return None

    body = C.surface(down, 5, 0.58, 0.22, body_deco)
    for p in ("head", "cap", "body", "tail", "bellyfluff"):
        pt(p, "all", body)
    pt("tuft", "all", C.surface(down, 8, 0.78, 0.25, body_deco))
    pt("tuft_tall", "all", C.surface(down, 8, 0.80, 0.25, body_deco))
    pt("ear", "all", C.surface(down, 9, 0.50, 0.25, body_deco))
    pt("belly", "all", C.surface(belly, 6, 0.62, 0.22))
    pt("bellyfluff", "all", C.surface(belly, 6, 0.66, 0.22))

    def wing_fn(f, x, y, w, h):
        t = 0.55 + (noise(x, y, 3) - 0.5) * 0.2
        if f in ("east", "west", "north", "south"):
            t += 0.12 * ((y // 3) % 2) - 0.1
            if deco == "sheen" and noise(x, y, 7) > 0.82:
                r, g, b = sp["sheen"]
                return rgb(r, g, b)
            if deco == "flame":
                t += 0.4 * (y / max(1, h - 1)) - 0.1
        if f == "down":
            t -= 0.15
        return pick(wing, t, x, y)
    pt("wing", "all", wing_fn)
    pt("wingtip", "all", wing_fn)
    pt("tailf", "all", wing_fn)

    def leg_fn(f, x, y, w, h):
        return pick(leg, 0.5 + (noise(x, y, 11) - 0.5) * 0.25 - (0.18 if f in ("south", "down") else 0.0) + (0.15 if f == "up" else 0), x, y)
    for p in ("leg", "foot", "toe"):
        pt(p, "all", leg_fn)
    if sp.get("owl_legs") or sp["deco"] == "speckle" and sp.get("disc"):
        pt("leg", "all", C.surface(down, 12, 0.75, 0.2))   # feathered legs

    # ---- the face on the head's front
    x0, y0, w, h = H.RECT[("head", "north")]
    T = pt.T
    skin = sp["skin"]
    for yy in range(h):
        for xx in range(w):
            inside = 1 <= xx <= w - 2 and yy <= h - 2
            if yy > h - 5 and (xx < 3 + (yy - (h - 5)) or xx > w - 4 - (yy - (h - 5))):
                inside = False
            if sp.get("disc"):                              # owl: a round, pale face disc
                inside = C.ellipse(xx, yy, w / 2, h * 0.55, w * 0.46, h * 0.52) <= 1.0
            if inside:
                T[y0 + yy][x0 + xx] = pick(skin, 0.55 + (noise(xx, yy, 2) - 0.5) * 0.1 + (0.15 if yy > h * 0.7 else 0), xx, yy)
            else:
                T[y0 + yy][x0 + xx] = body("north", xx, yy, w, h)
    e = sp["eye"]
    a, b = 3.3 * e, 4.4 * e
    ey = h * 0.50
    cxs = (w * 0.28, w * 0.72)
    for cx in cxs:
        C.paint_eye(T, x0 + cx, y0 + ey, a, b, sp["iris"], rgb(40, 24, 24), glint=True)
        C.blush(T, x0 + cx + (-1 if cx < w / 2 else 1), y0 + ey + b + 2.3, 2.6, 1.5, rgb(240, 130, 130))
    my = int(h * 0.86)
    for dx, dy in ((-2, 0), (-1, 1), (0, 1), (1, 1), (2, 0)):    # a tiny smile
        T[y0 + my + dy][x0 + int(w / 2) + dx] = rgb(150, 70, 74)

    # ---- eyelids: skin with a happy closed-eye curve (shown for a moment when she blinks)
    lx0, ly0, lw, lh = H.RECT[("eyelids", "north")]
    hw = H.size_of = H.PARTS["head"]["size"][0]
    for yy in range(lh):
        for xx in range(lw):
            T[ly0 + yy][lx0 + xx] = pick(skin, 0.6, xx, yy)
    elw = H.PARTS["eyelids"]["size"][0]
    for cxm in (-0.22 * hw, 0.22 * hw):
        ecx = (cxm + elw / 2) * D
        for dx in range(-int(a), int(a) + 1):
            dy = int(round(1.2 * (1 - (dx / max(1, a)) ** 2)))
            T[ly0 + int(lh / 2) - dy][lx0 + int(ecx) + dx] = rgb(70, 36, 36)
    for f in ("east", "west", "south", "up", "down"):
        px0, py0, pw, ph = H.RECT[("eyelids", f)]
        for yy in range(ph):
            for xx in range(pw):
                T[py0 + yy][px0 + xx] = pick(skin, 0.55, xx, yy)

    # ---- glow mask for the phoenix: flames on the head, wings and tail glow
    gmask = None
    if glow is not None:
        def hot(f, x, y, w, h):
            return rgb(255, 190, 60) if noise(x, y, 77) > 0.35 else rgb(255, 120, 30)
        for p in ("tuft", "tuft_tall", "tailf", "wingtip"):
            glow(p, "all", hot)
        gx, gy, gw, gh = H.RECT[("belly", "north")]
        for yy in range(gh):
            for xx in range(gw):
                if noise(xx, yy, 5) > 0.5:
                    glow.T[gy + yy][gx + xx] = rgb(255, 214, 110)
        gmask = glow.T
    return pt.T, gmask


# ----------------------------------------------------------------------------- geometry
def build(sp, name):
    m = H.Model(name)
    S = H.PARTS
    sz = lambda p: S[p]["size"]
    hw, hh, hd = sz("head")
    bw, bh, bd = sz("body")
    legw, legh, legd = sz("leg")
    fw, fh, fd = sz("foot")
    ww, wh, wd = sz("wing")
    k = sp["k"]
    y_body = fh + legh
    y_neck = y_body + bh - 0.25
    lx = bw * 0.26

    m.bone("root", None, [0, 0, 0])
    belly = [cube("belly", [-sz("belly")[0] / 2, y_body + 0.5 * k, -bd / 2 - sz("belly")[2] + 0.1]),
             cube("bellyfluff", [-sz("bellyfluff")[0] / 2, y_body - 0.25, -bd / 2 - 0.35])]
    m.bone("body", "root", [0, y_body, 0], [cube("body", [-bw / 2, y_body, -bd / 2])] + belly)

    head = [cube("head", [-hw / 2, y_neck, -hd / 2]), cube("cap", [-hw / 2, y_neck + hh - sz("cap")[1], -hd / 2], inflate=0.25 * k)]
    top = y_neck + hh
    tw = sz("tuft")[0]
    n = sp["tufts"]
    spots = [(0, -1.0, 0), (-1.8, 0.4, -14), (1.8, 0.4, 14), (-0.9, 1.6, -6), (0.9, 1.6, 6)][:max(n, 0)]
    for i, (x, z, rz) in enumerate(spots):
        part = "tuft_tall" if (sp.get("glow") and i == 0) else "tuft"
        head.append(cube(part, [x * k - tw / 2, top - 0.25, z * k - tw / 2], pivot=[x * k, top - 0.25, z * k], rotation=[0, 0, rz]))
    if sp.get("ears"):
        ew = sz("ear")[0]
        for sx in (-1, 1):
            head.append(cube("ear", [sx * hw * 0.34 - ew / 2, top - 0.25, -ew / 2], pivot=[sx * hw * 0.34, top - 0.25, 0], rotation=[0, 0, sx * 12]))
    m.bone("head", "body", [0, y_neck, 0], head)
    ey_c = y_neck + hh - (hh * 0.50)
    m.bone("eyelids", "head", [0, ey_c, -hd / 2 + 0.2], [cube("eyelids", [-sz("eyelids")[0] / 2, ey_c - sz("eyelids")[1] / 2, -hd / 2 + 0.1])])

    wy = y_body + bh * 0.88
    m.bone("wing_r", "body", [-bw / 2, wy, 0.2], [cube("wing", [-bw / 2 - ww, wy - wh, -wd / 2 + 0.2])], mirror=True)
    tw2, th2, td2 = sz("wingtip")
    m.bone("wing_tip_r", "wing_r", [-bw / 2 - ww / 2, wy - wh, 0.2], [cube("wingtip", [-bw / 2 - ww / 2 - tw2 / 2, wy - wh - th2, -td2 / 2 + 0.2])], mirror=True)

    tfw, tfh, tfd = sz("tailf")
    tail = [cube("tail", [-sz("tail")[0] / 2, y_body + 0.5, bd / 2 - 0.25])]
    for i, (x, rz) in enumerate(((-1.0, -16), (0, 0), (1.0, 16))):
        tail.append(cube("tailf", [x * k - tfw / 2, y_body + 1.2 * k, bd / 2 + sz("tail")[2] - 0.6],
                         pivot=[x * k, y_body + 1.2 * k + 0.5, bd / 2 + sz("tail")[2] - 0.5], rotation=[-18, 0, rz]))
    m.bone("tail", "body", [0, y_body + 1.0, bd / 2], tail)

    m.bone("leg_r", "root", [-lx, y_body, 0], [cube("leg", [-lx - legw / 2, fh, -legd / 2])], mirror=True)
    toes = []
    for dx in (-fw * 0.3, 0.0, fw * 0.3):
        toes.append(cube("toe", [-lx + dx - sz("toe")[0] / 2, 0, -fd / 2 - sz("toe")[2] + 0.3]))
    m.bone("foot_r", "leg_r", [-lx, fh, 0], [cube("foot", [-lx - fw / 2, 0, -fd / 2 + 0.25 * k])] + toes, mirror=True)
    return m, y_neck + hh


def animations(an):
    breath = track((0, (0, 0, 0)), (1.5, (0, -0.15, 0)), (3, (0, 0, 0)))
    an.add("idle", 3.0, True, merge({
        "body": {"position": breath},
        "head": {"rotation": track((0, (0, 0, 0)), (0.9, (-3, 8, 2)), (1.8, (2, -6, -2)), (3, (0, 0, 0)))},
        "tail": {"rotation": track((0, (0, 0, 0)), (0.75, (0, 10, 0)), (1.5, (0, 0, 0)), (2.25, (0, -10, 0)), (3, (0, 0, 0)))},
    }, H.blink(1.9), sym("wing", track((0, (0, 0, -9)), (1.5, (0, 0, -14)), (3, (0, 0, -9))))))
    swing = track((0, (30, 0, 0)), (0.3, (-30, 0, 0)), (0.6, (30, 0, 0)))
    swing_o = track((0, (-30, 0, 0)), (0.3, (30, 0, 0)), (0.6, (-30, 0, 0)))
    an.add("walk", 0.6, True, merge({
        "leg_r": {"rotation": swing}, "leg_l": {"rotation": swing_o},
        "body": {"position": track((0, (0, 0, 0)), (0.15, (0, 0.25, 0)), (0.3, (0, 0, 0)), (0.45, (0, 0.25, 0)), (0.6, (0, 0, 0))),
                 "rotation": track((0, (0, 0, 5)), (0.3, (0, 0, -5)), (0.6, (0, 0, 5)))},
        "head": {"rotation": track((0, (0, 0, -5)), (0.3, (0, 0, 5)), (0.6, (0, 0, -5)))},
        "tail": {"rotation": track((0, (0, 14, 0)), (0.3, (0, -14, 0)), (0.6, (0, 14, 0)))},
    }, sym("wing", track((0, (0, 0, -14)), (0.3, (0, 0, -26)), (0.6, (0, 0, -14))))))
    flap = track((0, (0, 0, -100)), (0.2, (0, 0, -25)), (0.4, (0, 0, -100)))
    an.add("fly", 0.4, True, merge({
        "body": {"position": track((0, (0, -0.4, 0)), (0.2, (0, 0.4, 0)), (0.4, (0, -0.4, 0))),
                 "rotation": track((0, (22, 0, 0)), (0.2, (14, 0, 0)), (0.4, (22, 0, 0)))},
        "head": {"rotation": hold(-20, 0, 0)},
        "leg_r": {"rotation": hold(35, 0, 0)}, "leg_l": {"rotation": hold(35, 0, 0)},
        "tail": {"rotation": track((0, (12, 0, 0)), (0.2, (-4, 0, 0)), (0.4, (12, 0, 0)))},
    }, sym("wing", flap), sym("wing_tip", track((0, (0, 0, 14)), (0.2, (0, 0, -10)), (0.4, (0, 0, 14))))))


def main():
    entries = []
    for species, sp in SPECIES.items():
        name = species + "_hatchling"
        C.declare(sizes(sp), sp["k"])
        tex, glow = paint(sp, name)
        model, height = build(sp, name)
        geo, adoc = C.write_model(name, tex, model, animations, glow)
        entries.append((geo, tex, adoc["animations"][f"animation.{name}.idle"], 0, height))
        if "--preview" in sys.argv:
            pass
    if "--preview" in sys.argv:
        C.lineup(sys.argv[sys.argv.index("--preview") + 1], entries, cols=4, cell=360, views=((-30, 12), (150, 10)))


if __name__ == "__main__":
    main()
