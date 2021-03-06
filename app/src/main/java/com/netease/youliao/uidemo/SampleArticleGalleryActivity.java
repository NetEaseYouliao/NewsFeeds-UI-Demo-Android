package com.netease.youliao.uidemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.netease.youliao.newsfeeds.model.NNFImageInfo;
import com.netease.youliao.newsfeeds.remote.reflect.NNFJsonUtils;
import com.netease.youliao.newsfeeds.ui.base.activity.BaseBlankActivity;
import com.netease.youliao.newsfeeds.ui.core.NNFArticleGalleryFragment;
import com.netease.youliao.newsfeeds.ui.core.NNewsFeedsUI;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnArticleGalleryCallback;

/**
 * Created by zhangdan on 2017/10/12.
 * <p>
 * 页面功能：展示文章类新闻正文中的图片集
 */

public class SampleArticleGalleryActivity extends BaseBlankActivity {
    private final static String KEY_POSITION_START = "positionStart";
    private final static String KEY_INFO_ID = "infoId";
    private final static String KEY_IMAGE_INFOS = "imageInfos";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRealContentView(R.layout.activity_main);
        setStatueBarColor(R.color.nnf_black);


        parseIntent();
    }

    /**
     * @param from
     * @param start      当前选中项
     * @param imageInfos 图片数组
     */
    public static void start(Context from, String infoId, int start, NNFImageInfo[] imageInfos) {
        Intent intent = new Intent();
        intent.setClass(from, SampleArticleGalleryActivity.class);
        intent.putExtra(KEY_POSITION_START, start);
        intent.putExtra(KEY_INFO_ID, infoId);
        intent.putExtra(KEY_IMAGE_INFOS, imageInfos);
        if (!(from instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        from.startActivity(intent);
    }

    private void parseIntent() {
        Intent intent = getIntent();

        int startIndex = intent.getIntExtra(KEY_POSITION_START, -1);
        Object object = intent.getSerializableExtra(KEY_IMAGE_INFOS);
        NNFImageInfo[] imageInfos = NNFJsonUtils.fromJson(NNFJsonUtils.toJson(object), NNFImageInfo[].class);
        String infoId = intent.getStringExtra(KEY_INFO_ID);

        /********* 集成方式请二选一 *********/
        // 快速集成
//        initGalleryByOneStep(startIndex, imageInfos, infoId);

        // 自定义集成
        initGalleryStepByStep(startIndex, imageInfos, infoId);
        /********* 集成方式请二选一 *********/
    }

    private void initGalleryByOneStep(int startIndex, NNFImageInfo[] imageInfos, String infoId) {
        /**
         * 第一步：实例化 NNFArticleGalleryFragment，展示文章类新闻正文中的图片集
         */
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NNFArticleGalleryFragment articleGalleryFragment = NNewsFeedsUI.createArticleGalleryFragment(infoId, startIndex, imageInfos, null, null);
        ft.replace(R.id.fragment_container, articleGalleryFragment);
        ft.commit();
    }

    private void initGalleryStepByStep(int startIndex, NNFImageInfo[] imageInfos, String infoId) {
        /**
         * 第一步：实例化 NNFArticleGalleryFragment，展示文章类新闻正文中的图片集
         */
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        /**
         * 第二步：可选，NNFArticleGalleryFragment 设置点击事件回调；如不设置，使用SDK内部的默认回调
         */
        NNFOnArticleGalleryCallback onArticleGalleryCallback = new NNFOnArticleGalleryCallback() {
            @Override
            public void onPicClick(Context context, NNFImageInfo imageInfo, Object extraData) {
                // 点击图片返回
                SampleArticleGalleryActivity.this.finish();
            }
        };
        NNFArticleGalleryFragment articleGalleryFragment = NNewsFeedsUI.createArticleGalleryFragment(infoId, startIndex, imageInfos,
                onArticleGalleryCallback, null);
        ft.replace(R.id.fragment_container, articleGalleryFragment);
        ft.commit();
    }
}
