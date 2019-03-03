package com.app.huaweiblog.connection.callbacks;

import java.util.ArrayList;
import java.util.List;

import com.app.huaweiblog.model.Category;
import com.app.huaweiblog.model.Post;

public class CallbackCategoryDetails {

    public String status = "";
    public int count = -1;
    public int pages = -1;
    public Category category = null;
    public List<Post> posts = new ArrayList<>();
}
