package ru.kuchanov.tproger.custom.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import ru.kuchanov.tproger.utils.MyUIL;

/**
 * Created by Юрий on 31.12.2015 17:48.
 * For ExpListTest.
 */
public class GifDecoderView extends ImageView
{
    public static final String LOG = GifDecoderView.class.getSimpleName();

    final Handler mHandler = new Handler();
    private boolean mIsPlayingGif = false;
    private GifDecoder mGifDecoder;
    private Bitmap mTmpBitmap;
    final Runnable mUpdateResults = new Runnable()
    {
        public void run()
        {
            if (mTmpBitmap != null && !mTmpBitmap.isRecycled())
            {
                GifDecoderView.this.setImageBitmap(mTmpBitmap);
            }
        }
    };

    private ImageLoader imageLoader;

//    public GifDecoderView(Context context, InputStream stream)
//    {
//        super(context);
//        playGif(stream);
//    }

    public GifDecoderView(Context context, String url)
    {
        super(context);

        imageLoader = MyUIL.get(context);

        imageLoader.loadImage(url, MyUIL.getSimple(), new SimpleImageLoadingListener()
        {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
            {
                super.onLoadingComplete(imageUri, view, loadedImage);
                Log.i(LOG, "onLoadingComplete for URI: " + imageUri);

                File file = DiskCacheUtils.findInCache(imageUri, imageLoader.getDiskCache());

                try
                {
                    FileInputStream in = new FileInputStream(file);
                    playGif(in);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void playGif(InputStream stream)
    {
        Log.i(LOG, "playGif");
        mGifDecoder = new GifDecoder();
        mGifDecoder.read(stream);

        mIsPlayingGif = true;

        new Thread(new Runnable()
        {
            public void run()
            {
                final int n = mGifDecoder.getFrameCount();
                final int ntimes = mGifDecoder.getLoopCount();
                int repetitionCounter = 0;
                do
                {
                    for (int i = 0; i < n; i++)
                    {
                        mTmpBitmap = mGifDecoder.getFrame(i);
                        int t = mGifDecoder.getDelay(i);
                        mHandler.post(mUpdateResults);
                        try
                        {
                            Thread.sleep(t);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    if (ntimes != 0)
                    {
                        repetitionCounter++;
                    }
                } while (mIsPlayingGif && (repetitionCounter <= ntimes));
            }
        }).start();
    }

    public void stopRendering()
    {
        mIsPlayingGif = true;
    }
}