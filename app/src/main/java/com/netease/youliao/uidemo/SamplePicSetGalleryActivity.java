package com.netease.youliao.uidemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.netease.youliao.newsfeeds.model.NNFNewsDetails;
import com.netease.youliao.newsfeeds.model.NNFNewsInfo;
import com.netease.youliao.newsfeeds.ui.base.activity.BaseBlankActivity;
import com.netease.youliao.newsfeeds.ui.core.NNewsFeedsUI;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnPicSetGalleryCallback;
import com.netease.youliao.newsfeeds.ui.core.NNFPicSetGalleryFragment;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnShareCallback;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.Map;

/**
 * Created by zhangdan on 2017/10/12.
 * <p>
 * 页面功能：展示图集类新闻
 */

public class SamplePicSetGalleryActivity extends BaseBlankActivity {
    private final static String KEY_NEWS_INFO = "newsInfo";
    private IWXAPI api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, BuildConfig.SHARE_WX_APP_ID);

        setRealContentView(R.layout.activity_main);
        setStatueBarColor(R.color.nnf_black);

        parseIntent();
    }

    public static void start(Context from, NNFNewsInfo newsInfo) {
        Intent intent = new Intent();
        intent.setClass(from, SamplePicSetGalleryActivity.class);
        intent.putExtra(KEY_NEWS_INFO, newsInfo);
        if (!(from instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        from.startActivity(intent);
    }

    private void parseIntent() {
        Intent intent = getIntent();

        NNFNewsInfo newsInfo = (NNFNewsInfo) intent.getSerializableExtra(KEY_NEWS_INFO);

        /********* 集成方式请二选一 *********/

        // 快速集成
//        initGalleryByOneStep(newsInfo);

        // 自定义集成
        initGalleryStepByStep(newsInfo);

        /********* 集成方式请二选一 *********/
    }

    /**
     * 第一步：快速集成图集类展示页 NNFPicSetGalleryFragment，展示图集类新闻
     */
    public void initGalleryByOneStep(NNFNewsInfo newsInfo) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NNFPicSetGalleryFragment picSetGalleryFragment = NNewsFeedsUI.createPicSetGalleryFragment(newsInfo, null, null);
        ft.replace(R.id.fragment_container, picSetGalleryFragment);
        ft.commit();
    }

    /**
     * 第一步：自定义集成图集类展示页 NNFPicSetGalleryFragment，展示图集类新闻
     */
    public void initGalleryStepByStep(NNFNewsInfo newsInfo) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        /**
         * 第二步：可选，为图集展示页 NNFPicSetGalleryFragment 设置点击事件回调；如不设置，使用SDK内部的默认回调
         */
        NNFOnPicSetGalleryCallback onPicSetGalleryCallback = new NNFOnPicSetGalleryCallback() {
            @Override
            public void onBackClick(Context context) {
                /**
                 * 第三步：设置图集展示页左上角返回按钮点击后的行为
                 */
                SamplePicSetGalleryActivity.this.finish();
            }

            @Override
            public void onPicSetClick(Context context, NNFNewsInfo newsInfo) {
                super.onPicSetClick(context, newsInfo);
                /**
                 * 第四步：设置相关图集被点击后的行为
                 */
                SamplePicSetGalleryActivity.start(context, newsInfo);
                // 避免OOM，展示相关图集时，销毁上一图集
                if (context instanceof SamplePicSetGalleryActivity) {
                    SamplePicSetGalleryActivity activity = (SamplePicSetGalleryActivity) context;
                    activity.finish();
                }
            }
        };

        NNFPicSetGalleryFragment picSetGalleryFragment = NNewsFeedsUI.createPicSetGalleryFragment(newsInfo, onPicSetGalleryCallback, new NNFOnShareCallback() {
            @Override
            public void onWebShareClick(Map<String, String> shareInfo, int index) {
                ShareUtil.shareImp(SamplePicSetGalleryActivity.this, api, shareInfo, index);
            }
        }, null);

        ft.replace(R.id.fragment_container, picSetGalleryFragment);
        ft.commit();
    }

}
