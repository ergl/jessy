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

import com.sleepycat.persist.model.Entity;

import fr.inria.jessy.transaction.Transaction;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fr.inria.jessy.ConstantPool.JESSY_MID;

@Entity
public class ScannerEntity extends AbsIndexEntity {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor implements AbsRUBiSEntity.Editor {
        private String mId;
        private ArrayList<Long> mPointers = new ArrayList<>();

        Editor(ScannerEntity source) {
            mId = source.getId();
            mPointers.addAll(source.getPointers());
        }

        public Editor addPointer(long id) {
            mPointers.add(id);
            return this;
        }

        private ScannerEntity done() {
            mPointers.trimToSize();
            return new ScannerEntity(mId, mPointers);
        }

        public Editor setId(String id) {
            mId = id;
            return this;
        }

        public void write(Transaction trans) {
            trans.write(done());
        }
    }

    private String mId;
    private List<Long> mPointers;

    public ScannerEntity() {
    }

    public ScannerEntity(String id) {
        super(id);
        mId = id;
        mPointers = Collections.unmodifiableList(new ArrayList<Long>());
    }

    private ScannerEntity(String id, List<Long> pointers) {
        super(id);
        mId = id;
        mPointers = new ArrayList<Long>(pointers);
        mPointers = Collections.unmodifiableList(mPointers);
    }

    @Override
    public Object clone() {
        ScannerEntity entity = (ScannerEntity) super.clone();
        entity.mId = mId;
        entity.mPointers = Collections.unmodifiableList(new ArrayList<>(mPointers));
        return entity;
    }

    public Editor edit() {
        return new Editor(this);
    }

    public String getId() {
        return mId;
    }

    public List<Long> getPointers() {
        return mPointers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        mId = (String) in.readObject();
        mPointers = (List<Long>) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(mId);
        out.writeObject(mPointers);
    }
}
