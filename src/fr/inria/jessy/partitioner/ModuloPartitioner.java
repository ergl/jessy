package fr.inria.jessy.partitioner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.fractal.membership.Group;

import org.apache.log4j.Logger;

import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadRequest;

/**
 * This class implements a simple uniform partitioner as follows:
 * Desitionation=Key % No_Of_Partitions
 * <p>
 * Attention: This class implementation may not be safe regarding some keys.
 * Attention: This class does not support dynamic groups.
 * 
 * @author Masoud Saeida Ardekani
 * 
 */
// TODO InComplete!!!
public class ModuloPartitioner extends Partitioner {

	private static Logger logger = Logger.getLogger(ModuloPartitioner.class);

	public ModuloPartitioner(JessyGroupManager m) {
		super(m);

	}

	@Override
	public <E extends JessyEntity> Set<Group> resolve(ReadRequest<E> readRequest) {
		Set<Group> ret = new HashSet<Group>();

		if (readRequest.hasExplicitTarget())
			return Collections.singleton(readRequest.getTarget());

		if (readRequest.isOneKeyRequest()) {
			ret.add(resolve(readRequest.getOneKey().getKeyValue().toString()));
		} else {
			//TODO
			return null;
		}

		return ret;
	}

	@Override
	public boolean isLocal(String k) {
		return manager.getMyGroups().contains(resolve(k));
	}

	@Override
	public Set<String> resolveNames(Set<String> keys) {
		Set<String> result = new HashSet<String>();

		/*
		 * return if there is no key!
		 */
		if (keys.size() == 0)
			return result;

		for (String key : keys) {
			result.add(resolve(key).name());
		}
		logger.debug("keys " + keys + " are resolved to" + result);
		return result;
	}

	/**
	 * This methods returns the group of a key.
	 * 
	 * @param k
	 *            a key
	 * @return the replica group of <i>k</i>.
	 */
	public Group resolve(String key) {
		int numericKey = 0;
		String mkey = key.replaceAll( "[^\\d]", "" );
		if(!mkey.equals("")){
			numericKey = Integer.valueOf(mkey); 
		}
		return manager
				.getReplicaGroups()
				.get(numericKey
						% manager.getReplicaGroups()
								.size());
	}

	@Override
	public Set<String> generateKeysInAllGroups() {
		Set<String> keys=new HashSet<String>();
		
		for (int i = 0; i < manager.getReplicaGroups().size(); i++) {
			keys.add("" + i);
		}
		return keys;
	}
	
	@Override
	public Set<String> generateLocalKey() {
		Set<String> keys=new HashSet<String>();
		
		for (int i = 0; i < manager.getReplicaGroups().size(); i++) {
			if (isLocal(""+i)){				
				keys.add("" + i);
				return keys;
			}
		}
		return keys;
	}

}
