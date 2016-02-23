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

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.SecondaryKey;

import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.transaction.Transaction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

@Entity
public class BuyNowEntity extends JessyEntity implements Externalizable {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor {
        private String mBuyerKey;
        private Date mDate;
        private long mId;
        private String mItemKey;
        private int mQty;

        Editor(BuyNowEntity source) {
            mBuyerKey = source.getBuyerKey();
            mDate = source.getDate();
            mId = source.getId();
            mItemKey = source.getItemKey();
            mQty = source.getQty();
        }

        private BuyNowEntity done() {
            return new BuyNowEntity(mId, mBuyerKey, mItemKey, mQty, mDate);
        }

        public Editor setBuyerKey(String buyerKey) {
            mBuyerKey = buyerKey;
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

        public Editor setItemKey(String itemKey) {
            mItemKey = itemKey;
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

    @SecondaryKey(relate = MANY_TO_ONE)
    private String mBuyerKey;
    private Date mDate;
    private long mId;
    @SecondaryKey(relate = MANY_TO_ONE)
    private String mItemKey;
    private int mQty;

    @Deprecated
    public BuyNowEntity() {
        super("");
    }

    public BuyNowEntity(long id, String buyerKey, String itemKey, int qty, Date date) {
        super("buy_now~id#" + id + "~buyer_key#" + buyerKey + "~item_key#" + itemKey);
        mId = id;
        mBuyerKey = buyerKey;
        mItemKey = itemKey;
        mQty = qty;
        mDate = date;
    }

    @Override
    public void clearValue() {
        throw new UnsupportedOperationException("This object is immutable.");
    }

    @Override
    public Object clone() {
        BuyNowEntity entity = (BuyNowEntity) super.clone();
        entity.mId = mId;
        entity.mBuyerKey = mBuyerKey;
        entity.mItemKey = mItemKey;
        entity.mQty = mQty;
        entity.mDate = (Date) mDate.clone();
        return entity;
    }

    public Editor edit() {
        return new Editor(this);
    }

    public String getBuyerKey() {
        return mBuyerKey;
    }

    public Date getDate() {
        return mDate;
    }

    public long getId() {
        return mId;
    }

    public String getItemKey() {
        return mItemKey;
    }

    public int getQty() {
        return mQty;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        mId = in.readLong();
        mBuyerKey = (String) in.readObject();
        mItemKey = (String) in.readObject();
        mQty = in.readInt();
        mDate = (Date) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(mId);
        out.writeObject(mBuyerKey);
        out.writeObject(mItemKey);
        out.writeInt(mQty);
        out.writeObject(mDate);
    }
}
