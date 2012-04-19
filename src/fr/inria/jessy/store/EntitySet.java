/**
 * 
 */
package fr.inria.jessy.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.vector.CompactVector;

/**
 * @author Masoud Saeida Ardekani This class maintains a list of entities read
 *         or written by a transaction. It is fundamental to
 *         {@link ExecutionHistory}
 * 
 */
public class EntitySet implements Serializable {

	private static final long serialVersionUID = ConstantPool.JESSY_MID;

	/**
	 * maps works as follows: ClassName > SecondaryKey > Entity
	 * 
	 */
	private ConcurrentMap<String, ConcurrentMap<String, ? extends JessyEntity>> entities;

	private CompactVector<String> compactVector;

	public EntitySet() {
		entities = new ConcurrentHashMap<String, ConcurrentMap<String, ? extends JessyEntity>>();
		compactVector = new CompactVector<String>();
	}

	public <E extends JessyEntity> void addEntityClass(Class<E> entityClass) {
		// initialize writeList
		entities.put(entityClass.toString(), new ConcurrentHashMap<String, E>());
	}

	public CompactVector<String> getCompactVector() {
		return compactVector;
	}

	@SuppressWarnings("unchecked")
	public <E extends JessyEntity> E getEntity(Class<E> entityClass,
			String keyValue) {
		Map<String, E> writes = (Map<String, E>) entities.get(entityClass
				.toString());
		return writes.get(keyValue);
	}

	@SuppressWarnings("unchecked")
	public <E extends JessyEntity> void addEntity(E entity) {
		compactVector.update(entity.getLocalVector());

		ConcurrentMap<String, E> temp = (ConcurrentMap<String, E>) entities
				.get(entity.getClass().toString());
		temp.put(entity.getSecondaryKey(), entity);

		entities.put(entity.getClass().toString(), temp);
	}

	@SuppressWarnings("unchecked")
	public <E extends JessyEntity> void addEntity(Collection<E> entityCol) {
		for (E entity : entityCol) {
			compactVector.update(entity.getLocalVector());

			ConcurrentMap<String, E> temp = (ConcurrentMap<String, E>) entities
					.get(entity.getClass().toString());
			temp.put(entity.getSecondaryKey(), entity);

			entities.put(entity.getClass().toString(), temp);
		}
	}

	public void addEntity(EntitySet entitySet) {
		Iterator<? extends JessyEntity> itr = entitySet.getEntities()
				.iterator();
		while (itr.hasNext()) {
			JessyEntity jessyEntity = itr.next();
			addEntity(jessyEntity);
			compactVector.update(jessyEntity.getLocalVector());
		}

	}

	public List<? extends JessyEntity> getEntities() {
		List<JessyEntity> result = new ArrayList<JessyEntity>();

		Collection<ConcurrentMap<String, ? extends JessyEntity>> writeListValues = entities
				.values();

		Iterator<ConcurrentMap<String, ? extends JessyEntity>> itr = writeListValues
				.iterator();
		while (itr.hasNext()) {
			ConcurrentMap<String, ? extends JessyEntity> entities = itr.next();
			result.addAll(entities.values());
		}

		return result;
	}

	public int size() {
		return compactVector.size();
	}

	@SuppressWarnings("unchecked")
	public <E extends JessyEntity> boolean contains(Class<E> entityClass,
			String keyValue) {

		Map<String, E> temp = (Map<String, E>) entities.get(entityClass
				.toString());
		E entity = temp.get(keyValue);
		if (entity != null) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		String result = "";

		Iterator<? extends JessyEntity> itr = getEntities().iterator();
		while (itr.hasNext()) {
			JessyEntity temp = itr.next();
			result = temp.getKey() + "--" + temp.getLocalVector() + "\n";
		}

		return result;
	}

	private ConcurrentMap<String, ConcurrentMap<String, ? extends JessyEntity>> getEntitiesMap() {
		return entities;
	}

	// FIXME Performance Bottleneck. There are so many loops here.
	public Set<String> getKeys() {
		Set<String> keys = new HashSet<String>();
		List<? extends JessyEntity> entityList = getEntities();
		for (JessyEntity e : entityList) {
			keys.add(e.getKey());
		}
		return keys;
	}

	public void clear() {
		entities.clear();
		compactVector = new CompactVector<String>();
	}
}
