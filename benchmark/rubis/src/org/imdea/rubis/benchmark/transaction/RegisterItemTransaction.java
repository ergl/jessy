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

import static org.imdea.rubis.benchmark.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Date;

import org.imdea.rubis.benchmark.entity.IndexEntity;
import org.imdea.rubis.benchmark.entity.ItemEntity;

public class RegisterItemTransaction extends AbsRUBiSTransaction {
    private ItemEntity mItem;

    public RegisterItemTransaction(Jessy jessy, long id, String name, String description, float initialPrice, int
                quantity, float reservePrice, float buyNow, int nbOfBids, float maxBid, Date startDate, Date endDate,
                long seller, long category) throws Exception {
        super(jessy);
        mItem = new ItemEntity(id, name, description, initialPrice, quantity, reservePrice, buyNow, nbOfBids, maxBid,
                startDate, endDate, seller, category);
    }

    private void createNeededIndexEntitties() {
        // TODO: This sucks. For less spaghetti code index entities should be created on the fly, when needed, but the
        // TODO: actual code of Jessy doesn't allow this: reading a non-existent org.imdea.benchmark.rubis.entity will increase the fail read
        // TODO: count (after retrying 10 times) and a lot of code should be changed in order to avoid this.
        createIndex(bids.item_id).justEmpty().forKey(mItem.getId());
        createIndex(buy_now.item_id).justEmpty().forKey(mItem.getId());
        createIndex(comments.item_id).justEmpty().forKey(mItem.getId());
    }

    @Override
    public ExecutionHistory execute() {
        try {
            create(mItem);
            createNeededIndexEntitties();
            updateIndexes();
            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateIndexes() {
        IndexEntity categoryIndex = readIndex(items.category).find(mItem.getCategory());
        categoryIndex.edit().addPointer(mItem.getId()).write(this);

        IndexEntity sellerIndex = readIndex(items.seller).find(mItem.getSeller());
        sellerIndex.edit().addPointer(mItem.getId()).write(this);
    }
}
