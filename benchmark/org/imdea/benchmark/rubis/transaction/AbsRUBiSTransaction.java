package org.imdea.benchmark.rubis.transaction;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.Transaction;

import java.util.ArrayList;

import org.imdea.benchmark.rubis.entity.IndexEntity;
import org.imdea.benchmark.rubis.entity.AbsRUBiSEntity;
import org.imdea.benchmark.rubis.table.Entities;
import org.imdea.benchmark.rubis.table.Index;
import org.imdea.benchmark.rubis.table.AbsTable;

public abstract class AbsRUBiSTransaction extends Transaction {
    public static final String NAME = AbsRUBiSTransaction.class.getName() + "::NAME";

    public AbsRUBiSTransaction(Jessy jessy) throws Exception {
        super(jessy);
        init();
    }

    public AbsRUBiSTransaction(Jessy jessy, int readOperations, int updateOperations, int createOperations) throws
            Exception {
        super(jessy, readOperations, updateOperations, createOperations);
        init();
    }

    protected void createIndexFor(Index index, long key) {
        create(new IndexEntity(Index.on(index).lookFor(key).getDatastoreUniqueIdentifier(), new ArrayList<Long>()));
    }

    protected void createIndexFor(Index index, long key, long pointer) {
        ArrayList<Long> pointers = new ArrayList<>();
        pointers.add(pointer);
        create(new IndexEntity(Index.on(index).lookFor(key).getDatastoreUniqueIdentifier(), pointers));
    }

    protected void createIndexFor(Index index, String value) {
        create(new IndexEntity(Index.on(index).lookFor(value).getDatastoreUniqueIdentifier(), new ArrayList<Long>()));
    }

    protected void createIndexFor(Index index, String value, long pointer) {
        ArrayList<Long> pointers = new ArrayList<>();
        pointers.add(pointer);
        create(new IndexEntity(Index.on(index).lookFor(value).getDatastoreUniqueIdentifier(), pointers));
    }

    private void init() {
        putExtra(NAME, getClass().getSimpleName());
    }

    protected <E extends AbsRUBiSEntity> E readEntity(AbsTable<E> table, long id) {
        return Entities.of(table).withKey(id).readAs(this);
    }

    protected IndexEntity readIndexFor(Index index, long key) {
        return Index.on(index).lookFor(key).readAs(this);
    }

    protected IndexEntity readIndexFor(Index index, String value) {
        return Index.on(index).lookFor(value).readAs(this);
    }
}
