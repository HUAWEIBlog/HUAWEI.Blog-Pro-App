package com.app.huaweiblog.plus.connection.callbacks;

import java.util.ArrayList;
import java.util.List;

import com.app.huaweiblog.plus.model.Post;

public class CallbackListPost {

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public List<Post> posts = new ArrayList<>();
}
