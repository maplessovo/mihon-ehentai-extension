#!/usr/bin/env python3
"""Decode a 16x16 32bpp ICO BMP frame to PNG."""
import struct
import sys
import zlib


def main(path):
    data = open(path, "rb").read()
    count = struct.unpack("<H", data[4:6])[0]
    w, h, colors, r, planes, bpp, size, offset = struct.unpack("<BBBBHHII", data[6:22])
    frame = data[offset : offset + size]
    bi_size = struct.unpack("<I", frame[:4])[0]
    print(f"frame {w}x{h} bpp={bpp} biSize={bi_size} frameLen={len(frame)}")
    if bi_size == 40:
        fw, fh, planes, bpp = struct.unpack("<iiHH", frame[4:16])
        # ICO directory height already includes the AND mask (h*2); the real
        # image height is fw's counterpart in the directory entry (16x16 here).
        real_h = fw if fh == fw * 2 else fh
        print(f"BITMAPINFOHEADER: {fw}x{fh} planes={planes} bpp={bpp} real_h={real_h}")
        row_size = ((fw * bpp + 31) // 32) * 4
        px = frame[40 : 40 + row_size * real_h]
        rows = [px[y * row_size : (y + 1) * row_size] for y in range(real_h)]
        rows.reverse()  # bottom-up -> top-down
        raw = b"".join(rows)
        fh = real_h
    else:
        raise SystemExit(f"unsupported biSize {bi_size}")
    # Build PNG (RGBA)
    png = build_png(fw, fh, raw)
    out = path + ".decoded.png"
    open(out, "wb").write(png)
    print("saved", out)


def build_png(width, height, rgba):
    def chunk(tag, payload):
        c = struct.pack(">I", len(payload)) + tag + payload
        return c + struct.pack(">I", zlib.crc32(tag + payload) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    raw = b"".join(b"\x00" + rgba[y * width * 4 : (y + 1) * width * 4] for y in range(height))
    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr)
        + chunk(b"IDAT", zlib.compress(raw))
        + chunk(b"IEND", b"")
    )


if __name__ == "__main__":
    main(sys.argv[1])
