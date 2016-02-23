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

import static com.sleepycat.persist.model.Relationship.*;

import static fr.inria.jessy.ConstantPool.JESSY_MID;

import com.sleepycat.persist.model.SecondaryKey;
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.transaction.Transaction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

public class CommentEntity extends JessyEntity implements Externalizable {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor {
        private String mComment;
        private Date mDate;
        private long mFromUserId;
        private long mId;
        private long mItemId;
        private int mRating;
        private long mToUserId;

        Editor(CommentEntity source) {
            mComment = source.getComment();
            mDate = source.getDate();
            mFromUserId = source.getFromUserId();
            mId = source.getId();
            mItemId = source.getItemId();
            mRating = source.getRating();
            mToUserId = source.getToUserId();
        }

        private CommentEntity done() {
            return new CommentEntity(mId, mFromUserId, mToUserId, mItemId, mRating, mDate, mComment);
        }

        public Editor setComment(String comment) {
            mComment = comment;
            return this;
        }

        public Editor setDate(Date date) {
            mDate = date;
            return this;
        }

        public Editor setFromUserId(long fromUserId) {
            mFromUserId = fromUserId;
            return this;
        }

        public Editor setId(long id) {
            mId = id;
            return this;
        }

        public Editor setItemId(long itemId) {
            mItemId = itemId;
            return this;
        }

        public Editor setRating(int rating) {
            mRating = rating;
            return this;
        }

        public Editor setToUserId(long toUserId) {
            mToUserId = toUserId;
            return this;
        }

        public void write(Transaction trans) {
            trans.write(done());
        }
    }

    private String mComment;
    private Date mDate;
    @SecondaryKey(relate = MANY_TO_ONE)
    private long mFromUserId;
    private long mId;
    @SecondaryKey(relate = MANY_TO_ONE)
    private long mItemId;
    private int mRating;
    @SecondaryKey(relate = MANY_TO_ONE)
    private long mToUserId;

    @Deprecated
    public CommentEntity() {
        super("");
    }

    public CommentEntity(long id, long fromUserId, long toUserId, long itemId, int rating, Date date, String comment) {
        super(Long.toString(id));
        mId = id;
        mFromUserId = fromUserId;
        mToUserId = toUserId;
        mItemId = itemId;
        mRating = rating;
        mDate = date;
        mComment = comment;
    }

    @Override
    public void clearValue() {
        throw new UnsupportedOperationException("This object is immutable.");
    }


    @Override
    public Object clone() {
        CommentEntity entity = (CommentEntity) super.clone();
        entity.mId = mId;
        entity.mFromUserId = mFromUserId;
        entity.mToUserId = mToUserId;
        entity.mItemId = mItemId;
        entity.mRating = mRating;
        entity.mDate = (Date) mDate.clone();
        entity.mComment = mComment;
        return entity;
    }

    public Editor edit() {
        return new Editor(this);
    }

    public String getComment() {
        return mComment;
    }

    public Date getDate() {
        return mDate;
    }

    public long getFromUserId() {
        return mFromUserId;
    }

    public long getId() {
        return mId;
    }

    public long getItemId() {
        return mItemId;
    }

    public int getRating() {
        return mRating;
    }

    public long getToUserId() {
        return mToUserId;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        mId = in.readLong();
        mFromUserId = in.readLong();
        mToUserId = in.readLong();
        mItemId = in.readLong();
        mRating = in.readInt();
        mDate = (Date) in.readObject();
        mComment = (String) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeLong(mId);
        out.writeLong(mFromUserId);
        out.writeLong(mToUserId);
        out.writeLong(mItemId);
        out.writeInt(mRating);
        out.writeObject(mDate);
        out.writeObject(mComment);
    }
}
