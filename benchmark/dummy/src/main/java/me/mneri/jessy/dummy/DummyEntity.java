package me.mneri.jessy.dummy;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import fr.inria.jessy.store.JessyEntity;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@Entity
public class DummyEntity extends JessyEntity implements Externalizable {
    private String mName;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String mValue;

    @Deprecated
    public DummyEntity() {
        super("");
    }

    public DummyEntity(String name, String value) {
        super(name);
        mName = name;
        mValue = value;
    }

    @Override
    public void clearValue() {
        throw new UnsupportedOperationException("This entity is immutable.");
    }

    public String getName() {
        return mName;
    }

    public String getValue() {
        return mValue;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        mName = (String) in.readObject();
        mValue = (String) in.readObject();
    }

    @Override
    public String toString() {
        return "<" + mName + ", " + mValue + ">";
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(mName);
        out.writeObject(mValue);
    }
}
