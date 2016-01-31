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

package org.imdea.rubis.benchmark.util;

import fr.inria.jessy.transaction.Transaction;

import org.imdea.rubis.benchmark.entity.AbsRUBiSEntity;
import org.imdea.rubis.benchmark.entity.ScannerEntity;
import org.imdea.rubis.benchmark.exception.UnaccessibleScannerException;
import org.imdea.rubis.benchmark.table.AbsTable;

public class ScannerHelper {
    private final Transaction mTrans;

    public ScannerHelper(Transaction trans) {
        mTrans = trans;
    }

    public <E extends AbsRUBiSEntity> void createScannerFor(AbsTable<E> table) {
        String id = generateId(table);
        mTrans.create(new ScannerEntity(id));
    }

    public static <E extends AbsRUBiSEntity> String generateId(AbsTable<E> table) {
        return Naming.of(table, "$id", "ALL");
    }

    public <E extends AbsRUBiSEntity> ScannerEntity readScannerOf(AbsTable<E> table) {
        String id = generateId(table);

        try {
            ScannerEntity entity = mTrans.read(ScannerEntity.class, id);

            if (entity == null)
                entity = new ScannerEntity(id);

            return entity;
        } catch (Exception e) {
            throw new UnaccessibleScannerException(id);
        }
    }
}
