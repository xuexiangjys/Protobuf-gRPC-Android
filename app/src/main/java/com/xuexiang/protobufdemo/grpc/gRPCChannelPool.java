package com.xuexiang.protobufdemo.grpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;

/**
 * gRPC Channel池
 *
 * @author xuexiang
 * @since 2019/3/24 下午1:57
 */
public class gRPCChannelPool {

    private static volatile gRPCChannelPool sInstance = null;

    /**
     * 管理gRPC Channel 的连接池
     * key：连接名， value：连接通道
     */
    private ConcurrentHashMap<Object, ManagedChannel> maps;

    private gRPCChannelPool() {
        maps = new ConcurrentHashMap<>();
    }

    /**
     * 服务器地址
     */
    private String mHost;
    /**
     * 服务器端口
     */
    private int mPort;

    /**
     * 获取单例
     *
     * @return
     */
    public static gRPCChannelPool getInstance() {
        if (sInstance == null) {
            synchronized (gRPCChannelPool.class) {
                if (sInstance == null) {
                    sInstance = new gRPCChannelPool();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化服务器信息
     *
     * @param host 服务器地址
     * @param port 服务器端口
     */
    public static void init(String host, int port) {
        getInstance().mHost = host;
        getInstance().mPort = port;
    }

    //============getChannel===========//
    /**
     * 获取通道
     *
     * @param name channel名
     * @param host 服务地址
     * @param port 服务端口
     * @return
     */
    public ManagedChannel getChannel(Object name, String host, int port) {
        ManagedChannel channel = maps.get(name);
        if (channel == null || channel.isShutdown()) {
            channel = gRPCChannelUtils.newChannel(host, port);
            maps.put(name, channel);
        }
        return channel;
    }

    /**
     * 获取通道
     *
     * @param name channel名
     * @return
     */
    public ManagedChannel getChannel(Object name) {
        return getChannel(name, mHost, mPort);
    }

    //============addChannel===========//

    public boolean addChannel(Object name, ManagedChannel channel) {
        ManagedChannel tmp = maps.get(name);
        if (tmp == null || tmp.isShutdown()) {
            tmp = channel;
            maps.put(name, tmp);
            return true;
        }
        return false;
    }

    public boolean addChannel(Object name, String host, int port) {
        return addChannel(name, gRPCChannelUtils.newChannel(host, port));
    }

    public boolean addChannel(Object name) {
        return addChannel(name, mHost, mPort);
    }


    public ManagedChannel get(Object name) {
        return maps.get(name);
    }

    public ManagedChannel remove(Object name) {
        return maps.remove(name);
    }

    public ManagedChannel add(Object name, ManagedChannel channel) {
        return maps.put(name, channel);
    }

    public ManagedChannel add(Object name, String host, int port) {
        return maps.put(name, gRPCChannelUtils.newChannel(host, port));
    }

    public ManagedChannel add(Object name) {
        return maps.put(name, gRPCChannelUtils.newChannel(mHost, mPort));
    }


    /**
     * 关闭通道
     *
     * @param name
     * @return
     */
    public boolean shutdown(Object name) {
        ManagedChannel channel = maps.get(name);
        if (channel != null) {
            try {
                return channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                maps.remove(name);
            }
        }
        return false;
    }

}
