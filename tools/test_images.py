#!/usr/bin/env python3
"""Test image URL fetching (standard + original) with Referer."""
import io
import re
import sys
import urllib.request

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
proxy = urllib.request.ProxyHandler({"http": "http://127.0.0.1:7890", "https": "http://127.0.0.1:7890"})
opener = urllib.request.build_opener(proxy)

VIEWER = "https://e-hentai.org/s/82559c457b/4120392-1"
html = open("viewer.html", encoding="utf-8", errors="replace").read()
m = re.search(r'<img id="img" src="([^"]+)"', html)
std = m.group(1) if m else None
print("standard img:", std)
m = re.search(r'href="(https://e-hentai.org/fullimg/[^"]+)"', html)
full = m.group(1) if m else None
print("fullimg href:", full)

for name, url in [("std", std), ("fullimg", full)]:
    if not url:
        continue
    req = urllib.request.Request(
        url,
        headers={
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0.0.0",
            "Referer": VIEWER,
        },
    )
    try:
        resp = opener.open(req, timeout=30)
        data = resp.read(200)
        print(f"[{name}] status={resp.status} type={resp.headers.get('Content-Type')} first={data[:16]!r} final_url={resp.geturl()[:120]}")
    except Exception as e:
        print(f"[{name}] FAIL {type(e).__name__}: {str(e)[:160]}")
