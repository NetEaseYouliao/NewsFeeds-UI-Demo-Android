package com.netease.youliao.uidemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.netease.youliao.newsfeeds.model.NNFImageInfo;
import com.netease.youliao.newsfeeds.model.NNFNewsInfo;
import com.netease.youliao.newsfeeds.ui.base.activity.BaseBlankActivity;
import com.netease.youliao.newsfeeds.ui.core.NNewsFeedsUI;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnArticleCallback;
import com.netease.youliao.newsfeeds.ui.core.NNFArticleWebFragment;

/**
 * Created by zhangdan on 2017/10/12.
 */

public class SampleArticleActivity extends BaseBlankActivity {
    public static final String KEY_NEWS_INFO = "newsInfo";

    private TextView mTextView;

    private NNFNewsInfo mNewsInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRealContentView(R.layout.activity_article);

        findViewById(R.id.tv_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // parse Intent
        mNewsInfo = (NNFNewsInfo) getIntent().getSerializableExtra(KEY_NEWS_INFO);

        mTextView = (TextView) this.findViewById(R.id.tv_title);
        if (null != mNewsInfo) {
            mTextView.setText(mNewsInfo.source);
        }

        /********* 集成方式请二选一 *********/

        // 快速集成
//        initArticleByOneStep();

        // 自定义集成
        initArticleStepByStep();

        /********* 集成方式请二选一 *********/
    }

    /**
     * 第一步：接入信息流UI SDK，快速集成文章类展示页 NNFArticleWebFragment
     */
    private void initArticleByOneStep() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NNFArticleWebFragment articleWebFragment = NNewsFeedsUI.createArticleFragment(mNewsInfo, null, null);
        ft.replace(R.id.fragment_container, articleWebFragment);
        ft.commit();
    }

    /**
     * 第一步：接入信息流UI SDK，自定义集成文章类展示页 NNFArticleWebFragment
     */
    private void initArticleStepByStep() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NNFArticleWebFragment articleWebFragment = NNewsFeedsUI.createArticleFragment(mNewsInfo, new NNFOnArticleCallback() {
            /**
             * 第二步：为文章类展示页 NNFArticleWebFragment 设置点击事件回调；
             */

            @Override
            public void onRelatedNewsClick(Context context, NNFNewsInfo newsInfo, Object extraData) {
                /**
                 * 第三步：自定义相关推荐新闻展示页面
                 */
                SampleArticleActivity.start(context, newsInfo);
            }

            @Override
            public void onWebImageClick(Context context, String infoId, int index, String source, NNFImageInfo[] imageInfos, Object extraData) {
                /**
                 * 第四步：点击文章正文内的图片后的响应事件
                 */
                SampleArticleGalleryActivity.start(context, infoId, index, imageInfos);
            }

            @Override
            public void onArticleLoaded(NNFNewsInfo newsInfo, Object extraData) {
                /**
                 * 第五步：通知新闻已阅，信息流主页UI刷新
                 */
                SampleFeedsActivity.sInstance.getFeedsFragment().markNewsRead(newsInfo.infoId);
            }

            @Override
            public void onIssueReporting(String issueDescription, Object extraData) {
                /**
                 * 第六步：点击文章底部的举报按钮时，弹出举报选择框，此时，WebView标题发生变化，将当前Activity Title展示为WebView标题
                 */
                if (null != issueDescription) {
                    mTextView.setText(issueDescription);
                }
            }

            @Override
            public void onIssueReportFinished(Object extraData) {
                /**
                 * 第七步：举报完成后，展示原来的标题
                 */
                if (null != mNewsInfo) {
                    mTextView.setText(mNewsInfo.source);
                }
            }
        }, null);

        ft.replace(R.id.fragment_container, articleWebFragment);
        ft.commit();
    }

    @Override
    protected void initPresenter() {

    }

    public static void start(Context from, NNFNewsInfo newsInfo) {
        Intent intent = new Intent();
        intent.setClass(from, SampleArticleActivity.class);
        intent.putExtra(KEY_NEWS_INFO, newsInfo);
        from.startActivity(intent);
    }
}
