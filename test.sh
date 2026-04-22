#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
case "$(uname -s 2>/dev/null || echo unknown)" in
    MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
    *) SEP=":" ;;
esac
rm -rf out-test
mkdir -p out-test
find test -name "*.java" > .test-sources.txt
javac -d out-test -cp "out${SEP}lib/*" @.test-sources.txt
rm -f .test-sources.txt
java -jar lib/junit-platform-console-standalone-1.10.2.jar \
    -cp "out${SEP}out-test" --scan-classpath --details=tree
