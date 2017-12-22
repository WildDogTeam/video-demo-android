package com.wilddog.conversation.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.wilddog.conversation.R;

/**
 * Created by Administrator on 2015/12/17.
 */
public class ImageLoadingUtil {
    public static void Load(String imgUrl, ImageView imageView){
               ImageLoader.getInstance().displayImage(imgUrl, imageView);

    }

    public static void Load2(String imgUrl, ImageView imageView){
        ImageLoader.getInstance().displayImage(imgUrl, imageView,options);
    }


   static DisplayImageOptions options=new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.logo) // resource or drawable
            .showImageForEmptyUri(R.drawable.logo) // resource or// drawable
            .showImageOnFail(R.drawable.logo) // resource or
                    // drawable
            .resetViewBeforeLoading(false) // default
            .delayBeforeLoading(1000).cacheInMemory(true) // default
            .cacheOnDisk(true) // default
            .considerExifParams(false) // default
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
            .bitmapConfig(Bitmap.Config.RGB_565) // default
            .displayer(new SimpleBitmapDisplayer()) // default
            .handler(new Handler())
            // default
            .build();


}
