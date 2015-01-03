package com.acbelter.collager;

import com.acbelter.nslib.NetworkApplication;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "", // will not be used
        mailTo = "acbelter.app@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash)
public class CollageApplication extends NetworkApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        L.disableLogging();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();

        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileCount(100)
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);
    }
}
