package com.app.huaweiblog.plus.model;

import com.app.huaweiblog.plus.realm.table.ImageRealm;

import java.io.Serializable;

public class Image implements Serializable {

    public String url;

    public ImageRealm getObjectRealm() {
        ImageRealm i = new ImageRealm();
        i.url = url;
        return i;
    }
}