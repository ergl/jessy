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
import org.imdea.rubis.benchmark.entity.RegionEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;
import org.imdea.rubis.benchmark.exception.UnaccessibleIndexException;

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
    private long mRegionId;

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

        putExtra(SPSI.LEVEL, SPSI.SER);
    }

    private void createNeededIndexeEntities() {
        createIndex(bids.user_id).justEmpty().forKey(mId);
        createIndex(buy_now.buyer_id).justEmpty().forKey(mId);
        createIndex(comments.from_user_id).justEmpty().forKey(mId);
        createIndex(comments.to_user_id).justEmpty().forKey(mId);
        createIndex(items.seller).justEmpty().forKey(mId);
        createIndex(users.nickname).withPointer(mId).forKey(mNickname);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            mRegionId = -1;

            for (long regionId : readScannerOf(regions).getPointers()) {
                RegionEntity region = readEntityFrom(regions).withKey(regionId);

                if (region.getName().equals(mRegion))
                    mRegionId = regionId;
            }

            if (mRegionId != -1) {
                IndexEntity nickIndex = null;

                try {
                    nickIndex = readIndex(users.nickname).find(mNickname);
                } catch (UnaccessibleIndexException ignored) {
                    // In the actual implementation when the index does not exist (i.e. the nickname is not in the
                    // database) an UnaccessibleIndexException is thrown. So nickIndex remains null.
                }

                if (nickIndex == null || nickIndex.getPointers().size() == 0) {
                    UserEntity user = new UserEntity(mId, mFirstname, mLastname, mNickname, mPassword, mEmail, mRating,
                            mBalance, mCreationDate, mRegionId);
                    create(user);
                    createNeededIndexeEntities();
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
        IndexEntity regionIndex = readIndex(users.region).find(mRegionId);
        regionIndex.edit().addPointer(mId).write(this);
    }
}
