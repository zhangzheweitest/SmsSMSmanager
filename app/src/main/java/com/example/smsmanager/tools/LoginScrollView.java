package com.example.smsmanager.tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;

//为解决登录界面控件遮挡问题加入的自定义滚动控件
public class LoginScrollView extends ScrollView {

    private int mDefaultBottom = -1;
    private View mChangeView;

    public LoginScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        post(new Runnable() {
            @Override
            public void run() {
                addListener(LoginScrollView.this, 0);
            }
        });
    }

    private void addListener(ViewGroup viewGroup, int layer) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof EditText) {
                View childParent = view;
                for (int j = 2; j < layer; j++) {
                    childParent = (View) childParent.getParent();
                }
                final View finalChildParent = childParent;
                view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mChangeView = finalChildParent;
                        scrollTo(0, finalChildParent.getTop());
                    }
                });
            } else if (view instanceof ViewGroup) {
                addListener((ViewGroup) view, layer + 1);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (b >= mDefaultBottom) {
            mDefaultBottom = b;
        } else {
            if (mChangeView != null) {
                smoothScrollTo(0, mChangeView.getTop());
            }
        }
    }
}
