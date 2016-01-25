package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Date;

import org.imdea.benchmark.rubis.entity.*;

public class StoreCommentTransaction extends AbsRUBiSTransaction {
    private CommentEntity mComment;

    public StoreCommentTransaction(Jessy jessy, long id, long fromUserId, long toUserId, long itemId, int
            rating, Date date, String comment) throws Exception {
        super(jessy);
        mComment = new CommentEntity(id, fromUserId, toUserId, itemId, rating, date, comment);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            // Insert the new comment in the data store.
            create(mComment);
            // Select the receiver from the data store and store the updated version of the user in the data store.
            UserEntity receiver = readEntity(users, mComment.getToUserId());
            receiver.edit().setRating(receiver.getRating() + mComment.getRating()).write(this);

            updateIndexes();

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateIndexes() {
        IndexEntity fromIndex = readIndexFor(comments.from_user_id, mComment.getFromUserId());
        fromIndex.edit().addPointer(mComment.getId()).write(this);

        IndexEntity itemIndex = readIndexFor(comments.item_id, mComment.getItemId());
        itemIndex.edit().addPointer(mComment.getId()).write(this);

        IndexEntity toIndex = readIndexFor(comments.to_user_id, mComment.getToUserId());
        toIndex.edit().addPointer(mComment.getId()).write(this);
    }
}
