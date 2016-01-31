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
import fr.inria.jessy.consistency.SPSI;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.rubis.benchmark.entity.CommentEntity;
import org.imdea.rubis.benchmark.entity.IndexEntity;
import org.imdea.rubis.benchmark.entity.ItemEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;

public class AboutMeTransaction extends AbsRUBiSTransaction {
    private String mNickname;
    private String mPassword;
    private long mTargetUserId;

    public AboutMeTransaction(Jessy jessy, long userId, String nickname, String password) throws Exception {
        super(jessy);
        putExtra(SPSI.LEVEL, SPSI.SER);
        mTargetUserId = userId;
        mNickname = nickname;
        mPassword = password;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            long userId = authenticate(mNickname, mPassword);

            if (userId != -1) {
                UserEntity user = readEntityFrom(users).withKey(mTargetUserId);

                if (user != null) {
                    listBids();
                    listItems();
                    listWonItems();
                    listBoughtItems();
                    listComments();
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void listBids() {
        IndexEntity wonIndex = readIndex(bids.user_id).find(mTargetUserId);

        for (long wonKey : wonIndex.getPointers()) {
            ItemEntity item = readEntityFrom(items).withKey(wonKey);
            UserEntity seller = readEntityFrom(users).withKey(item.getSeller());
        }
    }

    private void listBoughtItems() {
        IndexEntity boughtsIndex = readIndex(buy_now.buyer_id).find(mTargetUserId);

        for (long boughtsKey : boughtsIndex.getPointers()) {
            ItemEntity boughtItem = readEntityFrom(items).withKey(boughtsKey);
            UserEntity seller = readEntityFrom(users).withKey(boughtItem.getSeller());
        }
    }

    private void listComments() {
        IndexEntity commentsIndex = readIndex(comments.to_user_id).find(mTargetUserId);

        for (long commentKey : commentsIndex.getPointers()) {
            CommentEntity comment = readEntityFrom(comments).withKey(commentKey);
            UserEntity commenter = readEntityFrom(users).withKey(comment.getFromUserId());
        }
    }

    private void listItems() {
        IndexEntity sellingIndex = readIndex(items.seller).find(mTargetUserId);

        for (long sellingKey : sellingIndex.getPointers()) {
            ItemEntity item = readEntityFrom(items).withKey(sellingKey);
        }
    }

    private void listWonItems() {
        IndexEntity wonIndex = readIndex(bids.user_id).find(mTargetUserId);

        for (long wonKey : wonIndex.getPointers()) {
            ItemEntity item = readEntityFrom(items).withKey(wonKey);
            UserEntity seller = readEntityFrom(users).withKey(item.getSeller());
        }
    }
}
