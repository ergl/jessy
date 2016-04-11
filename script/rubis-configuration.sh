#!/bin/bash

#clauncher.sh timeout. Within this timeout, if the clauncher is not finished, it will be terminated and re-executed.
#this is needed because jessy clients sometimes never finish.
clauncherTimeout=2000

#if it will be run on grid5k, set to true, otherwise, set to false.
running_on_grid=false

SSHCMD="oarsh" # ssh or oarsh for oar equiped cluster

#basic calculator path. Note: lip6 cluster does not have bc.
bc="bc"

#The location of all the scripts.
scriptdir="/home/mneri/jessy/script"

#The location where all the databases will be created.
workingdir="/tmp/jessy_exec"

#Read by oarlauncher for reserving the nodes. 
nodes=("cluster1u1" "cluster1u10" "cluster1u11" "cluster1u12" "cluster1u13") # "cluster1u8" "cluster1u11" "cluster1u12" "cluster1u13" "cluster1u14" "cluster1u15" "cluster1u16" "cluster1u17" "cluster1u18"  "cluster1u19" "cluster1u20" "cluster1u21" "cluster1u22" "cluster1u23" "cluster1u24")
servers=("cluster1u1")
clients=("cluster1u10")

# Experience.sh settings

# servers
server_machine_increment="1"
server_machine_glb="1"
server_machine_lub="2"

# clients ( = client_machine * client_thread )
client_machine_increment="1"

#client_machine_glb = client_machine_lub_multiplier * number of servers
client_machine_glb_multiplier="2" 

#client_machine_lub = client_machine_lub_multiplier * number of servers
client_machine_lub_multiplier="2" 

client_thread_increment="1"
client_thread_glb="2"
client_thread_lub="2"

# Consistency
cons=("nmsi_pdv_gc" "spsi_pdv_gc" "ser_pdv_gc")

# Client specific settings 
workloadType="-load"
workloadName="transactionalworkloada"
nthreads=1

system=jessy

if [[ ${system} == "cassandra"  ]];
then
    clientclass=com.yahoo.ycsb.CassandraClient10;
    classpath=${scriptdir}/jessy.jar
    for jar in ${scriptdir}/cassandra/lib/*.jar; do
	classpath=$classpath:$jar
    done
fi;

if [[ ${system} == "jessy"  ]];
then
    clientclass=com.yahoo.ycsb.JessyDBClient;
    classpath=${scriptdir}/commons-lang.jar:${scriptdir}/log4j.jar:${scriptdir}/jessy.jar:${scriptdir}/fractal.jar:${scriptdir}/je.jar:${scriptdir}/concurrentlinkedhashmap.jar:${scriptdir}/netty.jar:${scriptdir}/high-scale-lib.jar:${scriptdir}/db.jar:${scriptdir}/commons-cli-1.3.1.jar;
fi;
