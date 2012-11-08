#!/bin/bash

#source /home/msaeidaardekani/jessy/scripts/configuration.sh
source /home/msaeida/jessy_script/configuration.sh
 
function stopExp(){
    let sc=${#servers[@]}-1
    for j in `seq 0 $sc`
    do
	nohup ${SSHCMD} ${servers[$j]} "killall -SIGTERM java" 2&>1 > /dev/null &
    done

    sleep 5

    let e=${#nodes[@]}-1
    for i in `seq 0 $e`
    do
	echo "stopping on ${nodes[$i]}"
	nohup ${SSHCMD} ${nodes[$i]} "killall -9 java" 2&>1 > /dev/null &
    done

}

function dump(){
    let c=${#clients[@]}-1
    for j in `seq 0 $c`
    do
	echo "stopping on ${clients[$j]}"
	nohup ${SSHCMD} ${clients[$j]} "killall -SIGQUIT java \
			 		&& wait 5 \
					&& kilall -9 java" 2&>1 > /dev/null &
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
    
    certificationTime_readonly=0;
    certificationTime_update=0;
    

	if ! [ -s "${scriptdir}/results/${servercount}.txt" ]; then
	    echo -e  "Consistency\tServer_Machines\tClient_Machines\tNumber_Of_Clients\tOverall_Throughput\tCommitted_Throughput\tupdateTran_Latency\treadonlyTran_Latency\tFailed_Termination_Ratio\tFailed_Execution_Ratio\tFailed_Read_Ratio\tTermination_Timeout_Ratio\tCertificationLatency_UpdateTran\tCertificationLatency_readonlyTran\tExecutionLatency_UpdateTran\tTerminationLatency_UpdateTran\tExecutionLatency_ReadOnlyTran\tTerminationLatency_ReadOnlyTran"
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

    done
    
    overallThroughput=`echo "scale=2;${overallThroughput}" | ${bc} `;
    committedThroughput=`echo "scale=2;${committedThroughput}" | ${bc} `;

    updateLatency=`echo "scale=2;(${updateLatency})/${#clients[@]}" | ${bc}`;
    readLatency=`echo "scale=2;(${readLatency})/${#clients[@]}" | ${bc}`;
    clientcount=`echo "${#clients[@]}*${t}" | ${bc}`;

    failedTerminationRatio=`echo "scale=10;(${failedTerminationRatio})/${#clients[@]}" | ${bc}`;
    failedExecutionRatio=`echo "scale=10;(${failedExecutionRatio})/${#clients[@]}" | ${bc}`;

    failedReadsRatio=`echo "scale=10;(${failedReadsRatio})/${#clients[@]}" | ${bc}`;
    timeoutRatio=`echo "scale=10;(${timeoutRatio})/${#clients[@]}" | ${bc}`;

    certificationTime_readonly=`echo "scale=10;(${certificationTime_readonly})/${#servers[@]}" | ${bc}`;
    certificationTime_update=`echo "scale=10;(${certificationTime_update})/${#servers[@]}" | ${bc}`;

    executionTime_readonly=`echo "scale=10;(${executionTime_readonly})/${#clients[@]}" | ${bc}`;
    executionTime_update=`echo "scale=10;(${executionTime_update})/${#clients[@]}" | ${bc}`;
    terminationTime_readonly=`echo "scale=10;(${terminationTime_readonly})/${#clients[@]}" | ${bc}`;
    terminationTime_update=`echo "scale=10;(${terminationTime_update})/${#clients[@]}" | ${bc}`;

    
    echo -e  "${consistency}\t${servercount}\t$[${#clients[@]}]\t${clientcount}\t${overallThroughput}\t${committedThroughput}\t${updateLatency}\t${readLatency}\t${failedTerminationRatio}\t${failedExecutionRatio}\t${failedReadsRatio}\t${timeoutRatio}\t${certificationTime_update}\t${certificationTime_readonly}\t${executionTime_update}\t${terminationTime_update}\t${executionTime_readonly}\t${terminationTime_readonly}"

}

trap "stopExp; wait; exit 255" SIGINT SIGTERM
trap "dump; wait;" SIGQUIT


# ##############
# # Experience #
# ##############
let servercount=${#servers[@]}

let consCount=${#cons[@]}-1
for selectedCons in `seq 0 $consCount`
do  

	sed -i "s/consistency_type.*/consistency_type\ =\ ${cons[$selectedCons]}/g" config.property

	#thread setup
	thread=`seq ${client_thread_glb} ${client_thread_increment} ${client_thread_lub}`


	for t in ${thread}; 
	do

	# 0 - Starting the server

	    echo "Starting servers ..."
	    sed -i 's/-t/-load/g' configuration.sh
	    ${scriptdir}/launcher.sh &


	# 1 - Loading phase
	    echo "Loading phase ..."
	    ${SSHCMD} ${clients[0]} "${scriptdir}/client.sh" > ${scriptdir}/loading

	    sleep 60

	# 2 - Benchmarking phase

	    echo "Benchmarking phase ..."
	    sed -i 's/-load/-t/g' configuration.sh

	    sed -i "s/nthreads.*/nthreads=${t}/g" configuration.sh
	    echo "using ${t} thread(s) per machine"   
	    ${scriptdir}/clauncher.sh

	    stopExp

	if $running_on_grid ; then
		echo "trnasfering experiment files to the main launcher frontend..."
		let sc=${#servers[@]}-1
		for ii in `seq 0 $sc`;
		do
			scpServer=${servers[${ii}]}
			scp ${scpServer}:~/jessy/scripts/${scpServer} .
		done

		let cc=${#clients[@]}-1
		for ii in `seq 0 $cc`;
		do
			scpClient=${clients[${ii}]}
			scp ${scpClient}:~/jessy/scripts/${scpClient} .
		done
	fi

	    echo "using ${t} thread(s) per machine is finished. Collecting stats"   
	    collectStats >>  ${scriptdir}/results/${servercount}.txt
		source collectMeasurements.sh
	    sleep 10
	    
	done

done
