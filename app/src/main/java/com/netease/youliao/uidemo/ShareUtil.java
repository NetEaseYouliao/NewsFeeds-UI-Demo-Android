package com.netease.youliao.uidemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.netease.youliao.newsfeeds.ui.utils.NNFUIConstants;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.util.Map;

/**
 * Created by zhangdan on 2017/11/29.
 */

public class ShareUtil {
    private static final int THUMB_SIZE = 150;

    public static void shareImp(Context context, final IWXAPI api, Map<String, String> shareInfo, final int type) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = buildShareReq(shareInfo);
        Log.d("SHARE", "webpageUrl->" + webpage.webpageUrl);
        final WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = shareInfo.get(NNFUIConstants.FIELD_TITLE);
        String summary = shareInfo.get(NNFUIConstants.FIELD_SUMMARY);
        msg.description = TextUtils.isEmpty(summary) ? shareInfo.get(NNFUIConstants.FIELD_SOURCE) : summary;
        String iconUrl = shareInfo.get(NNFUIConstants.FIELD_ICONURL);

        if (!TextUtils.isEmpty(iconUrl)) {
            Glide.with(context).load(iconUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    sendShareReq(api, resource, type, msg);
                }
            });
        } else {
            // 使用默认图标
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            sendShareReq(api, bmp, type, msg);
        }
    }

    private static void sendShareReq(IWXAPI api, Bitmap bmp, int type, WXMediaMessage msg) {
        int targetScene = SendMessageToWX.Req.WXSceneSession;
        switch (type) {
            case 0:
                targetScene = SendMessageToWX.Req.WXSceneSession;
                break;
            case 1:
                targetScene = SendMessageToWX.Req.WXSceneTimeline;
                break;
        }
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        msg.setThumbImage(thumbBmp);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = targetScene;
        api.sendReq(req);
    }

    // h5分享链接构造
    private static String buildShareReq(Map<String, String> shareInfo) {
        String infoId = shareInfo.get(NNFUIConstants.FIELD_INFOID);
        String infoType = shareInfo.get(NNFUIConstants.FIELD_INFOTYPE);
        String producer = shareInfo.get(NNFUIConstants.FIELD_PRODUCER);
        String source = shareInfo.get(NNFUIConstants.FIELD_SOURCE);
        String recId = shareInfo.get(NNFUIConstants.FIELD_RECID);
        String algInfo = shareInfo.get(NNFUIConstants.FIELD_ALGINFO);
        producer = TextUtils.isEmpty(producer) ? "recommendation" : producer;
        String urlFormat = BuildConfig.SHARE_SERVER + "m/#/info?fss=1&ak=%s&sk=%s&id=%s&it=%s&p=%s" +
                "&aou=%s" +
                "&iou=%s" +
                "&st=%s" +
                "&rid=%s" +
                "&info=%s";
        String openUrl = "youliao%3A%2F%2Fyouliao.163yun.com%3FinfoId%3D" + infoId + "%26infoType%3D" + infoType + "%26producer%3D" + producer;
        return String.format(urlFormat,
                BuildConfig.APP_KEY,
                BuildConfig.APP_SECRET,
                infoId,
                infoType,
                producer,
                openUrl,
                openUrl,
                source,
                recId,
                algInfo);
    }

    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
