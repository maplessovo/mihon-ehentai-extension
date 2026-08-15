#!/usr/bin/env python3
"""Fetch helper: downloads URLs through the local Clash proxy (127.0.0.1:7890)."""
import sys
import urllib.request

PROXY = "http://127.0.0.1:7890"


def opener():
    proxy = urllib.request.ProxyHandler({"http": PROXY, "https": PROXY})
    return urllib.request.build_opener(proxy)


def fetch(url, timeout=30):
    req = urllib.request.Request(
        url,
        headers={
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
            "Accept": "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8",
            "Accept-Language": "en-US,en;q=0.9",
        },
    )
    return opener().open(req, timeout=timeout)


if __name__ == "__main__":
    url = sys.argv[1]
    out = sys.argv[2] if len(sys.argv) > 2 else None
    resp = fetch(url)
    data = resp.read()
    if out:
        with open(out, "wb") as f:
            f.write(data)
        print(f"saved {len(data)} bytes -> {out}")
    else:
        sys.stdout.buffer.write(data)
