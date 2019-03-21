package com.app.huaweiblogplus.plus.model;

import java.io.Serializable;

import com.app.huaweiblogplus.plus.realm.table.CommentRealm;

public class Comment implements Serializable {

    public Long id = -1L;
    public String name = "";
    public String url = "";
    public String date = "";
    public String content = "";
    public long parent = -1;

    public CommentRealm getObjectRealm() {
        CommentRealm c = new CommentRealm();
        c.id = id;
        c.name = name;
        c.url = url;
        c.date = date;
        c.content = content;
        c.parent = parent;
        return c;
    }

}
