package org.imdea.rubis.benchmark.transaction;

import static org.imdea.rubis.benchmark.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.rubis.benchmark.entity.ItemEntity;

public class BuyNowTransaction extends AbsRUBiSTransaction {
    private long mItemId;
    private String mNickname;
    private String mPassword;

    public BuyNowTransaction(Jessy jessy, long itemId, String nickname, String password) throws Exception {
        super(jessy);
        mItemId = itemId;
        mNickname = nickname;
        mPassword = password;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            long userId = authenticate(mNickname, mPassword);

            if (userId != -1) {
                ItemEntity item = readEntityFrom(items).withKey(mItemId);
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
