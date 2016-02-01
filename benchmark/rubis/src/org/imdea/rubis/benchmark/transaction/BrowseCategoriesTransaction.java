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

package org.imdea.rubis.benchmark.transaction;

import static org.imdea.rubis.benchmark.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.rubis.benchmark.entity.CategoryEntity;
import org.imdea.rubis.benchmark.entity.RegionEntity;
import org.imdea.rubis.benchmark.util.TextUtils;

public class BrowseCategoriesTransaction extends AbsRUBiSTransaction {
    private String mNickname;
    private String mPassword;
    private String mRegionName;

    public BrowseCategoriesTransaction(Jessy jessy) throws Exception {
        this(jessy, "", "");
    }

    public BrowseCategoriesTransaction(Jessy jessy, String nickname, String password) throws Exception {
        this(jessy, "", nickname, password);
    }

    public BrowseCategoriesTransaction(Jessy jessy, String regionId, String nickname, String password) throws
            Exception {
        super(jessy);
        mRegionName = regionId;
        mNickname = nickname;
        mPassword = password;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            if (!TextUtils.isEmpty(mNickname) || !TextUtils.isEmpty(mPassword)) {
                long userId = authenticate(mNickname, mPassword);

                if (userId == -1)
                    return commitTransaction();
            }

            if (!TextUtils.isEmpty(mRegionName)) {
                boolean found = false;

                for (long regionId : readScannerOf(regions).getPointers()) {
                    RegionEntity region = readEntityFrom(regions).withKey(regionId);

                    if (region.getName().equals(mRegionName)) {
                        found = true;
                        break;
                    }
                }

                if (!found)
                    return commitTransaction();
            }

            for (long categoryId : readScannerOf(categories).getPointers()) {
                CategoryEntity category = readEntityFrom(categories).withKey(categoryId);
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
