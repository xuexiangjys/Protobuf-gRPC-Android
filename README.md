# ProtoBuf-gRPC-Android

教你如何使用ProtoBuf，通过gRPC服务在android上进行网络请求。

## 简介

### ProtoBuf

> google公司发布的一套开源编码规则，基于二进制流的序列化传输，可以转换成多种编程语言，几乎涵盖了市面上所有的主流编程语言，是目前公认的非常高效的序列化技术。

ProtoBuf的Github主页：https://github.com/protocolbuffers/protobuf

### gRPC

> gRPC是一个高性能、开源和通用的RPC框架，面向移动和HTTP/2设计。目前提供C、Java和Go语言版本，分别是grpc、grpc-java、grpc-go。gRPC基于HTTP/2标准设计，带来诸如双向流、流控、头部压缩、单TCP连接上的多复用请求等特性。这些特性使得其在移动设备上表现更好，更省电和节省空间占用。gRPC由google开发，是一款语言中立、平台中立、开源的远程过程调用系统。

gRPC(Java)的Github主页：https://github.com/grpc/grpc-java

## 为什么要使用ProtoBuf和gRPC

> 简而言之，ProtoBuf就好比信息传输的媒介，类似我们常用的json，而grpc则是传输他们的通道，类似我们常用的socket。

### ProtoBuf和json

如果用一句话来概括ProtoBuf和JSON的区别的话，那就是：对于较多信息存储的大文件而言，ProtoBuf的写入和解析效率明显高很多，而JSON格式的可读性明显要好。网上有一段数据用以对此ProtoBuf和JSON之间的性能差异：

#### JSON

```
总共写65535条Data记录到文件中，测试结果如下：
生成的文件尺寸是23,733k。
生成文件的时间是12.80秒。
从该文件中解析的时间是11.50秒。
```

#### ProtoBuf

```
总共写65535条Data记录到文件中，测试结果如下：
生成的文件尺寸是3760k。
生成文件的时间是0.08秒。
从该文件中解析的时间是0.07秒。
```

### gRPC

作为google公司极力推荐的分布式网络架构，基于HTTP2.0标准设计，使用用ProtoBuf作为序列化工具，在移动设备上表现更好，更省电和节省空间占用。google出品，品质值得信赖。

## 如何使用

像这种国外的开源框架，还是建议大家先直接阅读官方文档，再看国内的文章，这样才不容易被误导。

[官方教程](https://grpc.io/docs/quickstart/android.html)

[官方示例](https://github.com/grpc/grpc-java/tree/master/examples/android)

### 环境配置

1.首先需要下载安装Protobuf Support插件，如下图：

![](https://github.com/xuexiangjys/ProtoBuf-gRPC-Android/blob/master/art/1.png)

2.在项目的根目录的 build.gradle 的 buildscript中加入`protobuf-gradle-plugin`插件：

```
buildscript {
    ...
    dependencies {
        ...
        classpath "com.google.protobuf:protobuf-gradle-plugin:0.8.6"
    }
}
```

3.然后在应用Module的 build.gradle 中进行如下配置

```
apply plugin: 'com.android.application'
apply plugin: 'com.google.protobuf' //引用protobuf-gradle-plugin插件

android {
    ...

    lintOptions {
        abortOnError false
        disable 'GoogleAppIndexingWarning', 'HardcodedText', 'InvalidPackage'
        textReport true
        textOutput "stdout"
    }
}

protobuf {
    protoc { artifact = 'com.google.protobuf:protoc:3.6.1' }
    plugins {
        javalite { artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0" }
        grpc { artifact = 'io.grpc:protoc-gen-grpc-java:1.19.0' // CURRENT_GRPC_VERSION
        }
    }
    generateProtoTasks {
        all().each { task ->
            task.plugins {
                javalite {}
                grpc { // Options added to --grpc_out
                    option 'lite' }
            }
        }
    }
}

dependencies {
    //protobuf
    implementation 'io.grpc:grpc-okhttp:1.19.0'
    implementation 'io.grpc:grpc-protobuf-lite:1.19.0'
    implementation 'io.grpc:grpc-stub:1.19.0'
    implementation 'javax.annotation:javax.annotation-api:1.2'
}
```

4.最后将你`.proto`协议文件放至`src/main/proto/`文件夹下，点击build进行编译，如果出现如下图，则证明环境配置成功！

![](https://github.com/xuexiangjys/ProtoBuf-gRPC-Android/blob/master/art/2.png)

### 普通请求

在测试demo中的请求前，请务必先运行[服务端的代码](https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/helloworld/HelloWorldServer.java)。

1.构建Channel

```
/**
 * 构建一条普通的Channel
 *
 * @param host 主机服务地址
 * @param port 端口
 * @return
 */
public static ManagedChannel newChannel(String host, int port) {
    return ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();
}
```

2.构建服务请求API代理

```
//构建通道
final ManagedChannel channel = gRPCChannelUtils.newChannel(host, port);
//构建服务api代理
mStub = GreeterGrpc.newStub(channel);
```

3.构建请求实体

```
//HelloRequest是自动生成的实体类
HelloRequest request = HelloRequest.newBuilder().setName(message).build();
```

4.执行请求

```
//进行请求
mStub.sayHello(request, new SimpleStreamObserver<HelloReply>() {
    @Override
    protected void onSuccess(HelloReply value) {
        tvGrpcResponse.setText(value.getMessage());
        btnSend.setEnabled(true);
    }
    @MainThread
    @Override
    public void onError(Throwable t) {
        super.onError(t);
        tvGrpcResponse.setText(Log.getStackTraceString(t));
        btnSend.setEnabled(true);
    }
    @Override
    public void onCompleted() {
        super.onCompleted();
        gRPCChannelUtils.shutdown(channel); //关闭通道
    }
});
```

### Https请求

与普通请求相比，就在第一步建立通道有所不同，需要设置CA证书，其他步骤都相同。

```
/**
 * 构建一条SSLChannel
 *
 * @param host         主机服务地址
 * @param port         端口
 * @param authority    域名
 * @param certificates 证书
 * @return
 */
public static ManagedChannel newSSLChannel(String host, int port, String authority, InputStream... certificates) {
    HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(certificates);
    return OkHttpChannelBuilder.forAddress(host, port)
            //overrideAuthority非常重要，必须设置调用
            .overrideAuthority(authority)
            .sslSocketFactory(sslParams.sSLSocketFactory)
            .build();
}
```

## 拓展阅读

* [如果在Mac上安装BRPC](./HOW_TO_INSTALL_BRPC.md)

## 联系方式

[![](https://img.shields.io/badge/点击一键加入QQ交流群-602082750-blue.svg)](http://shang.qq.com/wpa/qunwpa?idkey=9922861ef85c19f1575aecea0e8680f60d9386080a97ed310c971ae074998887)

![](https://github.com/xuexiangjys/Resource/blob/master/img/qq/qq_group.jpg)

![](https://github.com/xuexiangjys/Resource/blob/master/img/qq/winxin.jpg)