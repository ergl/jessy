package me.mneri.jessy.dummy;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.Transaction;

public class InsertTransaction extends Transaction {
    private DummyEntity mEntity;

    public InsertTransaction(Jessy jessy, String name, String value) throws Exception {
        super(jessy);
        mEntity = new DummyEntity(name, value);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            create(mEntity);
            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
