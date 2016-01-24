package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.benchmark.rubis.entity.IndexEntity;
import org.imdea.benchmark.rubis.entity.ItemEntity;

public class SearchItemsByCategoryTransaction extends AbsRUBiSTransaction {
    private long mCategoryId;

    public SearchItemsByCategoryTransaction(Jessy jessy, long categoryId) throws Exception {
        super(jessy);
        mCategoryId = categoryId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            IndexEntity itemsIndex = readIndexFor(items.category, mCategoryId);

            for (long key : itemsIndex.getPointers()) {
                ItemEntity item = readEntity(items, key);
            }

            return commitTransaction();
        } catch (Exception ignored) {
        }

        return null;
    }
}
