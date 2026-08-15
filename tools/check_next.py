#!/usr/bin/env python3
"""Check nexturl preserves filter params."""
import io
import re
import sys
import urllib.request

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
proxy = urllib.request.ProxyHandler({"http": "http://127.0.0.1:7890", "https": "http://127.0.0.1:7890"})
opener = urllib.request.build_opener(proxy)
req = urllib.request.Request(
    "https://e-hentai.org/?f_search=original&f_srdd=5&f_cats=1019&f_spf=50&f_spt=200",
    headers={"User-Agent": "Mozilla/5.0"},
)
h = opener.open(req, timeout=25).read().decode("utf-8", "replace")
m = re.search(r'var nexturl="([^"]*)"', h)
print("nexturl:", m.group(1) if m else None)
m = re.search(r'var prevurl="([^"]*)"', h)
print("prevurl:", m.group(1) if m else None)
