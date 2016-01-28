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
import fr.inria.jessy.consistency.SPSI;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Date;

import org.imdea.rubis.benchmark.entity.IndexEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;
import org.imdea.rubis.benchmark.table.UnaccessibleIndexException;

public class RegisterUserTransaction extends AbsRUBiSTransaction {
    private UserEntity mUser;

    public RegisterUserTransaction(Jessy jessy, long id, String firstname, String lastname, String nickname, String
            password, String email, int rating, float balance, Date creationDate, long region) throws
            Exception {
        super(jessy);
        putExtra(SPSI.LEVEL, SPSI.SER);
        mUser = new UserEntity(id, firstname, lastname, nickname, password, email, rating, balance, creationDate,
                region);
    }

    private void createNeededIndexeEntities() {
        // TODO: This sucks. For less spaghetti code index entities should be created on the fly, when needed, but the
        // TODO: actual code of Jessy doesn't allow this: reading a non-existent org.imdea.benchmark.rubis.entity will increase the fail read
        // TODO: count (after retrying 10 times) and a lot of code should be changed in order to avoid this.
        long id = mUser.getId();
        createIndexFor(bids.user_id, id);
        createIndexFor(buy_now.buyer_id, id);
        createIndexFor(comments.from_user_id, id);
        createIndexFor(comments.to_user_id, id);
        createIndexFor(items.seller, id);
        // TODO: For the same reason it is not convenient to create an index on the pair <nickname, password>. If there
        // TODO: is a mismatch, Jessy will try 10 times before giving up. D:
        createIndexFor(users.nickname, mUser.getNickname(), id);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            IndexEntity nickIndex = null;

            // TODO: Same problem here, if the nickname does not exist (which is very likely in the benchmark) Jessy
            // TODO: will try to read the index 10 times.
            try {
                nickIndex = readIndexFor(users.nickname, mUser.getNickname());
            } catch (UnaccessibleIndexException ignored) {
                // TODO: In the actual implementation when the index does not exist (i.e. the nickname is not in the
                // TODO: database) an UnaccessibleIndexException is thrown. So nickIndex remains null.
            }

            if (nickIndex == null || nickIndex.getPointers().size() == 0) {
                create(mUser);
                createNeededIndexeEntities();
                updateIndexes();
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateIndexes() {
        IndexEntity regionIndex = readIndexFor(users.region, mUser.getRegion());
        regionIndex.edit().addPointer(mUser.getId()).write(this);
    }
}
