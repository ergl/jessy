package org.imdea.benchmark.rubis.table;

import org.imdea.benchmark.rubis.entity.RegionEntity;

public class RegionsTable extends AbsTable<RegionEntity> {
    RegionsTable() {
        super(RegionEntity.class, "regions");
    }
}
