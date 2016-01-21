package ru.kuchanov.tproger.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.logging.FLog;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.InputStream;
import java.util.concurrent.ExecutorCompletionService;

/**
 * Created by Юрий on 20.01.2016 21:54.
 * For TProger.
 */
public class ImageLoaderFresco implements Html.ImageGetter
{
    private Context ctx;
    private TextView textView;
    private UrlImageDownloader urlDrawable;

    public ImageLoaderFresco(Context ctx, TextView textView)
    {
        this.ctx = ctx;
        this.textView = textView;
    }

    public void setDataSubscriber(Context context, Uri uri/*, int width, int height*/)
    {
        DataSubscriber dataSubscriber = new BaseDataSubscriber<CloseableReference<CloseableBitmap>>()
        {
            @Override
            public void onNewResultImpl(
                    DataSource<CloseableReference<CloseableBitmap>> dataSource)
            {
                if (!dataSource.isFinished())
                {
                    return;
                }
                CloseableReference<CloseableBitmap> imageReference = dataSource.getResult();
                if (imageReference != null)
                {
                    final CloseableReference<CloseableBitmap> closeableReference = imageReference.clone();
                    try
                    {
                        CloseableBitmap closeableBitmap = closeableReference.get();
                        Bitmap bitmap = closeableBitmap.getUnderlyingBitmap();
                        if (bitmap != null && !bitmap.isRecycled())
                        {
                            //you can use bitmap here

//                            urlDrawable.drawable = new BitmapDrawable(ctx.getResources(), bitmap);

                            Log.i("fresco", "onNewResultImpl");

                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();

                            int newWidth = width;
                            int newHeight = height;

                            if (width > textView.getWidth())
                            {
                                newWidth = textView.getWidth();
                                newHeight = (newWidth * height) / width;
                            }

                            Drawable result = new BitmapDrawable(ctx.getResources(), bitmap);
                            result.setBounds(0, 0, newWidth, newHeight);

                            urlDrawable.setBounds(0, 0, newWidth, newHeight);

                            textView.setText(textView.getText());
                        }
                    }
                    finally
                    {
                        imageReference.close();
                        closeableReference.close();
                    }
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource)
            {
                Throwable throwable = dataSource.getFailureCause();
                // handle failure
            }
        };
        getBitmap(context, uri, /*width, height, */dataSubscriber);
    }

    /**
     * @param context
     * @param uri            //     * @param width
     *                       //     * @param height
     * @param dataSubscriber
     */
    public void getBitmap(Context context, Uri uri/*, int width, int height*/, DataSubscriber dataSubscriber)
    {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
//        if(width > 0 && height > 0){
//            builder.setResizeOptions(new ResizeOptions(width, height));
//        }
        ImageRequest request = builder.build();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(request, context);
//        dataSource.subscribe(dataSubscriber, UiThreadExecutorService.getInstance());
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

    @Override
    public Drawable getDrawable(String source)
    {
        urlDrawable = new UrlImageDownloader(ctx.getResources(), source);
        setDataSubscriber(ctx, Uri.parse(source));
        return urlDrawable;
    }

    public class UrlImageDownloader extends BitmapDrawable
    {
        public Drawable drawable;

        /**
         * Create a drawable by decoding a bitmap from the given input stream.
         *
         * @param res
         * @param is
         */
        public UrlImageDownloader(Resources res, InputStream is)
        {
            super(res, is);
        }

        /**
         * Create a drawable by opening a given file path and decoding the bitmap.
         *
         * @param res
         * @param filepath
         */
        public UrlImageDownloader(Resources res, String filepath)
        {
            super(res, filepath);
            drawable = new BitmapDrawable(res, filepath);
        }

        /**
         * Create drawable from a bitmap, setting initial target density based on
         * the display metrics of the resources.
         *
         * @param res
         * @param bitmap
         */
        public UrlImageDownloader(Resources res, Bitmap bitmap)
        {
            super(res, bitmap);
        }

        @Override
        public void draw(Canvas canvas)
        {
            // override the draw to facilitate refresh function later
            if (drawable != null)
            {
                drawable.draw(canvas);
            }
        }
    }


//    @Override
//    public Drawable getDrawable(String source)
//    {
//        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>()
//        {
//            @Override
//            public void onFinalImageSet(
//                    String id,
//                    @Nullable ImageInfo imageInfo,
//                    @Nullable Animatable anim)
//            {
//                if (imageInfo == null)
//                {
//                    return;
//                }
//                QualityInfo qualityInfo = imageInfo.getQualityInfo();
//                FLog.d("Final image received! " +
//                                "Size %d x %d",
//                        "Quality level %d, good enough: %s, full quality: %s",
//                        imageInfo.getWidth(),
//                        imageInfo.getHeight(),
//                        qualityInfo.getQuality(),
//                        qualityInfo.isOfGoodEnoughQuality(),
//                        qualityInfo.isOfFullQuality());
//            }
//
//            @Override
//            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo)
//            {
//                Log.d("TAG", "Intermediate image received");
//            }
//
//            @Override
//            public void onFailure(String id, Throwable throwable)
//            {
//                FLog.e(getClass(), throwable, "Error loading %s", id);
//            }
//        };
//
//
//        SimpleDraweeView draweeView = new SimpleDraweeView(ctx);
//
//        Uri uri = Uri.parse(source);
//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                .setControllerListener(controllerListener)
//                .setUri(uri)
//                .build();
//        draweeView.setController(controller);
//
////        draweeView.setImageURI(Uri.parse(source));
//        return draweeView.getTopLevelDrawable();
//    }
}
