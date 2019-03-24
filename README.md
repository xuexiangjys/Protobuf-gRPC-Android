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




