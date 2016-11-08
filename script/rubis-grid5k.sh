#!/usr/bin/env bash

set -e
trap "stopExecution" SIGINT SIGTERM

rm -f *.fr
running_on_grid=true

self=${0##*/}
export param=("$@")

if [ ${#} -lt 3 ]; then
  echo -e 'Usage:' ${self} '<site-name> <n-clients> <n-servers>'
  echo 'Additionally, add more triples of site name, number of clients and numner of servers'
  exit
fi

# As the format is <site> <nclient> <nserver> ...
# $# / 3 gives us the number of clusters
export clustersNumber=$(($# / 3))

# Run on ^C
function stopExecution {
  echo "${self}: stopping. Deleting jobs..."
  oargriddel ${RES_ID}
  echo "${self}: done"
  exit
}

# Change the path to the rubis config file in all other scripts
# s|foo|bar|g is the same as s/foo/bar/g. Use | to avoid escaping '/'
function changeScriptPaths {
  local path=$(pwd)
  local categories=${path}"/rubis/db/categories.txt"
  local regions=${path}"/rubis/db/regions.txt"
  local transitions=${path}"/rubis/transitions/default_transitions_15.txt"

  sed -i.bak -e "s|^scriptdir=.*|scriptdir=$path|g" rubis-configuration.sh

  sed -i.bak -e "s|^source.*|source $path/rubis-configuration.sh|g" clauncher.sh
  sed -i.bak -e "s|^source.*|source $path/rubis-configuration.sh|g" console.sh
  sed -i.bak -e "s|^source.*|source $path/rubis-configuration.sh|g" client.sh
  sed -i.bak -e "s|^source.*|source $path/rubis-configuration.sh|g" experience.sh
  sed -i.bak -e "s|^source.*|source $path/rubis-configuration.sh|g" jessy.sh
  sed -i.bak -e "s|^source.*|source $path/rubis-configuration.sh|g" launcher.sh

  sed -i.bak -e "s|^source.*|source $path/rubis-configuration.sh|g" rubis-init.sh
  sed -i.bak -e "s|^source.*|source $path/rubis-configuration.sh|g" rubis-client.sh
  sed -i.bak -e "s|^source.*|source $path/rubis-configuration.sh|g" rubis-server.sh

  sed -i.bak -e "s|^categories_file =.*|categories_file = ${categories}|g" rubis.properties
  sed -i.bak -e "s|^regions_file =.*|regions_file = ${categories}|g" rubis.properties
  sed -i.bak -e "s|^transitions_file =.*|transitions_file = ${categories}|g" rubis.properties
}

# Build the reservation arguments using the named sites
function buildReservation {
  local next=0
  local clusters=()
  reservation=""

  for i in `seq 0 $((${clustersNumber}-1))`; do
    # Name of the cluster
    clusters[$i]=${param[$next]}

    # Number of nodes = clients + servers
    node_number=$((${param[$next+1]}+${param[$next+2]}))

    # Build the reservation parameters
    # The format is different when using oargridsub, format:
    # clusterAlias1:rdef="/nodes=1/core=2":name=nomJob:type="time‐sharing":prop="hostname = 'node1'",
    # clusterAlias2:rdef="/nodes=2":name=nomJob2:prop="switch = 'sw3'", ...
    reservation="$reservation ${clusters[$i]}:rdef=/nodes=$node_number/core=4,"

    # If there are more, do the same for more sites
    next=$(($next+3))
  done

  # Trim the last (,) in the string
  reservation=${reservation%?}

  echo "Reserving at sites:"
  printf '\t%s' "${clusters[@]}"
  echo ""
}

# Tries to actually reserve the nodes
function reserveNodes {
  local reservation_failure=true;
  local increment=0
  local next=$(date '+%Y-%m-%d %H:%M:%S')

  while [[ ${reservation_failure} == "true" ]]; do
    echo "Trying to reserve nodes at...${next}"
    echo "With args${reservation}"
    # Try to reserve for 30 minutes, starting now
    oargridsub -w '0:30:00' ${reservation} -s "$next" > tmp

    # Outputs something similar to:
    # [OAR_GRIDSUB] [nancy] Date/TZ adjustment: 0 seconds
    # [OAR_GRIDSUB] [nancy] Reservation success on nancy : batchId = 1091121
    # [OAR_GRIDSUB] Grid reservation id = 56670
    # [OAR_GRIDSUB] SSH KEY : /tmp/oargrid/oargrid_ssh_key_(...)_56670
    #     You can use this key to connect directly to your OAR nodes with the oar user.

    # Retrieving the grid reservation ID
    RES_ID=$(grep "Grid reservation id" tmp | cut -f2 -d=)

    # Retrieve the ssh key file path
    OAR_JOB_KEY_PATH=$(grep "SSH KEY" tmp | cut -b 25-)

    # If no ID is found, command failed, try again
    if [[ -z "$RES_ID" ]]; then
      increment=$(($increment+30))
      next=$(date '+%Y-%m-%d %H:%M:%S' --date=' +'${increment}' minutes')
      # Replace all the occurences of \"\ with emtpy string
      next=${next//\"/}
      # Replace all the occurences of \'\ with empty string
      next=${next//\'/}
      echo "Clusters unavailable now, trying to reserve in ${increment} minutes"
      echo ""
    else
      # If ID is found, sleep until reservation is ready
      now=$(date +%s)
      next=$(date -d "$next" +%s)
      next=${next//\"/}
      next=${next//\'/}
      time_to_wait=$(($next - $now))
      minutes=$(($time_to_wait / 60))

      # Sleep only if time is positive
      if [[ ${time_to_wait} -ge 0 ]]; then
          echo "I will sleep for:" ${minutes} "minutes"
          sleep ${time_to_wait}
      fi

      reservation_failure=false
    fi
  done

  export OAR_JOB_KEY_FILE=${OAR_JOB_KEY_PATH}
  echo 'Exported oarJobKeyFile ' ${OAR_JOB_KEY_PATH}

  echo "Reservations completed successfully"
}

function buildFractal {
  local reservation_id=${RES_ID}
  rm myfractal.xml

  echo '<?xml version="1.0" encoding="ISO-8859-1" ?>' >> myfractal.xml
  echo '<FRACTAL>'  >> myfractal.xml
  echo '<BootstrapIdentity>' >> myfractal.xml
  echo '<nodelist>' >> myfractal.xml

  echo 'Grid reservation id: ' ${reservation_id}

  local nodeStr=''
  local servers=''
  local clients=''
  local nodes=''

  local j=0
  local next=0
  for i in `seq 0 $((${clustersNumber}-1))`; do
    nodeName=${param[$next]}
    serverNumber=${param[$next+1]}
    clientNumber=${param[$next+2]}

    echo ""
    echo "**********************"
    echo "* deploy on "${nodeName}" *"
    echo "**********************"
    echo "server(s): "${serverNumber}
    echo "client(s): "${clientNumber}
    echo ""

    oargridstat -w -l ${RES_ID} -c ${nodeName} | sed '/^$/d' | sort | uniq > ./machines

    local k=0
    local next=$(($next+3))
    while read line; do
      host ${line} > tmp
      local name=$(cut tmp -f1 -d ' ')
      local ip=$(cut tmp -f4 -d ' ')

      nodes="$nodes \"$name\""

      if [[ ${k} -lt ${serverNumber} ]]; then
        echo 'server: '${name}
        echo '<node id="'${j}'" ip="'${ip}'"/>' >> myfractal.xml
        servers="$servers \"$name\""
      else
        echo 'client: '${name}
        clients="$clients \"$name\""
      fi

      j=$((j+1))
      k=$((k+1))
    done < machines
  done

  echo '</nodelist>' >> myfractal.xml
  echo '</BootstrapIdentity>' >> myfractal.xml
  echo '</FRACTAL>' >> myfractal.xml

  echo ""

  nodeStr="nodes=("${nodes}")"
  servers="servers=("${servers}")"
  clients="clients=("${clients}")"

  # Shuffle the ips around
  ./shufflemyfractal.sh

  echo "Fractal configuration file is done"

  sed -i.bak -e "s|nodes=.*|${nodeStr}|g" rubis-configuration.sh
  sed -i.bak -e "s|servers=.*|${servers}|g" rubis-configuration.sh
  sed -i.bak -e "s|clients=.*|${clients}|g" rubis-configuration.sh

  rm machines tmp

  echo "rubis-configuration.sh file is done"
}

function syncCode {
  echo 'Synchronizing keys and data...'
  local next=0

  # Sync with all the provided sites
  for i in `seq 0 $((${clustersNumber}-1))`; do
    nodeName=${param[$next]}
    echo "Synchronizing ${nodeName}..."

    rsync -a -f"+ */" -f"- *" ../../jessy/script ${nodeName}.grid5000.fr:~/jessy
    rsync --delete -az ./* ${nodeName}.grid5000.fr:~/jessy/script/

    next=$(($next+3))
  done
}

function launchExperience {
  echo ""
  echo "grid5kLaucher: myfractal and configuration.sh are done"
  echo "Launching experience..."
  ./rubis-experience.sh ${param[*]}
}

function endScript {
  echo "grid5kLaucher: done, deleting jobs"
  oargriddel ${RES_ID}
}

function run {
  echo "starting grid5kLaucher..."
  changeScriptPaths
  buildReservation
  reserveNodes
  buildFractal
  syncCode
  launchExperience
  endScript
}

run
