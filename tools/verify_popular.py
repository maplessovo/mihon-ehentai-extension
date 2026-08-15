#!/usr/bin/env python3
"""Check popular page row/cover structure."""
import io
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
html = open("popular.html", encoding="utf-8", errors="replace").read()
print("tr count:", len(re.findall(r"<tr>", html)))
print("gl1c glcat:", bool(re.search(r'<td class="gl1c glcat">', html)))
print("gl3c glname:", bool(re.search(r'<td class="gl3c glname">', html)))
print("gl2c:", bool(re.search(r'<td class="gl2c">', html)))
print("gl4c:", bool(re.search(r'<td class="gl4c', html)))
imgs = re.findall(r"<img[^>]*>", html)
for i in imgs[:4]:
    print("IMG:", i[:260])
m = re.search(r'var nexturl="([^"]*)"', html)
print("nexturl:", m.group(1) if m else None)
m = re.search(r'var prevurl="([^"]*)"', html)
print("prevurl:", m.group(1) if m else None)
m = re.search(r'<div class="glink">([^<]+)</div>', html)
print("first glink:", m.group(1)[:80] if m else None)
