package com.app.huaweiblogplus.plus.model;

import com.app.huaweiblogplus.plus.realm.table.ImageRealm;

import java.io.Serializable;

public class Image implements Serializable {

    public String url;

    public ImageRealm getObjectRealm() {
        ImageRealm i = new ImageRealm();
        i.url = url;
        return i;
    }
}