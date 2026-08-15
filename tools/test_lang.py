#!/usr/bin/env python3
"""Test language: keyword search + posted date format."""
import io
import re
import sys
import urllib.request

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
proxy = urllib.request.ProxyHandler({"http": "http://127.0.0.1:7890", "https": "http://127.0.0.1:7890"})
opener = urllib.request.build_opener(proxy)


def fetch(url):
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0.0.0"})
    return opener.open(req, timeout=25).read().decode("utf-8", "replace")


h = fetch("https://e-hentai.org/?f_search=language%3Achinese")
gids = re.findall(r"https://e-hentai.org/g/(\d+)/", h)
langs = re.findall(r"<td class=\"gdt1\">Language:</td><td class=\"gdt2\">([^<]*)</td>", h)
print("language:chinese gids:", gids[:5])
print("rows:", len(gids))
# check each gallery's language via detail fetch for first 3
for g in gids[:3]:
    try:
        d = fetch(f"https://e-hentai.org/g/{g}/")
        m = re.search(r"<td class=\"gdt1\">Language:</td><td class=\"gdt2\">([^<]*)</td>", d)
        print(f"  gallery {g} lang:", m.group(1) if m else "?")
    except Exception as e:
        print(f"  gallery {g} FAIL {e}")
h2 = fetch("https://e-hentai.org/?f_search=test")
posted = re.findall(r'id="posted_(\d+)"[^>]*>([^<]+)</div>', h2)
print("posted samples:", posted[:4])
