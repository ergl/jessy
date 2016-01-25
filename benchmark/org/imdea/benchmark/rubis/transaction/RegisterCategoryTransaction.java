package org.imdea.benchmark.rubis.transaction;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.benchmark.rubis.entity.CategoryEntity;

public class RegisterCategoryTransaction extends AbsRUBiSTransaction {
    private CategoryEntity mCategory;

    public RegisterCategoryTransaction(Jessy jessy, long id, String name) throws Exception {
        super(jessy);
        mCategory = new CategoryEntity(id, name);
    }

    private void createNeededIndexEntities() {
        // TODO: This sucks. For less spaghetti code index entities should be created on the fly, when needed, but the
        // TODO: actual code of Jessy doesn't allow this: reading a non-existent entity will increase the fail read
        // TODO: count (after retrying 10 times) and a lot of code should be changed in order to avoid this.
        createIndexFor(items.category, mCategory.getId());
    }

    @Override
    public ExecutionHistory execute() {
        try {
            create(mCategory);
            createNeededIndexEntities();
            return commitTransaction();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return null;
    }
}
