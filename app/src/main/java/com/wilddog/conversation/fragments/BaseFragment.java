package com.wilddog.conversation.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fly on 17-6-12.
 */

public abstract class BaseFragment extends Fragment {
    public View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent) {
                parent.removeView(view);
            }
        } else {
            view = initView(inflater);// 控件初始化
        }

        return view;
    }

    public abstract View initView(LayoutInflater inflater);
}
