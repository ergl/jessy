package org.imdea.benchmark.rubis.table;

import org.imdea.benchmark.rubis.entity.BidEntity;

public class BidTable extends AbsTable<BidEntity> {
    public final Index item_id;
    public final Index user_id;

    BidTable() {
        super(BidEntity.class, "bids");
        item_id = new Index(this, "item_id");
        user_id = new Index(this, "user_id");
    }
}
