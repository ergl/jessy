#!/usr/bin/env bash

set -e

vscript="vagrant"
server_status=`vagrant status | awk '/^server/ {print$2}'`

if [[ ${server_status} == 'running' ]]; then
    vscript+=" reload server"
else
    vscript+=" up server"
fi

if [[ $# -eq 1 && $1 == "-d" ]]; then
    DEBUG_MACHINE="-s" ${vscript}
else
    ${vscript}
fi
