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

package org.imdea.rubis.benchmark.entity;

import static fr.inria.jessy.ConstantPool.JESSY_MID;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.transaction.Transaction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.atomic.AtomicLong;

@Entity
public class RegionEntity extends JessyEntity implements Externalizable {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor {
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

    @Entity
    public static class NameIndex extends JessyEntity implements Externalizable {
        private static final AtomicLong sSequence = new AtomicLong();

        @SecondaryKey(relate = Relationship.ONE_TO_ONE)
        @SuppressWarnings("unused")
        private String mName;
        private long mRegionId;

        @Deprecated
        public NameIndex() {
            super("");
        }

        public NameIndex(String name, long regionId) {
            super("?regions~id#" + sSequence.incrementAndGet() + ":name#" + name);
            mName = name;
            mRegionId = regionId;
        }

        @Override
        public void clearValue() {
        }

        @Override
        public Object clone() {
            NameIndex entity = (NameIndex) super.clone();
            entity.mName = mName;
            entity.mRegionId = mRegionId;
            return entity;
        }

        public long getRegionId() {
            return mRegionId;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            mRegionId = in.readLong();
            mName = (String) in.readObject();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeLong(mRegionId);
            out.writeObject(mName);
        }
    }

    @Entity
    public static class Scanner extends JessyEntity implements Externalizable {
        private static final AtomicLong sSequence = new AtomicLong();

        @SecondaryKey(relate = Relationship.MANY_TO_ONE)
        @SuppressWarnings("unused")
        private long mDummy;
        private long mRegionId;

        @Deprecated
        public Scanner() {
            super("");
        }

        public Scanner(long regionId) {
            super("?regions~id#" + sSequence.incrementAndGet() + ":all#0");
            mRegionId = regionId;
        }

        @Override
        public void clearValue() {
        }

        @Override
        public Object clone() {
            Scanner entity = (Scanner) super.clone();
            entity.mDummy = mDummy;
            entity.mRegionId = mRegionId;
            return entity;
        }

        public long getRegionId() {
            return mRegionId;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            mRegionId = in.readLong();
            mDummy = in.readLong();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeLong(mRegionId);
            out.writeLong(mDummy);
        }
    }

    private long mId;
    private String mName;

    @Deprecated
    public RegionEntity() {
        super("");
    }

    public RegionEntity(long id, String name) {
        super("@regions~id#" + Long.toString(id));
        mId = id;
        mName = name;
    }

    @Override
    public void clearValue() {
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
