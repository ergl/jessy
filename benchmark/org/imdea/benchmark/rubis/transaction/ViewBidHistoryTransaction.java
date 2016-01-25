package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.benchmark.rubis.entity.BidEntity;
import org.imdea.benchmark.rubis.entity.IndexEntity;
import org.imdea.benchmark.rubis.entity.ItemEntity;
import org.imdea.benchmark.rubis.entity.UserEntity;

public class ViewBidHistoryTransaction extends AbsRUBiSTransaction {
    private long mItemId;

    public ViewBidHistoryTransaction(Jessy jessy, long itemId) throws Exception {
        super(jessy);
        mItemId = itemId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            ItemEntity item = readEntity(items, mItemId);

            if (item != null) {
                IndexEntity bidsIndex = readIndexFor(bids.item_id, mItemId);

                for (long key : bidsIndex.getPointers()) {
                    BidEntity bid = readEntity(bids, key);
                    UserEntity bidder = readEntity(users, bid.getUserId());
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
