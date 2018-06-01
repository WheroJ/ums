package com.zetavision.panda.ums.fragments.base;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.exception.LoginStatusException;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.FormInfoDetail;
import com.zetavision.panda.ums.model.FormItem;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.model.SopMap;
import com.zetavision.panda.ums.model.User;
import com.zetavision.panda.ums.ui.MainActivity;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UserUtils;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.RxUtils;
import com.zetavision.panda.ums.utils.network.UmsApi;
import com.zetavision.panda.ums.widget.ViewHeaderBar;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {

    protected Unbinder unbinder;
    protected View contentView;
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
        User loginUser = UserUtils.INSTANCE.getCurretnLoginUser();
        if (loginUser != null) {
            RxUtils.INSTANCE.acquireString(Client.getApi(UmsApi.class).logout(loginUser.USERNAME)
                    , new RxUtils.DialogListener((AppCompatActivity) getActivity()) {
                @Override
                public void onResult(@NotNull Result result) {
                    IntentUtils.INSTANCE.goLogout(getContext());
                }

                @Override
                public void onError(@NotNull Throwable e) {
                    super.onError(e);
                    if (e instanceof LoginStatusException) {
                        LoginStatusException loginStatusException = (LoginStatusException) e;
                        int code = loginStatusException.getIntCode();
                        System.out.println("code=" + code);
                        if (code == Constant.USER_LOGOUT) {
                            IntentUtils.INSTANCE.goLogout(getContext());
                        } else ToastUtils.show(e.getMessage());
                    } else {
                        ToastUtils.show(e.getMessage());
                    }
                }
            });
        } else {
            IntentUtils.INSTANCE.goLogout(getContext());
        }
    }

    public void onRightTextClick() {
        //TODO 暂时调试使用
        if (Constant.DEBUG) {
            DataSupport.deleteAll(FormInfoDetail.class);
            DataSupport.deleteAll(FormInfo.class);
            DataSupport.deleteAll(FormItem.class);
            DataSupport.deleteAll(SopMap.class);
        }
//
//        if (getActivity() instanceof MainActivity) {
//            ((MainActivity)getActivity()).onChangeLanguage();
//        }
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
