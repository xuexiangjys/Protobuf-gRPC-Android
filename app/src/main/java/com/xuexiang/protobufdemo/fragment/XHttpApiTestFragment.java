package com.xuexiang.protobufdemo.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.xuexiang.protobufdemo.R;
import com.xuexiang.protobufdemo.grpc.SimpleStreamObserver;
import com.xuexiang.protobufdemo.grpc.gRPCChannelUtils;
import com.xuexiang.protobufdemo.utils.SettingSPUtils;
import com.xuexiang.springbootgrpc.APIServiceGrpc;
import com.xuexiang.springbootgrpc.XHttpApi;
import com.xuexiang.xaop.annotation.MainThread;
import com.xuexiang.xaop.annotation.Permission;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.utils.TitleBar;
import com.xuexiang.xutil.app.IntentUtils;
import com.xuexiang.xutil.app.PathUtils;
import com.xuexiang.xutil.common.StringUtils;
import com.xuexiang.xutil.display.DensityUtils;
import com.xuexiang.xutil.file.FileIOUtils;
import com.xuexiang.xutil.file.FileUtils;
import com.xuexiang.xutil.net.JsonUtil;
import com.xuexiang.xutil.net.NetworkUtils;
import com.xuexiang.xutil.tip.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import static android.app.Activity.RESULT_OK;
import static com.xuexiang.xaop.consts.PermissionConsts.STORAGE;

/**
 * @author XUE
 * @since 2019/4/17 11:08
 */
@Page(name = "XHttpApi-gRPC测试")
public class XHttpApiTestFragment extends XPageFragment {

    private static final int PORT = 8999;

    private static final int REQUEST_CODE_SELECT_FILE1 = 1000;
    private static final int REQUEST_CODE_SELECT_FILE2 = 1001;

