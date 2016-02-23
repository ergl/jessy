package fr.inria.jessy.partitioner;

import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadRequest;

import java.util.*;

import net.sourceforge.fractal.membership.Group;

public class SPIS4RUBiSPartitioner extends Partitioner {
    public SPIS4RUBiSPartitioner(JessyGroupManager m) {
        super(m);
    }

    @Override
    public Set<String> generateKeysInAllGroups() {
        Set<String> keys = new HashSet<String>();
        List<Group> groups = manager.getReplicaGroups();

        for (int i = 0; i < groups.size(); i++)
            keys.add(Integer.toString(i));

        return keys;
    }

    @Override
    public Set<String> generateLocalKey() {
        Set<String> keys = new HashSet<String>();
        List<Group> groups = manager.getReplicaGroups();

        for (int i = 0; i < groups.size(); i++) {
            if (isLocal(Integer.toString(i))) {
                keys.add(Integer.toString(i));
                return keys;
            }
        }

        return keys;
    }

    @Override
    public boolean isLocal(String k) {
        return manager.getMyGroups().contains(resolve(k));
    }

    @Override
    public <E extends JessyEntity> Set<Group> resolve(ReadRequest<E> readRequest) {
        Set<Group> groups = new HashSet<Group>();

        if (readRequest.hasExplicitTarget())
            return Collections.singleton(readRequest.getTarget());

        if (readRequest.isOneKeyRequest()) {
            groups.add(resolve(readRequest.getOneKey().getKeyValue().toString()));
        } else {
            groups.addAll(manager.getReplicaGroups());
        }

        return groups;
    }

    public Group resolve(String key) {
        String id = key;
        long value;

        try {
            id = key.split("#")[1];
            value = Long.valueOf(id);
        } catch (Exception e) {
            // Here in two circumstances: (1) key.split("#")[1] throws an ArrayIndexOutOfBoundException meaning that
            // there was no '#' character in the key; in this case we take the entire key (say, "xxyyzz") and get its
            // hash value. (2) Long.valueOf(id) throws a NumberFormatException, meaning that the part of the key
            // after '#' was not a number. So we take the part after '#' (say, the original key "xxyy#zz", then we take
            // "zz") and get its hash code.
            value = id.hashCode();
        }

        // Turns out that the hash code is negative sometimes.
        value = Math.abs(value);
        List<Group> groups = manager.getReplicaGroups();

        return groups.get((int) (value % groups.size()));
    }

    @Override
    public Set<String> resolveNames(Set<String> keys) {
        Set<String> result = new HashSet<String>();

        if (keys.size() == 0)
            return result;

        for (String key : keys)
            result.add(resolve(key).name());

        return result;
    }
}
