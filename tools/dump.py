#!/usr/bin/env python3
"""Dump raw HTML around a marker."""
import re
import sys


def main(path, marker, before=1200, after=600, count=2):
    html = open(path, encoding="utf-8", errors="replace").read()
    out = []
    pos = 0
    for _ in range(count):
        j = html.find(marker, pos)
        if j < 0:
            out.append(f"marker {marker!r} not found after {pos}")
            break
        out.append("=" * 30)
        out.append(html[max(0, j - before): j + after])
        pos = j + len(marker)
    print("\n".join(out))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
