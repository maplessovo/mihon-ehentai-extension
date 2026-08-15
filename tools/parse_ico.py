#!/usr/bin/env python3
"""Parse an ICO file and extract frames."""
import struct
import sys


def main(path):
    data = open(path, "rb").read()
    reserved, typ, count = struct.unpack("<HHH", data[:6])
    print(f"reserved={reserved} type={typ} count={count}")
    pos = 6
    for i in range(count):
        w, h, colors, r, planes, bpp, size, offset = struct.unpack("<BBBBHHII", data[pos : pos + 16])
        is_png = data[offset : offset + 4] == b"\x89PNG"
        dib = struct.unpack("<I", data[offset + 4 : offset + 8])[0] if size > 8 and not is_png else 0
        print(f"frame {i}: {w}x{h} colors={colors} bpp={bpp} size={size} off={offset} png={is_png} dib={dib}")
        if is_png:
            out = path + f".{w}x{h}.png"
            open(out, "wb").write(data[offset : offset + size])
            print("  -> extracted", out)
        pos += 16


if __name__ == "__main__":
    main(sys.argv[1])
