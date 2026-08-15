#!/usr/bin/env python3
"""Extract JUnit XML failure messages."""
import glob
import re
import sys

for f in glob.glob("ehentai/build/test-results/testDebugUnitTest/*.xml"):
    data = open(f, encoding="utf-8").read()
    for m in re.finditer(r'<testcase name="([^"]+)"[^>]*>\s*<failure[^>]*message="([^"]*)"', data):
        msg = m.group(2)[:250].replace("&#10;", " | ").replace("&quot;", '"').replace("&lt;", "<")
        print(m.group(1), "->", msg)
