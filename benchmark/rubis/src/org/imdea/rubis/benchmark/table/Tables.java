/*
 * RUBiS Benchmark
 * Copyright (C) 2016 IMDEA Software Institute
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.imdea.rubis.benchmark.table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Tables {
    public static final BidTable bids = new BidTable();
    public static final BuyNowTable buy_now = new BuyNowTable();
    public static final CategoriesTable categories = new CategoriesTable();
    public static final CommentsTable comments = new CommentsTable();
    public static final ItemsTable items = new ItemsTable();
    public static final RegionsTable regions = new RegionsTable();
    public static final UsersTable users = new UsersTable();

    public static List<AbsTable> list() {
        List<AbsTable> tables = new ArrayList<AbsTable>();

        try {
            for (Field field : Tables.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()))
                    tables.add((AbsTable) field.get(null));
            }
        } catch (IllegalAccessException ignored) {
        }

        return tables;
    }
}
