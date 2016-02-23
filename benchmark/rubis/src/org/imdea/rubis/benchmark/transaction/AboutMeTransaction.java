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
                UserEntity user = read(UserEntity.class, Long.toString(mTargetUserId));

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
        Collection<BidEntity> bids = readBySecondary(BidEntity.class, "mUserId", mTargetUserId);

        for (BidEntity bid : bids) {
            ItemEntity item = read(ItemEntity.class, Long.toString(bid.getItemId()));
            UserEntity seller = read(UserEntity.class, Long.toString(item.getSeller()));
        }
    }

    private void listBoughtItems() throws Exception {
        Collection<BuyNowEntity> buyNows = readBySecondary(BuyNowEntity.class, "mUserId", mTargetUserId);

        for (BuyNowEntity buyNow : buyNows) {
            ItemEntity item = read(ItemEntity.class, Long.toString(buyNow.getItemId()));
            UserEntity seller = read(UserEntity.class, Long.toString(item.getSeller()));
        }
    }

    private void listComments() throws Exception {
        Collection<CommentEntity> comments = readBySecondary(CommentEntity.class, "mToUserId", mTargetUserId);

        for (CommentEntity comment : comments) {
            UserEntity commenter = read(UserEntity.class, Long.toString(comment.getFromUserId()));
        }
    }

    private void listItems() throws Exception {
        Collection<ItemEntity> sellings = readBySecondary(ItemEntity.class, "mSeller", mTargetUserId);
    }

    private void listWonItems() throws Exception {
        Collection<BidEntity> wons = readBySecondary(BidEntity.class, "mUserId", mTargetUserId);

        for (BidEntity won : wons) {
            ItemEntity item = read(ItemEntity.class, Long.toString(won.getItemId()));
            UserEntity seller = read(UserEntity.class, Long.toString(item.getSeller()));
        }
    }
}
