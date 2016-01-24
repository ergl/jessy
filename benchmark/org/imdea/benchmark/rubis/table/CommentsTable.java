package org.imdea.benchmark.rubis.table;

import org.imdea.benchmark.rubis.entity.CommentEntity;

public class CommentsTable extends AbsTable<CommentEntity> {
    public final Index from_user_id;
    public final Index item_id;
    public final Index to_user_id;

    CommentsTable() {
        super(CommentEntity.class, "comments");
        from_user_id = new Index(this, "from_user_id");
        item_id = new Index(this, "item_id");
        to_user_id = new Index(this, "to_user_id");
    }
}
