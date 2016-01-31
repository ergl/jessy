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

import org.imdea.rubis.benchmark.entity.IndexEntity;
import org.imdea.rubis.benchmark.entity.ItemEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;

public class SearchItemsByRegionTransaction extends AbsRUBiSTransaction {
    private long mCategoryId;
    private long mRegionId;

    public SearchItemsByRegionTransaction(Jessy jessy, long regionId, long categoryId) throws Exception {
        super(jessy);
        mRegionId = regionId;
        mCategoryId = categoryId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            // This requires a little bit of explanation. The query selects all the items of a given category sold by
            // users in a given region. First we get the ids of all the users in the given region (regionsIndex). For
            // each of them we get the ids of all the items they sell (or sold). For each item we check if its
            // category matches the given one (checking if categoryIndex contains the index of that item), if yes we
            // read the corresponding ItemEntity.
            IndexEntity categoriesIndex = readIndex(items.category).find(mCategoryId);
            IndexEntity regionsIndex = readIndex(users.region).find(mRegionId);

            for (long userKey : regionsIndex.getPointers()) {
                UserEntity seller = readEntityFrom(users).withKey(userKey);
                IndexEntity itemsIndex = readIndex(items.seller).find(seller.getId());

                for (long itemKey : itemsIndex.getPointers()) {
                    // Only the items of the given category should be read. To do so we check the id of each item
                    // against the ids contained in categoriesIndex. If categoriesIndex contains such an id we read
                    // the corresponding ItemEntity.
                    if (categoriesIndex.getPointers().contains(itemKey)) {
                        ItemEntity item = readEntityFrom(items).withKey(itemKey);
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
