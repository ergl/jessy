#!/usr/bin/env bash

source ./rubis-configuration.sh

param=("$@")

function fetchExecutionResult(){
    input=$@
    next=0

    for i in `seq 1 ${clustersNumber}`; do
        nodeName=${param[$next]}

        if [[ "$input" == *"$nodeName"* ]]; then
            echo 'Fetching execution result of ' ${input} ' from ' ${nodeName}
            scp ${nodeName}:~/jessy/scripts/${input} .
            break
        fi

        next=$(($next+3))
    done
}


function syncConfig() {
  if ${running_on_grid}; then
    echo 'Synchronizing keys and data...'
    local next=0
    # TODO: Is this not missing the first node?
    for i in `seq 1 ${clustersNumber}`; do
      nodeName=${param[$next]}
      echo "synchronizing configuration in "${nodeName}"..."
      rsync --delete -az ./configuration.sh ${nodeName}:~/jessy/script/rubis-configuration.sh
      rsync --delete -az ./config.property ${nodeName}:~/jessy/script/config.property
      next=$(($next+3))
    done
  fi
}

# TODO: Isn't this redundant?
# Why stop servers first if we then go and stop in all nodes?
function stopExp() {
  local server_count=$((${#servers[@]}-1))
  for j in `seq 0 ${server_count}`; do
    echo "stopping server: ${servers[$j]}"
    nohup ${SSHCMD} ${servers[$j]} "killall -SIGTERM java" 2&>1 > /dev/null &
  done

  sleep 5

  local node_count=$((${#nodes[@]}-1))
  for k in `seq 0 ${node_count}`; do
    echo "stopping node: ${nodes[$k]}"
    nohup ${SSHCMD} ${nodes[$k]} "killall -9 java" 2&>1 > /dev/null &
  done
}

function dump() {
  local client_count=$((${#clients[@]}-1))
  for j in `seq 0 ${client_count}`; do
    echo "stopping client: ${clients[$j]}"
    nohup ${SSHCMD} ${clients[$j]} "killall -SIGQUIT java \
      && wait 5 \
      && killall -9 java" 2&>1 > /dev/null &
  done
}

function collectStats(){
    overallThroughput=0;
    committedThroughput=0;
    runtime=0;
    updateLatency=0
    readLatency=0
    consistency=${cons[$selectedCons]} #`grep 'consistency_type\ =' config.property | awk -F '=' '{print $2}'`
    failedTerminationRatio=0;
    failedExecutionRatio=0;
    failedReadsRatio=0;
    timeoutRatio=0;
    executionTime_readonly=0;
    executionTime_update=0;
    terminationTime_readonly=0;
    terminationTime_update=0;
    votingTime=0;
    
    certificationTime_readonly=0;
    certificationTime_update=0;
    certificationQueueingTime=0;
    applyingTransactionQueueingTime=0;
    

	if ! [ -s "${scriptdir}/results/${servercount}.txt" ]; then
	    echo -e  "Consistency\tServer_Machines\tClient_Machines\tNumber_Of_Clients\tOverall_Throughput\tCommitted_Throughput\tupdateTran_Latency\treadonlyTran_Latency\tFailed_Termination_Ratio\tFailed_Execution_Ratio\tFailed_Read_Ratio\tTermination_Timeout_Ratio\tExecutionLatency_UpdateTran\tupdateCertificationLatency\tExecutionLatency_ReadOnlyTran\treadonlyCertificationLatency\tVoting_Time\tCertificationLatency_UpdateTran\tCertificationLatency_readonlyTran\tCertificationQueueingTime\tApplyingTransactionQueueingTime"
	fi

    let scount=${#servers[@]}-1
    for j in `seq 0 $scount`
    do
	server=${servers[$j]}

	tmp=`grep -a "certificationTime_readonly" ${scriptdir}/${server} | gawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    certificationTime_readonly=`echo "${tmp}+${certificationTime_readonly}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "certificationTime_update" ${scriptdir}/${server} | gawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    certificationTime_update=`echo "${tmp}+${certificationTime_update}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "certificationQueueingTime_update" ${scriptdir}/${server} | gawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    certificationQueueingTime=`echo "${tmp}+${certificationQueueingTime}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "applyingTransactionQueueingTime_update" ${scriptdir}/${server} | gawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    applyingTransactionQueueingTime=`echo "${tmp}+${applyingTransactionQueueingTime}"| sed 's/E/*10^/g'`;	    
	fi

    done


    let e=${#clients[@]}-1
    for i in `seq 0 $e`
    do

	client=${clients[$i]}

	tmp=`grep -a Throughput ${scriptdir}/${client} | gawk -F',' '{print $3}'`;
	if [ -n "${tmp}" ]; then
	    overallThroughput=`echo "${tmp} + ${overallThroughput}" | ${bc}`;
	fi

	runtime=`grep -a RunTime ${scriptdir}/${client} | gawk -F',' '{print $3}'`;
	tmp=`grep -a "Return=0" ${scriptdir}/${client} | awk -F "," '{sum+= $3} END {print sum}'`;
	if [ -n "${tmp}" ]; then
	    committedThroughput=`echo "((1000*${tmp})/${runtime}) + ${committedThroughput}" | ${bc}`;
	fi

	tmp=`grep -a "\[UPDATE\], AverageLatency" ${scriptdir}/${client} | gawk -F',' '{print $3}'`;
	if [ -n "${tmp}" ]; then
	    updateLatency=`echo "${tmp} + ${updateLatency}" | ${bc}`;
	fi

	tmp=`grep -a "\[READ\], AverageLatency" ${scriptdir}/${client} | gawk -F',' '{print $3}'`;
	if [ -n "${tmp}" ]; then
	    readLatency=`echo "${tmp} + ${readLatency}" | ${bc}`;
	fi
	
	tmp=`grep -a "ratioFailedTermination" ${scriptdir}/${client} | gawk -F':' '{print $2}'`;
	if [[ (! ${tmp} =~ '/') && (-n "${tmp}") ]]; then
	    failedTerminationRatio=`echo "${tmp}+${failedTerminationRatio}"| sed 's/E/*10^/g'` ;	    
	fi

	tmp=`grep -a "ratioFailedExecution" ${scriptdir}/${client} | gawk -F':' '{print $2}'`;
	if [[ (! ${tmp} =~ '/') && (-n "${tmp}") ]]; then
	    failedExecutionRatio=`echo "${tmp}+${failedExecutionRatio}"| sed 's/E/*10^/g'` ;	    
	fi


	tmp=`grep -a "ratioFailedReads" ${scriptdir}/${client} | gawk -F':' '{print $2}'`;
	if [[ (! ${tmp} =~ '/') && (-n "${tmp}") ]]; then
	    failedReadsRatio=`echo "${tmp}+${failedReadsRatio}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "timeoutRatioAbortedTransactions" ${scriptdir}/${client} | gawk -F':' '{print $2}'`;
	if [[ (! ${tmp} =~ '/') && (-n "${tmp}") ]]; then
	    timeoutRatio=`echo "${tmp}+${timeoutRatio}"| sed 's/E/*10^/g'`;	    
	fi

	####################################################################################################
	####################################START OF PROBES IN TRANSACTION##################################
	tmp=`grep -a "transactionExecutionTime_ReadOlny" ${scriptdir}/${client} | gawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    executionTime_readonly=`echo "${tmp}+${executionTime_readonly}"| sed 's/E/*10^/g'`;	    
	fi
	
	tmp=`grep -a "transactionExecutionTime_Update" ${scriptdir}/${client} | gawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    executionTime_update=`echo "${tmp}+${executionTime_update}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "transactionTerminationTime_ReadOnly" ${scriptdir}/${client} | gawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    terminationTime_readonly=`echo "${tmp}+${terminationTime_readonly}"| sed 's/E/*10^/g'`;	    
	fi
	
	tmp=`grep -a "transactionTerminationTime_Update" ${scriptdir}/${client} | gawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    terminationTime_update=`echo "${tmp}+${terminationTime_update}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "votingTime" ${scriptdir}/${client} | gawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    votingTime=`echo "${tmp}+${votingTime}"| sed 's/E/*10^/g'`;	    
	fi


    done
    
    overallThroughput=`echo "scale=2;(${overallThroughput})/1" | ${bc} `;
    committedThroughput=`echo "scale=2;${committedThroughput}" | ${bc} `;

    updateLatency=`echo "scale=2;(${updateLatency})/${#clients[@]}" | ${bc}`;
    readLatency=`echo "scale=2;(${readLatency})/${#clients[@]}" | ${bc}`;
    clientcount=`echo "${#clients[@]}*${t}" | ${bc}`;

    failedTerminationRatio=`echo "scale=10;(${failedTerminationRatio})/${#clients[@]}" | ${bc}`;
    failedExecutionRatio=`echo "scale=10;(${failedExecutionRatio})/${#clients[@]}" | ${bc}`;

    failedReadsRatio=`echo "scale=10;(${failedReadsRatio})/${#clients[@]}" | ${bc}`;
    timeoutRatio=`echo "scale=10;(${timeoutRatio})/${#clients[@]}" | ${bc}`;

    certificationTime_readonly=`echo "scale=2;(${certificationTime_readonly})/${#servers[@]}" | ${bc}`;
    certificationTime_update=`echo "scale=2;(${certificationTime_update})/${#servers[@]}" | ${bc}`;

    certificationQueueingTime=`echo "scale=2;(${certificationQueueingTime})/${#servers[@]}" | ${bc}`;
    applyingTransactionQueueingTime=`echo "scale=2;(${applyingTransactionQueueingTime})/${#servers[@]}" | ${bc}`;

    executionTime_readonly=`echo "scale=2;(${executionTime_readonly})/${#clients[@]}" | ${bc}`;
    executionTime_update=`echo "scale=2;(${executionTime_update})/${#clients[@]}" | ${bc}`;
    terminationTime_readonly=`echo "scale=2;(${terminationTime_readonly})/${#clients[@]}" | ${bc}`;
    terminationTime_update=`echo "scale=2;(${terminationTime_update})/${#clients[@]}" | ${bc}`;
    votingTime=`echo "scale=2;(${votingTime})/${#clients[@]}" | ${bc}`;
    
    echo -e  "${consistency}\t${servercount}\t$[${#clients[@]}]\t${clientcount}\t${overallThroughput}\t${committedThroughput}\t${updateLatency}\t${readLatency}\t${failedTerminationRatio}\t${failedExecutionRatio}\t${failedReadsRatio}\t${timeoutRatio}\t${executionTime_update}\t${terminationTime_update}\t${executionTime_readonly}\t${terminationTime_readonly}\t${votingTime}\t${certificationTime_update}\t${certificationTime_readonly}\t${certificationQueueingTime}\t${applyingTransactionQueueingTime}"

}

function startServersPhase {
  echo "Starting servers..."
  sed -i.bak 's|workloadType=.*|workloadType="-load"|g' rubis-configuration.sh
  sed -i.bak 's|nthreads.*|nthreads=1|g' rubis-configuration.sh

  syncConfig

  sleep 30
  ${scriptdir}/server-launcher.sh -rubis
  sleep 20
}

# Launches rubis-init
# It is enough for one machine to perform this phase
function loadingPhase {
  echo "Loading phase..."
  local log_path="${scriptdir}/rubis/log"
  [[ ! -d ${log_path} ]] && mkdir -p ${log_path}
  ${SSHCMD} ${clients[0]} "${scriptdir/rubis-init.sh}" > ${log_path}/init-output.log
  sleep 10
}

function gatherResults {
  stopExp

  if ${running_on_grid}; then
    sleep 30
    echo "Transfering experiment results to frontend"
    local server_count=$((${#servers[@]}-1))
    for i in `seq 0 ${server_count}`; do
      fetchExecutionResult ${servers[${i}]}
    done

    local client_count=$((${#clients[@]}-1))
    for j in `seq 0 ${client_count}`; do
      fetchExecutionResult ${clients[${j}]}
    done
  fi
}

function publishResults {
  local server_count=${#servers[@]}
  local result_path="${scriptdir}/rubis/results"
  [[ ! -d ${result_path} ]] && mkdir -p ${result_path}
  collectStats >> ${result_path}/server-${server_count}.txt
}

function runExperience {
  local clauncher_success=3
  while [[ ${clauncher_success} -eq 3 ]]; do
    startServersPhase
    loadingPhase
    syncConfig
    sleep 30
    echo "Benchmark started, launching clients..."
    ${scriptdir}/client-launcher.sh -rubis
    clauncher_success=$?

    gatherResults
    publishResults

    sleep 30
  done
}

function run {
  local consistency_count=$((${#cons[@]}-1))

  for i_cons in `seq 0 ${consistency_count}`; do
    sed -i.bak "s|consistency_type.*|consistency_type = ${cons[$i_cons]}|g" config.property
    local thread=`seq ${client_thread_glb} ${client_thread_increment} ${client_thread_lub}`
    for t in ${thread}; do
      runExperience
    done
  done
}

trap "stopExp; wait; exit 255" SIGINT SIGTERM
trap "dump; wait;" SIGQUIT

run
