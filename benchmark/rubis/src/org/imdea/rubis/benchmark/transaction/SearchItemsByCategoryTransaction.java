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

public class SearchItemsByCategoryTransaction extends AbsRUBiSTransaction {
    private static final int DEFAULT_ITEMS_PER_PAGE = 25;

    private String mCategoryKey;
    private int mNbOfItems;
    private int mPage;


    public SearchItemsByCategoryTransaction(Jessy jessy, String categoryKey) throws Exception {
        this(jessy, categoryKey, 0, DEFAULT_ITEMS_PER_PAGE);
    }

    public SearchItemsByCategoryTransaction(Jessy jessy, String categoryKey, int page, int nbOfItems)
            throws Exception {
        super(jessy);
        mCategoryKey = categoryKey;
        mPage = page;
        mNbOfItems = nbOfItems;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            int start = mPage * mNbOfItems;
            int end = start + mNbOfItems;

            // It actually reads all the items in a given category, not only the ones in the current page
            Collection<ItemEntity> items = readBySecondary(ItemEntity.class, "mCategoryKey", mCategoryKey);

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
