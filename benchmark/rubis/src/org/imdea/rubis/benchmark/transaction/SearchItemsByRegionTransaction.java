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

import org.imdea.rubis.benchmark.entity.ItemEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;

public class SearchItemsByRegionTransaction extends AbsRUBiSTransaction {
    private static final int DEFAULT_ITEMS_PER_PAGE = 25;

    private String mCategoryId;
    private int mNbOfItems;
    private int mPage;
    private String mRegionId;

    public SearchItemsByRegionTransaction(Jessy jessy, String regionKey, String categoryKey) throws
            Exception {
        this(jessy, regionKey, categoryKey, 0, DEFAULT_ITEMS_PER_PAGE);
    }

    public SearchItemsByRegionTransaction(Jessy jessy, String regionKey, String categoryKey, int page, int nbOfItems)
            throws Exception {
        super(jessy);
        mRegionId = regionKey;
        mCategoryId = categoryKey;
        mPage = page;
        mNbOfItems = nbOfItems;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            Collection<ItemEntity> itemsInCategory = readBySecondary(ItemEntity.class, "mCategoryKey", mCategoryId);
            Collection<UserEntity> usersInRegion = readBySecondary(UserEntity.class, "mRegionKey", mRegionId);

            // We actually read all the items. Sorry.
            for (UserEntity seller : usersInRegion) {
                Collection<ItemEntity> itemsOfSeller = readBySecondary(ItemEntity.class, "mSellerKey", seller.getKey());
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
