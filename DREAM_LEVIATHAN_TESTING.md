# Dream-Leviathan: what to check in game

The gametests cover how the dungeon is built, the blessing bar, the calf, the parasites' bite, landing and dying, the horn rules and the cysts.
Everything below has only run in tests. The parts marked **(client)** have never been seen at all: look at those first.

## Test commands (op)

- `/harmontide leviathan summon`: she flies in, circles for 10 seconds and lands near you.
- `/harmontide leviathan land`: she lands near you at once.
- `/harmontide leviathan skip <rooms>`: marks that many rooms of the current visit done. It never skips the heart chamber.
- `/harmontide leviathan heal`, `kill`: heals her, or lets her die now.
- `/harmontide leviathan rot`: moves her rotting on by a day.
- `/harmontide leviathan status`: where she is and what she is doing.
- `/harmontide leviathan reset`: forgets everything, as in a new world (her first natural visit can happen again).
- `/harmontide leviathan unbless`: takes away your blessing and the record of your egg, so you can earn them again.

## Outside

1. **(client)** Far away she is a shape in the sky that moves smoothly, without jumping every half second. Check that she:
   - still shows at night (the stars and runes glow);
   - looks sick while she comes in;
   - hands over to her real body without a pop as she comes close.
2. **(client)** Up close, the 40-block whale is the right way round. The nose goes first, the tail flukes beat, the fins flap slowly, and glitter trails from her fins.
3. When she lands, everyone gets the chat message with her coordinates. She lies on her belly with her mouth hanging open and breathes clouds from her blowhole.
4. Walk to her mouth (or her rear under the tail, with `nsfw = true`): a hint appears, and crouching there takes you inside.
5. **(client)** As waves are beaten she looks less sick. Seen from far off, the projection should match her body.
6. Leave her for three days (or use `kill`). Everyone gets a message, and her body turns grey on its side. Then:
   - pulling at it gives dream scales (24 in all);
   - after a day it rots, after three it is bones;
   - after six it is gone, leaving bone-block ribs on the ground.
7. Dream horn: a goat horn surrounded by four dream scales. It works only at night, under open sky, in the overworld, and only when she isn't already here.

## Inside

1. Everyone arrives on the same spot in the entry chamber. Baleen hangs round the way back out; using it puts you beside her mouth.
2. The rooms come in order: a puzzle, then a wave, five times over, then the heart chamber. There are two side rooms with treasure chests. The puzzle order and the layout change with every landing.
3. Puzzles:
   - **Nerves:** hit the one that lights up, by hand or with an arrow. Hitting a dark one stings and starts over.
   - **Heartbeat:** the muscle walls open and clench with the heartbeat sound, and clench on you if you're caught inside.
   - **Acid:** the valves raise stepping stones for 10 seconds, and they bubble before they sink.
   - **Cysts:** the blue gland gives one antibody at a time; each cyst needs one. The cysts keep spawning parasites until cured.
   - **Cilia:** they fold away a moment after you step on them. Fall into the pit and she heaves you back up.
4. Waves: a boss bar shows "Wave n of 5". Mobs come in two groups, and later waves have more parasites. Wave 5 has a big Bloated Host.
   **(client)** Do the infested zombie, skeleton and creeper skins sit right on the bodies? Do the tumours follow the body and head, and do the violet eyes glow?
5. Die inside and you wake up outside beside her, with everything you carried. If everyone leaves mid-wave, the wave starts over next time.
6. Touching the heart blesses everyone in the room, puts them outside, and sends her rising away, healed.

## The reward

1. **(client)** A row of violet hearts sits above your health, and the armour bar moves up a row. It takes damage before your health does, and refills by half a heart every 20 seconds once you've been unhurt for 10 seconds.
2. The egg hatches the calf where you use it. She follows you and gives you Absorption while you're within 10 blocks. She grows over three in-game days to horse size. Crouch-use her to make her stay or follow.
3. If the calf is killed she drops her egg again.
4. A second healing gives nothing to players who already have the bar and the egg.

## Config (harmontide-common.toml)

- `dreamLeviathan` (on)
- `leviathanFirstDay` (3)
- `leviathanDays` (3)
