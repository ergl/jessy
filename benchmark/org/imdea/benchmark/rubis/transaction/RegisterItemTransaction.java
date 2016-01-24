package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Date;

import org.imdea.benchmark.rubis.entity.IndexEntity;
import org.imdea.benchmark.rubis.entity.ItemEntity;

public class RegisterItemTransaction extends AbsRUBiSTransaction {
    private ItemEntity mItem;

    public RegisterItemTransaction(Jessy jessy, long id, String name, String description, float initialPrice, int
                quantity, float reservePrice, float buyNow, int nbOfBids, float maxBid, Date startDate, Date endDate,
                long seller, long category) throws Exception {
        super(jessy);
        mItem = new ItemEntity(id, name, description, initialPrice, quantity, reservePrice, buyNow, nbOfBids, maxBid,
                startDate, endDate, seller, category);
    }

    private void createNeededIndexEntitties() {
        // TODO: This sucks. For less spaghetti code index entities should be created on the fly, when needed, but the
        // TODO: actual code of Jessy doesn't allow this: reading a non-existent entity will increase the fail read
        // TODO: count (after retrying 10 times) and a lot of code should be changed in order to avoid this.
        createIndexFor(bids.item_id, mItem.getId());
        createIndexFor(buy_now.item_id, mItem.getId());
        createIndexFor(comments.item_id, mItem.getId());
    }

    @Override
    public ExecutionHistory execute() {
        try {
            create(mItem);
            createNeededIndexEntitties();
            updateIndexes();
            return commitTransaction();
        } catch (Exception ignored) {
        }

        return null;
    }

    private void updateIndexes() {
        IndexEntity categoryIndex = readIndexFor(items.category, mItem.getCategory());
        categoryIndex.edit().addPointer(mItem.getId()).write(this);

        IndexEntity sellerIndex = readIndexFor(items.seller, mItem.getSeller());
        sellerIndex.edit().addPointer(mItem.getId()).write(this);
    }
}
