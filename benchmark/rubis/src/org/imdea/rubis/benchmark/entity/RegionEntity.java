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

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String mDummy = "";
    private long mId;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private String mName;

    @Deprecated
    public RegionEntity() {
        super("");
    }

    public RegionEntity(long id, String name) {
        super("regions~id#" + id + "~name#" + name);
        mId = id;
        mName = name;
    }

    @Override
    public void clearValue() {
        throw new UnsupportedOperationException("This object is immutable.");
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
        mDummy = (String) in.readObject();
        mId = in.readLong();
        mName = (String) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(mDummy);
        out.writeLong(mId);
        out.writeObject(mName);
    }
}
