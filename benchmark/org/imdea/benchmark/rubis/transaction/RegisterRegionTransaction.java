package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.benchmark.rubis.entity.RegionEntity;

public class RegisterRegionTransaction extends AbsRUBiSTransaction {
    private RegionEntity mRegion;

    public RegisterRegionTransaction(Jessy jessy, long id, String name) throws Exception {
        super(jessy);
        mRegion = new RegionEntity(id, name);
    }

    private void createNeededIndexEntities() {
        // TODO: This sucks. For less spaghetti code index entities should be created on the fly, when needed, but the
        // TODO: actual code of Jessy doesn't allow this: reading a non-existent entity will increase the fail read
        // TODO: count (after retrying 10 times) and a lot of code should be changed in order to avoid this.
        createIndexFor(users.region, mRegion.getId());
    }

    @Override
    public ExecutionHistory execute() {
        try {
            create(mRegion);
            createNeededIndexEntities();
            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
