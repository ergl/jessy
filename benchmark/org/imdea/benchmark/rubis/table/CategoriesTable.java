package org.imdea.benchmark.rubis.table;

import org.imdea.benchmark.rubis.entity.CategoryEntity;

public class CategoriesTable extends AbsTable<CategoryEntity> {
    CategoriesTable() {
        super(CategoryEntity.class, "categories");
    }
}
