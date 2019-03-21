package com.app.huaweiblogplus.realm.table;

import com.app.huaweiblogplus.model.Image;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ImageRealm extends RealmObject {

    @PrimaryKey
    public String url;

    public Image getOriginal() {
        Image i = new Image();
        i.url = url;
        return i;
    }
}
