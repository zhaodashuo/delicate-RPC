package zhaoshuo.client.client.proxy;


import zhaoshuo.client.client.RPCFuture;

/**
 *  @Description
 *  @Author zhaoshuo
 *  @Date 2020-02-24 13:12
 */
public interface ObjectProxy {
    public RPCFuture call(String funcName, Object... args);
}