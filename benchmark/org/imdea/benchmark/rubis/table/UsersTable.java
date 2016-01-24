package org.imdea.benchmark.rubis.table;

import org.imdea.benchmark.rubis.entity.UserEntity;

public class UsersTable extends AbsTable<UserEntity> {
    public final Index nickname;
    public final Index region;

    UsersTable() {
        super(UserEntity.class, "users");
        nickname = new Index(this, "nickname");
        region = new Index(this, "region");
    }
}
