package app.huaweiblogplus.providers.videos;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.util.HashMap;

public class PicassoVideoThumbnailHandler extends RequestHandler {

    public String EXTENSION = ".mp4";
    @Override
    public boolean canHandleRequest(Request data)
    {
        String url = data.uri.toString();
        return (url.contains(EXTENSION));
    }

    @Override
    public Result load(Request data, int arg1) throws IOException
    {
        Bitmap bitmap = null;
        try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
            mediaMetadataRetriever.setDataSource(data.uri.toString(), new HashMap<String, String>());
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException();
        }
        return new Result(bitmap, Picasso.LoadedFrom.NETWORK);
    }

    public static Picasso picassoWithVideoSupport(Context context){
        Picasso.Builder picassoBuilder = new Picasso.Builder(context);
        picassoBuilder.addRequestHandler(new PicassoVideoThumbnailHandler());
        return picassoBuilder.build();
    }
}
