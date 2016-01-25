package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.benchmark.rubis.entity.IndexEntity;
import org.imdea.benchmark.rubis.entity.ItemEntity;
import org.imdea.benchmark.rubis.entity.UserEntity;

public class SearchItemsByRegionTransaction extends AbsRUBiSTransaction {
    private long mRegionId;

    public SearchItemsByRegionTransaction(Jessy jessy, long regionId) throws Exception {
        super(jessy);
        mRegionId = regionId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            IndexEntity usersIndex = readIndexFor(users.region, mRegionId);

            for (long userKey : usersIndex.getPointers()) {
                UserEntity seller = readEntity(users, userKey);
                IndexEntity itemsIndex = readIndexFor(items.seller, seller.getId());

                for (long itemKey : itemsIndex.getPointers()) {
                    ItemEntity item = readEntity(items, itemKey);
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
