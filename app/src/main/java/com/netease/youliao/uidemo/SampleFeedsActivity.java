package com.netease.youliao.uidemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.netease.youliao.newsfeeds.core.NNewsFeedsSDK;
import com.netease.youliao.newsfeeds.model.NNFChannelInfo;
import com.netease.youliao.newsfeeds.model.NNFNewsInfo;
import com.netease.youliao.newsfeeds.remote.reflect.NNFJsonUtils;
import com.netease.youliao.newsfeeds.ui.base.activity.BaseNavigationBarActivity;
import com.netease.youliao.newsfeeds.ui.base.fragment.NNFChannelContentFragment;
import com.netease.youliao.newsfeeds.ui.core.NNewsFeedsUI;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnFeedsCallback;
import com.netease.youliao.newsfeeds.ui.core.NNFeedsFragment;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnFeedsExtendCallback;
import com.netease.youliao.newsfeeds.ui.core.callbacks.NNFOnShareCallback;
import com.netease.youliao.newsfeeds.ui.core.details.DefaultMoreVideosActivity;
import com.netease.youliao.newsfeeds.ui.core.entrance.NNFEntranceFragmentType;
import com.netease.youliao.newsfeeds.ui.custom.CustomOption;
import com.netease.youliao.newsfeeds.ui.custom.NNFCustomConfigure;
import com.netease.youliao.newsfeeds.ui.custom.NNFExtendNewsInfo;
import com.netease.youliao.newsfeeds.ui.custom.func.NNFFeedsFuncOption;
import com.netease.youliao.newsfeeds.ui.custom.ui.NNFFeedsUIOption;
import com.netease.youliao.newsfeeds.ui.utils.IOUtil;
import com.netease.youliao.newsfeeds.ui.utils.NNFUIConstants;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.netease.youliao.newsfeeds.ui.custom.func.NNFArticleFuncOption.NFSharePlatformWXTimeline;
import static com.netease.youliao.newsfeeds.ui.custom.func.NNFFeedsFuncOption.NFFeedsOptionThumModNone;
import static com.netease.youliao.newsfeeds.ui.custom.func.NNFFeedsFuncOption.NFFeedsOptionThumModNormal;

public class SampleFeedsActivity extends BaseNavigationBarActivity {

    private final static String TAG = "SampleFeedsActivity";
    // 高德定位
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private NNFeedsFragment mFeedsFragment;
    public static SampleFeedsActivity sInstance;

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRealContentView(R.layout.activity_main);

        api = WXAPIFactory.createWXAPI(this, BuildConfig.SHARE_WX_APP_ID, true);
        api.registerApp(BuildConfig.SHARE_WX_APP_ID);

        setTitle("网易有料");
        // 不显示左侧返回按钮
        setLeftViewVisible(false);
        getNavigationBarView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFeedsFragment != null) {
                    // 点击导航栏返回到顶部，模仿ios点击状态栏返回到顶部的功能
                    mFeedsFragment.scrollToTop();
                }
            }
        });
        sInstance = SampleFeedsActivity.this;

        /********* 全局初始化配置 *********/
        //initGlobalConfigure();
        //initGlobalConfigureByJson();
        /********* 全局初始化配置 *********/


        /********* 集成方式请二选一 *********/
        /***快速集成**/
        initFeedsByOneStep();
        /***快速集成**/

        /***自定义集成**/
