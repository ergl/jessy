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
public class CategoryEntity extends JessyEntity implements Externalizable {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor {
        private long mId;
        private String mName;

        Editor(CategoryEntity source) {
            mId = source.getId();
            mName = source.getName();
        }

        private CategoryEntity done() {
            return new CategoryEntity(mId, mName);
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
    public static class Scanner extends JessyEntity implements Externalizable {
        private static final AtomicLong sSequence = new AtomicLong();

        private long mCategoryId;
        @SecondaryKey(relate = Relationship.MANY_TO_ONE)
        @SuppressWarnings("unused")
        private long mDummy;

        @Deprecated
        public Scanner() {
            super("");
        }

        public Scanner(long categoryId) {
            super("?categories~id#" + sSequence.incrementAndGet() + ":all#0");
            mCategoryId = categoryId;
        }

        @Override
        public void clearValue() {
            throw new UnsupportedOperationException("This entity is immutable.");
        }

        @Override
        public Object clone() {
            Scanner entity = (Scanner) super.clone();
            entity.mCategoryId = mCategoryId;
            entity.mDummy = mDummy;
            return entity;
        }

        public long getCategoryId() {
            return mCategoryId;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            mCategoryId = in.readLong();
            mDummy = in.readLong();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeLong(mCategoryId);
            out.writeLong(mDummy);
        }
    }

    private long mId;
    private String mName;

    @Deprecated
    public CategoryEntity() {
        super("");
    }

    public CategoryEntity(long id, String name) {
        super("@categories~id#" + id);
        mId = id;
        mName = name;
    }

    @Override
    public void clearValue() {
        throw new UnsupportedOperationException("This object is immutable.");
    }


    @Override
    public Object clone() {
        CategoryEntity entity = (CategoryEntity) super.clone();
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
