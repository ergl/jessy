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
        private String mFromUserKey;
        private long mId;
        private String mItemKey;
        private int mRating;
        private String mToUserKey;

        Editor(CommentEntity source) {
            mComment = source.getComment();
            mDate = source.getDate();
            mFromUserKey = source.getFromUserKey();
            mId = source.getId();
            mItemKey = source.getItemKey();
            mRating = source.getRating();
            mToUserKey = source.getToUserKey();
        }

        private CommentEntity done() {
            return new CommentEntity(mId, mFromUserKey, mToUserKey, mItemKey, mRating, mDate, mComment);
        }

        public Editor setComment(String comment) {
            mComment = comment;
            return this;
        }

        public Editor setDate(Date date) {
            mDate = date;
            return this;
        }

        public Editor setFromUserKey(String fromUserKey) {
            mFromUserKey = fromUserKey;
            return this;
        }

        public Editor setId(long id) {
            mId = id;
            return this;
        }

        public Editor setItemKey(String itemKey) {
            mItemKey = itemKey;
            return this;
        }

        public Editor setRating(int rating) {
            mRating = rating;
            return this;
        }

        public Editor setToUserKey(String toUserKey) {
            mToUserKey = toUserKey;
            return this;
        }

        public void write(Transaction trans) {
            trans.write(done());
        }
    }

    private String mComment;
    private Date mDate;
    @SecondaryKey(relate = MANY_TO_ONE)
    private String mFromUserKey;
    private long mId;
    @SecondaryKey(relate = MANY_TO_ONE)
    private String mItemKey;
    private int mRating;
    @SecondaryKey(relate = MANY_TO_ONE)
    private String mToUserKey;

    @Deprecated
    public CommentEntity() {
        super("");
    }

    public CommentEntity(long id, String fromUserKey, String toUserKey, String itemKey, int rating, Date date, String
            comment) {
        super("comments~id#" + id + "~from_user_key#" + fromUserKey + "~item_key#" + itemKey + "~to_user_key#"
                + toUserKey);
        mId = id;
        mFromUserKey = fromUserKey;
        mToUserKey = toUserKey;
        mItemKey = itemKey;
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
        entity.mFromUserKey = mFromUserKey;
        entity.mToUserKey = mToUserKey;
        entity.mItemKey = mItemKey;
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

    public String getFromUserKey() {
        return mFromUserKey;
    }

    public long getId() {
        return mId;
    }

    public String getItemKey() {
        return mItemKey;
    }

    public int getRating() {
        return mRating;
    }

    public String getToUserKey() {
        return mToUserKey;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        mId = in.readLong();
        mFromUserKey = (String) in.readObject();
        mToUserKey = (String) in.readObject();
        mItemKey = (String) in.readObject();
        mRating = in.readInt();
        mDate = (Date) in.readObject();
        mComment = (String) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(mId);
        out.writeObject(mFromUserKey);
        out.writeObject(mToUserKey);
        out.writeObject(mItemKey);
        out.writeInt(mRating);
        out.writeObject(mDate);
        out.writeObject(mComment);
    }
}
