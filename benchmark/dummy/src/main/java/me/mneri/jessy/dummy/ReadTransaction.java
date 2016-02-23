package me.mneri.jessy.dummy;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.Transaction;

public class ReadTransaction extends Transaction {
    private String mName;

    public ReadTransaction(Jessy jessy, String name) throws Exception {
        super(jessy);
        mName = name;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            DummyEntity entity = read(DummyEntity.class, mName);
            System.out.println(entity);
            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