    APIServiceGrpc.APIServiceStub mStub;
    @BindView(R.id.et_loginName)
    EditText etLoginName;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.et_path1)
    EditText etPath1;
    @BindView(R.id.et_path2)
    EditText etPath2;
    @BindView(R.id.tv_grpc_response)
    TextView tvGrpcResponse;

    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_get_user)
    Button btnGetUser;
    @BindView(R.id.btn_upload1)
    Button btnUpload1;
    @BindView(R.id.btn_upload2)
    Button btnUpload2;
    @BindView(R.id.btn_download1)
    Button btnDownload1;
    @BindView(R.id.btn_download2)
    Button btnDownload2;

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

    /**
     * 布局的资源id
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_xhttpapi_test;
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        tvGrpcResponse.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * 初始化监听
     */
    @Override
    protected void initListeners() {

    }

    @SingleClick
    @OnClick({R.id.btn_login, R.id.btn_get_user, R.id.btn_select_file1, R.id.btn_select_file2, R.id.btn_upload1, R.id.btn_upload2, R.id.btn_download1, R.id.btn_download2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                testLoginApi(etLoginName.getText().toString(), etPassword.getText().toString());
                break;
            case R.id.btn_get_user:
                testGetAllUserApi();
                break;
            case R.id.btn_select_file1:
                selectFile(REQUEST_CODE_SELECT_FILE1);
                break;
            case R.id.btn_select_file2:
                selectFile(REQUEST_CODE_SELECT_FILE2);
                break;
            case R.id.btn_upload1:
                testUpload1();
                break;
            case R.id.btn_upload2:
                testUpload2();
                break;
            case R.id.btn_download1:
                testDownload1();
                break;
            case R.id.btn_download2:
                testDownload2();
                break;
            default:
                break;
        }
    }

    @Permission(STORAGE)
    public void selectFile(int requestCode) {
        startActivityForResult(IntentUtils.getDocumentPickerIntent(IntentUtils.DocumentType.ANY), requestCode);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                String path = PathUtils.getFilePathByUri(data.getData());
                if (requestCode == REQUEST_CODE_SELECT_FILE1) {
                    etPath1.setText(path);
                } else {
                    etPath2.setText(path);
                }
            }
        }
    }

    /**
     * 测试登录请求
     */
    private void testLoginApi(String loginName, String password) {
        if (StringUtils.isEmpty(loginName)) {
            ToastUtils.toast("请输入登录名！");
            return;
        }
        if (StringUtils.isEmpty(password)) {
            ToastUtils.toast("请输入密码！");
            return;
        }

        btnLogin.setEnabled(false);
        //开始网络请求
        //构建通道
        final ManagedChannel channel = gRPCChannelUtils.newChannel(SettingSPUtils.getInstance().getApiURL(), PORT);
        //构建服务api代理
        mStub = APIServiceGrpc.newStub(channel);
        //构建请求实体
        XHttpApi.LoginRequest request = XHttpApi.LoginRequest.newBuilder()
                .setLoginName(loginName)
                .setPassword(password)
                .build();
        //进行请求
        mStub.login(request, new SimpleStreamObserver<XHttpApi.LoginReply>() {
            @Override
            protected void onSuccess(XHttpApi.LoginReply value) {
                ToastUtils.toast("登录成功!");
                btnLogin.setEnabled(true);
                tvGrpcResponse.setText(String.format("登录成功! token:%s", value.getToken()));
            }

            @MainThread
            @Override
            public void onError(Throwable t) {
                super.onError(t);
                btnLogin.setEnabled(true);
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
                tvGrpcResponse.setText("获取用户信息数量:" + value.getUsersList().size() + ", 用户信息:" + JsonUtil.toJson(value.getUsersList()));
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                gRPCChannelUtils.shutdown(channel);
            }
        });
    }

    /**
     * 采用流式上传
     */
    private void testUpload1() {
        if (StringUtils.isEmpty(etPath1.getText().toString()) && StringUtils.isEmpty(etPath2.getText().toString())) {
            ToastUtils.toast("请输入上传文件路径！");
            return;
        }

        btnUpload1.setEnabled(false);
        final ManagedChannel channel = gRPCChannelUtils.newChannel(SettingSPUtils.getInstance().getApiURL(), PORT);
        //构建服务api代理
        mStub = APIServiceGrpc.newStub(channel);
        StreamObserver<XHttpApi.FileInfo> observer = mStub.uploadFileStream(new SimpleStreamObserver<XHttpApi.FilePath>() {
            @Override
            protected void onSuccess(XHttpApi.FilePath value) {
                tvGrpcResponse.setText(String.format("文件:%s，上传成功", value.getPath()));
            }

            @MainThread
            @Override
            public void onError(Throwable t) {
                super.onError(t);
                btnUpload1.setEnabled(true);
            }

            @MainThread
            @Override
            public void onCompleted() {
                super.onCompleted();
                gRPCChannelUtils.shutdown(channel);
                btnUpload1.setEnabled(true);
            }
        });
        if (!StringUtils.isEmpty(etPath1.getText().toString())) {
            XHttpApi.FileInfo fileInfo1 = getFileInfoByPath(etPath1.getText().toString());
            if (fileInfo1 != null) {
                observer.onNext(fileInfo1);
            }
        }
        if (!StringUtils.isEmpty(etPath2.getText().toString())) {
            XHttpApi.FileInfo fileInfo2 = getFileInfoByPath(etPath2.getText().toString());
            if (fileInfo2 != null) {
                observer.onNext(fileInfo2);
            }
        }
        observer.onCompleted();
    }


    private XHttpApi.FileInfo getFileInfoByPath(String path) {
        File file = FileUtils.getFileByPath(path);
        if (file == null) {
            return null;
        }
        byte[] data = FileIOUtils.readFile2BytesByChannel(file);
        return XHttpApi.FileInfo.newBuilder()
                .setData(ByteString.copyFrom(data))
                .setFileName(file.getName())
                .setSize(file.length()).build();
    }

    /**
     * 采用普通打包上传
     */
    private void testUpload2() {
        if (StringUtils.isEmpty(etPath1.getText().toString()) && StringUtils.isEmpty(etPath2.getText().toString())) {
            ToastUtils.toast("请输入上传文件路径！");
            return;
        }

        btnUpload2.setEnabled(false);

        XHttpApi.FilePackage.Builder builder = XHttpApi.FilePackage.newBuilder();
        if (!StringUtils.isEmpty(etPath1.getText().toString())) {
            XHttpApi.FileInfo fileInfo1 = getFileInfoByPath(etPath1.getText().toString());
            if (fileInfo1 != null) {
                builder.addFile(fileInfo1);
            }
        }
        if (!StringUtils.isEmpty(etPath2.getText().toString())) {
            XHttpApi.FileInfo fileInfo2 = getFileInfoByPath(etPath2.getText().toString());
            if (fileInfo2 != null) {
                builder.addFile(fileInfo2);
            }
        }

        final ManagedChannel channel = gRPCChannelUtils.newChannel(SettingSPUtils.getInstance().getApiURL(), PORT);
        //构建服务api代理
        mStub = APIServiceGrpc.newStub(channel);
        mStub.uploadFile(builder.build(), new SimpleStreamObserver<XHttpApi.FilePathPackage>() {
            @Override
            protected void onSuccess(XHttpApi.FilePathPackage value) {
                tvGrpcResponse.setText(String.format("文件上传成功数：%d", value.getPathCount()));
                btnUpload2.setEnabled(true);
            }

            @MainThread
            @Override
            public void onError(Throwable t) {
                super.onError(t);
                btnUpload2.setEnabled(true);
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                gRPCChannelUtils.shutdown(channel);
            }
        });
    }

    /**
     * 采用流式下载
     */
    private void testDownload1() {
        if (StringUtils.isEmpty(etPath1.getText().toString()) && StringUtils.isEmpty(etPath2.getText().toString())) {
            ToastUtils.toast("请输入下载文件路径！");
            return;
        }

        btnDownload1.setEnabled(false);
        final ManagedChannel channel = gRPCChannelUtils.newChannel(SettingSPUtils.getInstance().getApiURL(), PORT);
        //构建服务api代理
        mStub = APIServiceGrpc.newStub(channel);
        StreamObserver<XHttpApi.FilePath> observer = mStub.downloadStream(new StreamObserver<XHttpApi.FileInfo>() {
            @Override
            public void onNext(XHttpApi.FileInfo value) {
                String path = PathUtils.getExtDownloadsPath() + "/" + value.getFileName();
                boolean result = FileIOUtils.writeFileFromBytesByChannel(path, value.getData().toByteArray());
                showDownResult(path, result);
            }

            @MainThread
            @Override
            public void onError(Throwable t) {
                tvGrpcResponse.setText(Log.getStackTraceString(t));
                btnDownload1.setEnabled(true);
            }

            @MainThread
            @Override
            public void onCompleted() {
                gRPCChannelUtils.shutdown(channel);
                btnDownload1.setEnabled(true);
            }
        });

        if (!StringUtils.isEmpty(etPath1.getText().toString())) {
            XHttpApi.FilePath filePath1 = getFilePathByPath(etPath1.getText().toString());
            if (filePath1 != null) {
                observer.onNext(filePath1);
            }
        }
        if (!StringUtils.isEmpty(etPath2.getText().toString())) {
            XHttpApi.FilePath filePath2 = getFilePathByPath(etPath2.getText().toString());
            if (filePath2 != null) {
                observer.onNext(filePath2);
            }
        }
        observer.onCompleted();
    }

    private XHttpApi.FilePath getFilePathByPath(String path) {
        File file = FileUtils.getFileByPath(path);
        if (file == null) {
            return null;
        }
        return XHttpApi.FilePath.newBuilder()
                .setPath(file.getName())
                .build();
    }

    @MainThread
    private void showDownResult(String path, boolean result) {
        if (result) {
            tvGrpcResponse.setText(String.format("文件下载成功：%s", path));
        } else {
            ToastUtils.toast("文件下载失败！");
        }
    }

    /**
     * 采用普通打包下载
     */
    private void testDownload2() {
        if (StringUtils.isEmpty(etPath1.getText().toString()) && StringUtils.isEmpty(etPath2.getText().toString())) {
            ToastUtils.toast("请输入下载文件路径！");
            return;
        }

        btnDownload2.setEnabled(false);

        XHttpApi.FilePathPackage.Builder builder = XHttpApi.FilePathPackage.newBuilder();
        if (!StringUtils.isEmpty(etPath1.getText().toString())) {
            String fileName1 = getFileNameByPath(etPath1.getText().toString());
            if (fileName1 != null) {
                builder.addPath(fileName1);
            }
        }
        if (!StringUtils.isEmpty(etPath2.getText().toString())) {
            String fileName2 = getFileNameByPath(etPath2.getText().toString());
            if (fileName2 != null) {
                builder.addPath(fileName2);
            }
        }

        final ManagedChannel channel = gRPCChannelUtils.newChannel(SettingSPUtils.getInstance().getApiURL(), PORT);
        //构建服务api代理
        mStub = APIServiceGrpc.newStub(channel);
        mStub.download(builder.build(), new StreamObserver<XHttpApi.FilePackage>() {
            @Override
            public void onNext(XHttpApi.FilePackage value) {
                List<String> result = new ArrayList<>();
                String path;
                for (XHttpApi.FileInfo fileInfo: value.getFileList()) {
                    path = PathUtils.getExtDownloadsPath() + "/" + fileInfo.getFileName();
                    if (FileIOUtils.writeFileFromBytesByChannel(path, fileInfo.getData().toByteArray())) {
                        result.add(path);
                    }
                }
                showDownResult(result);
            }

            @MainThread
            @Override
            public void onError(Throwable t) {
                tvGrpcResponse.setText(Log.getStackTraceString(t));
                btnDownload2.setEnabled(true);            }

            @Override
            public void onCompleted() {
                gRPCChannelUtils.shutdown(channel);
            }
        });
    }

    @MainThread
    private void showDownResult(List<String> result) {
        if (result.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.size(); i++) {
                sb.append("文件下载成功：").append(result.get(i)).append("\r\n");
            }
            tvGrpcResponse.setText(sb);
        } else {
            ToastUtils.toast("文件下载失败！");
        }
        btnDownload2.setEnabled(true);
    }

    private String getFileNameByPath(String path) {
        File file = FileUtils.getFileByPath(path);
        if (file == null) {
            return null;
        }
        return file.getName();
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
