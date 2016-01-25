package org.imdea.benchmark.rubis.util;

public class Sequence {
    private long mValue;

    public synchronized long next() {
        return mValue++;
    }
}
