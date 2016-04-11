package org.imdea.rubis.benchmark.stats;

import static fr.inria.jessy.transaction.ExecutionHistory.TransactionType.*;
import static fr.inria.jessy.transaction.TransactionState.*;

import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.ArrayList;
import java.util.Collections;

public class StatsCollector {
    private long mEndEmulation;
    private int mExceptions;
    private int mFailedReadOnly;
    private int mFailedUpdate;
    private ArrayList<Long> mReadOnlyLatencies = new ArrayList<Long>();
    private long mStartEmulation;
    private ArrayList<Long> mUpdateLatencies = new ArrayList<Long>();

    public synchronized void add(ExecutionHistory history) {
        if (history != null) {
            if (history.getTransactionType() == READONLY_TRANSACTION) {
                if (history.getTransactionState() == COMMITTED)
                    mReadOnlyLatencies.add(history.getExecutionTime());
                else
                    mFailedReadOnly++;
            } else {
                if (history.getTransactionState() == COMMITTED)
                    mUpdateLatencies.add(history.getExecutionTime());
                else
                    mFailedUpdate++;
            }
        } else {
            mExceptions++;
        }
    }

    private double averageOf(ArrayList<Long> latencies) {
        long sum = 0;

        for (long latency : latencies)
            sum += latency;

        return ((double) sum) / latencies.size();
    }

    public synchronized void endEmulation() {
        mEndEmulation = System.currentTimeMillis();
    }

    public synchronized void print() {
        printOverallStats();
        printReadOnlyStats();
        printUpdateStats();
    }

    private void printLine(String scope, String name, double value) {
        System.out.printf("[%s], %s, %f\n", scope, name, value);
    }

    private void printOverallStats() {
        long time = mEndEmulation - mStartEmulation;
        int ops = mReadOnlyLatencies.size() + mUpdateLatencies.size();

        printLine("OVERALL", "RunTime(ms)", time);
        printLine("OVERALL", "Throughput(ops/sec)", ops / (time / 1000.0));
        printLine("OVERALL", "Exceptions", mExceptions);
    }

    private void printReadOnlyStats() {
        Collections.sort(mReadOnlyLatencies);
        int size = mReadOnlyLatencies.size();
        double avg = averageOf(mReadOnlyLatencies);
        long min = mReadOnlyLatencies.get(0);
        long max = mReadOnlyLatencies.get(size - 1);
        long percentile99 = mReadOnlyLatencies.get((int) Math.round(size * 0.99));
        long percentile95 = mReadOnlyLatencies.get((int) Math.round(size * 0.95));

        printLine("READ", "Operations", mReadOnlyLatencies.size() + mFailedReadOnly);
        printLine("READ", "AverageLatency(ms)", avg);
        printLine("READ", "MinLatency(ms)", min);
        printLine("READ", "MaxLatency(ms)", max);
        printLine("READ", "95thPercentileLatency(ms)", percentile95);
        printLine("READ", "99thPercentileLatency(ms)", percentile99);
        printLine("READ", "Return=0", mReadOnlyLatencies.size());
        printLine("READ", ">1000", countOfSlow(mReadOnlyLatencies));
    }

    private void printUpdateStats() {
        Collections.sort(mUpdateLatencies);
        int size = mUpdateLatencies.size();
        double avg = averageOf(mUpdateLatencies);
        long min = mUpdateLatencies.get(0);
        long max = mUpdateLatencies.get(size - 1);
        long percentile99 = mUpdateLatencies.get((int) Math.round(size * 0.99));
        long percentile95 = mUpdateLatencies.get((int) Math.round(size * 0.95));

        printLine("UPDATE", "Operations", mUpdateLatencies.size() + mFailedUpdate);
        printLine("UPDATE", "AverageLatency(ms)", avg);
        printLine("UPDATE", "MinLatency(ms)", min);
        printLine("UPDATE", "MaxLatency(ms)", max);
        printLine("UPDATE", "95thPercentileLatency(ms)", percentile95);
        printLine("UPDATE", "99thPercentileLatency(ms)", percentile99);
        printLine("UPDATE", "Return=0", mUpdateLatencies.size());
        printLine("UPDATE", ">1000", countOfSlow(mUpdateLatencies));
    }

    private int countOfSlow(ArrayList<Long> latencies) {
        int count = 0;

        for (long latency : latencies) {
            if (latency > 1000)
                count++;
        }

        return count;
    }

    public synchronized void startEmulation() {
        mStartEmulation = System.currentTimeMillis();
    }
}
