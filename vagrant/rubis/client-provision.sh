#!/usr/bin/env bash

set -e

vscript="vagrant"
client_status=`vagrant status | awk '/^client/ {print$2}'`

if [[ ${client_status} == 'running' ]]; then
    vscript+=" reload client"
else
    vscript+=" up client"
fi

if [[ $# -eq 1 && $1 == "-d" ]]; then
    DEBUG_MACHINE="-c" ${vscript}
else
    ${vscript}
fi
