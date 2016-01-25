package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.consistency.SPSI;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Date;

import org.imdea.benchmark.rubis.entity.IndexEntity;
import org.imdea.benchmark.rubis.entity.UserEntity;
import org.imdea.benchmark.rubis.table.UnaccessibleEntityException;
import org.imdea.benchmark.rubis.table.UnaccessibleIndexException;

public class RegisterUserTransaction extends AbsRUBiSTransaction {
    private UserEntity mUser;

    public RegisterUserTransaction(Jessy jessy, long id, String firstname, String lastname, String nickname, String
            password, String email, int rating, float balance, Date creationDate, long region) throws
            Exception {
        super(jessy);
        putExtra(SPSI.LEVEL, SPSI.SER);
        mUser = new UserEntity(id, firstname, lastname, nickname, password, email, rating, balance, creationDate,
                region);
    }

    private void createNeededIndexeEntities() {
        // TODO: This sucks. For less spaghetti code index entities should be created on the fly, when needed, but the
        // TODO: actual code of Jessy doesn't allow this: reading a non-existent entity will increase the fail read
        // TODO: count (after retrying 10 times) and a lot of code should be changed in order to avoid this.
        long id = mUser.getId();
        createIndexFor(bids.user_id, id);
        createIndexFor(buy_now.buyer_id, id);
        createIndexFor(comments.from_user_id, id);
        createIndexFor(comments.to_user_id, id);
        createIndexFor(items.seller, id);
        // TODO: For the same reason it is not convenient to create an index on the pair <nickname, password>. If there
        // TODO: is a mismatch, Jessy will try 10 times before giving up. D:
        createIndexFor(users.nickname, mUser.getNickname(), id);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            IndexEntity nickIndex = null;

            // TODO: Same problem here, if the nickname does not exist (which is very likely in the benchmark) Jessy
            // TODO: will try to read the index 10 times.
            try {
                nickIndex = readIndexFor(users.nickname, mUser.getNickname());
            } catch (UnaccessibleIndexException ignored) {
                // TODO: In the actual implementation when the index does not exist (i.e. the nickname is not in the
                // TODO: database) an UnaccessibleIndexException is thrown. So nickIndex remains null.
            }

            if (nickIndex == null || nickIndex.getPointers().size() == 0) {
                create(mUser);
                createNeededIndexeEntities();
                updateIndexes();
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateIndexes() {
        IndexEntity regionIndex = readIndexFor(users.region, mUser.getRegion());
        regionIndex.edit().addPointer(mUser.getId()).write(this);
    }
}
