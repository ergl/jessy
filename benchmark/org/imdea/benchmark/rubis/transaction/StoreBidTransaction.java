package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Date;

import org.imdea.benchmark.rubis.entity.*;

public class StoreBidTransaction extends AbsRUBiSTransaction {
    private BidEntity mBid;

    public StoreBidTransaction(Jessy jessy, long id, long userId, long itemId, int qty, float bid, float maxBid,
                               Date date) throws Exception {
        super(jessy);
        mBid = new BidEntity(id, userId, itemId, qty, bid, maxBid, date);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            // Insert the new bid in the data store.
            create(mBid);
            // Select the respective item from the data store.
            ItemEntity item = readEntity(items, mBid.getItemId());
            // Update nbOfBids and maxBid fields.
            int nbOfBids = item.getNbOfBids() + 1;
            float maxBid = Math.max(item.getMaxBid(), mBid.getBid());
            item.edit().setNbOfBids(nbOfBids).setMaxBid(maxBid).write(this);

            updateIndexes();

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateIndexes() {
        IndexEntity itemIndex = readIndexFor(bids.item_id, mBid.getItemId());
        itemIndex.edit().addPointer(mBid.getId()).write(this);

        IndexEntity userIndex = readIndexFor(bids.user_id, mBid.getUserId());
        userIndex.edit().addPointer(mBid.getId()).write(this);
    }
}
