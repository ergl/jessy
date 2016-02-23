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

import org.imdea.rubis.benchmark.entity.CommentEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;

public class ViewUserInfoTransaction extends AbsRUBiSTransaction {
    private String mUserKey;

    public ViewUserInfoTransaction(Jessy jessy, String userKey) throws Exception {
        super(jessy);
        mUserKey = userKey;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            UserEntity user = read(UserEntity.class, mUserKey);

            if (user != null) {
                Collection<CommentEntity> comments = readBySecondary(CommentEntity.class, "mToUserKey", mUserKey);

                for (CommentEntity comment : comments) {
                    UserEntity author = read(UserEntity.class, comment.getFromUserKey());
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
