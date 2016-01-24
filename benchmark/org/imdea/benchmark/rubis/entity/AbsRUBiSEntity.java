package org.imdea.benchmark.rubis.entity;

import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.transaction.Transaction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class AbsRUBiSEntity extends JessyEntity implements Cloneable, Externalizable {
    public interface Editor {
        void write(Transaction trans);
    }

    public AbsRUBiSEntity(String id) {
        super(id);
    }

    @Override
    public void clearValue() {
    }

    @Override
    public Object clone() {
        return super.clone();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }
}
