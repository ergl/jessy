/*
 * RUBiS Benchmark
 * Copyright (C) 2016 IMDEA Software Institute
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.imdea.rubis.benchmark.table;

import org.imdea.rubis.benchmark.entity.BuyNowEntity;

public class BuyNowTable extends AbsTable<BuyNowEntity> {
    public final Index buyer_id;
    public final Index item_id;

    BuyNowTable() {
        super(BuyNowEntity.class, "buy_now");
        buyer_id = new Index(this, "buyer_id");
        item_id = new Index(this, "item_id");
    }
}
