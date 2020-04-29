package zhaoshuo.client.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zhaoshuo.client.client.RpcClient;
import zhaoshuo.client.client.ServiceDiscovery;


/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-23 22:11
 */
@Configuration
public class config {


    String registryAddress="114.215.179.51:2181";
    @Bean
    public RpcClient rpcClient(){
        return new RpcClient(serviceDiscovery());
    }
    @Bean
    public ServiceDiscovery serviceDiscovery(){
        return new ServiceDiscovery(registryAddress);
    }

}
