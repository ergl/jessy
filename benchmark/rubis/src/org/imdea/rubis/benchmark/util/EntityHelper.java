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

package org.imdea.rubis.benchmark.util;

import fr.inria.jessy.transaction.Transaction;

import org.imdea.rubis.benchmark.entity.AbsRUBiSEntity;
import org.imdea.rubis.benchmark.exception.UnaccessibleEntityException;
import org.imdea.rubis.benchmark.table.AbsTable;

public class EntityHelper {
    public class Reader<E extends AbsRUBiSEntity> {
        private AbsTable<E> mTable;

        Reader(AbsTable<E> table) {
            mTable = table;
        }

        public E withKey(long key) {
            return withKey(Long.toString(key));
        }

        public E withKey(String value) {
            String id = generateId(mTable, value);

            try {
                return mTrans.read(mTable.getEntityClass(), id);
            } catch (Exception e) {
                throw new UnaccessibleEntityException(id);
            }
        }
    }

    private final Transaction mTrans;

    public EntityHelper(Transaction trans) {
        mTrans = trans;
    }

    public static <E extends AbsRUBiSEntity> String generateId(AbsTable<E> table, String value) {
        return Naming.of(table, "id", value);
    }

    public <E extends AbsRUBiSEntity> Reader<E> readEntityFrom(AbsTable<E> table) {
        return new Reader<>(table);
    }
}
