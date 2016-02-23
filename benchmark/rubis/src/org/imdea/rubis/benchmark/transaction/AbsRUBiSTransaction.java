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
import fr.inria.jessy.transaction.Transaction;

import java.util.Collection;

import org.imdea.rubis.benchmark.entity.UserEntity;

public abstract class AbsRUBiSTransaction extends Transaction {
    public static final String NAME = AbsRUBiSTransaction.class.getName() + "::NAME";

    public enum Outcome {
        SUCCESS, FAILURE, ERROR
    }

    public AbsRUBiSTransaction(Jessy jessy) throws Exception {
        super(jessy);
        init();
    }

    public AbsRUBiSTransaction(Jessy jessy, int readOperations, int updateOperations, int createOperations) throws
            Exception {
        super(jessy, readOperations, updateOperations, createOperations);
        init();
    }

    protected long authenticate(String nickname, String password) throws Exception {
        Collection<UserEntity> users = readBySecondary(UserEntity.class, "mNickname", nickname);

        if (users != null && users.size() > 0) {
            UserEntity user = users.iterator().next();

            if (user.getPassword().equals(password))
                return user.getId();
        }

        return -1;
    }

    private void init() {
        putExtra(NAME, getClass().getSimpleName());
    }
}
