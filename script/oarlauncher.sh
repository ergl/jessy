#!/bin/bash

source ./configuration.sh

let e=${#nodes[@]}-1
nodeQuery="hostname='${nodes[0]}'"
for i in `seq 1 $e`
do
    nodeQuery=$nodeQuery" OR hostname='${nodes[$i]}'"
done

res=`date --rfc-3339="s" | gawk -F"+" '{print $1}'`

oarsub -l nodes=${#nodes[@]}/blade=1,walltime=12:00:00 -p "${nodeQuery}" --reservation="$res"



