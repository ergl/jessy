package org.imdea.benchmark.rubis.table;

import org.imdea.benchmark.rubis.entity.BuyNowEntity;

public class BuyNowTable extends AbsTable<BuyNowEntity> {
    public final Index buyer_id;
    public final Index item_id;

    BuyNowTable() {
        super(BuyNowEntity.class, "buy_now");
        buyer_id = new Index(this, "buyer_id");
        item_id = new Index(this, "item_id");
    }
}
