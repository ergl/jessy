#!/usr/bin/env bash

set -e

server_status=`vagrant status | awk '/^server/ {print$2}'`
if [[ ${server_status} == 'running' ]]; then
    vagrant reload server
else
    vagrant up server
fi

vagrant ssh server