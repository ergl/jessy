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

package org.imdea.rubis.benchmark.table;

import fr.inria.jessy.transaction.Transaction;

import org.imdea.rubis.benchmark.entity.AbsRUBiSEntity;

public class Entities {
    public static class EntityReader<E extends AbsRUBiSEntity> {
        private Class<E> mEntityClass;
        private String mId;

        EntityReader(Class<E> clazz, String id) {
            mEntityClass = clazz;
            mId = id;
        }

        public String getDatastoreUniqueIdentifier() {
            return mId;
        }

        public E readAs(Transaction trans) {
            try {
                return trans.read(mEntityClass, getDatastoreUniqueIdentifier());
            } catch (Exception e) {
                throw new UnaccessibleEntityException();
            }
        }
    }

    public static class LookupNameCreator<E extends AbsRUBiSEntity> {
        private Class<E> mEntityClass;
        private String mTableName;

        LookupNameCreator(Class<E> clazz, String tableName) {
            mEntityClass = clazz;
            mTableName = tableName;
        }

        public EntityReader<E> withKey(long id) {
            return new EntityReader<>(mEntityClass, "@" + mTableName + "~id#" + id);
        }
    }

    public static <E extends AbsRUBiSEntity> LookupNameCreator<E> of(AbsTable<E> table) {
        return new LookupNameCreator<>(table.getEntityClass(), table.getName());
    }
}