//        initFeedsStepByStep();
        /***自定义集成**/
        /********* 集成方式请二选一 *********/


        /***生成配置文件**/
        //generateStr();
        /***生成配置文件**/


        /***初始化小入口**/
        //initSmallEntrance();
        /***初始化小入口**/

        initLocation();
        parseIntent(getIntent());
    }

    private void generateStr() {
        String str = null;
        str = JSON.toJSONString(NNFCustomConfigure.getInstance());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        parseIntent(intent);
    }

    /**
     * 分享回流处理
     *
     * @param intent
     */
    private void parseIntent(Intent intent) {
        if (null == intent) return;

        Uri uri = intent.getData();

        if (null != uri) {
            String infoId = uri.getQueryParameter("infoId");

            if (!TextUtils.isEmpty(infoId)) {
                NNFNewsInfo newsInfo = new NNFNewsInfo();
                newsInfo.infoId = infoId;
                newsInfo.source = uri.getQueryParameter("source");
                String infoType = uri.getQueryParameter("infoType");
                newsInfo.infoType = TextUtils.isEmpty(infoType) ? NNFUIConstants.INFO_TYPE_ARTICLE : infoType;
                String producer = uri.getQueryParameter("producer");
                newsInfo.producer = TextUtils.isEmpty(producer) ? "recommendation" : producer;

                switch (newsInfo.infoType) {
                    case NNFUIConstants.INFO_TYPE_ARTICLE:
                        SampleArticleActivity.start(this, newsInfo);
                        break;
                    case NNFUIConstants.INFO_TYPE_PICSET:
                        SamplePicSetGalleryActivity.start(this, newsInfo);
                        break;
                    case NNFUIConstants.INFO_TYPE_VIDEO:
                        DefaultMoreVideosActivity.start(this, newsInfo.infoId, newsInfo.infoType, newsInfo.producer);
                        break;
                }

            }
        }
    }

    /**
     * 第一步：接入信息流UI SDK，快速集成信息流主页 NNFeedsFragment
     */
    private void initFeedsByOneStep() {
        Map<String, Map<String, Object>> map = new HashMap<>();
        // initNNFeedsConfigure(map);
        // 设置全局分享回调
        NNewsFeedsUI.setShareCallback(new NNFOnShareCallback() {
            @Override
            public void onWebShareClick(Map<String, String> shareInfo, int index) {
                // context不能传入activity，这是因为会发生发生页面跳转后，activity onStop，图片加载也会随之stop
                ShareUtil.shareImp(getApplicationContext(), api, shareInfo, index);
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFeedsFragment = NNewsFeedsUI.createFeedsFragment(null, null, map);
        ft.replace(R.id.fragment_container, mFeedsFragment);
        ft.commitAllowingStateLoss();
    }

    public void initSmallEntrance() {
        //信息流小入口样式配置Map
        Map<String, Object> smallUIMap = new HashMap<>();
        smallUIMap.put("titleColor", "#FFC0CB");
        smallUIMap.put("selectBackgroundColor", "#333333");
        smallUIMap.put("singleIconImage", R.mipmap.ic_launcher);

        //信息流小入口功能配置Map
        Map<String, Object> smallFuncMap = new HashMap<>();
        smallFuncMap.put("autoScrollInterval", 1000);


        String jsonStr = "{ \"smallEntranceFuncOption\": {\n" +
                "    \"autoScrollInterval\": 3000,\n" +
                "    \"countInPage\": 2,\n" +
                "    \"refreshCount\": 6\n" +
                "  },\n" +
                "  \"smallEntranceUIOption\": {\n" +
                "    \"backgroundColor\": \"#FFFFFFFF\",\n" +
                "    \"borderColor\": \"#00000000\",\n" +
                "    \"lineSpace\": 20,\n" +
                "    \"selectBackgroundColor\": \"#f1f4f7\",\n" +
                "    \"titleColor\": \"#333333\",\n" +
                "    \"titleFontSize\": 17\n" +
                "  } }";
        Map<String, Map<String, Object>> map = JSON.parseObject(jsonStr, Map.class);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        //创建Map时导入
        NNFChannelContentFragment fragment = NNewsFeedsUI.createChannelContentViewFragment(null, map, NNFEntranceFragmentType.NNFChannelFragmentSmallEntrance, null, "989ea0a71a2740b7a8d8eca9d8bd82c3");
        ft.replace(R.id.fragment_container, fragment);
        ft.commitAllowingStateLoss();
    }

    public void initGlobalConfigure() {

        //信息流列表UI样式配置，属性和对应的值通过Key，Value形式传入
        Map<String, Object> feedsUIOptionMap = new HashMap<>();
        feedsUIOptionMap.put("cellBackgroundColor", "#FFFFFF");
        feedsUIOptionMap.put("channelBackgroundColor", "#FFFFFF");
        feedsUIOptionMap.put("backgroundColor", "#00BFFF");
        feedsUIOptionMap.put("imagePostion", 1);
        feedsUIOptionMap.put("smallImageRatio", 3 / 2f);
        feedsUIOptionMap.put("largeImageRatio", 2f);
        feedsUIOptionMap.put("trippleImageMargin", 9);
        feedsUIOptionMap.put("seperatorMargin", new int[]{16, 0});
        feedsUIOptionMap.put("titleImageMargin", 10);
        feedsUIOptionMap.put("cellMargin", new int[]{16, 16, 16, 16});
        feedsUIOptionMap.put("sourceTextSize", 12);


        //信息流列表功能配置，属性和对应的值通过Key，Value形式传入
        Map<String, Object> feedsFuncMap = new HashMap<>();
        feedsFuncMap.put("slidable", true);
        feedsFuncMap.put("thumbMode", NFFeedsOptionThumModNormal);

        //文章详情列表配置，属性和对应的值通过Key，Value形式传入
        Map<String, Object> articleUIMap = new HashMap<>();
        articleUIMap.put("backgroundColor", "#00BFFF");
        articleUIMap.put("titleColor", "#ab2b2b");
        articleUIMap.put("articleTextColor", "#0099FB");

        //文章详情功能配置，属性和对应的值通过Key，Value形式传入
        Map<String, Object> articleFuncMap = new HashMap<>();
        articleFuncMap.put("showRelated", true);
        articleFuncMap.put("platforms", new int[]{NFSharePlatformWXTimeline});

        //传入各个独立的配置项
        Map<String, Object> customConfigureMap = new HashMap<>();
        customConfigureMap.put(NNFCustomConfigure.NNFFeedsUIOptionKey, feedsUIOptionMap);
        customConfigureMap.put(NNFCustomConfigure.NNFFeedsFuncOptionKey, feedsFuncMap);
        customConfigureMap.put(NNFCustomConfigure.NNFArticleUIOptionKey, articleUIMap);
        customConfigureMap.put(NNFCustomConfigure.NNFArticleFuncOptionKey, articleFuncMap);

        //调用setCustomConfigure，传入全局的配置
        NNewsFeedsUI.setCustomConfigure(customConfigureMap);
    }

    public void initGlobalConfigureByJson() {
        String json = null;
        try {
            InputStream inputStream = getAssets().open("json/customconfigure.json");
            json = IOUtil.readInputStream(inputStream);
        } catch (IOException e) {

        }
        Map<String, Object> customConfigureMap = JSON.parseObject(json, Map.class);
        NNewsFeedsUI.setCustomConfigure(customConfigureMap);
    }

    public void initNNFeedsConfigure() {
        Map<String, Map<String, Object>> map = new HashMap<>();

        //配置信息流页面UI样式
        Map<String, Object> feedsUIMap = new HashMap<>();
        feedsUIMap.put("cellBackgroundColor", "#ab2b2b");
        feedsUIMap.put("channelBackgroundColor", "#0099FB");
        feedsUIMap.put("backgroundColor", "#FFFFFF");
        feedsUIMap.put("channelBackgroundColor", "#ab2b2b");
        feedsUIMap.put("pullingText", "这就是下滑");
        feedsUIMap.put("refreshSuccessBackgroundColor", "#FFFFFF");

        //配置信息流页面功能
        Map<String, Object> feedsFuncMap = new HashMap<>();
        feedsFuncMap.put("slidable", false);
        feedsFuncMap.put("thumbMode", NFFeedsOptionThumModNone);

        //添加UI，功能配置项
        map.put(NNFCustomConfigure.NNFFeedsUIOptionKey, feedsUIMap);
        map.put(NNFCustomConfigure.NNFFeedsFuncOptionKey, feedsFuncMap);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //集成创建单个信息流页面时传入
        mFeedsFragment = NNewsFeedsUI.createFeedsFragment(null, null, map);
        ft.replace(R.id.fragment_container, mFeedsFragment);
        ft.commitAllowingStateLoss();
    }


    /**
     * 第一步：接入信息流UI SDK，自定义集成信息流主页 NNFeedsFragment
     */
    private void initFeedsStepByStep() {
        HashMap<String, CustomOption> map = new HashMap<>();

        initCustomStyle(map);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFeedsFragment = NNewsFeedsUI.createFeedsFragment(new FeedsCallbackSample(), null, null);
        mFeedsFragment.setOnFeedsExtendCallback(new SampleExtendCallback(mFeedsFragment));
        ft.replace(R.id.fragment_container, mFeedsFragment);
        ft.commitAllowingStateLoss();
    }

    private class SampleExtendCallback extends NNFOnFeedsExtendCallback {

        private WeakReference<NNFeedsFragment> mWeakReference;
        HashMap<Integer, NNFExtendNewsInfo[]> mHashMap;

        public SampleExtendCallback(NNFeedsFragment feedsFragment) {
            mWeakReference = new WeakReference<NNFeedsFragment>(feedsFragment);
            mHashMap = new HashMap<>();
        }

        @Override
        public void onStart(NNFChannelInfo channelInfo, int loadType, Object extraData) {
            Log.d("数据扩展", "onStart@channel = " + channelInfo.channelName + ", @loadType = " + loadType);

            // 读取缓存或发起网络请求获取待插入的新闻数据
            String json = "{\n" +
                    "\t\t\t\"producer\": \"recommendation\",\n" +
                    "\t\t\t\"recId\": \"736633ceedf146e78a5086acaa821d2b_930340049\",\n" +
                    "\t\t\t\"algInfo\": \"|HOT_GBREAK|g_break-36\",\n" +
                    "\t\t\t\"infoId\": \"DBNEGGRU00097U7S\",\n" +
                    "\t\t\t\"infoType\": \"ad\",\n" +
                    "\t\t\t\"title\": \"iOS 11沦陷，美执法部门已能破解几乎所有iPhone\",\n" +
                    "\t\t\t\"summary\": \"2月28日消息，据《福布斯》杂志报道，美国的执法部门最近取得了一项破解iPhone手机的重大技术突破，然而对全球众多苹果手机的消费者来说，这或许会对他们造成新的隐私问题。\",\n" +
                    "\t\t\t\"source\": \"网易科技报道\",\n" +
                    "\t\t\t\"imgType\": 2,\n" +
                    "\t\t\t\"thumbnails\": [{\n" +
                    "\t\t\t\t\"width\": 300,\n" +
                    "\t\t\t\t\"height\": 200,\n" +
                    "\t\t\t\t\"url\": \"http://irec.nosdn.127.net/icovr-20180228-2229684e11bf912a0e1aeefed2ad5f4e.jpg\",\n" +
                    "\t\t\t\t\"note\": null\n" +
                    "\t\t\t}],\n" +
                    "\t\t\t\"deliverId\": null,\n" +
                    "\t\t\t\"publishTime\": \"2018-02-28 10:42:43\",\n" +
                    "\t\t\t\"feedbacks\": [{\n" +
                    "\t\t\t\t\"name\": \"不感兴趣\",\n" +
                    "\t\t\t\t\"value\": \"D_0\"\n" +
                    "\t\t\t}, {\n" +
                    "\t\t\t\t\"name\": \"内容质量差\",\n" +
                    "\t\t\t\t\"value\": \"D_1\"\n" +
                    "\t\t\t}, {\n" +
                    "\t\t\t\t\"name\": \"看过了\",\n" +
                    "\t\t\t\t\"value\": \"D_2\"\n" +
                    "\t\t\t}, {\n" +
                    "\t\t\t\t\"name\": \"不想看:网易科技报道\",\n" +
                    "\t\t\t\t\"value\": \"S_网易科技报道\"\n" +
                    "\t\t\t}, {\n" +
                    "\t\t\t\t\"name\": \"不想看:手机\",\n" +
                    "\t\t\t\t\"value\": \"C_36\"\n" +
                    "\t\t\t}, {\n" +
                    "\t\t\t\t\"name\": \"不想看:执法部门\",\n" +
                    "\t\t\t\t\"value\": \"P_C手机_I执法部门\"\n" +
                    "\t\t\t}, {\n" +
                    "\t\t\t\t\"name\": \"不想看:ios\",\n" +
                    "\t\t\t\t\"value\": \"P_C手机_Iios\"\n" +
                    "\t\t\t}]\n" +
                    "\t\t}";

            NNFExtendNewsInfo newsInfo = NNFJsonUtils.fromJson(json, NNFExtendNewsInfo.class);
            NNFExtendNewsInfo[] infos = new NNFExtendNewsInfo[]{newsInfo};

            mHashMap.put(channelInfo.hashCode(), infos);
        }

        @Override
        public void onFinish(NNFChannelInfo channelInfo, int loadType, Object extraData) {
            Log.d("数据扩展", "onFinish@channel = " + channelInfo.channelName + ", @loadType = " + loadType);
            // 该方法被触发，表示SDK内部完成网络请求；等待数据插入
            int[] indexArr = new int[]{3};
            NNFExtendNewsInfo[] infos = mHashMap.get(channelInfo.hashCode());

            // 通知SDK，插入结束
            mFeedsFragment.insertUserData(channelInfo, loadType, indexArr, infos);
        }
    }


    private void initCustomStyle(HashMap<String, CustomOption> map) {
        //频道相关设置
        NNFFeedsUIOption nnfFeedsUIOption = new NNFFeedsUIOption();

        nnfFeedsUIOption.showSeperator = false;

        NNFFeedsFuncOption feedsFuncOption = new NNFFeedsFuncOption();
        feedsFuncOption.refreshCount = 30;
        feedsFuncOption.thumbMode = NFFeedsOptionThumModNone;


        map.put(NNFCustomConfigure.NNFFeedsFuncOptionKey, feedsFuncOption);
        map.put(NNFCustomConfigure.NNFFeedsUIOptionKey, nnfFeedsUIOption);
    }

    /**
     * 第二步：可选，为信息流主页 NNFeedsFragment 设置点击事件回调；如不设置，使用SDK内部的默认回调
     */
    private class FeedsCallbackSample extends NNFOnFeedsCallback {
        @Override
        public void onNewsClick(Context context, NNFNewsInfo newsInfo, Object extraData) {
            if (null == newsInfo || null == newsInfo.infoType) return;
            if (newsInfo instanceof NNFExtendNewsInfo) {
                // 用户自定义新闻被点击
                switch (newsInfo.infoType) {
                    case NNFUIConstants.INFO_TYPE_AD:
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("https://www.baidu.com/");
                        intent.setData(content_url);
                        if (!(context instanceof Activity)) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        context.startActivity(Intent.createChooser(intent, "请选择浏览器"));
                        break;
                }
            } else {
                // 普通新闻被点击
                switch (newsInfo.infoType) {
                    case NNFUIConstants.INFO_TYPE_ARTICLE:
                        /**
                         * 第三步：自定义文章类新闻展示页面
                         */
                        SampleArticleActivity.start(context, newsInfo);
                        break;
                    case NNFUIConstants.INFO_TYPE_PICSET:
                        /**
                         * 第四步：自定义图集类新闻展示页面
                         */
                        SamplePicSetGalleryActivity.start(context, newsInfo);
                        break;
                    case NNFUIConstants.INFO_TYPE_VIDEO:
                        /**
                         * 第五步：自定义视频类新闻展示页面
                         */
                        DefaultMoreVideosActivity.start(context, newsInfo);
                        break;
                }
            }
        }
    }

    public NNFeedsFragment getFeedsFragment() {
        return mFeedsFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocation();
        destroyLocation();
    }

    /***************接入定位SDK获取经纬度****************/

    /**
     * 为了准确推荐本地新闻，NNewsFeedsSDK 内部需要获取经纬度。用户可以通过调用以下接口传入最新的经纬度：
     *
     * NNewsFeedsSDK.getInstance().setLocation(double longitude, double latitude);
     *
     * 由于经纬度的获取涉及面较广，所以我们推荐App层自己实现，SDK内部不主动读取经纬度。
     * 作为演示，本DEMO接入的是高德定位SDK，用户也可以使用其他定位SDK。
     */

    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
        //初始化client
        mLocationClient = new AMapLocationClient(this.getApplicationContext());
        mLocationOption = getDefaultOption();
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 设置定位监听
        mLocationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
        // 设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }

    /**
     * 停止定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void stopLocation() {
        // 停止定位
        mLocationClient.stopLocation();
    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
        if (null != mLocationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationOption = null;
        }
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    /**
                     * 将实时变化的经纬度传入 NNewsFeedsSDK
                     */
                    NNewsFeedsSDK.getInstance().setLocation(location.getLongitude(), location.getLatitude());
                    Log.v(TAG, "定位成功" + "\n");
                    Log.v(TAG, "定位类型: " + location.getLocationType() + "\n");
//                    Toast.makeText(SampleFeedsActivity.this, "经    度    : " + location.getLongitude() + "\n纬    度    : " + location.getLatitude(), Toast.LENGTH_LONG).show();
                    Log.v(TAG, "经    度    : " + location.getLongitude() + "\n");
                    Log.v(TAG, "纬    度    : " + location.getLatitude() + "\n");
                    Log.v(TAG, "精    度    : " + location.getAccuracy() + "米" + "\n");
                    Log.v(TAG, "提供者    : " + location.getProvider() + "\n");

                    Log.v(TAG, "速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    Log.v(TAG, "角    度    : " + location.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    Log.v(TAG, "星    数    : " + location.getSatellites() + "\n");
                    Log.v(TAG, "国    家    : " + location.getCountry() + "\n");
                    Log.v(TAG, "省            : " + location.getProvince() + "\n");
                    Log.v(TAG, "市            : " + location.getCity() + "\n");
                    Log.v(TAG, "城市编码 : " + location.getCityCode() + "\n");
                    Log.v(TAG, "区            : " + location.getDistrict() + "\n");
                    Log.v(TAG, "区域 码   : " + location.getAdCode() + "\n");
                    Log.v(TAG, "地    址    : " + location.getAddress() + "\n");
                    Log.v(TAG, "兴趣点    : " + location.getPoiName() + "\n");
                } else {
                    //定位失败
                    Log.e(TAG, "定位失败" + "\n");
                    Log.e(TAG, "错误码:" + location.getErrorCode() + "\n");
                    Log.e(TAG, "错误信息:" + location.getErrorInfo() + "\n");
                    Log.e(TAG, "错误描述:" + location.getLocationDetail() + "\n");
                }

            } else {
                Log.e(TAG, "定位失败，loc is null");
            }
        }
    };

    /***************接入定位SDK获取经纬度****************/
}
