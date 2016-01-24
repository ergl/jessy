package org.imdea.benchmark.rubis.entity;

import static fr.inria.jessy.ConstantPool.JESSY_MID;

import com.sleepycat.persist.model.Entity;

import fr.inria.jessy.transaction.Transaction;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class IndexEntity extends AbsIndexEntity {
	private static final long serialVersionUID = JESSY_MID;
	
    public static class Editor implements AbsRUBiSEntity.Editor {
        private String mId;
        private ArrayList<Long> mPointers = new ArrayList<>();

        Editor(IndexEntity source) {
            mId = source.getId();
            mPointers.addAll(source.getPointers());
        }

        private IndexEntity done() {
            mPointers.trimToSize();
            return new IndexEntity(mId, mPointers);
        }

        public Editor addPointer(long id) {
            mPointers.add(id);
            return this;
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

    public IndexEntity() {
    }

    public IndexEntity(String id, List<Long> pointers) {
        super(id);
        mId = id;
        mPointers = new ArrayList<>(pointers);
        mPointers = Collections.unmodifiableList(mPointers);
    }

    @Override
    public Object clone() {
        IndexEntity entity = (IndexEntity) super.clone();
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
