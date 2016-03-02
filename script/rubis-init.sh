#!/bin/bash

source /home/mneri/Progetti/jessy/script/configuration.sh

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
java -Xms1000m -Xmx2000m -XX:+UseConcMarkSweepGC org.imdea.rubis.benchmark.cli.CommandLineInterface --init
