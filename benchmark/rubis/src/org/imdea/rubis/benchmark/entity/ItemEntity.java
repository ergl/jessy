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
public class ItemEntity extends JessyEntity implements Externalizable {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor {
        private float mBuyNow;
        private String mCategoryKey;
        private String mDescription;
        private Date mEndDate;
        private long mId;
        private float mInitialPrice;
        private float mMaxBid;
        private String mName;
        private int mNbOfBids;
        private int mQuantity;
        private float mReservePrice;
        private String mSeller;
        private Date mStartDate;

        Editor(ItemEntity source) {
            mBuyNow = source.getBuyNow();
            mCategoryKey = source.getCategoryKey();
            mDescription = source.getDescription();
            mEndDate = source.getEndDate();
            mId = source.getId();
            mInitialPrice = source.getInitialPrice();
            mMaxBid = source.getMaxBid();
            mName = source.getName();
            mNbOfBids = source.getNbOfBids();
            mQuantity = source.getQuantity();
            mReservePrice = source.getReservePrice();
            mSeller = source.getSellerKey();
            mStartDate = source.getStartDate();
        }

        private ItemEntity done() {
            return new ItemEntity(mId, mName, mDescription, mInitialPrice, mQuantity, mReservePrice, mBuyNow,
                    mNbOfBids, mMaxBid, mStartDate, mEndDate, mSeller, mCategoryKey);
        }

        public Editor setBuyNow(float buyNow) {
            mBuyNow = buyNow;
            return this;
        }

        public Editor setCategoryKey(String categoryKey) {
            mCategoryKey = categoryKey;
            return this;
        }

        public Editor setDescription(String description) {
            mDescription = description;
            return this;
        }

        public Editor setEndDate(Date endDate) {
            mEndDate = endDate;
            return this;
        }

        public Editor setId(long id) {
            mId = id;
            return this;
        }

        public Editor setInitialPrice(float initialPrice) {
            mInitialPrice = initialPrice;
            return this;
        }

        public Editor setMaxBid(float maxBid) {
            mMaxBid = maxBid;
            return this;
        }

        public Editor setName(String name) {
            mName = name;
            return this;
        }

        public Editor setNbOfBids(int nbOfBids) {
            mNbOfBids = nbOfBids;
            return this;
        }

        public Editor setQuantity(int quantity) {
            mQuantity = quantity;
            return this;
        }

        public Editor setReservePrice(float reservePrice) {
            mReservePrice = reservePrice;
            return this;
        }

        public Editor setSellerKey(String sellerKey) {
            mSeller = sellerKey;
            return this;
        }

        public Editor setStartDate(Date startDate) {
            mStartDate = startDate;
            return this;
        }

        public void write(Transaction trans) {
            trans.write(done());
        }
    }

    private float mBuyNow;
    @SecondaryKey(relate = MANY_TO_ONE)
    private String mCategoryKey;
    private String mDescription;
    private Date mEndDate;
    private long mId;
    private float mInitialPrice;
    private float mMaxBid;
    private String mName;
    private int mNbOfBids;
    private int mQuantity;
    private float mReservePrice;
    @SecondaryKey(relate = MANY_TO_ONE)
    private String mSellerKey;
    private Date mStartDate;

    @Deprecated
    public ItemEntity() {
        super("");
    }

    public ItemEntity(long id, String name, String description, float initialPrice, int quantity, float reservePrice,
                      float buyNow, int nbOfBids, float maxBid, Date startDate, Date endDate, String sellerKey, String
                              categoryKey) {
        super("items~id#" + id + "~category_key#" + categoryKey + "~seller_key#" + sellerKey);
        mId = id;
        mName = name;
        mDescription = description;
        mInitialPrice = initialPrice;
        mQuantity = quantity;
        mReservePrice = reservePrice;
        mBuyNow = buyNow;
        mNbOfBids = nbOfBids;
        mMaxBid = maxBid;
        mStartDate = startDate;
        mEndDate = endDate;
        mSellerKey = sellerKey;
        mCategoryKey = categoryKey;
    }

    @Override
    public void clearValue() {
        throw new UnsupportedOperationException("This object is immutable.");
    }


    @Override
    public Object clone() {
        ItemEntity entity = (ItemEntity) super.clone();
        entity.mId = mId;
        entity.mName = mName;
        entity.mDescription = mDescription;
        entity.mInitialPrice = mInitialPrice;
        entity.mQuantity = mQuantity;
        entity.mReservePrice = mReservePrice;
        entity.mBuyNow = mBuyNow;
        entity.mNbOfBids = mNbOfBids;
        entity.mMaxBid = mMaxBid;
        entity.mStartDate = (Date) mStartDate.clone();
        entity.mEndDate = (Date) mEndDate.clone();
        entity.mSellerKey = mSellerKey;
        entity.mCategoryKey = mCategoryKey;
        return entity;
    }

    public Editor edit() {
        return new Editor(this);
    }

    public float getBuyNow() {
        return mBuyNow;
    }

    public String getCategoryKey() {
        return mCategoryKey;
    }

    public String getDescription() {
        return mDescription;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public long getId() {
        return mId;
    }

    public float getInitialPrice() {
        return mInitialPrice;
    }

    public float getMaxBid() {
        return mMaxBid;
    }

    public String getName() {
        return mName;
    }

    public int getNbOfBids() {
        return mNbOfBids;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public float getReservePrice() {
        return mReservePrice;
    }

    public String getSellerKey() {
        return mSellerKey;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        mId = in.readLong();
        mName = (String) in.readObject();
        mDescription = (String) in.readObject();
        mInitialPrice = in.readFloat();
        mQuantity = in.readInt();
        mReservePrice = in.readFloat();
        mBuyNow = in.readFloat();
        mNbOfBids = in.readInt();
        mMaxBid = in.readFloat();
        mStartDate = (Date) in.readObject();
        mEndDate = (Date) in.readObject();
        mSellerKey = (String) in.readObject();
        mCategoryKey = (String) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(mId);
        out.writeObject(mName);
        out.writeObject(mDescription);
        out.writeFloat(mInitialPrice);
        out.writeInt(mQuantity);
        out.writeFloat(mReservePrice);
        out.writeFloat(mBuyNow);
        out.writeInt(mNbOfBids);
        out.writeFloat(mMaxBid);
        out.writeObject(mStartDate);
        out.writeObject(mEndDate);
        out.writeObject(mSellerKey);
        out.writeObject(mCategoryKey);
    }
}
