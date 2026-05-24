#!/usr/bin/env sh
set -eu

rm -rf out
mkdir -p out
javac -d out src/*.java
