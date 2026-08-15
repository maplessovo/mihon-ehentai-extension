#!/usr/bin/env python3
"""List external script/link srcs in an HTML file."""
import re
import sys


def main(path):
    html = open(path, encoding="utf-8", errors="replace").read()
    for m in re.finditer(r'<script[^>]*\bsrc="([^"]+)"', html):
        print("script:", m.group(1))
    for m in re.finditer(r'<link[^>]*\bhref="([^"]+\.js[^"]*)"', html):
        print("link:", m.group(1))


if __name__ == "__main__":
    main(sys.argv[1])
