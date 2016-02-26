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
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadRequestKey;
import fr.inria.jessy.transaction.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        Collection<UserEntity.NicknameIndex> pointers = readIndex(UserEntity.NicknameIndex.class, "mNickname", nickname);

        if (pointers.size() > 0) {
            UserEntity.NicknameIndex pointer = pointers.iterator().next();
            UserEntity user = read(UserEntity.class, pointer.getUserId());

            if (user.getPassword().equals(password))
                return user.getId();
        }

        return -1;
    }

    private void init() {
        putExtra(NAME, getClass().getSimpleName());
    }

    public <E extends JessyEntity> E read(Class<E> clazz, long value) throws Exception {
        return read(clazz, Long.toString(value));
    }

    protected <E extends JessyEntity, SK> Collection<E> readIndex(Class<E> clazz, String key, SK value) throws
            Exception {
        ReadRequestKey<SK> requestKey = new ReadRequestKey<>(key, value);
        List<ReadRequestKey<?>> requestKeys = new ArrayList<ReadRequestKey<?>>();
        requestKeys.add(requestKey);
        Collection<E> entities = read(clazz, requestKeys);
        return entities != null ? entities : new ArrayList<E>();
    }
}
