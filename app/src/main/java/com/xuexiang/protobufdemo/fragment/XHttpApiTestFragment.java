package com.xuexiang.protobufdemo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.xuexiang.protobufdemo.grpc.SimpleStreamObserver;
import com.xuexiang.protobufdemo.grpc.gRPCChannelUtils;
import com.xuexiang.protobufdemo.utils.SettingSPUtils;
import com.xuexiang.springbootgrpc.APIServiceGrpc;
import com.xuexiang.springbootgrpc.XHttpApi;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageSimpleListFragment;
import com.xuexiang.xpage.utils.TitleBar;
import com.xuexiang.xutil.display.DensityUtils;
import com.xuexiang.xutil.net.JsonUtil;
import com.xuexiang.xutil.net.NetworkUtils;
import com.xuexiang.xutil.tip.ToastUtils;

import java.util.List;

import io.grpc.ManagedChannel;

/**
 * @author XUE
 * @since 2019/4/17 11:08
 */
@Page(name = "XHttpApi-gRPC测试")
public class XHttpApiTestFragment extends XPageSimpleListFragment {

    private static final int PORT = 8999;

    APIServiceGrpc.APIServiceStub mStub;

    @Override
    protected TitleBar initTitleBar() {
        TitleBar titleBar = super.initTitleBar();
        titleBar.addAction(new TitleBar.TextAction("服务器地址") {
            @Override
            public void performAction(View view) {
                showInputDialog(getContext());
            }
        });
        return titleBar;
    }

    @Override
    protected List<String> initSimpleData(List<String> lists) {
        lists.add("测试登录接口");
        lists.add("测试获取所有用户信息接口");
        return lists;
    }

    @Override
    protected void onItemClick(int position) {
        switch(position) {
            case 0:
                testLoginApi();
                break;
            case 1:
                testGetAllUserApi();
                break;
            default:
                break;
        }
    }

    /**
     * 测试登录请求
     */
    private void testLoginApi() {
        //开始网络请求
        //构建通道
        final ManagedChannel channel = gRPCChannelUtils.newChannel(SettingSPUtils.getInstance().getApiURL(), PORT);
        //构建服务api代理
        mStub = APIServiceGrpc.newStub(channel);
        //构建请求实体
        XHttpApi.LoginRequest request = XHttpApi.LoginRequest.newBuilder()
                .setLoginName("xuexiang")
                .setPassword("123456")
                .build();
        //进行请求
        mStub.login(request, new SimpleStreamObserver<XHttpApi.LoginReply>() {
            @Override
            protected void onSuccess(XHttpApi.LoginReply value) {
                ToastUtils.toast("登录成功:" + JsonUtil.toJson(value.getUser()));
            }
            @Override
            public void onCompleted() {
                super.onCompleted();
                gRPCChannelUtils.shutdown(channel);
            }
        });
    }

    private void testGetAllUserApi() {
        final ManagedChannel channel = gRPCChannelUtils.newChannel(SettingSPUtils.getInstance().getApiURL(), PORT);
        //构建服务api代理
        mStub = APIServiceGrpc.newStub(channel);
        //进行请求
        mStub.getAllUser(XHttpApi.Empty.newBuilder().build(), new SimpleStreamObserver<XHttpApi.UserList>() {
            @Override
            protected void onSuccess(XHttpApi.UserList value) {
                ToastUtils.toast("获取用户信息数量:" + value.getUsersList().size());
            }
            @Override
            public void onCompleted() {
                super.onCompleted();
                gRPCChannelUtils.shutdown(channel);
            }
        });

    }

    public Dialog showInputDialog(Context context) {
        FrameLayout linearLayout = new FrameLayout(context);
        final EditText etInput = new EditText(context);
        etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        etInput.setHint("请输入服务器地址");
        etInput.setText(SettingSPUtils.getInstance().getApiURL());
        linearLayout.addView(etInput);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) etInput.getLayoutParams();
        layoutParams.setMargins(DensityUtils.dip2px(context, 15), 0, DensityUtils.dip2px(context, 15), 0);
        etInput.setLayoutParams(layoutParams);

        return new AlertDialog.Builder(context)
                .setTitle("服务器地址修改")
                .setMessage("请输入服务器的IP地址")
                .setView(linearLayout)
                .setPositiveButton("确定", (dialog, which) -> {
                    String value = etInput.getText().toString();
                    if (NetworkUtils.isIP(value)) {
                        SettingSPUtils.getInstance().setApiURL(value);
                    } else {
                        ToastUtils.toast("输入的IP地址不合法！");
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
