package com.app.huaweiblog.plus.model;

import com.app.huaweiblog.plus.realm.table.AttachmentRealm;

import java.io.Serializable;

public class Attachment implements Serializable {
    public long id = -1;
    public String url;
    public String mime_type;

    public AttachmentRealm getObjectRealm() {
        AttachmentRealm a = new AttachmentRealm();
        a.id = id;
        a.url = url;
        a.mime_type = mime_type;
        return a;
    }
}
