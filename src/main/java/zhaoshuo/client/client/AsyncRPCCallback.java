package zhaoshuo.client.client;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-02-24 13:12
 */
public interface AsyncRPCCallback{

    void success(Object result);

    void fail(Exception e);

}
