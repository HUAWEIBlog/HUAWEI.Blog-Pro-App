package com.app.huaweiblogplus.model;

import com.app.huaweiblogplus.realm.table.ThumbnailsRealm;

import java.io.Serializable;

public class Thumbnails implements Serializable {

    public Image full = null;
    public Image medium = null;
    public Image medium_large = null;
    public Image large = null;

    public ThumbnailsRealm getObjectRealm() {
        ThumbnailsRealm t = new ThumbnailsRealm();
        t.full = (full != null ? full.getObjectRealm() : null);
        t.medium = (medium != null ? medium.getObjectRealm() : null);
        t.medium_large = (medium_large != null ? medium_large.getObjectRealm() : null);
        t.large = (large != null ? large.getObjectRealm() : null);
        return t;
    }

}
