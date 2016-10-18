#!/usr/bin/env bash

source ./rubis-configuration.sh

hostname=`hostname`;
stout=${hostname}".stout"
sterr=${hostname}".sterr"

rm -Rf ${workingdir};
mkdir ${workingdir};
cd ${workingdir};
cp ${scriptdir}/rubis.properties ${workingdir};
cp ${scriptdir}/config.property ${workingdir};
cp ${scriptdir}/log4j.properties ${workingdir};
cp ${scriptdir}/myfractal.xml ${workingdir};
cp ${scriptdir}/config/YCSB/workloads/${workloadName} ${workingdir}/workload

cd ${workingdir};

export CLASSPATH=${classpath}

if [[ "$#" -eq 1 && $1 = "true" ]]; then
  java -Xms500m \
       -Xmx500m \
       -XX:+UseConcMarkSweepGC \
       -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 \
       org.imdea.rubis.benchmark.cli.CommandLineInterface --init -p `pwd`/rubis.properties
else
  java -Xms500m \
       -Xmx500m \
       -XX:+UseConcMarkSweepGC \
       org.imdea.rubis.benchmark.cli.CommandLineInterface --init -p `pwd`/rubis.properties
fi
