#!/usr/bin/env python3
"""Verify gallery p=1 page thumbnails + ptt pagination."""
import io
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
html = open("gallery_p1.html", encoding="utf-8", errors="replace").read()
m = re.search(r'<p class="gpc">[^<]*</p>', html)
print("gpc:", m.group(0) if m else None)
gdt = re.search(r'<div id="gdt"[^>]*>(.*?)</div>\s*<div class="c">', html, re.S)
if gdt:
    links = re.findall(r'<a href="(https://e-hentai.org/s/[^"]+)"', gdt.group(1))
    print("thumb links:", len(links), links[:2], links[-1:] if links else "")
ptt = re.findall(r'<td[^>]*><a href="([^"]*p=\d+[^"]*)"[^>]*>(\d+)</a>', html)
print("ptt links:", ptt)
# also check a gallery WITHOUT ?p= (first page) for the >20 case: gtb present?
