# Paints

The textures are in an optional second jar, `monstergirls-paints-<version>.jar` (`src/paints`, built by `./gradlew paintsJar` or `build`), because there are far too many for the main jar. It is only needed on clients. Without it a painted creature keeps her paint, in saves and for her young, but is drawn unpainted.

Every Harmontide paint, 119 in all. A paint is a full texture for a model: `textures/entity/<model>_paint_<paint>.png`, made by
`tools/gen_paints.py` for every model. One wild girl, harpy or anuran in 64 is painted (`PAINT_RARITY`), like a shiny, and a
painted mother has painted young. The list lives in `AbstractMonsterGirl.PAINTS`. It is synced by index, so **only ever append
to it**.

**Texture** names the Minecraft texture a paint tiles, if any: a paint named after something Minecraft draws must use that
texture (`vtile` in gen_paints). **Notes** lists the special cases:

- *Glows*: a glow mask (`PaintGlowLayer`, and `GLOWING` in gen_paints).
- *See-through*: drawn translucent (`Paints.translucent`).
- *Seasonal*: only rolled in that month (`Painted.roll`).

To test one, summon a girl with it: `/summon monstergirls:wolf_canid ~ ~ ~ {Paint:"lapis"}`.

## Colours

| Paint | Look | Texture | Notes |
|---|---|---|---|
| `blue` | Blue | | |
| `red` | Red | | |
| `green` | Green | | |
| `yellow` | Yellow | | |
| `purple` | Purple | | |
| `black` | Black | | |
| `inverted` | Inverted colours | | |

## Treats

| Paint | Look | Texture | Notes |
|---|---|---|---|
| `chocolate` | Chocolate, darker chocolate dripping down every part | | |
| `icecream` | Strawberry ice cream melting off her hair, sprinkles | | |
| `strawberry` | Strawberry hair with seeds and a leafy crown | | |
| `jello` | Glossy jello | | See-through |
| `milk` | Creamy porcelain white | | |
| `honey` | Honey dripping from her hair | `honey_block_side` | |
| `cookie` | Baked dough, chocolate chips, browned edges | | |
| `gummy` | Bright candy | | See-through |
| `candy_cane` | Candy cane stripes | | Seasonal: December |
| `lemonade` | Lemon slices on pale yellow | | Seasonal: June |
| `watermelon` | Red flesh with seeds over a green rind | `melon_side` | Seasonal: July |

## Metals and stone

| Paint | Look | Texture | Notes |
|---|---|---|---|
| `gold` | A gilded statue | `gold_block` | |
| `copper` | Fresh copper | `copper_block` | Weathers stage by stage; honeycomb waxes it, an axe scrapes it |
| `copper_exposed` | Exposed copper | `exposed_copper` | Second stage, never rolled |
| `copper_weathered` | Weathered copper | `weathered_copper` | Third stage, never rolled |
| `copper_oxidized` | Oxidized copper | `oxidized_copper` | Last stage, never rolled |
| `chrome` | Mirror metal with a bright reflection band | | |
| `rust` | Orange-brown rust through grey steel | | |
| `marble` | White stone with grey veins | | |
| `jade` | Carved green stone, pale veins | | |
| `terrazzo` | Cream stone set with coloured chips | | |
| `mosaic` | Little glazed tiles in grout | | |
| `porcelain` | White glaze with blue motifs | | |
| `obsidian` | Obsidian | `obsidian` | |
| `sandstone` | Sandstone | `sandstone` | |
| `lapis` | Lapis block | `lapis_block` | |
| `prismarine` | Prismarine | `prismarine` | |
| `amethyst` | Amethyst block | `amethyst_block` | |
| `sculk` | Sculk | `sculk` | Glows: its bright specks |
| `mushroom` | Red mushroom block | `red_mushroom_block` | |
| `magma` | Magma block | `magma` | Glows: its cracks |
| `bookshelf` | Bookshelf | `bookshelf` | |
| `glazed` | Blue glazed terracotta | `blue_glazed_terracotta` | |
| `redstone_lamp` | A lit redstone lamp | `redstone_lamp_on` | Glows: its light |
| `netherite` | Netherite block | `netherite_block` | |
| `purpur` | Purpur block | `purpur_block` | |
| `crying_obsidian` | Crying obsidian | `crying_obsidian` | Glows: its tears |
| `shroomlight` | Shroomlight | `shroomlight` | Glows: its light |
| `end_stone` | End stone | `end_stone` | |
| `hay_bale` | Hay bale | `hay_block_side` | |
| `ancient_debris` | Ancient debris | `ancient_debris_side` | |
| `sea_lantern` | Sea lantern | `sea_lantern` | Glows |
| `redstone` | Redstone block | `redstone_block` | Glows: its bright lines |
| `glowstone` | Glowstone | `glowstone` | Glows |
| `cherry` | Cherry planks | `cherry_planks` | |
| `terracotta` | Plain terracotta | `terracotta` | |
| `tnt` | TNT | `tnt_side` | |
| `warped` | Warped planks | `warped_planks` | |
| `dripstone` | Dripstone block | `dripstone_block` | |
| `brain_coral` | Brain coral block | `brain_coral_block` | |
| `crimson` | Crimson planks | `crimson_planks` | |
| `bone` | Bone block | `bone_block_side` | |
| `bamboo` | Block of bamboo | `bamboo_block` | |
| `sponge` | Sponge | `sponge` | |
| `soul_sand` | Soul sand | `soul_sand` | |
| `nether_wart` | Nether wart block | `nether_wart_block` | |
| `mycelium` | Mycelium | `mycelium_top` | |
| `basalt` | Basalt | `basalt_side` | |
| `blue_ice` | Blue ice | `blue_ice` | |
| `chorus` | Chorus plant | `chorus_plant` | |
| `calcite` | Calcite | `calcite` | |
| `tuff` | Tuff | `tuff` | |
| `moss` | Moss over stone | `moss_block`, `stone` | |
| `frost` | Icy, frosted on every top | `packed_ice` | |

