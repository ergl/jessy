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

import org.imdea.rubis.benchmark.entity.BuyNowEntity;
import org.imdea.rubis.benchmark.entity.ItemEntity;

public class StoreBuyNowTransaction extends AbsRUBiSTransaction {
    private BuyNowEntity mBuyNow;

    public StoreBuyNowTransaction(Jessy jessy, long id, long buyerId, long itemId, int qty, Date date)
            throws Exception {
        super(jessy);
        mBuyNow = new BuyNowEntity(id, buyerId, itemId, qty, date);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            // Select the data item we are buying.
            ItemEntity item = read(ItemEntity.class, ItemEntity.getKeyFromId(mBuyNow.getItemId()));
            int quantityLeft = item.getQuantity() - mBuyNow.getQty();

            if (quantityLeft >= 0) {
                create(mBuyNow);
                Date endDate = quantityLeft == 0 ? new Date() : item.getEndDate();

                // Update item's quantity and end endDate.
                item.edit().setQuantity(quantityLeft).setEndDate(endDate).write(this);
                updateIndexes();
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateIndexes() {
        create(new BuyNowEntity.BuyerIdIndex(mBuyNow.getBuyerId(), mBuyNow.getId()));
        create(new BuyNowEntity.ItemIdIndex(mBuyNow.getItemId(), mBuyNow.getId()));
    }
}
