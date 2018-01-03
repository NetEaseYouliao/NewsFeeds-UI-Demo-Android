package com.netease.youliao.uidemo;

import android.app.Application;
import android.text.TextUtils;

import com.netease.nis.bugrpt.CrashHandler;
import com.netease.youliao.newsfeeds.ui.core.NNewsFeedsUISDK;
import com.netease.youliao.newsfeeds.utils.NNFLogUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by zhangdan on 2017/10/10.
 */

public class YLApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String processName = getProcessName();
        // 判断进程名，保证只有主进程才初始化网易有料UI SDK
        if (!TextUtils.isEmpty(processName) && processName.equals(this.getPackageName())) {
            /**
             * 初始化SDK：在自定义Application中初始化网易有料API SDK
             */
            new NNewsFeedsUISDK.Builder()
                    .setAppKey(BuildConfig.APP_KEY)
                    .setAppSecret(BuildConfig.APP_SECRET)
                    .setContext(getApplicationContext())
                    .setMaxCacheNum(60)
                    .setMaxCacheTime(60 * 60 * 1000)
                    .setAutoRefreshInterval(60 * 60 * 1000)
                    .setLogLevel(NNFLogUtil.LOG_VERBOSE)
                    .build();

            /**
             * 网易云埔
             */
            CrashHandler.init(getApplicationContext());
        }
    }

    public static String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
