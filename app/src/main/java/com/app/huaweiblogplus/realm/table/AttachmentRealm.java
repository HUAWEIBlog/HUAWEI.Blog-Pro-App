package com.app.huaweiblogplus.realm.table;

import com.app.huaweiblogplus.model.Attachment;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AttachmentRealm extends RealmObject {

    @PrimaryKey
    public long id = -1;
    public String url;
    public String mime_type;

    public Attachment getOriginal() {
        Attachment a = new Attachment();
        a.id = id;
        a.url = url;
        a.mime_type = mime_type;
        return a;
    }

}
