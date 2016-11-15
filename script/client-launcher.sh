#!/usr/bin/env bash

set -e

if [[ $# -ne 1 ]]; then
  echo "Usage: client-launcher -rubis | -default"
  exit 1
fi

# Which kind of client should we launch?
case $1 in
  -default)
    source ./configuration.sh
    _log_path=${scriptdir}/jessy/log
    _client_program="${scriptdir}/client.sh"
  ;;
  -rubis)
    source ./rubis-configuration.sh
    _log_path=${scriptdir}/rubis/log
    _client_program="${scriptdir}/rubis-client.sh"
  ;;
  *)
    echo "Mode not supported: $1"
    exit 1
  ;;
esac

rm -f nohup.*

# Stops all the clients in every node
function stopExp {
  local n_clients=$((${#clients[@]}-1))
  for i in `seq 0 ${n_clients}`; do
    echo "Stopping on ${clients[$i]}"
    nohup ${SSHCMD} ${clients[$i]} "killall -SIGTERM java" 2>&1 > /dev/null &
  done
}

function stopTimer {
  kill ${TIMERPID}
}

# If cancelled, stop collecting and kill all clients
trap "echo 'client-launcher catched: quit signal'; stopTimer; stopExp; wait; exit 255" SIGINT SIGTERM

# Sometimes, client get stuck; on timeout, kill all the clients and exit 3
trap "echo 'client-launcher catched: timeout'; stopExp; wait; exit 3" SIGUSR1

# Create log output if not present
[[ ! -d ${_log_path} ]] && mkdir -p ${_log_path}

# For each client machine, ssh into it and launch the clients
_n_clients=$((${#clients[@]}-1))
for i in `seq 0 ${_n_clients}`; do
  echo "Launching client on ${clients[$i]}"
  nohup ${SSHCMD} ${clients[$i]} ${_client_program} > ${_log_path}/client-${clients[$i]}.log
  CLIENTPID[$i]=$!
done

export CLIENTPID
export CLAUNCHERPID=$$

# Clients might get stuck - retry after clauncherTimeout
(sleep ${clauncherTimeout}; kill -SIGUSR1 ${CLAUNCHERPID}) &
TIMERPID=$!
export TIMERPID

wait ${CLIENTPID[*]}
echo "client-launcher finished succesfully - returning to experience"
stopTimer
exit 0
