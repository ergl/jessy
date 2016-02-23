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

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Collection;

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

    public BrowseCategoriesTransaction(Jessy jessy, String regionName) throws Exception {
        this(jessy, regionName, "", "");
        mRegionName = regionName;
    }

    public BrowseCategoriesTransaction(Jessy jessy, String nickname, String password) throws Exception {
        this(jessy, "", nickname, password);
    }

    public BrowseCategoriesTransaction(Jessy jessy, String regionName, String nickname, String password) throws
            Exception {
        super(jessy);
        mRegionName = regionName;
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
                Collection<RegionEntity.NameIndex> pointers = readIndex(RegionEntity.NameIndex.class, "mName",
                        mRegionName);

                if (pointers.size() == 0)
                    return commitTransaction();
            }


            Collection<CategoryEntity.Scanner> pointers = readIndex(CategoryEntity.Scanner.class, "mDummy",
                    (String) null);

            for (CategoryEntity.Scanner pointer : pointers) {
                CategoryEntity category = read(CategoryEntity.class, pointer.getCategoryId());
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
