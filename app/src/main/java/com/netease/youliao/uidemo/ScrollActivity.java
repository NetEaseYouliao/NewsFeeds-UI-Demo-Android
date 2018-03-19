package com.netease.youliao.uidemo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.netease.youliao.newsfeeds.ui.core.NNFeedsFragment;
import com.netease.youliao.newsfeeds.ui.core.NNewsFeedsUI;
import com.netease.youliao.newsfeeds.ui.libraries.magicindicator.buildins.UIUtil;

import java.util.HashMap;
import java.util.Map;

public class ScrollActivity extends AppCompatActivity {

    private FrameLayout container;
    private MyScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initView();
        initFeedsByOneStep();
    }

    private void initView() {
        container = (FrameLayout) findViewById(R.id.container);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtil.getScreenHeight(this));
        container.setLayoutParams(layoutParams);

        scrollView = (MyScrollView) findViewById(R.id.scroll_view);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"点击了按钮",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ScrollActivity.this,CommonActivity.class);
                startActivity(intent);
            }
        });
        if (Build.VERSION.SDK_INT > 21)
        scrollView.setNestedScrollingEnabled(true);
    }

    private void initFeedsByOneStep() {
        Map<String, Map<String, Object>> map = new HashMap<>();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NNFeedsFragment feedsFragment = NNewsFeedsUI.createFeedsFragment(null, null, map);
        ft.add(R.id.container, feedsFragment);
        ft.commitAllowingStateLoss();
    }

}
