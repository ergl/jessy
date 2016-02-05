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

import static fr.inria.jessy.ConstantPool.*;

import static org.imdea.rubis.benchmark.table.Tables.*;

import com.sleepycat.persist.model.Entity;

import fr.inria.jessy.transaction.Transaction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

@Entity
public class BidEntity extends AbsTableEntity implements Externalizable {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor implements AbsRUBiSEntity.Editor {
        private float mBid;
        private Date mDate;
        private long mId;
        private long mItemId;
        private float mMaxBid;
        private int mQty;
        private long mUserId;

        Editor(BidEntity source) {
            mBid = source.getBid();
            mDate = source.getDate();
            mId = source.getId();
            mItemId = source.getItemId();
            mMaxBid = source.getMaxBid();
            mQty = source.getQty();
            mUserId = source.getUserId();
        }

        private BidEntity done() {
            return new BidEntity(mId, mUserId, mItemId, mQty, mBid, mMaxBid, mDate);
        }

        public Editor setBid(float bid) {
            this.mBid = bid;
            return this;
        }

        public Editor setDate(Date date) {
            this.mDate = date;
            return this;
        }

        public Editor setId(long id) {
            this.mId = id;
            return this;
        }

        public Editor setItemId(long itemId) {
            this.mItemId = itemId;
            return this;
        }

        public Editor setMaxBid(float maxBid) {
            this.mMaxBid = maxBid;
            return this;
        }

        public Editor setQty(int qty) {
            this.mQty = qty;
            return this;
        }

        public Editor setUserId(long userId) {
            this.mUserId = userId;
            return this;
        }

        public void write(Transaction trans) {
            trans.write(done());
        }
    }

    private float mBid;
    private Date mDate;
    private long mId;
    private long mItemId;
    private float mMaxBid;
    private int mQty;
    private long mUserId;

    @Deprecated
    public BidEntity() {
    }

    public BidEntity(long id, long userId, long itemId, int qty, float bid, float maxBid, Date date) {
        super(bids, id);

        mId = id;
        mUserId = userId;
        mItemId = itemId;
        mQty = qty;
        mBid = bid;
        mMaxBid = maxBid;
        mDate = date;
    }

    @Override
    public Object clone() {
        BidEntity entity = (BidEntity) super.clone();
        entity.mId = mId;
        entity.mUserId = mUserId;
        entity.mItemId = mItemId;
        entity.mQty = mQty;
        entity.mBid = mBid;
        entity.mMaxBid = mMaxBid;
        entity.mDate = (Date) mDate.clone();
        return entity;
    }

    public Editor edit() {
        return new Editor(this);
    }

    public float getBid() {
        return mBid;
    }

    public Date getDate() {
        return mDate;
    }

    public long getId() {
        return mId;
    }

    public long getItemId() {
        return mItemId;
    }

    public float getMaxBid() {
        return mMaxBid;
    }

    public int getQty() {
        return mQty;
    }

    public long getUserId() {
        return mUserId;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        mId = in.readLong();
        mUserId = in.readLong();
        mItemId = in.readLong();
        mQty = in.readInt();
        mBid = in.readFloat();
        mMaxBid = in.readFloat();
        mDate = (Date) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeLong(mId);
        out.writeLong(mUserId);
        out.writeLong(mItemId);
        out.writeInt(mQty);
        out.writeFloat(mBid);
        out.writeFloat(mMaxBid);
        out.writeObject(mDate);
    }
}
