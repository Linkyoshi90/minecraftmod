"""One colour to a ramp of n shades, dark to light, for the harpy recolour modules."""


def shade(col, n=5):
    r, g, b = (int(col[i:i + 2], 16) for i in (1, 3, 5))
    out = []
    for i in range(n):
        t = i / (n - 1)
        if t < 0.6:
            k = 0.35 + 0.65 * t / 0.6
            out.append((int(r * k), int(g * k), int(b * k)))
        else:
            k = (t - 0.6) / 0.4 * 0.8
            out.append((int(r + (255 - r) * k), int(g + (255 - g) * k), int(b + (255 - b) * k)))
    return out
