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
import fr.inria.jessy.consistency.SPSI;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.rubis.benchmark.entity.CommentEntity;
import org.imdea.rubis.benchmark.entity.IndexEntity;
import org.imdea.rubis.benchmark.entity.ItemEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;
import org.imdea.rubis.benchmark.table.Tables;

public class AboutMeTransaction extends AbsRUBiSTransaction {
    private long mUserId;

    public AboutMeTransaction(Jessy jessy, long userId) throws Exception {
        super(jessy);
        putExtra(SPSI.LEVEL, SPSI.SER);
        mUserId = userId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            UserEntity user = readEntity(Tables.users, mUserId);

            if (user != null) {
                listBids();
                listItems();
                listWonItems();
                listBoughtItems();
                listComments();
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void listBids() {
        IndexEntity wonIndex = readIndexFor(Tables.bids.user_id, mUserId);

        for (long wonKey : wonIndex.getPointers()) {
            ItemEntity item = readEntity(Tables.items, wonKey);
            UserEntity seller = readEntity(Tables.users, item.getSeller());
        }
    }

    private void listBoughtItems() {
        IndexEntity boughtsIndex = readIndexFor(Tables.buy_now.buyer_id, mUserId);

        for (long boughtsKey : boughtsIndex.getPointers()) {
            ItemEntity boughtItem = readEntity(Tables.items, boughtsKey);
            UserEntity seller = readEntity(Tables.users, boughtItem.getSeller());
        }
    }

    private void listComments() {
        IndexEntity commentsIndex = readIndexFor(Tables.comments.to_user_id, mUserId);

        for (long commentKey : commentsIndex.getPointers()) {
            CommentEntity comment = readEntity(Tables.comments, commentKey);
            UserEntity commenter = readEntity(Tables.users, comment.getFromUserId());
        }
    }

    private void listItems() {
        IndexEntity sellingIndex = readIndexFor(Tables.items.seller, mUserId);

        for (long sellingKey : sellingIndex.getPointers()) {
            ItemEntity item = readEntity(Tables.items, sellingKey);
        }
    }

    private void listWonItems() {
        IndexEntity wonIndex = readIndexFor(Tables.bids.user_id, mUserId);

        for (long wonKey : wonIndex.getPointers()) {
            ItemEntity item = readEntity(Tables.items, wonKey);
            UserEntity seller = readEntity(Tables.users, item.getSeller());
        }
    }
}
