package com.app.huaweiblog.model;

import com.app.huaweiblog.realm.table.ImageRealm;

import java.io.Serializable;

public class Image implements Serializable {

    public String url;

    public ImageRealm getObjectRealm() {
        ImageRealm i = new ImageRealm();
        i.url = url;
        return i;
    }
}