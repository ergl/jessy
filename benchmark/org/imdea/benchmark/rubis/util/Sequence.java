package org.imdea.benchmark.rubis.util;

public class Sequence {
    private long mValue;

    public long next() {
        return mValue++;
    }
}
