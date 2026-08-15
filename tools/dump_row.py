#!/usr/bin/env python3
"""Dump a specific <tr> row fully from a list page."""
import re
import sys


def main(path, idx):
    html = open(path, encoding="utf-8", errors="replace").read()
    trs = list(re.finditer(r"<tr[^>]*>", html))
    start = trs[idx].start()
    end = html.find("</tr>", start)
    print(html[start:end])


if __name__ == "__main__":
    main(sys.argv[1], int(sys.argv[2]))
