#!/usr/bin/env bash

CONS=("ser_pdv_gc" "spsi_pdv_gc" "nmsi_pdv_gc")
CLIENT="jvm-1"
SERVERS=("jvm-2" "jvm-3" "jvm-4")
JESSYPATH=/home/mneri/Progetti/jessy/script
EXECUTIONS=24

function clean {
    echo "======"
    echo "KILLING SERVERS"
    echo "======"

    for I in `seq 0 $((${#SERVERS[@]}-1))`; do
        ssh ${SERVERS[$I]} 'ps aux | grep rubis | grep -v grep | awk '"'"'{print $2}'"'"' | xargs kill -9'
    done;

    echo "======"
    echo "ENSURING CLIENT IS KILLED"
    echo "======"

    ssh $CLIENT 'ps aux | grep rubis | grep -v grep | awk '"'"'{print $2}'"'"' | xargs kill -9'
}

function end {
    clean
    exit
}

function main {
    clean

    for C in ${CONS[@]}; do
        for E in `seq 1 $EXECUTIONS`; do
            echo "======"
            echo "EXECUTING $C $E"
            echo "======"
            echo "======"
            echo "STARTING SERVERS"
            echo "======"

            for S in ${SERVERS[@]}; do
                ssh $S sed -i "s/consistency_type.*/consistency_type=$C/g" $JESSYPATH/config.property
                ssh $S "cd $JESSYPATH; nohup ./rubis-server.sh > ~/${C}.${E}.${S}.stats &"
            done

            echo "======"
            echo "WAITING FOR THE SERVERS TO STARTUP"
            echo "======"
            sleep 10

            ssh $S sed -i "s/consistency_type.*/consistency_type=$C/g" $JESSYPATH/config.property
            echo "======"
            echo "INITIALIZING DATA STORE"
            echo "======"
            ssh $CLIENT "cd $JESSYPATH; ./rubis-init.sh"

            echo "======"
            echo "EXECUTING CLIENT"
            echo "======"
            ssh $CLIENT 'cd '$JESSYPATH'; ./rubis-client.sh'

            clean
        done
    done
}

trap "end" SIGINT SIGTERM
main