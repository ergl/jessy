package org.imdea.benchmark.rubis.entity;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class AbsIndexEntity extends AbsRUBiSEntity {
	public AbsIndexEntity() {
		super("");
	}
	
    public AbsIndexEntity(String name) {
        super(name);
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