## Glowing and see-through

| Paint | Look | Texture | Notes |
|---|---|---|---|
| `glow` | Dark, with neon markings | | Glows |
| `neon` | Dark, with tubes along every edge | | Glows |
| `circuit` | A green circuit board | | Glows: its traces |
| `lava_lamp` | Warm blobs rising through violet | | Glows: its blobs |
| `thunder` | Storm grey with lightning bolts | | Glows: its bolts |
| `ghost` | Pale | | See-through |
| `hologram` | Cyan with scanlines | | See-through |
| `stained_glass` | Bright panes and lead lines | `*_stained_glass` | See-through |

## Skies and light

| Paint | Look | Texture | Notes |
|---|---|---|---|
| `galaxy` | A night sky with stars | | |
| `aurora` | A night body with green-to-violet bands | | |
| `cloud` | Soft white, sky-blue shade underneath | | |
| `sunset` | Orange melting into violet | | |
| `synthwave` | Dark violet, a neon sunset gradient and grid | | |
| `rainbow` | Bands of colour running down every part | | |
| `opal` | Pearly white with a rainbow sheen | | |
| `fireworks` | Bursts on a night-blue body | | Seasonal: January |

## Cloth and craft

| Paint | Look | Texture | Notes |
|---|---|---|---|
| `plush` | Pastel felt with stitched seams | | |
| `origami` | Folded paper | | |
| `denim` | Blue twill, orange stitching | | |
| `tartan` | A woven plaid | | |
| `burlap` | Sackcloth with stitched seams | | |
| `lace` | Ivory open-work lace | | |
| `patchwork` | Quilted squares, stitched | | |
| `tie_dye` | A swirl of rainbow dye | | |
| `camo` | Woodland camouflage | | |

## Print and pattern

| Paint | Look | Texture | Notes |
|---|---|---|---|
| `ink` | Manga line art | | |
| `sepia` | An old photograph | | |
| `pixel` | An 8-bit palette in chunky pixels | | |
| `comic` | Flat colours, halftone dots, bold outlines | | |
| `blueprint` | White lines on blueprint blue | | |
| `chalk` | Chalk lines on a blackboard | | |
| `watercolour` | Soft washes bleeding at the edges | | |
| `checkered` | A chessboard | | |
| `polka` | White dots on bright colours | | |

## Animals

| Paint | Look | Texture | Notes |
|---|---|---|---|
| `cow` | The cow's hide | `entity/cow/cow` | |
| `leopard` | Rosettes on gold | | |
| `zebra` | Wavy black and white stripes | | |
| `giraffe` | Brown patches in a net of cream | | |
| `koi` | Red-orange patches on pearl white | | |

## Plants and seasons

| Paint | Look | Texture | Notes |
|---|---|---|---|
| `sakura` | Soft pink, petals in her hair | `cherry_leaves` | |
| `valentine` | Pink and red with little hearts | | Seasonal: February |
| `clover` | Green, shamrocks, flecks of gold | | Seasonal: March |
| `easter` | Pastel egg stripes | | Seasonal: April |
| `bluebell` | Blue bells on green stems | | Seasonal: May |
| `sunflower` | Sunflower heads on leaf green | `sunflower_front` | Seasonal: August |
| `acorn` | Little acorns on warm brown | | Seasonal: September |
| `pumpkin` | Pumpkin | `pumpkin_side` | Seasonal: October |
| `autumn` | Falling-leaf colours | | Seasonal: November |

## Adding a paint

1. Append its name to `AbstractMonsterGirl.PAINTS`, and to the two lists and `TREATS` in `tools/gen_paints.py`.
2. Draw it in `finish()`. If Minecraft draws the thing it is named after, tile that texture with `vtile`.
3. If it glows, add it to `GLOWING` and `PaintGlowLayer.glows`. If it is see-through, add it to `Paints.translucent`. If it is seasonal, add it to `Painted.roll`.
4. Run `uv run --no-project --with pillow python tools/gen_paints.py <paint>`. A glow mask with no pixels crashes GeckoLib: gen_paints guards against that, but check that the glow actually shows.
5. Add it to this file.
