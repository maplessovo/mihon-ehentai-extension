#!/usr/bin/env python3
"""Generate the adaptive-icon foreground PNG from the e-hentai 16x16 favicon.

Renders the favicon glyph (indigo on transparent) at 14x scale, centered on a
432x432 canvas (108dp @ 4x). The adaptive icon background is white, matching
the favicon's own background.
"""
from PIL import Image

SRC = "eh_favicon.ico.decoded.png"
OUT = "../ehentai/src/main/res/drawable/ic_launcher_foreground.png"
CANVAS = 432  # 108dp * 4
SCALE = 14

img = Image.open(SRC).convert("RGBA")
print("source:", img.size)

# Isolate the glyph: favicon pixels are either white (bg) or indigo (ink).
px = img.load()
glyph = Image.new("RGBA", img.size, (0, 0, 0, 0))
gp = glyph.load()
for y in range(img.height):
    for x in range(img.width):
        r, g, b, a = px[x, y]
        if a > 0 and not (r > 200 and g > 200 and b > 200):
            gp[x, y] = (r, g, b, 255)

# Render glyph at SCALE with nearest neighbor, centered on the canvas.
glyph_big = glyph.resize((img.width * SCALE, img.height * SCALE), Image.NEAREST)
canvas = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
x0 = (CANVAS - glyph_big.width) // 2
y0 = (CANVAS - glyph_big.height) // 2
canvas.paste(glyph_big, (x0, y0), glyph_big)

canvas.save(OUT)
print("saved", OUT, canvas.size)

# Sanity: ink bounding box on the final canvas (must stay inside the 66/108 safe circle)
bbox = canvas.getbbox()
print("ink bbox:", bbox)
cx = cy = CANVAS / 2
safe_radius = CANVAS * 66 / 108 / 2
print("safe radius:", safe_radius)
