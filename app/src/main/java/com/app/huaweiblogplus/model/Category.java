package com.app.huaweiblogplus.model;

import java.io.Serializable;

import com.app.huaweiblogplus.realm.table.CategoryRealm;

public class Category implements Serializable {

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
