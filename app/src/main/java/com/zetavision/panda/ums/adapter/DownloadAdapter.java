package com.zetavision.panda.ums.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.BaseFragment;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.FormInfoDetail;
import com.zetavision.panda.ums.service.UmsService;
import com.zetavision.panda.ums.ui.formdownload.DownloadFragment;
import com.zetavision.panda.ums.ui.formup.UploadFragment;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.NetUtils;
import com.zetavision.panda.ums.utils.ToastUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DownloadAdapter extends BaseAdapter {

    private Context mContext;
    private List<FormInfo> list = new ArrayList<>();
    private BaseFragment fragment;

    public DownloadAdapter(Context mContext, BaseFragment fragment) {
        this.mContext = mContext;
        this.fragment = fragment;
    }

    public void notifyDataSetChanged(List<FormInfo> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    @Override
    public FormInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(mContext, R.layout.list_download_item, null);
            ButterKnife.bind(holder, view);//用butterKnife绑定
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        holder.setData(getItem(position), position);
        return view;
    }

    class ViewHolder {
        @BindView(R.id.form_number) TextView form_number;
        @BindView(R.id.category) TextView category;
        @BindView(R.id.line_or_eqp) TextView line_or_eqp;
        @BindView(R.id.desc) TextView desc;
        @BindView(R.id.status) TextView status;
        @BindView(R.id.form_sort) TextView formSort;

        @BindView(R.id.downloadBtn) ImageView downloadBtn;
        @BindView(R.id.progressBar) ProgressBar progressBar;
        @BindView(R.id.pauseBtn) ImageView pauseBtn;
        @BindView(R.id.doneImg) ImageView doneImg;
        @BindView(R.id.textInfo) TextView textInfo;

        @BindView(R.id.sop_downloadBtn) ImageView sopDownloadBtn;
        @BindView(R.id.sop_progressBar) ProgressBar sopProgressBar;
        @BindView(R.id.sop_pauseBtn) ImageView sopPauseBtn;
        @BindView(R.id.sop_doneImg) ImageView sopDoneImg;
        @BindView(R.id.sop_textInfo) TextView sopTextInfo;

        private FormInfo data;

        private HashMap<String, String> statusMap;

        public ViewHolder() {
            statusMap = new HashMap<>();
            statusMap.put(Constant.FORM_STATUS_PLANNED, mContext.getString(R.string.status_planed));
            statusMap.put(Constant.FORM_STATUS_INPROGRESS, mContext.getString(R.string.status_inprogress));
            statusMap.put(Constant.FORM_STATUS_COMPLETED, mContext.getString(R.string.status_complete));
            statusMap.put(Constant.FORM_STATUS_CLOSED, mContext.getString(R.string.status_closed));
        }

        public void setData(FormInfo data, int position) {
            this.data = data;

            form_number.setText(data.getFormCode());
            category.setText(data.getActionType());
            line_or_eqp.setText(data.getLineOrEqp());
            desc.setText(data.getDesc());
            status.setText(statusMap.get(data.getStatus()));
            formSort.setText(String.valueOf(position + 1));

            switch (data.getDownload_status()) {
                case FormInfo.DONE:
                    downloadBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.VISIBLE);
                    textInfo.setText(R.string.common_finish);
                    break;
                case FormInfo.FAIL:
                    downloadBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText(R.string.common_fail);
                    break;
                case FormInfo.PROGRESS:
                    downloadBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.VISIBLE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText(R.string.common_stop);
                    break;
                default:
                    downloadBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText(R.string.common_download);
            }

            if (data.getDownload_status() == FormInfo.DONE) {
                if (!TextUtils.isEmpty(data.sopLocalPath)) {
                    sopDoneImg.setVisibility(View.VISIBLE);
                    sopDownloadBtn.setVisibility(View.GONE);
                    sopPauseBtn.setVisibility(View.GONE);
                    sopProgressBar.setVisibility(View.GONE);
                    sopTextInfo.setVisibility(View.GONE);
                } else if (TextUtils.isEmpty(data.sopUrl)) {
                    sopDoneImg.setVisibility(View.GONE);
                    sopDownloadBtn.setVisibility(View.GONE);
                    sopPauseBtn.setVisibility(View.GONE);
                    sopProgressBar.setVisibility(View.GONE);
                    sopTextInfo.setVisibility(View.VISIBLE);
                    sopTextInfo.setText("N");
                } else {
                    sopTextInfo.setVisibility(View.GONE);
                    switch (data.sop_download_status) {
                        case FormInfo.DONE:
                            sopDownloadBtn.setVisibility(View.GONE);
                            sopProgressBar.setVisibility(View.GONE);
                            sopPauseBtn.setVisibility(View.GONE);
                            sopDoneImg.setVisibility(View.VISIBLE);
                            break;
                        case FormInfo.FAIL:
                            sopDownloadBtn.setVisibility(View.VISIBLE);
                            sopProgressBar.setVisibility(View.GONE);
                            sopPauseBtn.setVisibility(View.GONE);
                            sopDoneImg.setVisibility(View.GONE);
                            break;
                        case FormInfo.PROGRESS:
                            sopDownloadBtn.setVisibility(View.GONE);
                            sopProgressBar.setVisibility(View.VISIBLE);
                            sopPauseBtn.setVisibility(View.VISIBLE);
                            sopDoneImg.setVisibility(View.GONE);
                            break;
                        default:
                            sopDownloadBtn.setVisibility(View.VISIBLE);
                            sopProgressBar.setVisibility(View.GONE);
                            sopPauseBtn.setVisibility(View.GONE);
                            sopDoneImg.setVisibility(View.GONE);
                    }
                }
            } else {
                sopDoneImg.setVisibility(View.GONE);
                sopDownloadBtn.setVisibility(View.GONE);
                sopPauseBtn.setVisibility(View.GONE);
                sopProgressBar.setVisibility(View.GONE);
                sopTextInfo.setVisibility(View.GONE);
            }
        }

        @OnClick(R.id.downloadBtn) void download() {
            if (!NetUtils.INSTANCE.isNetConnect(mContext)) {
                ToastUtils.show(mContext.getString(R.string.connect_net2download));
                return;
            }
            if (fragment instanceof DownloadFragment) {
                int downloadedCount = DataSupport.count(FormInfoDetail.class);
                if (downloadedCount >= Constant.MAX_DOWN) {
                    ToastUtils.showLong(mContext.getString(R.string.max_down, Constant.MAX_DOWN));
                } else {
                    UmsService umsService = ((DownloadFragment) fragment).umsService;
                    if (umsService != null) {
                        umsService.startDownload(data.getFormId());
                    }
                }
            } else if (fragment instanceof UploadFragment) {
                UmsService umsService = ((UploadFragment) fragment).umsService;
                if (umsService != null) {
                    umsService.startDownload(data.getFormId());
                }
            }
        }

        @OnClick(R.id.pauseBtn) void pause() {
            if (fragment instanceof DownloadFragment) {
                UmsService umsService = ((DownloadFragment) fragment).umsService;
                if (umsService != null) {
                    umsService.stopDownload(data.getFormId());
                }
            } else if (fragment instanceof UploadFragment) {
                UmsService umsService = ((UploadFragment) fragment).umsService;
                if (umsService != null) {
                    umsService.stopUpload(data.getFormId());
                }
            }
        }

        @OnClick(R.id.sop_downloadBtn) void downloadSop() {
            if (!NetUtils.INSTANCE.isNetConnect(mContext)) {
                ToastUtils.show(mContext.getString(R.string.connect_net2download));
                return;
            }
            if (fragment instanceof DownloadFragment) {
                UmsService umsService = ((DownloadFragment) fragment).umsService;
                if (umsService != null) {
                    if (data.getDownload_status() == FormInfo.DONE) {
                        umsService.startDownloadSop(data.getFormId());
                    } else {
                        ToastUtils.show(mContext.getString(R.string.notice_download_form));
                    }
                }
            } else if (fragment instanceof UploadFragment) {
                UmsService umsService = ((UploadFragment) fragment).umsService;
                if (umsService != null) {
                    if (data.getDownload_status() == FormInfo.DONE) {
                        umsService.startDownloadSop(data.getFormId());
                    } else {
                        ToastUtils.show(mContext.getString(R.string.notice_download_form));
                    }
                }
            }
        }

        @OnClick(R.id.sop_pauseBtn) void pauseSop() {
            if (fragment instanceof DownloadFragment) {
                UmsService umsService = ((DownloadFragment) fragment).umsService;
                if (umsService != null) {
                    umsService.stopDownloadSop(data.getFormId(), data.sopUrl);
                }
            } else if (fragment instanceof UploadFragment) {
                UmsService umsService = ((UploadFragment) fragment).umsService;
                if (umsService != null) {
                    umsService.stopUpload(data.getFormId());
                }
            }
        }
    }
}
