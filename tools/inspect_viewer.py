#!/usr/bin/env python3
"""Inspect a viewer (single image) page."""
import re
import sys


def main(path):
    html = open(path, encoding="utf-8", errors="replace").read()
    print(f"== {path} len={len(html)}")
    m = re.search(r'<img[^>]*id="img"[^>]*>', html)
    print("img#img:", (m.group(0)[:400] if m else "NOT FOUND"))
    for pat in [r'imgurl\s*=\s*"([^"]+)"', r'"imgurl"\s*:\s*"([^"]+)"', r"var\s+imgurl\s*=\s*'([^']+)'"]:
        m = re.search(pat, html)
        print(f"imgurl regex {pat[:30]}:", (m.group(1)[:120] if m else "no"))
    for pat in ["showkey", "original", "fullimg", "nl('", "nl(\"", "next", "prev"]:
        i = html.find(pat)
        print(f"{pat!r} at {i}:", html[max(0, i - 60): i + 160].replace("\n", " ") if i >= 0 else "")
    # all <script> blocks
    for m in re.finditer(r"<script[^>]*>(.*?)</script>", html, re.S):
        txt = m.group(1).strip()
        if txt:
            print("SCRIPT:", txt[:600].replace("\n", " "))


if __name__ == "__main__":
    main(sys.argv[1])
