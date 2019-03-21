package com.app.huaweiblogplus.model;

import com.app.huaweiblogplus.realm.table.CategoryRealm;

import java.io.Serializable;

public class Tags implements Serializable {

    public long id = -1;
    public String slug = "";
    public String title = "";
    public String description = "";
    public long parent = -1;
    public long post_count = -1;

    public CategoryRealm getObjectRealm(){
        CategoryRealm c = new CategoryRealm();
        c.id = id;
        c.slug = slug;
        c.title = title;
        c.description = description;
        c.parent = parent;
        c.post_count = post_count;
        return c;
    }
}
