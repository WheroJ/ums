package com.zetavision.panda.ums.utils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.zetavision.panda.ums.R;

public class LoadingDialog extends android.app.DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);

        View contentView = inflater.inflate(R.layout.dialog_loading, container);
        TextView tvContent = contentView.findViewById(R.id.fragmentDialog_tvContent);

        Bundle arguments = getArguments();
        if (arguments != null) {
            String content = arguments.getString(Constant.LOADING_CONTENT_KEY);
            if (!TextUtils.isEmpty(content)) tvContent.setText(content);
        }
        return contentView;
    }
}
