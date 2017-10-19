package com.netease.youliao.uidemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.netease.youliao.newsfeeds.model.NNFNewsInfo;
import com.netease.youliao.newsfeeds.ui.base.activity.BaseBlankActivity;
import com.netease.youliao.newsfeeds.ui.core.NNewsFeedsUI;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnPicSetGalleryCallback;
import com.netease.youliao.newsfeeds.ui.core.NNFPicSetGalleryFragment;

/**
 * Created by zhangdan on 2017/10/12.
 * <p>
 * 页面功能：展示图集类新闻
 */

public class SamplePicSetGalleryActivity extends BaseBlankActivity {
    private final static String KEY_NEWS_INFO = "newsInfo";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRealContentView(R.layout.activity_main);
        setStatueBarColor(R.color.black);

        parseIntent();
    }

    public static void start(Context from, NNFNewsInfo newsInfo) {
        Intent intent = new Intent();
        intent.setClass(from, SamplePicSetGalleryActivity.class);
        intent.putExtra(KEY_NEWS_INFO, newsInfo);
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
         * 第二步：为图集展示页 NNFPicSetGalleryFragment 设置点击事件回调；
         */
        NNFOnPicSetGalleryCallback onPicSetGalleryCallback = new NNFOnPicSetGalleryCallback() {
            @Override
            public void onPicSetLoaded(NNFNewsInfo newsInfo, Object extraData) {
                /**
                 * 第三步：通知新闻已阅，信息流主页UI刷新
                 */
                SampleFeedsActivity.sInstance.getFeedsFragment().markNewsRead(newsInfo.infoId);
            }

            @Override
            public void onBackClick(Context context) {
                /**
                 * 第四步：设置图集展示页左上角返回按钮点击后的行为
                 */
                SamplePicSetGalleryActivity.this.finish();
            }
        };

        NNFPicSetGalleryFragment picSetGalleryFragment = NNewsFeedsUI.createPicSetGalleryFragment(newsInfo, onPicSetGalleryCallback, null);

        ft.replace(R.id.fragment_container, picSetGalleryFragment);
        ft.commit();
    }

}
