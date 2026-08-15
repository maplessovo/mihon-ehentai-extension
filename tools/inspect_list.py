#!/usr/bin/env python3
"""Inspect e-hentai HTML structure (list page)."""
import re
import sys


def main(path):
    html = open(path, encoding="utf-8", errors="replace").read()
    print(f"== {path}: len={len(html)}")
    m = re.search(r'<table[^>]*class="[^"]*itg[^"]*"', html)
    print("itg table:", m.group(0) if m else "NOT FOUND")
    trs = re.findall(r'<tr class="gtr"[^>]*>', html)
    print("tr.gtr count:", len(trs))
    if trs:
        print("first gtr tag:", trs[0])
    gl = re.search(r'<div class="glink">.*?</div>', html, re.S)
    print("glink:", (gl.group(0)[:400] if gl else "NOT FOUND"))
    cov = re.search(r'<td class="gl1c">.*?</td>', html, re.S)
    print("gl1c:", (cov.group(0)[:400] if cov else "NOT FOUND"))
    gldt = re.search(r'<div class="gldt">.*?</div>', html, re.S)
    print("gldt:", (gldt.group(0)[:200] if gldt else "NOT FOUND"))
    ptb = re.search(r'<[^>]+class="ptb"', html)
    print("ptb:", ptb.group(0) if ptb else "NOT FOUND")
    pages = sorted(set(int(p) for p in re.findall(r'[?&]page=(\d+)', html)))
    print("page= values:", pages[:15])
    # anchors with page=
    links = re.findall(r'<a [^>]*href="([^"]*page=\d+[^"]*)"[^>]*>', html)
    print("page links:", links[:10])


if __name__ == "__main__":
    main(sys.argv[1])
