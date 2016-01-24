package org.imdea.benchmark.rubis.entity;

import static fr.inria.jessy.ConstantPool.JESSY_MID;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.transaction.Transaction;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

public class CommentEntity extends AbsTableEntity {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor implements AbsRUBiSEntity.Editor {
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
    private long mFromUserId;
    private long mId;
    private long mItemId;
    private int mRating;
    private long mToUserId;
    
    public CommentEntity() {
    }

    public CommentEntity(long id, long fromUserId, long toUserId, long itemId, int rating, Date date, String comment) {
        super(comments, id);

        mId = id;
        mFromUserId = fromUserId;
        mToUserId = toUserId;
        mItemId = itemId;
        mRating = rating;
        mDate = date;
        mComment = comment;
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
