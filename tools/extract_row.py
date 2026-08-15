#!/usr/bin/env python3
"""Extract a full <tr> row from a list page and show pagination area."""
import re
import sys


def main(path, row_idx=0):
    html = open(path, encoding="utf-8", errors="replace").read()
    trs = list(re.finditer(r"<tr[^>]*>", html))
    print(f"total <tr>: {len(trs)}")
    # print class of each tr
    for i, m in enumerate(trs[:8]):
        print(f"tr[{i}]:", m.group(0))
    start = trs[row_idx].start()
    # find matching </tr>
    end = html.find("</tr>", start)
    row = html[start:end]
    print("=" * 40)
    print(row[:2500])
    print("=" * 40)
    # pagination: search for 'page=' links
    pages = re.findall(r'href="([^"]*page=\d+[^"]*)"', html)
    print("pagination links:", pages[:12])
    # search for next/prev
    for pat in ["ptb", "pagination", "next", "prev", ">Next<", "page_next"]:
        idx = html.find(pat)
        if idx >= 0:
            print(f"found {pat!r} at {idx}:", html[max(0, idx - 100): idx + 200].replace("\n", " "))
            break


if __name__ == "__main__":
    main(sys.argv[1])
