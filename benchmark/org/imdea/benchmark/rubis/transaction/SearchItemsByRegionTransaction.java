package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.benchmark.rubis.entity.IndexEntity;
import org.imdea.benchmark.rubis.entity.ItemEntity;
import org.imdea.benchmark.rubis.entity.UserEntity;

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
            // user in a given region. First we get the ids of all the users in the given region (regionsIndex). For
            // each of them we get the ids of all the items they sell (or sold). For each item we check if its
            // category matches the given one (checking if categoryIndex contains the index of that item), if yes we
            // read the corresponding ItemEntity.
            IndexEntity categoriesIndex = readIndexFor(items.category, mCategoryId);
            IndexEntity regionsIndex = readIndexFor(users.region, mRegionId);

            for (long userKey : regionsIndex.getPointers()) {
                UserEntity seller = readEntity(users, userKey);
                IndexEntity itemsIndex = readIndexFor(items.seller, seller.getId());

                for (long itemKey : itemsIndex.getPointers()) {
                    // Only the items of the given category should be read. To do so we check the id of each item
                    // against the ids contained in categoriesIndex. If categoriesIndex contains such an id we read
                    // the corresponding ItemEntity.
                    if (categoriesIndex.getPointers().contains(itemKey)) {
                        ItemEntity item = readEntity(items, itemKey);
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
