#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
# Windows Java needs ';' as classpath separator; Unix uses ':'.
case "$(uname -s 2>/dev/null || echo unknown)" in
    MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
    *) SEP=":" ;;
esac
rm -rf out
mkdir -p out
find src -name "*.java" > .sources.txt
javac -d out -cp "lib/*" @.sources.txt
rm -f .sources.txt
# Copy vendored fonts (if present) so FontLoader can resolve them from classpath.
if [ -d lib/fonts ]; then
    mkdir -p out/fonts
    cp lib/fonts/*.ttf out/fonts/ 2>/dev/null || true
fi
echo "Build OK -> out/  (cp separator: '$SEP')"
