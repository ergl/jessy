#!/usr/bin/env bash

set -e

if [[ $# -ne 1 ]]; then
  echo "Usage: server-launcher -rubis | -default"
  exit 1
fi

# Which kind of client should we launch?
case $1 in
  -default)
    source ./configuration.sh
    _log_path=${scriptdir}/jessy/log
    _server_program="${scriptdir}/jessy.sh"
  ;;
  -rubis)
    source ./rubis-configuration.sh
    _log_path=${scriptdir}/rubis/log
    _server_program="${scriptdir}/rubis-server.sh"
  ;;
  *)
    echo "Mode not supported: $1"
    exit 1
  ;;
esac

rm -f nohup.*

# Stops all the clients in every node
function stopExp {
  local n_servers=$((${#servers[@]}-1))
  for i in `seq 0 ${n_servers}`; do
    echo "Stopping on ${servers[$i]}"
    nohup ${SSHCMD} ${servers[$i]} "killall -SIGTERM java" 2>&1 >/dev/null &
  done
}

# If cancelled, stop collecting and kill all servers
trap "echo 'server-launcher catched: quit signal'; stopExp; wait; exit 255" SIGINT SIGTERM


# Create log output if not present
[[ ! -d ${_log_path} ]] && mkdir -p ${_log_path}

# For each client machine, ssh into it and launch the clients
_n_servers=$((${#servers[@]}-1))
for i in `seq 0 ${_n_servers}`; do
  echo "Launching on ${servers[$i]}"
  nohup ${SSHCMD} ${servers[$i]} ${_server_program} > ${_log_path}/server-${servers[$i]}.log
done

wait
exit 0
