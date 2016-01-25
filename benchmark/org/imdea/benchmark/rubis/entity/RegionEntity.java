package org.imdea.benchmark.rubis.entity;

import static fr.inria.jessy.ConstantPool.JESSY_MID;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.transaction.Transaction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.sleepycat.persist.model.Entity;

@Entity
public class RegionEntity extends AbsTableEntity implements Externalizable {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor implements AbsRUBiSEntity.Editor {
        private long mId;
        private String mName;

        Editor(RegionEntity source) {
            mId = source.getId();
            mName = source.getName();
        }

        private RegionEntity done() {
            return new RegionEntity(mId, mName);
        }

        public Editor setId(long id) {
            mId = id;
            return this;
        }

        public Editor setName(String name) {
            mName = name;
            return this;
        }

        public void write(Transaction trans) {
            trans.write(done());
        }
    }

    private long mId;
    private String mName;

    public RegionEntity() {
    }

    public RegionEntity(long id, String name) {
        super(regions, id);
        mId = id;
        mName = name;
    }

    @Override
    public Object clone() {
        RegionEntity entity = (RegionEntity) super.clone();
        entity.mId = mId;
        entity.mName = mName;
        return entity;
    }

    public Editor edit() {
        return new Editor(this);
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        mId = in.readLong();
        mName = (String) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(mId);
        out.writeObject(mName);
    }
}
