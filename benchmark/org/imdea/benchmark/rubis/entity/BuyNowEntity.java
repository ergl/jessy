package org.imdea.benchmark.rubis.entity;

import static fr.inria.jessy.ConstantPool.JESSY_MID;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.transaction.Transaction;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

public class BuyNowEntity extends AbsTableEntity {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor implements AbsRUBiSEntity.Editor {
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

    private long mBuyerId;
    private Date mDate;
    private long mId;
    private long mItemId;
    private int mQty;

    public BuyNowEntity() {
    }

    public BuyNowEntity(long id, long buyerId, long itemId, int qty, Date date) {
        super(buy_now, id);

        mId = id;
        mBuyerId = buyerId;
        mItemId = itemId;
        mQty = qty;
        mDate = date;
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
