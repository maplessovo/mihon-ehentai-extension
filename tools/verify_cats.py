#!/usr/bin/env python3
"""Verify exclusion mask and param effects."""
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


def cats(html):
    return re.findall(r'<td class="gl1c glcat">.*?<div class="cn[^"]*"[^>]*>([^<]+)</div>', html)


def gids(html):
    return re.findall(r"https://e-hentai.org/g/(\d+)/", html)[:5]


h = fetch("https://e-hentai.org/?f_search=original&f_cats=1021")
print("f_cats=1021 (only Doujinshi):", cats(h)[:8])
h = fetch("https://e-hentai.org/?f_search=original&f_cats=1019")
print("f_cats=1019 (only Manga):", cats(h)[:8])
h = fetch("https://e-hentai.org/?f_search=original&f_cats=0")
print("f_cats=0:", cats(h)[:8], gids(h))
h = fetch("https://e-hentai.org/?f_search=original")
print("baseline:", gids(h))
h = fetch("https://e-hentai.org/?f_search=original&f_sname=on")
print("f_sname=on:", gids(h))
h = fetch("https://e-hentai.org/?f_search=original&f_srdd=5")
print("f_srdd=5:", gids(h))
h = fetch("https://e-hentai.org/popular")
m = re.search(r'var nexturl="([^"]*)"', h)
print("popular nexturl:", m.group(1) if m else None)
m = re.search(r'var prevurl="([^"]*)"', h)
print("popular prevurl:", m.group(1) if m else None)
