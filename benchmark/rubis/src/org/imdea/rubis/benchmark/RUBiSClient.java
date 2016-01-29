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

package org.imdea.rubis.benchmark;

import static fr.inria.jessy.transaction.TransactionState.*;

import fr.inria.jessy.DistributedJessy;
import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.Transaction;

import java.util.Date;

import org.imdea.rubis.benchmark.entity.AbsRUBiSEntity;
import org.imdea.rubis.benchmark.transaction.*;
import org.imdea.rubis.benchmark.util.ThreadSafeSequence;

public class RUBiSClient {
    public static final int EXIT_FAILURE = -1;
    public static final int EXIT_SUCCESS = 0;

    private ThreadSafeSequence mBidsSequence = new ThreadSafeSequence();
    private ThreadSafeSequence mBuyNowSequence = new ThreadSafeSequence();
    private ThreadSafeSequence mCategoriesSequence = new ThreadSafeSequence();
    private ThreadSafeSequence mCommentSequence = new ThreadSafeSequence();
    private ThreadSafeSequence mItemsSequence = new ThreadSafeSequence();
    private Jessy mJessy;
    private ThreadSafeSequence mRegionsSequence = new ThreadSafeSequence();
    private ThreadSafeSequence mUsersSequence = new ThreadSafeSequence();

    public RUBiSClient() {
        try {
            mJessy = DistributedJessy.getInstance();
            mJessy.addEntity(AbsRUBiSEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int aboutMe(long userId) {
        try {
            AboutMeTransaction trans = new AboutMeTransaction(mJessy, userId);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public void connect() {
        try {
            mJessy.registerClient(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void end() {
        mJessy.close(this);
    }

    private int executeTransaction(Transaction trans) throws Exception {
        ExecutionHistory h = trans.execute();

        if (h == null || h.getTransactionState() != COMMITTED)
            return EXIT_FAILURE;

        return EXIT_SUCCESS;
    }

    public int registerCategory(String name) {
        try {
            RegisterCategoryTransaction trans = new RegisterCategoryTransaction(mJessy, mCategoriesSequence.next(),
                    name);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int registerItem(String name, String description, float initialPrice, int quantity, float reservePrice,
                            float buyNow, Date startDate, Date endDate, long seller, long category) {
        try {
            RegisterItemTransaction trans = new RegisterItemTransaction(mJessy, mItemsSequence.next(), name,
                    description, initialPrice, quantity, reservePrice, buyNow, 0, 0.0f, startDate, endDate, seller,
                    category);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int registerRegion(String name) {
        try {
            RegisterRegionTransaction trans = new RegisterRegionTransaction(mJessy, mRegionsSequence.next(), name);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int registerUser(String firstname, String lastname, String nickname, String password, String email,
                            long region) {
        try {
            RegisterUserTransaction trans = new RegisterUserTransaction(mJessy, mUsersSequence.next(), firstname,
                    lastname, nickname, password, email, 0, 0.0f, new Date(), region);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int searchItemByCategory(long categoryId) {
        try {
            SearchItemsByCategoryTransaction trans = new SearchItemsByCategoryTransaction(mJessy, categoryId);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int searchItemByRegion(long regionId, long categoryId) {
        try {
            SearchItemsByRegionTransaction trans = new SearchItemsByRegionTransaction(mJessy, regionId, categoryId);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int storeBid(long userId, long itemId, int qty, float bid, float maxBid) {
        try {
            StoreBidTransaction trans = new StoreBidTransaction(mJessy, mBidsSequence.next(), userId, itemId, qty,
                    bid, maxBid, new Date());
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int storeBuyNow(long buyerId, long itemId, int qty) {
        try {
            StoreBuyNowTransaction trans = new StoreBuyNowTransaction(mJessy, mBuyNowSequence.next(), buyerId,
                    itemId, qty,
                    new Date());
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int storeComment(long fromUserId, long toUserId, long itemId, int rating, String comment) {
        try {
            StoreCommentTransaction trans = new StoreCommentTransaction(mJessy, mCommentSequence.next(), fromUserId,
                    toUserId, itemId, rating, new Date(), comment);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int viewBidHistory(long itemId) {
        try {
            ViewBidHistoryTransaction trans = new ViewBidHistoryTransaction(mJessy, itemId);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int viewItem(long itemId) {
        try {
            ViewItemTransaction trans = new ViewItemTransaction(mJessy, itemId);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }

    public int viewUserInfo(long userId) {
        try {
            ViewUserInfoTransaction trans = new ViewUserInfoTransaction(mJessy, userId);
            return executeTransaction(trans);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_FAILURE;
        }
    }
}
