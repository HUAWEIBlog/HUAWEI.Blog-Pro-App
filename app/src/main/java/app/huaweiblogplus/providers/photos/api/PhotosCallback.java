package app.huaweiblogplus.providers.photos.api;

import app.huaweiblogplus.providers.photos.PhotoItem;

import java.util.ArrayList;

public interface PhotosCallback {

    void completed(ArrayList<PhotoItem> photos, boolean canLoadMore);
    void failed();
}
