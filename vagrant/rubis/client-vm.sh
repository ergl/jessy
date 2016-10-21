#!/usr/bin/env bash

set -e

client_status=`vagrant status | awk '/^client/ {print$2}'`
if [[ ${client_status} == 'running' ]]; then
    vagrant reload client
else
    vagrant up client
fi

vagrant ssh client
