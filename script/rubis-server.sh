#!/usr/bin/env bash

source ./rubis-configuration.sh

hostname=`hostname`;
stout=${hostname}".stout"
sterr=${hostname}".sterr"

rm -Rf ${workingdir};
mkdir ${workingdir};
cd ${workingdir};
cp ${scriptdir}/config.property ${workingdir};
cp ${scriptdir}/log4j.properties ${workingdir};
cp ${scriptdir}/myfractal.xml ${workingdir};
cp ${scriptdir}/config/YCSB/workloads/${workloadName} ${workingdir}/workload

cd ${workingdir};

export CLASSPATH=${classpath}

# If we're on debug mode, launch the jvm with jdwp enabled
# The server won't wait for an attached debugger,
# change 'suspend=n' to 'suspend=y' to wait for debugger.
if [[ "$#" -eq 1 && $1 = "debug" ]]; then
  java -Xms500m \
       -Xmx500m \
       -XX:+UseConcMarkSweepGC \
       -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
       org.imdea.rubis.benchmark.cli.CommandLineInterface --server
else
  java -Xms500m \
       -Xmx500m \
       -XX:+UseConcMarkSweepGC \
       org.imdea.rubis.benchmark.cli.CommandLineInterface --server
fi
