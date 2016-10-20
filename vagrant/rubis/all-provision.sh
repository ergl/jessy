#!/usr/bin/env bash

set -e

if [[ $# -eq 1 && $1 == "-a" ]]; then
    ./server-provision.sh -d
    ./client-provision.sh -d
elif [[ $# -eq 1 && $1 == "-s" ]]; then
    ./server-provision.sh -d
    ./client-provision.sh
elif [[ $# -eq 1 && $1 == "-c" ]]; then
    ./server-provision.sh
    ./client-provision.sh -d
else
    ./server-provision.sh
    ./client-provision.sh
fi
