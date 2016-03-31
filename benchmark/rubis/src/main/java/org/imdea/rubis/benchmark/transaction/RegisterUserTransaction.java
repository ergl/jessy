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
import fr.inria.jessy.consistency.SSERPSI;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Collection;
import java.util.Date;

import org.imdea.rubis.benchmark.entity.RegionEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;

public class RegisterUserTransaction extends AbsRUBiSTransaction {
    private float mBalance;
    private Date mCreationDate;
    private String mEmail;
    private String mFirstname;
    private long mId;
    private String mLastname;
    private String mNickname;
    private String mPassword;
    private int mRating;
    private String mRegion;
    private UserEntity mUser;

    public RegisterUserTransaction(Jessy jessy, long id, String firstname, String lastname, String nickname, String
            password, String email, String region) throws Exception {
        this(jessy, id, firstname, lastname, nickname, password, email, 0, 0.0f, new Date(), region);
    }

    public RegisterUserTransaction(Jessy jessy, long id, String firstname, String lastname, String nickname, String
            password, String email, int rating, float balance, Date creationDate, String region) throws
            Exception {
        super(jessy);

        mId = id;
        mFirstname = firstname;
        mLastname = lastname;
        mNickname = nickname;
        mPassword = password;
        mEmail = email;
        mRating = rating;
        mBalance = balance;
        mCreationDate = creationDate;
        mRegion = region;

        putExtra(SSERPSI.LEVEL, SSERPSI.SER);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            Collection<RegionEntity.NameIndex> pointers = readIndex(RegionEntity.NameIndex.class, "mName", mRegion);

            if (pointers.size() > 0) {
                String regionKey = pointers.iterator().next().getRegionKey();
                RegionEntity region = read(RegionEntity.class, regionKey);

                Collection<UserEntity.NicknameIndex> nicknames = readIndex(UserEntity.NicknameIndex.class,
                        "mNickname", mNickname);

                if (nicknames.size() == 0) {
                    mUser = new UserEntity(mId, mFirstname, mLastname, mNickname, mPassword, mEmail, mRating,
                            mBalance, mCreationDate, region.getId());

                    create(mUser);
                    updateIndexes();
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateIndexes() {
        create(new UserEntity.NicknameIndex(mUser.getNickname(), mUser.getId()));
        create(new UserEntity.RegionIdIndex(mUser.getRegionId(), mUser.getId()));
    }
}
