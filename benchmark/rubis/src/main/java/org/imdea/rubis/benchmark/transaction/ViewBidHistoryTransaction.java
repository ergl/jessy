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

import java.util.Collection;

import org.imdea.rubis.benchmark.entity.BidEntity;
import org.imdea.rubis.benchmark.entity.ItemEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;

public class ViewBidHistoryTransaction extends AbsRUBiSTransaction {
    private long mItemId;

    public ViewBidHistoryTransaction(Jessy jessy, long itemId) throws Exception {
        super(jessy);
        mItemId = itemId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            ItemEntity item = read(ItemEntity.class, ItemEntity.getKeyFromId(mItemId));

            if (item != null) {
                Collection<BidEntity.ItemIdIndex> pointers = readIndex(BidEntity.ItemIdIndex.class, "mItemId", mItemId);

                for (BidEntity.ItemIdIndex pointer : pointers) {
                    BidEntity bid = read(BidEntity.class, pointer.getBidKey());
                    UserEntity bidder = read(UserEntity.class, UserEntity.getKeyFromId(bid.getUserId()));
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
