# Android UI SDK 接入文档

为便于用户接入我们的 UI SDK，我们提供集成了 UI SDK 的演示Demo，用户可参考Demo源码及文档使用我们的 UI SDK。

[NewsFeeds-Demo-Android源码](https://github.com/NetEaseYouliao/NewsFeeds-Demo-Android)

## UI SDK 概述

网易有料 NewsFeeds UI SDK 封装了信息流的核心视图控件，方便第三方应用快速的集成并实现内容分发功能。UI SDK兼容Android 14+，UI Demo兼容Android 14+。

NewsFeeds UI SDK提供的功能如下：

- 快速集成信息流

> 信息流的基本功能包含信息流主页、文章类新闻展示页、图集类新闻展示页等，若用户选择快速集成方式，则使用UI SDK提供的所有默认页面。

- 自定义集成信息流

> 用户可以根据UI SDK提供的回调，自定义交互逻辑。例如，点击新闻列表后的目标调转页面、点击相关推荐后的目标调转页面等等。

## SDK类说明

网易有料NNewsFeeds UI SDK主要提供了以下类：

- NNFeedsFragment：信息流主页，提供频道切换和新闻列表的默认实现，采用ViewPager内嵌Fragment完成。

- NNFArticleWebFragment：文章类新闻展示的默认实现，采用Fragment内嵌WebView完成。

- NNFPicSetGalleryFragment：展示图集类新闻

- NNFArticleGalleryFragment：展示文章类新闻正文中的图片集

## 开发准备

### 1. Gradle集成

我们推荐通过Gradle集成我们的sdk。由于ui-sdk是在data-sdk的基础上开发的，因此，使用ui-sdk必须同时依赖data-sdk，data-sdk的接入，请参考data-sdk接入文档。

- jcenter远程依赖

第一步，在project module下的build.gradle配置repositories。创建项目时，会自动包含jcenter。若无，请添加。

```java
allprojects {
    repositories {
        jcenter()
    }
}
```

第二步，在app module下的build.gradle中同时引入我们的data-sdk和ui-sdk的依赖，请自行将x.x替换为版本号，目前最新版均为1.3


```java
compile 'com.netease.youliao:newsfeeds-data:x.x'
compile 'com.netease.youliao:newsfeeds-ui:x.x'
```

- aar本地依赖

第一步，导入aar包。在工程app结构下新建libs目录，同时将我们提供的`newsfeeds-sdk-x.x.aar`和`newsfeeds-ui-x.x.aar`文件复制到当前libs目录下

第二步，在project module下的build.gradle配置repositories

```java
allprojects {
    repositories {
        jcenter()
        
        // 添加aar所在目录
        flatDir {
            dirs 'libs'
        }
    }
}
```

第三步，在app module下的build.gradle中引入我们sdk的aar依赖，请自行将x.x替换为版本号，目前最新版均为1.3

我们的data-sdk和ui-sdk内部依赖了一些第三方库，

其中, data-sdk依赖了如下第三方库：

```java
compile 'com.alibaba:fastjson:1.2.8'
```

ui-sdk依赖了如下第三方库：

```java
compile "com.readystatesoftware.systembartint:systembartint:1.0.+"
compile 'com.github.bumptech.glide:glide:3.7.0'
compile 'org.greenrobot:eventbus:3.0.0'
compile "com.android.support:recyclerview-v7:25.3.1"
```

因此，采用aar本地引入的方式，需要同时引入data-sdk依赖库 & ui-sdk依赖库 & data-sdk & ui-sdk。

```java
dependencies {
    // 新建工程时自动生成
    compile fileTree(include: ['*.jar'], dir: 'libs')
    ...
    // data-sdk依赖库
    compile 'com.alibaba:fastjson:1.2.8'
    // ui-sdk依赖库
    compile "com.readystatesoftware.systembartint:systembartint:1.0.+"
    compile 'com.github.bumptech.glide:glide:3.7.0'
    compile 'org.greenrobot:eventbus:3.0.0'
    compile "com.android.support:recyclerview-v7:25.3.1"
    // aar文件依赖data-sdk & ui-sdk
    compile 'com.netease.youliao:newsfeeds-data:x.x@aar'
    compile 'com.netease.youliao:newsfeeds-ui:x.x@aar'
}
```

### 2. 初始化

由于我们的ui-sdk是在data-sdk的基础上进行开发的，因此，在使用ui-sdk之前，需要首先初始化data-sdk。

在自定义Application的OnCreate中添加以下代码，初始化我们的data-sdk

```
new NNewsFeedsSDK.Builder()
    .setAppKey("4c92fbfc2e6e7046d6e3cafced******")
    .setAppSecret("b430f8362f9f65bc09a639f62b******")
    .setContext(getApplicationContext())
    .setLogLevel(NNFLogUtil.LOG_DEBUG)
    .build();
```

NNewsFeedsSDK为data-sdk的主入口，具体接口说明请参考data-sdk的使用文档。

### 3. 混淆

若您的App开启了混淆，请为我们的SDK添加下述混淆规则


```java
# 网易有料 - 信息流SDK
-keep class com.netease.youliao.newsfeeds.**{*;}
```

由于我们的data-sdk内部依赖了腾讯广点通和个推，因此，混淆时也需要添加如下混淆规则：

```java
# 腾讯广点通
-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.app.NotificationCompat**{
    public *;
}

# 个推
-dontwarn com.igexin.**
-keep class com.igexin.** { *; }
-keep class org.json.** { *; }
-keep public class * extends com.igexin.sdk.GTIntentService

-keep class android.support.v4.app.NotificationCompat { *; }
-keep class android.support.v4.app.NotificationCompat$Builder { *; }

# fastjson
-keep class javax.ws.rs.** { *; }
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
```

## ui-sdk接口说明

### ui-sdk主入口

NNewsFeedsUI 为ui-sdk主入口，提供多个Fragment的实例化调用。

#### 创建信息流主页NNFeedsFragment实例

```java
/**
 * 信息流主页，提供频道切换和新闻列表的默认实现
 *
 * @param onFeedsCallback 回调
 * @param extraData       用户自定义数据
 * @return
 */
public static NNFeedsFragment createFeedsFragment(OnFeedsCallback onFeedsCallback, Object extraData)
```
其中 extraData 为用户自定义数据，该参数会在onFeedsCallback回调中回传。

==注意==：Activity配置

由于NNFeedsFragment包含视频播放功能，视频播放支持横屏播放，为确保视频切换到横屏时保留页面状态，请按照如下示例在AndroidManifest.xml中配置NNFeedsFragment依附的Activity的configChanges属性：

```java
<activity
    android:name=".SampleFeedsActivity"
    // 设置后，切屏不会重新调用各个生命周期，只会执行onConfigurationChanged方法
    android:configChanges="orientation|screenSize|keyboardHidden"
    android:launchMode="singleTask"
    android:screenOrientation="portrait">
</activity>
```

==注意==：视频退出全屏

由于NNFeedsFragment包含视频播放功能，视频播放支持全屏播放，若视频正在全屏播放，此时点击返回键，应先退出全屏播放。App开发人员需要重写NNFeedsFragment所在Activity的onBackPressed方法，并调用com.netease.youliao.newsfeeds.ui.libraries.jcvideoplayer_lib.JCVideoPlayer.onBackPressed();

```java
@Override
public void onBackPressed() {
    if (JCVideoPlayer.backPress()) {
        return;
    }
    super.onBackPressed();
}
```

==注意==：提供两种集成模式

- 第一种，未自定义回调，则所有页面已经整合到一起，及文章详情页面、图集页面、新闻正文图片浏览页面都已封装

```java
/**
 * 接入信息流UI SDK，快速集成信息流主页 NNFeedsFragment
 */
private void initFeedsByOneStep() {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    // 回调设置为null，表示使用SDK内部回调
    mFeedsFragment = NNewsFeedsUI.createFeedsFragment(null, null);
    ft.replace(R.id.fragment_container, mFeedsFragment);
    ft.commitAllowingStateLoss();
}
```

- 第二种，实现自定义回调，那么用户在点击新闻列表的时候跳转到用户回调，由用户自定义页面跳转

```java
/**
 * 第一步：接入信息流UI SDK，自定义集成信息流主页 NNFeedsFragment
 */
private void initFeedsStepByStep() {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    mFeedsFragment = NNewsFeedsUI.createFeedsFragment(new FeedsCallbackSample(), null);
    ft.replace(R.id.fragment_container, mFeedsFragment);
    ft.commitAllowingStateLoss();
}

/**
 * 第二步：为信息流主页 NNFeedsFragment 设置点击事件回调
 */
private class FeedsCallbackSample extends NNFOnFeedsCallback {
    @Override
    public void onNewsClick(Context context, NNFNewsInfo newsInfo, Object extraData) {
        if (null != newsInfo && null != newsInfo.infoType) {
            if (YLConstants.INFO_TYPE_ARTICLE.equals(newsInfo.infoType)) {
                /**
                 * 第三步：自定义文章类新闻展示页面
                 */
                SampleArticleActivity.start(context, newsInfo);
            } else if (YLConstants.INFO_TYPE_PICSET.equals(newsInfo.infoType)) {
                /**
                 * 第四步：自定义图集类新闻展示页面
                 */
                SamplePicSetGalleryActivity.start(context, newsInfo);
            }// 目前只提供文章类和图集类新闻的点击调转，后续会扩展更多类型
        }
    }
}
```
---

#### 信息流主页回调接口说明

NNFOnFeedsCallback为回调抽象类，提供信息流主页交互事件回调，目前支持的交互事件回调有：新闻被点击。

- 新闻被点击

```java
/**
 * 新闻被点击
 *
 * @param context
 * @param newsInfo  当前新闻对应新闻列表中的数据源
 * @param extraData 用户自定义数据
 */
public abstract void onNewsClick(Context context, NNFNewsInfo newsInfo, Object extraData);
```
 
 注意这里的extraData是初始化NNFeedsFragment实例时，传入的用户自定义数据。
 
 若用户未实现该回调，则SDK会根据新闻的infoType自动跳转到对应的默认展示页面。
 
 若用户实现该回调，则跳转到用户回调。
 
---

#### 创建文章类新闻展示页NNFArticleWebFragment实例

```java
/**
 * 文章类新闻展示的默认实现
 *
 * @param newsInfo          当前新闻对应新闻列表中的数据源
 * @param onArticleCallback 回调
 * @param extraData         用户自定义数据
 * @return
 */
public static NNFArticleWebFragment createArticleFragment(NNFNewsInfo newsInfo, NNFOnArticleCallback onArticleCallback, Object extraData) 
```
用户选择自己创建新闻详情页面时，可以通过该接口创建新闻详情视图实例，其中newsInfo为当前新闻对应新闻列表中的数据源，onArticleCallback为自定义回调，extraData 为用户自定义数据，该参数会在onArticleCallback回调中回传。

==注意==：Activity配置

由于文章类新闻展示页采用内嵌WebView实现，为了使WebView中的编辑框在编辑时能够弹出软键盘输入，请按照如下示例在AndroidManifest.xml中配置NNFArticleWebFragment依附的Activity的windowSoftInputMode属性：

```java
<activity
    android:name=".SampleArticleActivity"
    android:configChanges="orientation|screenSize|keyboardHidden"
    android:screenOrientation="portrait"
    // 设置后，该Activity总是调整屏幕的大小以便留出软键盘的空间
    android:windowSoftInputMode="adjustResize">
</activity>
```

==注意==：提供两种模式

- 快速集成，SDK提供一套界面交互事件的默认实现

```java
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
```

- 自定义集成，用户指定界面交互回调逻辑。

```java
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
```
---

#### 文章类新闻展示页回调接口说明

NNFOnArticleCallback为回调抽象类，提供文章类新闻展示页交互事件回调，目前支持的交互事件回调有：点击正文中的相关推荐、点击正文中的图片、文章类新闻详情加载成功、文章详情页面跳转到报错页面的回调、报错完成返回到文章详情页面的回调。

- 点击正文中的相关推荐

```java
/**
 * 点击正文中的相关推荐
 *
 * @param context
 * @param newsInfo
 * @param extraData 用户自定义数据
 */
public abstract void onRelatedNewsClick(Context context, NNFNewsInfo newsInfo, Object extraData);
```
文章类新闻详情页面相关推荐新闻点击时的回调

---

- 点击正文中的图片

```java
/**
 * 点击正文中的图片
 *
 * @param context
 * @param infoId     所属新闻ID
 * @param index      当前图片是正文第几张图片
 * @param source     图片网络链接
 * @param imageInfos 正文图片数组
 * @param extraData  用户自定义数据
 */
public abstract void onWebImageClick(Context context, String infoId, int index, String source, NNFImageInfo[] imageInfos, Object extraData);
```
新闻正文中图片点击的回调，其中，imageInfos为图片集合，index为当前点击图片下标

---

- 文章类新闻详情加载成功

```java
/**
 * 文章类新闻加载成功
 *
 * @param newsInfo  当前新闻对应新闻列表中的数据源
 * @param extraData 用户自定义数据
 */
public abstract void onArticleLoaded(NNFNewsInfo newsInfo, Object extraData);
```
该回调主要方便用户在新闻加载成功时，调用NewsFeedsSDK的markRead接口将新闻标记为已读，同时刷新列表已读状态

---

- 文章详情页面跳转到报错页面的回调

```java
/**
 * 举报中
 *
 * @param issueDescription 举报描述
 * @param extraData        用户自定义数据
 */
public void onIssueReporting(String issueDescription, Object extraData)
```
点击文章底部的举报按钮时，弹出举报选择框，触发该回调。其中issueDescription为报错页面的相关描述，方便更改页面title之类的参数

---

- 报错完成返回到文章详情页面的回调

```java
/**
 * 举报完成
 *
 * @param extraData 用户自定义数据
 */
public void onIssueReportFinished(Object extraData) 
```

报错操作结束后触发该回调。

---

#### 创建图集类新闻展示页NNFPicSetGalleryFragment实例

```java
/**
 * 展示图集类新闻
 *
 * @param newsInfo                当前新闻对应新闻列表中的数据源
 * @param onPicSetGalleryCallback 回调
 * @param extraData               用户自定义数据
 * @return
 */
public static NNFPicSetGalleryFragment createPicSetGalleryFragment(NNFNewsInfo newsInfo, NNFOnPicSetGalleryCallback onPicSetGalleryCallback, Object extraData)
```
可以通过该接口返回图集页面的实例，其中newsInfo为当前新闻对应新闻列表中的数据源，onPicSetGalleryCallback为自定义回调，extraData 为用户自定义数据，该参数会在onPicSetGalleryCallback回调中回传。


==注意==：提供两种模式

- 快速集成

```java
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
```

- 自定义集成

```java
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
```

---

#### 图集类新闻展示页回调接口说明

NNFOnPicSetGalleryCallback为回调抽象类，提供图集类新闻展示页交互事件回调，目前支持的交互事件回调有：图集加载成功时的回调、左上角返回按钮的点击响应。

- 图集加载成功时的回调

```java
/**
 * 图集类新闻加载成功
 *
 * @param newsInfo  当前新闻对应新闻列表中的数据源
 * @param extraData 用户自定义数据
 */
public abstract void onPicSetLoaded(NNFNewsInfo newsInfo, Object extraData);
```

该回调主要方便用户在新闻加载成功时刷新列表已读状态

---

- 左上角返回按钮的点击响应

```java
/**
 * 左上角返回按钮的点击响应
 *
 * @param context 返回按钮上下文
 */
public abstract void onBackClick(Context context);
```

当图集类新闻展示页的左上角返回按钮被点击时触发

---

#### 创建文章类新闻正文图片集展示页NNFArticleGalleryFragment实例

```java
/**
 * 展示文章类新闻正文中的图片集
 *
 * @param infoId                   所属新闻ID
 * @param startIndex               当前图片是正文第几张图片
 * @param imageInfos               正文图片数组
 * @param onArticleGalleryCallback 回调
 * @param extraData                用户自定义数据
 * @return
 */
public static NNFArticleGalleryFragment createArticleGalleryFragment(String infoId, int startIndex, NNFImageInfo[] imageInfos, NNFOnArticleGalleryCallback onArticleGalleryCallback, Object extraData)
```
可以通过该接口返回新闻详情图片浏览页面的实例，其中infoId为当前新闻的ID，startIndex为当前的图片的索引，imageInfos为新闻详情的图片数组，onArticleGalleryCallback为自定义回调，extraData 为用户自定义数据，该参数会在onArticleGalleryCallback回调中回传。

---

==注意==：提供两种模式

- 快速集成

```java
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
```

- 自定义集成

```java
private void initGalleryStepByStep(int startIndex, NNFImageInfo[] imageInfos, String infoId) {
    /**
     * 第一步：实例化 NNFArticleGalleryFragment，展示文章类新闻正文中的图片集
     */
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();

    /**
     * 第二步：NNFArticleGalleryFragment 设置点击事件回调；
     */
    NNFOnArticleGalleryCallback onArticleGalleryCallback = new NNFOnArticleGalleryCallback() {
        @Override
        public void onPicClick(Context context, NNFImageInfo imageInfo, Object extraData) {
            /**
             * 第三步：设置图片点击行为
             */
            SampleArticleGalleryActivity.this.finish();
        }
    };
    NNFArticleGalleryFragment articleGalleryFragment = NNewsFeedsUI.createArticleGalleryFragment(infoId, startIndex, imageInfos,
            onArticleGalleryCallback, null);
    ft.replace(R.id.fragment_container, articleGalleryFragment);
    ft.commit();
}
```

#### 文章类新闻正文图片集展示页回调接口说明

NNFOnArticleGalleryCallback为回调抽象类，提供文章类新闻正文图片集展示页交互事件回调，目前支持的交互事件回调有：图片被点击。

- 图片被点击

```java
/**
 * 图片被点击
 *
 * @param context   图片上下文
 * @param imageInfo 图片数据源
 * @param extraData 初始化NNFArticleGalleryFragment时传入的用户自定义数据
 */
public abstract void onPicClick(Context context, NNFImageInfo imageInfo, Object extraData);
```

当图片集中的某张图片被点击时，触发该回调

---

### NNFeedsFragment类接口说明

#### 刷新已读新闻的状态

```java
/**
 * 新闻被阅读，将新闻修改成已读状态
 *
 * @param infoId          当前新闻的ID
 * @param adapterPosition 当前新闻在新闻列表适配器中的position
 */
public void markNewsRead(String infoId, int adapterPosition)
```

 使用SDK整合的View，无需调用该接口；
 
 用户自定义跳转页面，需在新闻详情加载成功后，将新闻标记为已读，并主动调用该接口，刷新新闻列表的已读状态。其中infoId为当前新闻的ID，adapterPos为当前新闻在新闻列表适配器中的position。
 
---

#### 强制刷新当前列表

```java
/**
 * 为当前频道执行下拉刷新
 */
public void performRefresh()
```
可调用该接口实现强制下拉刷新当前列表的功能

---