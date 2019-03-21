package com.app.huaweiblog.plus.connection.callbacks;

import com.app.huaweiblog.plus.model.Tags;
import com.app.huaweiblog.plus.model.Post;

import java.util.ArrayList;
import java.util.List;

public class CallbackTagsDetails {

    public String status = "";
    public int count = -1;
    public int pages = -1;
    public Tags tags = null;
    public List<Post> posts = new ArrayList<>();
}
