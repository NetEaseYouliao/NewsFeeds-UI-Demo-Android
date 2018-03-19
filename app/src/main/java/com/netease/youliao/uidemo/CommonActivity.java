package com.netease.youliao.uidemo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.netease.youliao.newsfeeds.ui.core.NNFeedsFragment;
import com.netease.youliao.newsfeeds.ui.core.NNewsFeedsUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengxuanzhi on 18/2/1.
 */

public class CommonActivity extends AppCompatActivity {

    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initFeedsByOneStep();
    }

    private void initView() {
        container = (FrameLayout) findViewById(R.id.fragment_container);
        if (container != null){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.setLayoutParams(layoutParams);
        }

    }

    private void initFeedsByOneStep() {
        Map<String, Map<String, Object>> map = new HashMap<>();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NNFeedsFragment feedsFragment = NNewsFeedsUI.createFeedsFragment(null, null, map);
        ft.add(R.id.fragment_container, feedsFragment);
        ft.commitAllowingStateLoss();
    }

}
