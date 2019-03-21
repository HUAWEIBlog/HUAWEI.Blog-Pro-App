package com.app.huaweiblogplus.realm.table;

import com.app.huaweiblogplus.model.Comment;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CommentRealm extends RealmObject {

    @PrimaryKey
    public Long id = -1L;
    public String name = "";
    public String url = "";
    public String date = "";
    public String content = "";
    public long parent = -1;

    public Comment getOriginal() {
        Comment c = new Comment();
        c.id = id;
        c.name = name;
        c.url = url;
        c.date = date;
        c.content = content;
        c.parent = parent;
        return c;
    }
}
