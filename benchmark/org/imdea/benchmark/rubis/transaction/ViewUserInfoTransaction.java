package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.benchmark.rubis.entity.CommentEntity;
import org.imdea.benchmark.rubis.entity.IndexEntity;
import org.imdea.benchmark.rubis.entity.UserEntity;

public class ViewUserInfoTransaction extends AbsRUBiSTransaction {
    private long mUserId;

    public ViewUserInfoTransaction(Jessy jessy, long userId) throws Exception {
        super(jessy);
        mUserId = userId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            UserEntity user = readEntity(users, mUserId);

            if (user != null) {
                IndexEntity commentsIndex = readIndexFor(comments.to_user_id, mUserId);

                for (long key : commentsIndex.getPointers()) {
                    CommentEntity comment = readEntity(comments, key);
                    UserEntity author = readEntity(users, comment.getFromUserId());
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
