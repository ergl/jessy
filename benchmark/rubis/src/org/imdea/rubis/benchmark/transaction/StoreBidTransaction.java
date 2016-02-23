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

package org.imdea.rubis.benchmark.transaction;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Date;

import org.imdea.rubis.benchmark.entity.BidEntity;
import org.imdea.rubis.benchmark.entity.ItemEntity;

public class StoreBidTransaction extends AbsRUBiSTransaction {
    private BidEntity mBid;

    public StoreBidTransaction(Jessy jessy, long id, long userId, long itemId, int qty, float bid, float maxBid,
                               Date date) throws Exception {
        super(jessy);
        mBid = new BidEntity(id, userId, itemId, qty, bid, maxBid, date);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            // Insert the new bid in the data store.
            create(mBid);
            // Select the respective item from the data store.
            ItemEntity item = read(ItemEntity.class, mBid.getItemId());
            // Update nbOfBids and maxBid fields.
            int nbOfBids = item.getNbOfBids() + 1;
            float maxBid = Math.max(item.getMaxBid(), mBid.getBid());
            item.edit().setNbOfBids(nbOfBids).setMaxBid(maxBid).write(this);

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
