#!/usr/bin/env sh
set -eu

scripts/compile.sh
java -cp out Main --self-test
