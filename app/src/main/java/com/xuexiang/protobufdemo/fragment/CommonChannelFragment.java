package com.xuexiang.protobufdemo.fragment;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.xuexiang.protobufdemo.R;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author xuexiang
 * @since 2019/3/24 下午1:31
 */
@Page(name = "普通请求")
public class CommonChannelFragment extends XPageFragment {

    @BindView(R.id.et_host)
    EditText etHost;
    @BindView(R.id.et_port)
    EditText etPort;
    @BindView(R.id.et_message)
    EditText etMessage;
    @BindView(R.id.tv_grpc_response)
    TextView tvGrpcResponse;

    /**
     * 布局的资源id
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.layout_grpc_test;
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {

    }

    /**
     * 初始化监听
     */
    @Override
    protected void initListeners() {

    }

    @SingleClick
    @OnClick(R.id.btn_send)
    public void onViewClicked(View view) {



    }
}
