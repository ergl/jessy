package org.imdea.benchmark.rubis.table;

import org.imdea.benchmark.rubis.entity.ItemEntity;

public class ItemsTable extends AbsTable<ItemEntity> {
    public final Index category;
    public final Index seller;

    ItemsTable() {
        super(ItemEntity.class, "items");
        category = new Index(this, "category");
        seller = new Index(this, "seller");
    }
}
