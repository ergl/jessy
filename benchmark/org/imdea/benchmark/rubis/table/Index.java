package org.imdea.benchmark.rubis.table;

import fr.inria.jessy.transaction.Transaction;

import java.util.ArrayList;

import org.imdea.benchmark.rubis.entity.IndexEntity;

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
