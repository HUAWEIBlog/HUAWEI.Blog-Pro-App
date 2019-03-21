package com.app.huaweiblogplus.model;

import java.io.Serializable;

import com.app.huaweiblogplus.realm.table.AuthorRealm;

public class Author implements Serializable {

    public long id = -1;
    public String slug = "";
    public String name = "";
    public String first_name = "";
    public String last_name = "";
    public String nickname = "";
    public String url = "";
    public String description = "";

    public AuthorRealm getObjectRealm() {
        AuthorRealm a = new AuthorRealm();
        a.id = id;
        a.slug = slug;
        a.name = name;
        a.first_name = first_name;
        a.last_name = last_name;
        a.nickname = nickname;
        a.url = url;
        a.description = description;
        return a;
    }

}
