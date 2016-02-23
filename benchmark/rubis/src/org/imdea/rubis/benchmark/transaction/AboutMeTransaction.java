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

import java.util.Collection;

import org.imdea.rubis.benchmark.entity.*;

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
                UserEntity user = read(UserEntity.class, mTargetUserId);

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

    private void listBids() throws Exception {
        Collection<BidEntity.UserIdIndex> pointers = readIndex(BidEntity.UserIdIndex.class, "mUserId", mTargetUserId);

        for (BidEntity.UserIdIndex pointer : pointers) {
            BidEntity bid = read(BidEntity.class, pointer.getBidId());
            ItemEntity item = read(ItemEntity.class, bid.getItemId());
            UserEntity seller = read(UserEntity.class, item.getSeller());
        }
    }

    private void listBoughtItems() throws Exception {
        Collection<BuyNowEntity.BuyerIdIndex> pointers = readIndex(BuyNowEntity.BuyerIdIndex.class, "mBuyerId",
                mTargetUserId);

        for (BuyNowEntity.BuyerIdIndex pointer : pointers) {
            BuyNowEntity buyNow = read(BuyNowEntity.class, pointer.getBuyNowId());
            ItemEntity item = read(ItemEntity.class, Long.toString(buyNow.getItemId()));
            UserEntity seller = read(UserEntity.class, Long.toString(item.getSeller()));
        }
    }

    private void listComments() throws Exception {
        Collection<CommentEntity.ToUserIdIndex> pointers = readIndex(CommentEntity.ToUserIdIndex.class, "mToUserId",
                mTargetUserId);

        for (CommentEntity.ToUserIdIndex pointer : pointers) {
            CommentEntity comment = read(CommentEntity.class, pointer.getCommentId());
            UserEntity commenter = read(UserEntity.class, comment.getFromUserId());
        }
    }

    private void listItems() throws Exception {
        Collection<ItemEntity.SellerIndex> pointers = readIndex(ItemEntity.SellerIndex.class, "mSeller", mTargetUserId);

        for (ItemEntity.SellerIndex pointer : pointers) {
            ItemEntity item = read(ItemEntity.class, pointer.getItemId());
        }
    }

    private void listWonItems() throws Exception {
        Collection<BidEntity.UserIdIndex> pointers = readIndex(BidEntity.UserIdIndex.class, "mUserId", mTargetUserId);

        for (BidEntity.UserIdIndex pointer : pointers) {
            BidEntity bid = read(BidEntity.class, pointer.getBidId());
            ItemEntity item = read(ItemEntity.class, bid.getItemId());
            UserEntity seller = read(UserEntity.class, item.getSeller());
        }
    }
}
