package org.imdea.rubis.benchmark.util;

import fr.inria.jessy.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

import org.imdea.rubis.benchmark.entity.IndexEntity;
import org.imdea.rubis.benchmark.exception.UnaccessibleIndexException;
import org.imdea.rubis.benchmark.table.Index;

public class IndexHelper {
    public class Creator {
        private Index mIndex;
        private List<Long> mPointers;

        Creator(Index index, List<Long> pointers) {
            mIndex = index;
            mPointers = pointers;
        }

        public void forKey(long key) {
            forKey(Long.toString(key));
        }

        public void forKey(String value) {
        	String id = generateId(mIndex, value);
            mTrans.create(new IndexEntity(id, mPointers));
        }
    }

    public class Initializer {
        private Index mIndex;

        public Initializer(Index index) {
            mIndex = index;
        }
        
        public Creator justEmpty() {
        	ArrayList<Long> pointers = new ArrayList<Long>();
            return withPointers(pointers);
        }

        public Creator withPointer(long pointer) {
            ArrayList<Long> pointers = new ArrayList<Long>();
            pointers.add(pointer);
            return withPointers(pointers);
        }

        public Creator withPointers(List<Long> pointers) {
            return new Creator(mIndex, pointers);
        }
    }

    public class Reader {
        private Index mIndex;

        Reader(Index index) {
            mIndex = index;
        }

        public IndexEntity find(long key) {
            return find(Long.toString(key));
        }

        public IndexEntity find(String value) {
            try {
                String id = generateId(mIndex, value);
                IndexEntity entity = mTrans.read(IndexEntity.class, id);

                if (entity == null)
                    entity = new IndexEntity(id, new ArrayList<Long>());

                return entity;
            } catch (Exception e) {
                throw new UnaccessibleIndexException();
            }
        }
    }

    private final Transaction mTrans;

    public IndexHelper(Transaction trans) {
        mTrans = trans;
    }

    public Initializer createIndex(Index index) {
        return new Initializer(index);
    }

    public static String generateId(Index index, String value) {
        return "@" + index.getTable().getName() + "~" + index.getAttributeName() + "#" + value;
    }

    public Reader readIndex(Index index) {
        return new Reader(index);
    }
}
