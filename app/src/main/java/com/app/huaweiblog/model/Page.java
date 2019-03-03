package com.app.huaweiblog.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page implements Serializable {

    public long id = -1;
    public String type = "";
    public String slug = "";
    public String url = "";
    public String status = "";
    public String title = "";
    public String title_plain = "";
    public String content = "";
    public String excerpt = "";
    public String date = "";
    public String modified = "";
    public String thumbnail = "";

    public Author author = null;
    public List<Attachment> attachments = new ArrayList<>();
    public Thumbnails thumbnail_images = null;

    public Page() {
    }

    public Page(Post post) {
        this.id = post.id;
        this.type = post.type;
        this.slug = post.slug;
        this.url = post.url;
        this.status = post.status;
        this.title = post.title;
        this.title_plain = post.title_plain;
        this.content = post.content;
        this.excerpt = post.excerpt;
        this.date = post.date;
        this.modified = post.modified;
        this.thumbnail = post.thumbnail;

        this.author = post.author;
        this.attachments = post.attachments;
        this.thumbnail_images = post.thumbnail_images;
    }

    public boolean isDraft() {
        return !(content != null && !content.trim().equals(""));
    }

}
