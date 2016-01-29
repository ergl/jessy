package org.imdea.rubis.benchmark.util;

import org.imdea.rubis.benchmark.entity.AbsRUBiSEntity;
import org.imdea.rubis.benchmark.table.AbsTable;

public class Naming {
    public static <E extends AbsRUBiSEntity> String of(AbsTable<E> table, String attrName, String attrValue) {
        return  "@" + table.getName() + "~" + attrName + "#" + attrValue;
    }
}
