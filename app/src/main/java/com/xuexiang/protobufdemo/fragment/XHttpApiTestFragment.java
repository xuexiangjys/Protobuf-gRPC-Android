package com.xuexiang.protobufdemo.fragment;

import com.xuexiang.protobufdemo.grpc.SimpleStreamObserver;
import com.xuexiang.protobufdemo.grpc.gRPCChannelUtils;
import com.xuexiang.springbootgrpc.APIServiceGrpc;
import com.xuexiang.springbootgrpc.XHttpApi;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageSimpleListFragment;
import com.xuexiang.xutil.net.JsonUtil;
import com.xuexiang.xutil.tip.ToastUtils;

import java.util.List;

import io.grpc.ManagedChannel;

/**
 * @author XUE
 * @since 2019/4/17 11:08
 */
@Page(name = "XHttpApi-gRPC测试")
public class XHttpApiTestFragment extends XPageSimpleListFragment {

    private static final String HOST = "192.168.0.118";
    private static final int PORT = 8999;

    APIServiceGrpc.APIServiceStub mStub;

    @Override
    protected List<String> initSimpleData(List<String> lists) {
        lists.add("测试登录接口");
        return lists;
    }

    @Override
    protected void onItemClick(int position) {
        switch(position) {
            case 0:
                testLoginApi();
                break;
            case 1:

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
        final ManagedChannel channel = gRPCChannelUtils.newChannel(HOST, PORT);
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
}
