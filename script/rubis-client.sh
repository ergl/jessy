#!/bin/bash

source /home/mneri/jessy/script/rubis-configuration.sh

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
java -Xms500m -Xmx500m -XX:+UseConcMarkSweepGC org.imdea.rubis.benchmark.cli.CommandLineInterface --client -p `pwd`/rubis.properties
