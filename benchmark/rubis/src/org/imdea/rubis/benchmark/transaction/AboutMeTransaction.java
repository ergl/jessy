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
    private String mTargetUserKey;

    public AboutMeTransaction(Jessy jessy, String userKey, String nickname, String password) throws Exception {
        super(jessy);
        putExtra(SPSI.LEVEL, SPSI.SER);
        mTargetUserKey = userKey;
        mNickname = nickname;
        mPassword = password;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            long userId = authenticate(mNickname, mPassword);

            if (userId != -1) {
                UserEntity user = read(UserEntity.class, mTargetUserKey);

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
        Collection<BidEntity> bids = readBySecondary(BidEntity.class, "mUserKey", mTargetUserKey);

        for (BidEntity bid : bids) {
            ItemEntity item = read(ItemEntity.class, bid.getItemKey());
            UserEntity seller = read(UserEntity.class, item.getSellerKey());
        }
    }

    private void listBoughtItems() throws Exception {
        Collection<BuyNowEntity> buyNows = readBySecondary(BuyNowEntity.class, "mUserKey", mTargetUserKey);

        for (BuyNowEntity buyNow : buyNows) {
            ItemEntity item = read(ItemEntity.class, buyNow.getItemKey());
            UserEntity seller = read(UserEntity.class, item.getSellerKey());
        }
    }

    private void listComments() throws Exception {
        Collection<CommentEntity> comments = readBySecondary(CommentEntity.class, "mUserKey", mTargetUserKey);

        for (CommentEntity comment : comments) {
            UserEntity commenter = read(UserEntity.class, comment.getFromUserKey());
        }
    }

    private void listItems() throws Exception {
        Collection<ItemEntity> sellings = readBySecondary(ItemEntity.class, "mUserKey", mTargetUserKey);
    }

    private void listWonItems() throws Exception {
        Collection<BidEntity> wons = readBySecondary(BidEntity.class, "mUserKey", mTargetUserKey);

        for (BidEntity won : wons) {
            ItemEntity item = read(ItemEntity.class, won.getItemKey());
            UserEntity seller = read(UserEntity.class, item.getSellerKey());
        }
    }
}
