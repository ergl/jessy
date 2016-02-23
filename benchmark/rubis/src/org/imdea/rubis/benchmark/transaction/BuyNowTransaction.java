package org.imdea.rubis.benchmark.transaction;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.rubis.benchmark.entity.ItemEntity;

public class BuyNowTransaction extends AbsRUBiSTransaction {
    private String mItemKey;
    private String mNickname;
    private String mPassword;

    public BuyNowTransaction(Jessy jessy, String itemKey, String nickname, String password) throws Exception {
        super(jessy);
        mItemKey = itemKey;
        mNickname = nickname;
        mPassword = password;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            long userId = authenticate(mNickname, mPassword);

            if (userId != -1) {
                ItemEntity item = read(ItemEntity.class, mItemKey);
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
