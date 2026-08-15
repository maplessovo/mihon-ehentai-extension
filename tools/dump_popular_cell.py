#!/usr/bin/env python3
"""Dump title cell + cover img from popular page."""
import io
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
html = open("popular.html", encoding="utf-8", errors="replace").read()
# find a gl3c cell
i = html.find("glink")
print("== around first glink ==")
print(html[max(0, i - 900): i + 300])
print()
# cover with data-src
m = re.search(r'<img[^>]*data-src="[^"]*"[^>]*>', html)
print("== lazy img with data-src ==")
print(m.group(0)[:400] if m else "none")
