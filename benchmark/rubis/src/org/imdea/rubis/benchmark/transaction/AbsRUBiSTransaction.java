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

import static org.imdea.rubis.benchmark.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.Transaction;

import org.imdea.rubis.benchmark.entity.AbsRUBiSEntity;
import org.imdea.rubis.benchmark.entity.IndexEntity;
import org.imdea.rubis.benchmark.entity.ScannerEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;
import org.imdea.rubis.benchmark.util.EntityHelper;
import org.imdea.rubis.benchmark.table.Index;
import org.imdea.rubis.benchmark.table.AbsTable;
import org.imdea.rubis.benchmark.util.IndexHelper;
import org.imdea.rubis.benchmark.util.ScannerHelper;

public abstract class AbsRUBiSTransaction extends Transaction {
    public static final String NAME = AbsRUBiSTransaction.class.getName() + "::NAME";

    public enum Outcome {
        SUCCESS, FAILURE, ERROR
    }

    private EntityHelper mEntityHelper = new EntityHelper(this);
    private IndexHelper mIndexHelper = new IndexHelper(this);
    private ScannerHelper mScannerHelper = new ScannerHelper(this);

    public AbsRUBiSTransaction(Jessy jessy) throws Exception {
        super(jessy);
        init();
    }

    public AbsRUBiSTransaction(Jessy jessy, int readOperations, int updateOperations, int createOperations) throws
            Exception {
        super(jessy, readOperations, updateOperations, createOperations);
        init();
    }

    protected long authenticate(String nickname, String password) {
        IndexEntity nickIndex = readIndex(users.nickname).find(nickname);

        if (nickIndex != null && nickIndex.getPointers().size() > 0) {
            long userId = nickIndex.getPointers().get(0);
            UserEntity user = readEntityFrom(users).withKey(userId);

            if (user.getPassword().equals(password))
                return userId;
        }

        return -1;
    }

    protected IndexHelper.Initializer createIndex(Index index) {
        return mIndexHelper.createIndex(index);
    }

    protected <E extends AbsRUBiSEntity> void createScannerFor(AbsTable<E> table) {
        mScannerHelper.createScannerFor(table);
    }

    private void init() {
        putExtra(NAME, getClass().getSimpleName());
    }

    protected <E extends AbsRUBiSEntity> EntityHelper.Reader<E> readEntityFrom(AbsTable<E> table) {
        return mEntityHelper.readEntityFrom(table);
    }

    protected IndexHelper.Reader readIndex(Index index) {
        return mIndexHelper.readIndex(index);
    }

    protected <E extends AbsRUBiSEntity> ScannerEntity readScannerOf(AbsTable<E> table) {
        return mScannerHelper.readScannerOf(table);
    }
}
