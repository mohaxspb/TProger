package ru.kuchanov.tproger.utils;

import android.graphics.drawable.Animatable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;

/**
 * Created by Юрий on 06.01.2016 16:11.
 * For ExpListTest.
 */
public class FrescoUtils
{
    public static final String LOG = FrescoUtils.class.getSimpleName();

    public static ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFinalImageSet(
                String id,
                @Nullable ImageInfo imageInfo,
                @Nullable Animatable anim) {
            if (imageInfo == null) {
                return;
            }
            QualityInfo qualityInfo = imageInfo.getQualityInfo();
//            FLog.d("Final image received! " +
//                            "Size %d x %d",
//                    "Quality level %d, good enough: %s, full quality: %s",
//                    imageInfo.getWidth(),
//                    imageInfo.getHeight(),
//                    qualityInfo.getQuality(),
//                    qualityInfo.isOfGoodEnoughQuality(),
//                    qualityInfo.isOfFullQuality());
            Log.i("Final image received!", "Size " + imageInfo.getWidth() + " x " + imageInfo.getHeight());
        }

        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            Log.i(LOG, "Intermediate image received");
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            Log.e(LOG, "Error loading!");
        }
    };
}
