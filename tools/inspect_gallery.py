#!/usr/bin/env python3
"""Inspect a gallery detail page."""
import re
import sys


def main(path):
    html = open(path, encoding="utf-8", errors="replace").read()
    print(f"== {path} len={len(html)}")
    for sel in ["#gn", "gj", "gd1", "gdd", "taglist", "gd2", "gdt", "gdtm", "cover"]:
        m = re.search(r'id="' + sel + '"', html)
        print(f"id#{sel}:", "FOUND" if m else "missing")
    m = re.search(r'<h1[^>]*id="gn"[^>]*>(.*?)</h1>', html, re.S)
    print("gn:", m.group(1)[:120] if m else "NOT FOUND")
    m = re.search(r'<h1[^>]*id="gj"[^>]*>(.*?)</h1>', html, re.S)
    print("gj:", m.group(1)[:120] if m else "NOT FOUND")
    m = re.search(r'<div[^>]*id="gd1"[^>]*>.*?</div>', html, re.S)
    print("gd1:", (m.group(0)[:300] if m else "NOT FOUND"))
    m = re.search(r'<div[^>]*id="gdd"[^>]*>(.*?)</div>\s*</div>', html, re.S)
    print("gdd:", (m.group(1)[:600] if m else "NOT FOUND"))
    m = re.search(r'<div[^>]*id="taglist"[^>]*>', html)
    print("taglist tag:", m.group(0) if m else "NOT FOUND")
    m = re.search(r'<div[^>]*id="gd2"[^>]*>(.*?)</div>', html, re.S)
    print("gd2:", (m.group(1)[:200] if m else "NOT FOUND"))
    m = re.search(r'id="gdt"[^>]*>', html)
    print("gdt tag:", m.group(0) if m else "NOT FOUND")
    gdtms = re.findall(r'class="gdtm"', html)
    print("gdtm count:", len(gdtms))
    pgs = re.findall(r'[?&]p=(\d+)', html)
    print("p= params:", sorted(set(int(x) for x in pgs)))
    m = re.search(r'<div[^>]*class="gtb"[^>]*>.*?</div>', html, re.S)
    print("gtb:", (m.group(0)[:400] if m else "NOT FOUND"))
    # Length / Posted metadata
    for key in ["Length", "Posted", "Uploader", "Category", "Rating"]:
        i = html.find(key)
        print(f"meta {key} at:", i, html[i:i+120].replace("\n", " ") if i >= 0 else "")


if __name__ == "__main__":
    main(sys.argv[1])
