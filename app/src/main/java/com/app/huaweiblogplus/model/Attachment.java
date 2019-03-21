package com.app.huaweiblogplus.plus.model;

import com.app.huaweiblogplus.plus.realm.table.AttachmentRealm;

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
