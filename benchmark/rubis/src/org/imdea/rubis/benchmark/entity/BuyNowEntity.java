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
public class BuyNowEntity extends JessyEntity implements Externalizable {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor {
        private long mBuyerId;
        private Date mDate;
        private long mId;
        private long mItemId;
        private int mQty;

        Editor(BuyNowEntity source) {
            mBuyerId = source.getBuyerId();
            mDate = source.getDate();
            mId = source.getId();
            mItemId = source.getItemId();
            mQty = source.getQty();
        }

        private BuyNowEntity done() {
            return new BuyNowEntity(mId, mBuyerId, mItemId, mQty, mDate);
        }

        public Editor setBuyerId(long buyerId) {
            mBuyerId = buyerId;
            return this;
        }

        public Editor setDate(Date date) {
            mDate = date;
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

        public Editor setQty(int qty) {
            mQty = qty;
            return this;
        }

        public void write(Transaction trans) {
            trans.write(done());
        }
    }

    @Entity
    public static class BuyerIdIndex extends JessyEntity implements Externalizable {
        private static final AtomicLong sSequence = new AtomicLong();

        @SecondaryKey(relate = Relationship.MANY_TO_ONE)
        @SuppressWarnings("unused")
        private long mBuyerId;
        private long mBuyNowId;

        @Deprecated
        public BuyerIdIndex() {
            super("");
        }

        public BuyerIdIndex(long buyNowId, long buyerId) {
            super("?buy_now~id#" + sSequence.incrementAndGet() + ":buyer_id#" + buyerId);
            mBuyNowId = buyNowId;
            mBuyerId = buyerId;
        }

        @Override
        public void clearValue() {
        }

        @Override
        public Object clone() {
            BuyerIdIndex entity = (BuyerIdIndex) super.clone();
            entity.mBuyerId = mBuyerId;
            entity.mBuyNowId = mBuyNowId;
            return entity;
        }

        public long getBuyNowId() {
            return mBuyNowId;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            mBuyerId = in.readLong();
            mBuyNowId = in.readLong();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeLong(mBuyerId);
            out.writeLong(mBuyNowId);
        }
    }

    @Entity
    public static class ItemIdIndex extends JessyEntity implements Externalizable {
        private static final AtomicLong sSequence = new AtomicLong();

        @SecondaryKey(relate = Relationship.MANY_TO_ONE)
        @SuppressWarnings("unused")
        private long mItemId;
        private long mBuyNowId;

        @Deprecated
        public ItemIdIndex() {
            super("");
        }

        public ItemIdIndex(long buyNowId, long itemId) {
            super("?buy_now~id#" + sSequence.incrementAndGet() + ":item_id#" + itemId);
            mBuyNowId = buyNowId;
            mItemId = itemId;
        }

        @Override
        public void clearValue() {
        }

        @Override
        public Object clone() {
            ItemIdIndex entity = (ItemIdIndex) super.clone();
            entity.mBuyNowId = mBuyNowId;
            entity.mItemId = mItemId;
            return entity;
        }

        public long getBuyNowId() {
            return mBuyNowId;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            mItemId = in.readLong();
            mBuyNowId = in.readLong();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeLong(mItemId);
            out.writeLong(mBuyNowId);
        }
    }

    private long mBuyerId;
    private Date mDate;
    private long mId;
    private long mItemId;
    private int mQty;

    @Deprecated
    public BuyNowEntity() {
        super("");
    }

    public BuyNowEntity(long id, long buyerId, long itemId, int qty, Date date) {
        super("@buy_now~id#" + Long.toString(id));
        mId = id;
        mBuyerId = buyerId;
        mItemId = itemId;
        mQty = qty;
        mDate = date;
    }

    @Override
    public void clearValue() {
    }

    @Override
    public Object clone() {
        BuyNowEntity entity = (BuyNowEntity) super.clone();
        entity.mId = mId;
        entity.mBuyerId = mBuyerId;
        entity.mItemId = mItemId;
        entity.mQty = mQty;
        entity.mDate = (Date) mDate.clone();
        return entity;
    }

    public Editor edit() {
        return new Editor(this);
    }

    public long getBuyerId() {
        return mBuyerId;
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

    public int getQty() {
        return mQty;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        mId = in.readLong();
        mBuyerId = in.readLong();
        mItemId = in.readLong();
        mQty = in.readInt();
        mDate = (Date) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(mId);
        out.writeLong(mBuyerId);
        out.writeLong(mItemId);
        out.writeInt(mQty);
        out.writeObject(mDate);
    }
}
