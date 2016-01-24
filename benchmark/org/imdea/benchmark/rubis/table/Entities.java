package org.imdea.benchmark.rubis.table;

import fr.inria.jessy.transaction.Transaction;

import org.imdea.benchmark.rubis.entity.AbsRUBiSEntity;

public class Entities {
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

    public static <E extends AbsRUBiSEntity> LookupNameCreator<E> of(AbsTable<E> table) {
        return new LookupNameCreator<>(table.getEntityClass(), table.getName());
    }
}
