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

import java.util.ArrayList;

import org.imdea.rubis.benchmark.entity.IndexEntity;

public class Index {
    public static class IndexReader {
        private String mIndexName;

        IndexReader(String name) {
            mIndexName = name;
        }

        public String getDatastoreUniqueIdentifier() {
            return mIndexName;
        }

        public IndexEntity readAs(Transaction trans) {
            try {
                IndexEntity entity = trans.read(IndexEntity.class, getDatastoreUniqueIdentifier());

                if (entity == null)
                    entity = new IndexEntity(mIndexName, new ArrayList<Long>());

                return entity;
            } catch (Exception e) {
                throw new UnaccessibleIndexException();
            }
        }
    }

    public static class LookupNameCreator {
        private Index mIndex;

        LookupNameCreator(Index index) {
            mIndex = index;
        }

        public IndexReader lookFor(long key) {
            return lookFor(Long.toString(key));
        }

        public IndexReader lookFor(String value) {
            return new IndexReader("@" + mIndex.mTable.getName() + "~" + mIndex.mAttr + "#" + value);
        }
    }
    private String mAttr;
    private AbsTable mTable;

    public Index(AbsTable table, String attr) {
        mTable = table;
        mAttr = attr;
    }

    public static LookupNameCreator on(Index index) {
        return new LookupNameCreator(index);
    }
}
