package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Date;

import org.imdea.benchmark.rubis.entity.*;

public class StoreBuyNowTransaction extends AbsRUBiSTransaction {
    private BuyNowEntity mBuyNow;

    public StoreBuyNowTransaction(Jessy jessy, long id, long buyerId, long itemId, int qty, Date date)
            throws Exception {
        super(jessy);
        mBuyNow = new BuyNowEntity(id, buyerId, itemId, qty, date);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            // Select the data item we are buying.
            ItemEntity item = readEntity(items, mBuyNow.getItemId());
            int quantityLeft = item.getQuantity() - mBuyNow.getQty();

            if (quantityLeft >= 0) {
                create(mBuyNow);
                Date endDate = quantityLeft == 0 ? new Date() : item.getEndDate();

                // Update item's quantity and end endDate.
                item.edit().setQuantity(quantityLeft).setEndDate(endDate).write(this);

                updateIndexes();
            }

            return commitTransaction();
        } catch (Exception ignored) {
        }

        return null;
    }

    private void updateIndexes() {
        IndexEntity buyerIndex = readIndexFor(buy_now.buyer_id, mBuyNow.getBuyerId());
        buyerIndex.edit().addPointer(mBuyNow.getId()).write(this);

        IndexEntity itemIndex = readIndexFor(buy_now.item_id, mBuyNow.getItemId());
        itemIndex.edit().addPointer(mBuyNow.getId()).write(this);
    }
}
