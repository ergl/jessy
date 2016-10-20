#!/usr/bin/env bash

# Sets up jessy on the different machines

# Abort on error or null globs
set -e
shopt -s nullglob

target_path=(~/jessy/) # Bash arrays auto-expand tilde (~) globbing
[[ -d ${target_path} ]] && rm -rf ${target_path}
git clone --depth 1 https://github.com/ergl/jessy.git -b provision ${target_path}
