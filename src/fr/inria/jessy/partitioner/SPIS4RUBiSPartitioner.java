package fr.inria.jessy.partitioner;

import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadRequest;

import fr.inria.jessy.store.ReadRequestKey;
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

        if (readRequest.isOneKeyRequest()) {
            groups.add(resolve(readRequest.getOneKey().getKeyValue().toString()));
        } else {
            for (ReadRequestKey key : readRequest.getMultiKeys()) {
                groups.add(resolve(key.getKeyValue().toString()));
            }
        }

        return groups;
    }

    public Group resolve(String key) {
        String id;
        long value;

        // It's an entity
        if (key.startsWith("@"))
            id = key.split("#")[1];
        else if (key.startsWith("?")) // If it starts with "?" it's an index
            id = key.split(":")[1].split("#")[1];
        else // This is the case of index lookup ("only the stuff after "#"")
            id = key;

        try {
            value = Long.valueOf(id);
        } catch (NumberFormatException ignored) {
            value = Math.abs(id.hashCode());
        }

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
