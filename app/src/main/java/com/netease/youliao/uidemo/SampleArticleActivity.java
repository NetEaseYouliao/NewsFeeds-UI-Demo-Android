package com.netease.youliao.uidemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.youliao.newsfeeds.model.NNFImageInfo;
import com.netease.youliao.newsfeeds.model.NNFNewsDetails;
import com.netease.youliao.newsfeeds.model.NNFNewsInfo;
import com.netease.youliao.newsfeeds.ui.base.activity.BaseBlankActivity;
import com.netease.youliao.newsfeeds.ui.base.utils.ResourcesUtil;
import com.netease.youliao.newsfeeds.ui.base.view.popupwindowview.PopupWindowView;
import com.netease.youliao.newsfeeds.ui.core.NNewsFeedsUI;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnArticleCallback;
import com.netease.youliao.newsfeeds.ui.core.NNFArticleWebFragment;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnShareCallback;
import com.netease.youliao.newsfeeds.ui.utils.PopWindowSamples;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.Map;

/**
 * Created by zhangdan on 2017/10/12.
 */

public class SampleArticleActivity extends BaseBlankActivity {
    public static final String KEY_NEWS_INFO = "newsInfo";

    private TextView mTextView;

    private NNFNewsInfo mNewsInfo;

    private ImageView mIvShare;

    private IWXAPI api;
    private NNFArticleWebFragment mArticleWebFragment;

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

        api = WXAPIFactory.createWXAPI(this, BuildConfig.SHARE_WX_APP_ID);


        // parse Intent
        mNewsInfo = (NNFNewsInfo) getIntent().getSerializableExtra(KEY_NEWS_INFO);

        mTextView = (TextView) this.findViewById(R.id.tv_title);
        mIvShare = (ImageView) this.findViewById(R.id.iv_share);
        mIvShare.setVisibility(View.INVISIBLE);
        if (null != mNewsInfo) {
            mTextView.setText(mNewsInfo.source);
        }

        setStatueBarColor(R.color.nnf_black);

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
        mArticleWebFragment = NNewsFeedsUI.createArticleFragment(mNewsInfo, null, new NNFOnShareCallback() {
            @Override
            public void onWebShareClick(Map<String, String> shareInfo, int index) {
                ShareUtil.shareImp(SampleArticleActivity.this, api, shareInfo, index);
            }
        }, null);
        ft.replace(R.id.fragment_container, mArticleWebFragment);
        ft.commit();
    }

    /**
     * 第一步：接入信息流UI SDK，自定义集成文章类展示页 NNFArticleWebFragment
     */
    private void initArticleStepByStep() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mArticleWebFragment = NNewsFeedsUI.createArticleFragment(mNewsInfo, new NNFOnArticleCallback() {
            /**
             * 第二步：可选，为文章类展示页 NNFArticleWebFragment 设置点击事件回调；如不设置，使用SDK内部的默认回调
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
            public void onArticleLoaded(NNFNewsDetails details, Object extraData) {
                /**
                 * 第五步：显示分享图标等
                 */

                mNewsInfo.source = details.source;
                mTextView.setText(mNewsInfo.source);

                initSharePopView(details);
                showShare();
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
        }, new NNFOnShareCallback() {
            @Override
            public void onWebShareClick(Map<String, String> shareInfo, int index) {
                ShareUtil.shareImp(SampleArticleActivity.this, api, shareInfo, index);
            }
        }, null);

        ft.replace(R.id.fragment_container, mArticleWebFragment);
        ft.commit();
    }

    @Override
    protected void initPresenter() {

    }

    public static void start(Context from, NNFNewsInfo newsInfo) {
        Intent intent = new Intent();
        intent.setClass(from, SampleArticleActivity.class);
        intent.putExtra(KEY_NEWS_INFO, newsInfo);
        if (!(from instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        from.startActivity(intent);
    }

    private void initSharePopView(final NNFNewsDetails details) {
        final String[] dataSource = new String[]{
                ResourcesUtil.getString(this, com.netease.youliao.newsfeeds.ui.R.string.nnf_share_wx_session),
                ResourcesUtil.getString(this, com.netease.youliao.newsfeeds.ui.R.string.nnf_share_wx_timeline)};

        final Pair<String, Integer>[] pairs = new Pair[2];
        pairs[0] = new Pair<>(dataSource[0], com.netease.youliao.newsfeeds.ui.R.drawable.nnf_selector_share_wx_session);
        pairs[1] = new Pair<>(dataSource[1], com.netease.youliao.newsfeeds.ui.R.drawable.nnf_selector_share_wx_timeline);
        final PopupWindowView sharePopUp = PopWindowSamples.createSharePopUp(this, -1, com.netease.youliao.newsfeeds.ui.R.string.cancel, pairs, new PopWindowSamples.IClick() {
            @Override
            public boolean onViewClick(int viewId) {
                return true;
            }
        }, new PopWindowSamples.IItemClick() {
            @Override
            public boolean onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mArticleWebFragment) {
                    mArticleWebFragment.onShareClick(details, position);
                }
                return true;
            }
        });


        mIvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 从屏幕底部弹出
                PopWindowSamples.showBelowActionBar(sharePopUp, mContentView, true);
            }
        });
    }

    private void showShare() {
        boolean showShare = null != mArticleWebFragment ? mArticleWebFragment.showShare() : false;
        if (showShare) {
            mIvShare.setVisibility(View.VISIBLE);
        }
    }
}
