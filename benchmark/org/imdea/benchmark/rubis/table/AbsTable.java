package org.imdea.benchmark.rubis.table;

import org.imdea.benchmark.rubis.entity.AbsRUBiSEntity;

public abstract class AbsTable<T extends AbsRUBiSEntity> {
    private Class<T> mClass;
    private String mName;

    AbsTable(Class<T> clazz, String name) {
        mClass = clazz;
        mName = name;
    }

    Class<T> getEntityClass() {
        return mClass;
    }

    String getName() {
        return mName;
    }
}
