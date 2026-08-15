#!/usr/bin/env python3
"""Test e-hentai search filter params empirically."""
import io
import re
import sys
import urllib.request

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
PROXY = "http://127.0.0.1:7890"
proxy = urllib.request.ProxyHandler({"http": PROXY, "https": PROXY})
opener = urllib.request.build_opener(proxy)


def fetch(url):
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0.0.0"})
    return opener.open(req, timeout=25).read().decode("utf-8", "replace")


def cats(html):
    return [m for m in re.findall(r'<td class="gl1c glcat">.*?<div class="cn[^"]*"[^>]*>([^<]+)</div>', html)]


def pages(html):
    return re.findall(r"<div>(\d+) pages</div>", html)


def nexturl(html):
    m = re.search(r'var nexturl="([^"]*)"', html)
    return m.group(1) if m else None


tests = [
    ("cats=2 (Doujinshi)", "https://e-hentai.org/?f_search=original&f_cats=2"),
    ("cats=4 (Manga)", "https://e-hentai.org/?f_search=original&f_cats=4"),
    ("cats=1 (Misc)", "https://e-hentai.org/?f_search=original&f_cats=1"),
    ("srdd=5", "https://e-hentai.org/?f_search=original&f_srdd=5"),
    ("spf=5&spt=10", "https://e-hentai.org/?f_search=original&f_spf=5&f_spt=10"),
    ("spf=100&spt=200", "https://e-hentai.org/?f_search=original&f_spf=100&f_spt=200"),
    ("f_sname=on", "https://e-hentai.org/?f_search=original&f_sname=on"),
    ("front page", "https://e-hentai.org/"),
    ("front page page=1", "https://e-hentai.org/?page=1"),
]
for name, url in tests:
    try:
        html = fetch(url)
        c = cats(html)
        p = pages(html)
        nu = nexturl(html)
        print(f"[{name}] rows={len(c)} cats={c[:6]} pages_sample={p[:4]} nexturl={'Y' if nu else 'N'}")
    except Exception as e:
        print(f"[{name}] FAIL {e}")
