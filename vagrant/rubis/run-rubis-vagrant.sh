#!/usr/bin/env bash

set -e

status=`vagrant status | sed '3q;d' | awk '{ print $2 }'`
vscript="vagrant"
case ${status} in
  'running' )
    vscript+=" reload"
    ;;
  'poweroff' | 'not' )
    vscript+=" up"
    ;;
  * )
    echo -e "Status unknown"
    exit 1
esac

if [[ $# -eq 1 ]]; then
    DEBUG_MACHINE=$1 ${vscript}
else
    ${vscript}
fi
