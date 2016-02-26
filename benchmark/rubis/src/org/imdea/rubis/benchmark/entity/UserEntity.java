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

package org.imdea.rubis.benchmark.entity;

import static fr.inria.jessy.ConstantPool.JESSY_MID;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.transaction.Transaction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@Entity
public class UserEntity extends JessyEntity implements Externalizable {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor {
        private float mBalance;
        private Date mCreationDate;
        private String mEmail;
        private String mFirstname;
        private long mId;
        private String mLastname;
        private String mNickname;
        private String mPassword;
        private int mRating;
        private long mRegionId;

        Editor(UserEntity source) {
            mBalance = source.getBalance();
            mCreationDate = source.getCreationDate();
            mEmail = source.getEmail();
            mFirstname = source.getFirstname();
            mId = source.getId();
            mLastname = source.getLastname();
            mNickname = source.getNickname();
            mPassword = source.getPassword();
            mRating = source.getRating();
            mRegionId = source.getRegionId();
        }

        private UserEntity done() {
            return new UserEntity(mId, mFirstname, mLastname, mNickname, mPassword, mEmail, mRating, mBalance,
                    mCreationDate, mRegionId);
        }

        public Editor setBalance(float balance) {
            mBalance = balance;
            return this;
        }

        public Editor setCreationDate(Date creationDate) {
            mCreationDate = creationDate;
            return this;
        }

        public Editor setEmail(String email) {
            mEmail = email;
            return this;
        }

        public Editor setFirstname(String firstname) {
            mFirstname = firstname;
            return this;
        }

        public Editor setId(long id) {
            mId = id;
            return this;
        }

        public Editor setLastname(String lastname) {
            mLastname = lastname;
            return this;
        }

        public Editor setNickname(String nickname) {
            mNickname = nickname;
            return this;
        }

        public Editor setPassword(String password) {
            mPassword = password;
            return this;
        }

        public Editor setRating(int rating) {
            mRating = rating;
            return this;
        }

        public Editor setRegionId(long regionId) {
            mRegionId = regionId;
            return this;
        }

        public void write(Transaction trans) {
            trans.write(done());
        }
    }

    @Entity
    public static class NicknameIndex extends JessyEntity implements Externalizable {
        private static final AtomicLong sSequence = new AtomicLong();

        @SecondaryKey(relate = Relationship.ONE_TO_ONE)
        @SuppressWarnings("unused")
        private String mNickname;
        private long mUserId;

        @Deprecated
        public NicknameIndex() {
            super("");
        }

        public NicknameIndex(String nickname, long userId) {
            super("?users~id#" + sSequence.incrementAndGet() + ":nickname#" + nickname);
            mNickname = nickname;
            mUserId = userId;
        }

        @Override
        public void clearValue() {
        }

        @Override
        public Object clone() {
            NicknameIndex entity = (NicknameIndex) super.clone();
            entity.mNickname = mNickname;
            entity.mUserId = mUserId;
            return entity;
        }

        public String getUserKey() {
            return "@users~id#" + mUserId;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            mNickname = (String) in.readObject();
            mUserId = in.readLong();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeObject(mNickname);
            out.writeLong(mUserId);
        }
    }

    @Entity
    public static class RegionIdIndex extends JessyEntity implements Externalizable {
        private static final AtomicLong sSequence = new AtomicLong();

        @SecondaryKey(relate = Relationship.MANY_TO_ONE)
        @SuppressWarnings("unused")
        private long mRegionId;
        private long mUserId;

        @Deprecated
        public RegionIdIndex() {
            super("");
        }

        public RegionIdIndex(long regionId, long userId) {
            super("?users~id#" + sSequence.incrementAndGet() + ":region_id#" + regionId);
            mRegionId = regionId;
            mUserId = userId;
        }

        @Override
        public void clearValue() {
        }

        @Override
        public Object clone() {
            RegionIdIndex entity = (RegionIdIndex) super.clone();
            entity.mRegionId = mRegionId;
            entity.mUserId = mUserId;
            return entity;
        }

        public String getUserKey() {
            return "@users~id#" + mUserId;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            mRegionId = in.readLong();
            mUserId = in.readLong();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeLong(mRegionId);
            out.writeLong(mUserId);
        }
    }

    private float mBalance;
    private Date mCreationDate;
    private String mEmail;
    private String mFirstname;
    private long mId;
    private String mLastname;
    private String mNickname;
    private String mPassword;
    private int mRating;
    private long mRegionId;

    @Deprecated
    public UserEntity() {
        super("");
    }

    public UserEntity(long id, String firstname, String lastname, String nickname, String password, String email, int
            rating, float balance, Date creationDate, long regionId) {
        super("@user~id#" + id);

        mId = id;
        mFirstname = firstname;
        mLastname = lastname;
        mNickname = nickname;
        mPassword = password;
        mEmail = email;
        mRating = rating;
        mBalance = balance;
        mCreationDate = creationDate;
        mRegionId = regionId;
    }

    @Override
    public void clearValue() {
    }

    @Override
    public Object clone() {
        UserEntity entity = (UserEntity) super.clone();
        entity.mId = mId;
        entity.mFirstname = mFirstname;
        entity.mLastname = mLastname;
        entity.mNickname = mNickname;
        entity.mPassword = mPassword;
        entity.mEmail = mEmail;
        entity.mRating = mRating;
        entity.mBalance = mBalance;
        entity.mCreationDate = (Date) mCreationDate.clone();
        entity.mRegionId = mRegionId;
        return entity;
    }

    public Editor edit() {
        return new Editor(this);
    }

    public float getBalance() {
        return mBalance;
    }

    public Date getCreationDate() {
        return mCreationDate;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getFirstname() {
        return mFirstname;
    }

    public long getId() {
        return mId;
    }

    public String getLastname() {
        return mLastname;
    }

    public String getNickname() {
        return mNickname;
    }

    public String getPassword() {
        return mPassword;
    }

    public int getRating() {
        return mRating;
    }

    public long getRegionId() {
        return mRegionId;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        mId = in.readLong();
        mFirstname = (String) in.readObject();
        mLastname = (String) in.readObject();
        mNickname = (String) in.readObject();
        mPassword = (String) in.readObject();
        mEmail = (String) in.readObject();
        mRating = in.readInt();
        mBalance = in.readFloat();
        mCreationDate = (Date) in.readObject();
        mRegionId = in.readLong();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(mId);
        out.writeObject(mFirstname);
        out.writeObject(mLastname);
        out.writeObject(mNickname);
        out.writeObject(mPassword);
        out.writeObject(mEmail);
        out.writeInt(mRating);
        out.writeFloat(mBalance);
        out.writeObject(mCreationDate);
        out.writeLong(mRegionId);
    }
}