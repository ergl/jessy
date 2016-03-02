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

import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import org.imdea.rubis.benchmark.entity.ItemEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;

public class SearchItemsByRegionTransaction extends AbsRUBiSTransaction {
    private static final int DEFAULT_ITEMS_PER_PAGE = 25;

    private long mCategoryId;
    private int mNbOfItems;
    private int mPage;
    private long mRegionId;

    public SearchItemsByRegionTransaction(Jessy jessy, long regionId, long categoryId) throws
            Exception {
        this(jessy, regionId, categoryId, 0, DEFAULT_ITEMS_PER_PAGE);
    }

    public SearchItemsByRegionTransaction(Jessy jessy, long regionId, long categoryId, int page, int nbOfItems) throws
            Exception {
        super(jessy);
        mRegionId = regionId;
        mCategoryId = categoryId;
        mPage = page;
        mNbOfItems = nbOfItems;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            // This requires a little bit of explanation. The query selects all the items of a given category sold by
            // users in a given region. First we get the ids of all the users in the given region (regionsIndex). For
            // each of them we get the ids of all the items they sell (or sold). For each item we check if its
            // category matches the given one (checking if categoryIndex contains the index of that item), if yes we
            // read the corresponding ItemEntity.
            Collection<ItemEntity.CategoryIdIndex> itemsInCategory = readIndex(ItemEntity.CategoryIdIndex.class,
                    "mCategoryId", mCategoryId);
            List<String> pointersInCategory = new ArrayList<>();

            for (ItemEntity.CategoryIdIndex pointer : itemsInCategory)
                pointersInCategory.add(pointer.getItemKey());

            Collection<UserEntity.RegionIdIndex> usersInRegion = readIndex(UserEntity.RegionIdIndex.class,
                    "mRegionId", mRegionId);
            // We only want to read elements that are in the given page.
            int current = 0;
            int start = mPage * mNbOfItems;
            int read = 0;

            for (UserEntity.RegionIdIndex userId : usersInRegion) {
                UserEntity seller = read(UserEntity.class, userId.getUserKey());
                Collection<ItemEntity.SellerIndex> itemsOfSeller = readIndex(ItemEntity.SellerIndex.class, "mSeller",
                        seller.getId());

                for (ItemEntity.SellerIndex pointer : itemsOfSeller) {
                    // Only the items of the given category should be read. To do so we check the id of each item
                    // against the ids contained in categoriesIndex. If categoriesIndex contains such an id we read
                    // the corresponding ItemEntity.
                    if (pointersInCategory.contains(pointer.getItemKey())) {
                        // Only read elements from start to start + mNbOfItems
                        if (++current < start)
                            continue;

                        ItemEntity item = read(ItemEntity.class, pointer.getItemKey());

                        // Only read elements from start to start + mNbOfItems
                        if (++read == mNbOfItems)
                            return commitTransaction();
                    }
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
