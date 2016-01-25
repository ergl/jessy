package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.benchmark.rubis.entity.ItemEntity;
import org.imdea.benchmark.rubis.entity.UserEntity;

public class ViewItemTransaction extends AbsRUBiSTransaction {
    private long mItemId;

    public ViewItemTransaction(Jessy jessy, long itemId) throws Exception {
        super(jessy);
        mItemId = itemId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            ItemEntity item = readEntity(items, mItemId);

            if (item != null) {
                UserEntity seller = readEntity(users, item.getSeller());
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
