#!/usr/bin/env bash

# Configures a myfractal.xml file for servers and clients

# Abort on error or null globs
set -e
shopt -s nullglob

# Bash arrays auto-expand tilde (~) globbing
target_path=(~/jessy/)
cd ${target_path}

# Remove old network configuration
[[ -f script/myfractal.xml ]] && rm script/myfractal.xml

if [[ $# -eq 1 && "$1" == "server" ]]; then
    # If we're in server mode (limit to only 1 server),
    # get the current ip address
    locip=`ifconfig eth0 \
        | grep 'inet addr:' \
        | cut -d: -f2 \
        | awk '{print $1}'`

    # Remove old shared net config
    [[ -f /srv/myfractal.xml ]] && rm /srv/myfractal.xml

    # Generate a new fractal configuration, using our ip address
    echo '<?xml version="1.0" encoding="ISO-8859-1" ?>' >> script/myfractal.xml
    echo '<FRACTAL>'  >> script/myfractal.xml
    echo '<BootstrapIdentity>' >> script/myfractal.xml
    echo '<nodelist>' >> script/myfractal.xml
    echo '<node id="0" ip="'${locip}'"/>' >> script/myfractal.xml
    echo '</nodelist>' >> script/myfractal.xml
    echo '</BootstrapIdentity>' >> script/myfractal.xml
    echo '</FRACTAL>' >> script/myfractal.xml

    # Copy fractal configuration to current machine
    cp script/myfractal.xml /srv
else
    # If in client mode, get the shared configuration and copy it
    # to the current machine. If no server was started, abort.
    [[ ! -f /srv/myfractal.xml ]] && echo -e "No fractal file present" && exit 1
    cp /srv/myfractal.xml script/
fi
