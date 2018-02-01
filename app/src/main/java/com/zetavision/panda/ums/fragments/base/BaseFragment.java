package com.zetavision.panda.ums.fragments.base;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.ui.MainActivity;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.widget.ViewHeaderBar;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {

    protected Unbinder unbinder;
    private View contentView;
    private boolean hasTitle;
    private LinearLayout llContainer;
    private ViewHeaderBar viewHeadBar;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(getLayoutId(), container, false);
        hasTitle = getHasTitle();
        gainView(contentView);
        ButterKnife.bind(this, contentView);

        init();
        return contentView;
    }

    public ViewHeaderBar getHeader() {
        return viewHeadBar;
    }

    public boolean getHasTitle() {
        return true;
    }

    public int getLayoutId() {
        return R.layout.fragment_base;
    }

    public abstract int getContentLayoutId();

    private void gainView(View contentView) {
        llContainer = contentView.findViewById(R.id.fragmentBase_content);
        LinearLayout.LayoutParams LayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        View view = View.inflate(getContext(), getContentLayoutId(), null);
        llContainer.addView(view, LayoutParams);

        viewHeadBar = contentView.findViewById(R.id.fragmentBase_header);
        if (hasTitle) {
            viewHeadBar.setOnItemClickListener(new ViewHeaderBar.OnItemClickListener() {
                @Override
                public void onLeftClick() {
                    BaseFragment.this.onLeftClick();
                }

                @Override
                public void onLogoutClick() {
                    BaseFragment.this.onLogoutClick();
                }

                @Override
                public void onRightTextClick() {
                    BaseFragment.this.onRightTextClick();
                }
            });
        } else {
            viewHeadBar.setVisibility(View.GONE);
        }
    }

    public void onLeftClick() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).onMenu();
        }
    }

    public void onLogoutClick() {
        IntentUtils.INSTANCE.goLogout(getContext());
    }

    public void onRightTextClick() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).onChangeLanguage();
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    protected abstract void init();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null) {
            unbinder.unbind();
        }
    }
}
