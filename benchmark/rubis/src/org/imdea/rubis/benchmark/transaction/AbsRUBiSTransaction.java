/*
 * RUBiS Benchmark
 * Copyright (C) 2016 IMDEA Software Institute
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.imdea.rubis.benchmark.transaction;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.Transaction;

import java.util.ArrayList;

import org.imdea.rubis.benchmark.entity.IndexEntity;
import org.imdea.rubis.benchmark.entity.AbsRUBiSEntity;
import org.imdea.rubis.benchmark.table.Entities;
import org.imdea.rubis.benchmark.table.Index;
import org.imdea.rubis.benchmark.table.AbsTable;

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
