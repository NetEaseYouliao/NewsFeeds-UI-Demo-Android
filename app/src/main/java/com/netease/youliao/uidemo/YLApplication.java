package com.netease.youliao.uidemo;

import android.app.Application;

import com.netease.nis.bugrpt.CrashHandler;
import com.netease.youliao.newsfeeds.core.NNewsFeedsSDK;
import com.netease.youliao.newsfeeds.utils.NNFLogUtil;

/**
 * Created by zhangdan on 2017/10/10.
 */

public class YLApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * 初始化SDK：在自定义Application中初始化网易有料API SDK
         */

        new NNewsFeedsSDK.Builder()
                .setAppKey(BuildConfig.APP_KEY)
                .setAppSecret(BuildConfig.APP_SECRET)
                .setContext(getApplicationContext())
                .setLogLevel(NNFLogUtil.LOG_VERBOSE)
                .setServerType(BuildConfig.SERVER_TYPE)
                .build();

        /**
         * 网易云埔
         */
        CrashHandler.init(getApplicationContext());

    }
}
