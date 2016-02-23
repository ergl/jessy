package me.mneri.jessy.dummy;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.store.ReadRequestKey;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.Transaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReadBySecondaryTransaction extends Transaction {
    private String mValue;

    public ReadBySecondaryTransaction(Jessy jessy, String value) throws Exception {
        super(jessy);
        mValue = value;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            Collection<DummyEntity> entities = readBySecondary(DummyEntity.class, "mValue", mValue);

            for (DummyEntity entity : entities)
                System.out.print(":" + entity);

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
