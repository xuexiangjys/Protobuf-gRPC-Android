
# 如何在Mac上安装BRPC

> BRPC是百度内最常使用的工业级RPC框架, 有1,000,000+个实例(不包含client)和上千种多种服务, 在百度内叫做"baidu-rpc"，是一个非常有价值的战斗级RPC框架。

## BRPC的优势

过多优势的介绍不是本文的重点，以下提供几个链接作为参考：

* [官方介绍](https://github.com/apache/incubator-brpc/blob/master/docs/cn/overview.md)

* [性能对比](https://blog.csdn.net/breaksoftware/article/details/81564405)

## BRPC的安装

> 由于brpc是使用C++编写的，目前官方并没有提供比较友好的安装方式，需要clone[官方提供的源码](https://github.com/apache/incubator-brpc)进行编译安装才可以运行。

先给出[官方文档-macos](https://github.com/apache/incubator-brpc/blob/master/docs/cn/getting_started.md#macos)，供大家参考。

虽说官方文档写得确实不错，在C++老手看来是非常简单的，但是对于我这个Android出身，C++水平一般的人而言，光是编译安装就花了近3个小时才安装成功，下面我将一步一步讲解如何在Mac安装BRPC以及中间遇到的各种坑。

### 安装前的准备工作

> 在安装前，百度也事先提醒了，当前Mac版本的性能比Linux版本差2.5倍，建议不要使用MacOS用作生产环境，不过这应该不影响我进行开发吧，哈哈～～

1. 安装通用的开发工具

执行下面的命令，安装openssl、git、gnu-getopt、coreutils
```
brew install openssl git gnu-getopt coreutils
```

2. 安装BRPC依赖的工具

执行下面的命令，安装gflags, protobuf, leveldb

```
brew install gflags protobuf leveldb
```

* gflags：google的命令行参数解析工具
* protobuf：google公司发布的一套开源编码规则，基于二进制流的序列化传输工具。
* leveldb：google实现的非常高效的kv数据库。

3. 安装性能检测工具gperftools（可选）

```
brew install gperftools
```

4. 安装单元测试工具googletest（可选）

```
git clone https://github.com/google/googletest && cd googletest/googletest && mkdir bld && cd bld && cmake -DCMAKE_CXX_FLAGS="-std=c++11" .. && make && sudo mv libgtest* /usr/lib/ && cd -
```

### 开始编译安装

> 这里我使用的是执行`config_brpc.sh`脚本进行编译安装。

1. clone [brpc](https://github.com/apache/incubator-brpc) 

```
git clone https://github.com/apache/incubator-brpc.git
```

2. cd进入源码目录，执行如下命令

```
$ sh config_brpc.sh --headers=/usr/local/include --libs=/usr/local/lib --cc=clang --cxx=clang++
$ make
```

### 在mac上编译遇到的坑

执行后你会发现编译各种报错。好了，下面我开始讲解编译过程中遇到的各种坑：

* 检查一下你安装的protobuf的版本，如果>3.6.1,请先进行降级处理。

至于如何使用homebrew安装指定版本的程序，我提供如下两种方法：

（1）[通用方法](https://www.jianshu.com/p/c5c298486dbd)

（2）[终极方法](https://blog.csdn.net/aa464971/article/details/84860937)

* 检查openssl版本，如果是1.0.2r版本，那么请你修改config.mk文件，手动添加openssl的目录：

```
HDRS=/usr/local/Cellar/openssl/1.0.2r/include /usr/local/include/ /usr/local/include/node/
LIBS=/usr/local/Cellar/openssl/1.0.2r/lib /usr/local/lib
```

解决了以上两个大坑后，基本是也就能编译成功了。

### 测试程序运行

```
$ cd example/echo_c++
$ make
$ ./echo_server &
$ ./echo_client
```
执行后，打开浏览器，输入`localhost:8000`就可以看到服务器的状态了，如下图：

![](https://github.com/xuexiangjys/ProtoBuf-gRPC-Android/blob/master/art/3.png)

## 拓展阅读

* [如何使用ProtoBuf，通过gRPC服务在android上进行网络请求](./README.md)

## 联系方式

[![](https://img.shields.io/badge/点击一键加入QQ交流群-602082750-blue.svg)](http://shang.qq.com/wpa/qunwpa?idkey=9922861ef85c19f1575aecea0e8680f60d9386080a97ed310c971ae074998887)

![](https://github.com/xuexiangjys/Resource/blob/master/img/qq/qq_group.jpg)

![](https://github.com/xuexiangjys/Resource/blob/master/img/qq/winxin.jpg)








