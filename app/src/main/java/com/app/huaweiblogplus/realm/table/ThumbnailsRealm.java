package com.app.huaweiblogplus.plus.realm.table;

import com.app.huaweiblogplus.plus.model.Thumbnails;

import io.realm.RealmObject;

public class ThumbnailsRealm extends RealmObject {

    public ImageRealm full = new ImageRealm();
    public ImageRealm medium = new ImageRealm();
    public ImageRealm medium_large = new ImageRealm();
    public ImageRealm large = new ImageRealm();

    public Thumbnails getOriginal() {
        Thumbnails t = new Thumbnails();
        t.full = (full != null ? full.getOriginal() : null);
        t.medium = (medium != null ? medium.getOriginal() : null);
        t.medium_large = (medium_large != null ? medium_large.getOriginal() : null);
        t.large = (large != null ? large.getOriginal() : null);
        return t;
    }
}
