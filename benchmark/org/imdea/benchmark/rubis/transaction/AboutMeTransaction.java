package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.consistency.SPSI;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.benchmark.rubis.entity.CommentEntity;
import org.imdea.benchmark.rubis.entity.IndexEntity;
import org.imdea.benchmark.rubis.entity.ItemEntity;
import org.imdea.benchmark.rubis.entity.UserEntity;

public class AboutMeTransaction extends AbsRUBiSTransaction {
    private long mUserId;

    public AboutMeTransaction(Jessy jessy, long userId) throws Exception {
        super(jessy);
        putExtra(SPSI.LEVEL, SPSI.SER);
        mUserId = userId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            UserEntity user = readEntity(users, mUserId);

            if (user != null) {
                listBids();
                listItems();
                listWonItems();
                listBoughtItems();
                listComments();
            }

            return commitTransaction();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return null;
    }

    private void listBids() {
        IndexEntity wonIndex = readIndexFor(bids.user_id, mUserId);

        for (long wonKey : wonIndex.getPointers()) {
            ItemEntity item = readEntity(items, wonKey);
            UserEntity seller = readEntity(users, item.getSeller());
        }
    }

    private void listBoughtItems() {
        IndexEntity boughtsIndex = readIndexFor(buy_now.buyer_id, mUserId);

        for (long boughtsKey : boughtsIndex.getPointers()) {
            ItemEntity boughtItem = readEntity(items, boughtsKey);
            UserEntity seller = readEntity(users, boughtItem.getSeller());
        }
    }

    private void listComments() {
        IndexEntity commentsIndex = readIndexFor(comments.to_user_id, mUserId);

        for (long commentKey : commentsIndex.getPointers()) {
            CommentEntity comment = readEntity(comments, commentKey);
            UserEntity commenter = readEntity(users, comment.getFromUserId());
        }
    }

    private void listItems() {
        IndexEntity sellingIndex = readIndexFor(items.seller, mUserId);

        for (long sellingKey : sellingIndex.getPointers()) {
            ItemEntity item = readEntity(items, sellingKey);
        }
    }

    private void listWonItems() {
        IndexEntity wonIndex = readIndexFor(bids.user_id, mUserId);

        for (long wonKey : wonIndex.getPointers()) {
            ItemEntity item = readEntity(items, wonKey);
            UserEntity seller = readEntity(users, item.getSeller());
        }
    }
}
