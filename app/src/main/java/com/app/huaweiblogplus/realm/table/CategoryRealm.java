package com.app.huaweiblogplus.realm.table;

import com.app.huaweiblogplus.model.Category;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CategoryRealm extends RealmObject {

    @PrimaryKey
    public long id = -1;
    public String slug = "";
    public String title = "";
    public String description = "";
    public long parent = -1;
    public long post_count = -1;

    public Category getOriginal() {
        Category c = new Category();
        c.id = id;
        c.slug = slug;
        c.title = title;
        c.description = description;
        c.parent = parent;
        c.post_count = post_count;
        return c;
    }

}
